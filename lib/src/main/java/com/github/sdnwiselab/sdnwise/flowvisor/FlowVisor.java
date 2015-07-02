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
package com.github.sdnwiselab.sdnwise.flowvisor;

import com.github.sdnwiselab.sdnwise.adapter.Adapter;
import com.github.sdnwiselab.sdnwise.adapter.AdapterUdp;
import com.github.sdnwiselab.sdnwise.application.ApplicationId;
import com.github.sdnwiselab.sdnwise.controller.ControllerId;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.ReportPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.Set;

/**
 * This class registers Nodes and Controllers of the SDN-WISE Network.
 * 
 * This class is created by FlowVisorFactory.
 * It permits Network slicing and implements Runnable and the Observer 
 * pattern.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class FlowVisor implements Observer, Runnable {

    private final Adapter lower;
    private final AdapterUdp upper;
    private final Scanner scanner;
    private boolean isStopped;
    private final HashMap<ControllerId, Set<NodeAddress>> controllerMapping;
    private final HashMap<ApplicationId, ControllerId> applicationMapping;
    

    /** 
     * Constructor for the FlowVisor.
     * It defines Lower and Upper Adapter.
     */

    // TODO 
    //questo deve diventare una lista o una struttura dati che tenga conto 
    // delle diverse applicazioni e dei diversi controller. Per ora inserisci 
    // solamente un controller e un'applicazione
    FlowVisor(Adapter lower, AdapterUdp upper) {
        this.lower = lower;
        this.upper = upper;
        scanner = new Scanner(System.in, "UTF-8");
        isStopped = false;
        controllerMapping = new HashMap<>();
        applicationMapping = new HashMap<>();
    }

    /**
     * This method permits to register a Controller to this FlowVisor and its
     * Nodes.
     * 
     * @param controller Controller Identity to register
     * @param set Set of Nodes to register
     */
    public final void addController(ControllerId controller, Set<NodeAddress> set){
        controllerMapping.put(controller, set);
    }
    
    /**
     * This method permits to register an Application to this FlowVisor and its
     * Controller.
     * 
     * @param application Application Identity to register
     * @param controller Controller Identity for the Application
     */
    public final void addApplication(ApplicationId application, ControllerId controller){
        applicationMapping.put(application, controller);
    }
    
    /**
     * Remove a Controller from this FlowVisor
     * 
     * @param controller Controller Identity to remove
     */
    public final void removeController(ControllerId controller){
        controllerMapping.remove(controller);
    }
    
    /**
     * Remove an Application from this FlowVisor
     * 
     * @param application Application Identity to remove
     */
    public final void removeApplication(ApplicationId application){
        applicationMapping.remove(application);
    }
    
    @Override
    public final void update(Observable o, Object arg) {
        if (o.equals(lower)) {
            // if it is a data packet send to the application, else send it to
            // the controller
            byte[] data = (byte[]) arg;
            NetworkPacket np = new NetworkPacket(data);
            switch (np.getType()) {
                case 0:
                    manageData(data);
                    break;
                case 2: // report packets
                    manageReports(data);
                    break;
                default: // request packets
                    manageRequests(data);
                    break;
            }
        } else if (o.equals(upper)) {
            manageResponses((byte[]) arg);
        }
    }

    /**
     * This method consists of a way to manage reports.
     * 
     * @param data Byte Array contains data message
     */
    private void manageReports(byte[] data) {
        for (Entry<ControllerId, Set<NodeAddress>> set : controllerMapping.entrySet()) {
            ReportPacket pkt = new ReportPacket(
                    Arrays.copyOf(data, data.length));
            HashMap<NodeAddress, Byte> map = pkt.getNeighborsHashMap();
            if (set.getValue().contains(pkt.getSrc())) {
                boolean mod = false;
                final int numNeigh = pkt.getNeigh();
                for (int i = 0; i < numNeigh; i++) {
                    NodeAddress tmp = pkt.getNeighbourAddress(i);
                    if (!set.getValue().contains(tmp)) {
                        map.remove(tmp);
                        mod = true;
                    }
                }

                if (mod) {
                    pkt.setNeighborsHashMap(map);
                }
                
                upper.send(pkt.toByteArray(), set.getKey().getAddress(),
                        set.getKey().getPort());
            }
        }
    }

    /**
     * This method consists of a way to manage requests.
     * 
     * @param data Byte Array contains data message
     */
    private void manageRequests(byte[] data) {
        NetworkPacket pkt = new NetworkPacket(data);
        for (Entry<ControllerId, Set<NodeAddress>> set : controllerMapping.entrySet()) {
            if (set.getValue().contains(pkt.getSrc())
                    && set.getValue().contains(pkt.getDst())) {
                upper.send(data, set.getKey().getAddress(),
                        set.getKey().getPort());
                System.out.println("[FLW]: Sending request to " + set.getKey().getAddress() + ":"
                        + set.getKey().getPort());
            }
        }
    }

    private void manageData(byte[] data) {
        DataPacket pkt = new DataPacket(data);
        
        for (ApplicationId app : applicationMapping.keySet()) {
            Set<NodeAddress> nodes = controllerMapping.get(applicationMapping.get(app));
            if (nodes.contains(pkt.getSrc())
                    && nodes.contains(pkt.getDst())) {
                upper.send(data, app.getAddress(),
                        app.getPort());
                System.out.println("[FLW]: Sending data to " + app.getAddress() + ":"
                        + app.getPort());
            }
        }
    }   
    
    private void manageResponses(byte[] data) {
        System.out.println("[FLW]: receiving " + Arrays.toString(data));
        lower.send(data);
    }

    @Override
    public final void run() {
        if (lower.open() && upper.open()) {
            lower.addObserver(this);
            upper.addObserver(this);
            while (!isStopped) {
                //if (scanner.nextLine().equals("exit -l Flowvisor")) {
                    //isStopped = true;
                //}
            }
            lower.close();
            upper.close();
        }
    }    
}
