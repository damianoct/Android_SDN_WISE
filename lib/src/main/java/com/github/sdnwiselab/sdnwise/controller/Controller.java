/*
 * Copyright (C) 2015 SDN-WISE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.sdnwiselab.sdnwise.controller;

import com.github.sdnwiselab.sdnwise.adapter.Adapter;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.function.FunctionInterface;
import com.github.sdnwiselab.sdnwise.packet.ConfigPacket;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_GET_RULE_INDEX;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.OpenPathPacket;
import com.github.sdnwiselab.sdnwise.packet.ReportPacket;
import com.github.sdnwiselab.sdnwise.packet.ResponsePacket;
import com.github.sdnwiselab.sdnwise.topology.NetworkGraph;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jodah.expiringmap.ExpiringMap;

/**
 * This class holds a representation of the sensor network and resolves all the
 * requests coming from the network itself. This abstract class has two main
 * methods. manageRoutingRequest and graphUpdate. The first is called when a
 * request is coming from the network while the latter is called when something
 * in the topology of the network changes.
 * <p>
 * There are two main implementation of this class: ControllerDijkstra and
 * Controller Static.
 * <p>
 * This class also offers methods to send messages and configure the nodes in
 * the network.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public abstract class Controller implements Observer, Runnable, ControllerInterface {

    final static byte SDN_WISE_DATA = 0;
    final static byte SDN_WISE_BEACON = 1;
    final static byte SDN_WISE_REPORT = 2;
    final static byte SDN_WISE_RESPONSE = 4;
    final static byte SDN_WISE_OPEN_PATH = 5;
    final static byte SDN_WISE_CONFIG = 6;

    final static int SDN_WISE_RLS_MAX = 16;
    final static int RESPONSE_TIMEOUT = 250;

    private final Adapter lower;
    final Scanner scanner;
    final NetworkGraph networkGraph;

    final HashMap<NodeAddress, LinkedList<NodeAddress>> results;
    private boolean isStopped;
    private final ArrayBlockingQueue<NetworkPacket> bQ;

    private final Map<String, ConfigPacket> cache = ExpiringMap.builder()
            .expiration(5, TimeUnit.SECONDS)
            .build();

    private ControllerId id;

    private final NodeAddress sinkAddress;

    /**
     * Constructor Method for the Controller Class.
     * 
     * @param id ControllerId object.
     * @param lower Lower Adpater object.
     * @param networkGraph NetworkGraph object.
     */
    Controller(ControllerId id, Adapter lower, NetworkGraph networkGraph) {
        this.id = id;
        this.lower = lower;
        bQ = new ArrayBlockingQueue<>(1000);
        this.networkGraph = networkGraph;
        results = new HashMap<>();
        scanner = new Scanner(System.in, "UTF-8");
        isStopped = false;
        sinkAddress = new NodeAddress("0.0");
    }

    @Override
    public final ControllerId getId(){
        return id;
    }

    @Override
    public final void setId(ControllerId id) {
        this.id = id;
    }

    private void managePacket(NetworkPacket data) {
        System.err.println("[CTR]: " + data.toString());
        /*DatagramPacket dp = new DatagramPacket(data.toByteArray(), data.toByteArray().length);
        String rcvd = new String(dp.getData(), 0, dp.getLength());
        System.err.println("[CTR] HUMAN READABLE: " + rcvd);*/
        System.err.println("[CTR] HUMAN READABLE: " + data.getSrc().toString());

        switch (data.getType()) {
            case SDN_WISE_REPORT:
                networkGraph.updateMap(new ReportPacket(data.toByteArray()));
                break;
            case SDN_WISE_DATA:
            case SDN_WISE_BEACON:
            case SDN_WISE_RESPONSE:
            case SDN_WISE_OPEN_PATH:
                break;
            case SDN_WISE_CONFIG:
                ConfigPacket cp = new ConfigPacket(data.toByteArray());
                String key;
                if (cp.getPayloadAt(0) == (SDN_WISE_CNF_GET_RULE_INDEX)) {
                    key = cp.getNetId() + " "
                            + cp.getSrc() + " "
                            + cp.getPayloadAt(0) + " "
                            + cp.getPayloadAt(1) + " "
                            + cp.getPayloadAt(2);
                } else {
                    key = cp.getNetId() + " "
                            + cp.getSrc() + " "
                            + cp.getPayloadAt(0);
                }
                System.out.println("[CTR]: key_add - " + key);
                cache.put(key, cp);
                break;
            default:
                manageRoutingRequest(data);
                break;
        }
    }

    /**
     * This methods manages updates coming from the lower adapter or the network
     * representation. When a message is received from the lower adapter it is
     * inserted in a ArrayBlockingQueue and then the method managePacket it is
     * called on it. While for updates coming from the network representation
     * the method graphUpdate is invoked.
     *
     * @param o the source of the event.
     * @param arg Object sent by Observable.
     */
    @Override
    public final void update(Observable o, Object arg) {
        if (o.equals(lower)) {
            try {
                bQ.put(new NetworkPacket((byte[]) arg));
            } catch (InterruptedException ex) {
                log(Level.SEVERE, ex.getMessage());
            }
        } else if (o.equals(networkGraph)) {
            graphUpdate();
        }
    }

    /**
     * Starts the working thread that manages incoming requests and it listens
     * to messages coming from the standard input.
     */
    @Override
    public final void run() {
        System.out.println("IL CONTROLLER è PARTITO!");
        if (lower.open()) {
            Thread th = new Thread(new Worker(bQ));
            th.start();
            lower.addObserver(this);
            networkGraph.addObserver(this);
            register();
            setupNetwork();
            while (!isStopped) {
                //if (scanner.nextLine().equals("exit -l Controller")) {
                  //  isStopped = true;
                //}
            }
            lower.close();
        }
    }

    /**
     * This method sends a SDN_WISE_OPEN_PATH messages to a generic node. This
     * kind of message holds a list of nodes that will create a path inside the
     * network.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param path the list of all the NodeAddresses in the path.
     */
    @Override
    public final void sendPath(byte netId, NodeAddress destination, 
            List<NodeAddress> path) {
        OpenPathPacket op = new OpenPathPacket();
        op.setPath(path)
                .setNetId(netId)
                .setSrc(sinkAddress)
                .setDst(destination)
                .setNxhop(sinkAddress);
        sendNetworkPacket(op);
    }

    /**
     * This method sends a generic message to a node. The message is represented
     * by a NetworkPacket.
     *
     * @param packet the packet to be sent.
     */
    public void sendNetworkPacket(NetworkPacket packet) {
        lower.send(packet.toByteArray());
    }

    private ConfigPacket sendQuery(ConfigPacket cp) throws TimeoutException {

        sendNetworkPacket(cp);

        try {
            Thread.sleep(RESPONSE_TIMEOUT);
        } catch (InterruptedException ex) {
            log(Level.SEVERE, ex.getMessage());
        }

        String key;

        if (cp.getPayloadAt(0) == (SDN_WISE_CNF_GET_RULE_INDEX)) {
            key = cp.getNetId() + " "
                    + cp.getDst() + " "
                    + cp.getPayloadAt(0) + " "
                    + cp.getPayloadAt(1) + " "
                    + cp.getPayloadAt(2);
        } else {
            key = cp.getNetId() + " "
                    + cp.getDst() + " "
                    + cp.getPayloadAt(0);
        }
        System.out.println("[CTR]: key_remove - " + key);

        if (cache.containsKey(key)) {
            ConfigPacket data = cache.remove(key);
            return data;
        } else {
            throw new TimeoutException("No answer from the node");
        }
    }

    /**
     * This method sets the address of a node. The new address value is passed
     * using two bytes.
     *
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newAddress the new address.
     */
    @Override
    public final void setNodeAddress(byte netId, NodeAddress destination, 
            NodeAddress newAddress) {
        ConfigPacket cp = new ConfigPacket();
        cp.setNodeAddressValue(newAddress)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the address of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the NodeAddress of a node, null if it does exists.
     */
    public final NodeAddress getNodeAddress(byte netId, 
        NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadNodeAddressValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return null;
        }
        return response.getNodeAddress();
    }

    /**
     * This method sets the Network ID of a node. The new value is passed using
     * a byte.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     */
    @Override
    public final void resetNode(byte netId, NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setResetValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method sets the Network ID of a node. The new value is passed using
     * a byte.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newNetId value of the new net ID
     */
    @Override
    public final void setNodeNetId(byte netId, NodeAddress destination, 
            byte newNetId) {
        ConfigPacket cp = new ConfigPacket();
        cp.setNetworkIdValue(newNetId)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the Network ID of a node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the nedId, -1 if not found.
     */
    public final int getNodeNetId(byte netId, NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadNetworkIdValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return response.getNetworkIdValue();
    }

    /**
     * This method sets the beacon period of a node. The new value is passed
     * using a short.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param period beacon period in seconds (TODO check).
     */
    @Override
    public final void setNodeBeaconPeriod(byte netId, NodeAddress destination, 
            short period) {
        ConfigPacket cp = new ConfigPacket();
        cp.setBeaconPeriodValue(period)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the beacon period of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the beacon period, -1 if not found
     */
    @Override
    public final int getNodeBeaconPeriod(byte netId, NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadBeaconPeriodValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;

        }
        return response.getBeaconPeriodValue();
    }

    /**
     * This method sets the report period of a node. The new value is passed
     * using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period report period in seconds (TODO check)
     */
    @Override
    public final void setNodeReportPeriod(byte netId, NodeAddress destination, 
            short period) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReportPeriodValue(period)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the report period of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the report period, -1 if not found
     */
    @Override
    public final int getNodeReportPeriod(byte netId, NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadReportPeriodValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return response.getReportPeriodValue();
    }

    /**
     * This method sets the update table period of a node. The new value is
     * passed using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period update table period in seconds (TODO check)
     */
    @Override
    public final void setNodeUpdateTablePeriod(byte netId, 
            NodeAddress destination, short period) {
        ConfigPacket cp = new ConfigPacket();
        cp.setUpdateTablePeriodValue(period)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the Update table period of a node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the updateTablePeriod, -1 if not found.
     */
    @Override
    public final int getNodeUpdateTablePeriod(byte netId, 
            NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadUpdateTablePeriodValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return response.getNetworkIdValue();
    }

    /**
     * This method sets the maximum time to live for each message sent by a
     * node. The new value is passed using a byte.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newTtl time to live in number of hops.
     */
    @Override
    public final void setNodeTtlMax(byte netId, NodeAddress destination, 
            byte newTtl) {
        ConfigPacket cp = new ConfigPacket();
        cp.setDefaultTtlMaxValue(newTtl)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the maximum time to live for each message sent by a
     * node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the maximum time to live, -1 if not found.
     */
    @Override
    public final int getNodeTtlMax(byte netId, NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadDefaultTtlMaxValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return response.getDefaultTtlMaxValue();
    }

    /**
     * This method sets the minimum RSSI in order to consider a node as a
     * neighbor.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newRssi new threshold rssi value.
     */
    @Override
    public final void setNodeRssiMin(byte netId, NodeAddress destination, 
            byte newRssi) {
        ConfigPacket cp = new ConfigPacket();
        cp.setDefaultRssiMinValue(newRssi)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the minimum RSSI in order to consider a node as a
     * neighbor.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the minimum RSSI, -1 if not found.
     */
    @Override
    public final int getNodeRssiMin(byte netId, NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadDefaultRssiMinValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return response.getDefaultRssiMinValue();
    }

    /**
     * This method adds a new address in the list of addresses accepted by the
     * node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newAddr the address.
     */
    @Override
    public final void addAcceptedAddress(byte netId, NodeAddress destination, 
            NodeAddress newAddr) {
        ConfigPacket cp = new ConfigPacket();
        cp.setAddAcceptedAddressValue(newAddr)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method removes an address in the list of addresses accepted by the
     * node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newAddr the address.
     */
    @Override
    public final void removeAcceptedAddress(byte netId, NodeAddress destination, 
            NodeAddress newAddr) {
        ConfigPacket cp = new ConfigPacket();
        cp.setRemoveAcceptedAddressValue(newAddr)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method returns the list of addresses accepted by the node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the list of accepted Addresses.
     */
    @Override
    public final List<NodeAddress> getAcceptedAddressesList(byte netId, 
            NodeAddress destination) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadAcceptedAddressesValue()
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return null;
        }
        return response.getAcceptedAddressesValues();
    }

    /**
     * This method installs a rule in the node
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param rule the rule to be installed.
     */
    @Override
    public final void addRule(byte netId, NodeAddress destination, 
            FlowTableEntry rule) {
        /*
         ConfigPacket cp = new ConfigPacket();
         cp.setAddRuleValue(rule)
         .setNetId(netId)
         .setDst(destination)
         .setSrc(sinkAddress)
         .setNxhop(sinkAddress);
         sendNetworkPacket(cp);
         */

        ResponsePacket rp = new ResponsePacket();
        rp.setRule(rule)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(rp);
    }

    /**
     * This method removes a rule in the node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param index index of the erased row.
     */
    @Override
    public final void removeRule(byte netId, 
            NodeAddress destination, int index) {
        ConfigPacket cp = new ConfigPacket();
        cp.setRemoveRuleAtPositionValue(index)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method removes a rule in the node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param rule the rule to be removed.
     */
    @Override
    public final void removeRule(byte netId, NodeAddress destination, 
            FlowTableEntry rule) {
        ConfigPacket cp = new ConfigPacket();
        cp.setRemoveRuleValue(rule)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method gets the WISE flow table of a node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the list of the entries in the WISE Flow Table.
     */
    @Override
    public final List<FlowTableEntry> getRules(byte netId, 
            NodeAddress destination) {
        List<FlowTableEntry> list = new ArrayList<>(SDN_WISE_RLS_MAX);
        for (int i = 0; i < SDN_WISE_RLS_MAX; i++) {
            list.add(i, getRuleAtPosition(netId, destination, i));
        }
        return list;
    }

    /**
     * This method gets the WISE flow table entry of a node at position n.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param index position of the entry in the table.
     * @return returns the list of the entries in the WISE Flow Table.
     */
    @Override
    public final FlowTableEntry getRuleAtPosition(byte netId, 
            NodeAddress destination, int index) {
        ConfigPacket cp = new ConfigPacket();
        cp.setReadRuleAtPositionValue(index)
                .setNetId(netId)
                .setDst(destination)
                .setSrc(sinkAddress)
                .setNxhop(sinkAddress);
        ConfigPacket response;
        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return null;
        }
        return response.getRule();
    }

    /**
     * This method is used to register the Controller with the FlowVisor.
     */
    //TODO we need to implement same sort of security check/auth.
    private void register() {
    }

    private List<NetworkPacket> createPackets(
            byte netId, 
            NodeAddress src, 
            NodeAddress dest, 
            NodeAddress nextHop,
            byte id, 
            byte[] buf) 
    {
        ConfigPacket np = new ConfigPacket();
        LinkedList<NetworkPacket> ll = new LinkedList<>();

        np.setNetId(netId)
          .setDst(dest)
          .setSrc(src)
          .setNxhop(nextHop);
        
        int packetNumber = buf.length / 101;
        int remaining = buf.length % 101;
        int totalPackets = packetNumber + (remaining > 0 ? 1 : 0);
        int pointer = 0;
        int i = 0;

        if (packetNumber < 256) {
            if (packetNumber > 0) {
                for (i = 0; i < packetNumber; i++) {
                    byte[] payload = new byte[103];
                    payload[0] = (byte) (i + 1);
                    payload[1] = (byte) totalPackets;
                    System.arraycopy(buf, pointer, payload, 2, 101);
                    pointer += 101;
                    np.setAddFunctionAtPositionValue(id,payload);
                    ll.add(np.clone());
                }
            }
            
            if (remaining > 0) {
                byte[] payload = new byte[remaining+2];
                payload[0] = (byte) (i + 1);
                payload[1] = (byte) totalPackets;
                System.arraycopy(buf, pointer, payload, 2, remaining);
                np.setAddFunctionAtPositionValue(id,payload);
                ll.add(np.clone());
            }
        }
        
        for (NetworkPacket n : ll){
            System.out.println(n);
        }
        
        return ll;
    }
    
    /**
     * Logs messages depending on the verbosity level.
     *
     * @param level a standard logging level.
     * @param msg the string message to be logged.
     */
    public void log(Level level, String msg) {
        Logger.getLogger(this.getClass().getName()).log(level, "[ADA]: {0}", msg);
    }

    @Override
    public void sendFunction(
            byte netId, 
            NodeAddress src, 
            NodeAddress dest, 
            NodeAddress nextHop,
            byte id,
            String className
            ) 
    {   
        try {
            URL main = FunctionInterface.class.getResource(className);
            File path = new File(main.getPath());
            byte[] buf = Files.readAllBytes(path.toPath());
            
            List<NetworkPacket> ll = createPackets(
                    netId,src,dest,nextHop,id,buf);
            
            for (NetworkPacket np : ll){
                this.sendNetworkPacket(np);
            }
        } catch (IOException ex) {
            log(Level.SEVERE, ex.getMessage());
        }
    }
    
    /**
     * This method gets the NetworkGraph of the controller.
     *
     * @return returns a NetworkGraph object.
     */
    @Override
    public NetworkGraph getNetworkGraph() {
        return networkGraph;
    }

    private class Worker implements Runnable {

        private final ArrayBlockingQueue<NetworkPacket> bQ;
        boolean isStopped = false;

        Worker(ArrayBlockingQueue<NetworkPacket> bQ) {
            this.bQ = bQ;
        }

        @Override
        public void run() {
            while (!isStopped) {
                try {
                    NetworkPacket packetToManage = bQ.take();
                    System.out.println("\n\nPACKET TO MANAGE SRC: " + packetToManage.getSrc().toString()+ "\n\n");
                    managePacket(packetToManage);
                } catch (InterruptedException ex) {
                    isStopped = true;
                }
            }
        }
    }
}
