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
package com.github.sdnwiselab.sdnwise.node;

import com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction.SDN_WISE_AGGREGATE;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction.SDN_WISE_DROP;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction.SDN_WISE_FORWARD_BROADCAST;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction.SDN_WISE_FORWARD_UNICAST;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction.SDN_WISE_FORWARD_UP;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction.SDN_WISE_MODIFY;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableActionCallback;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableActionDrop;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableActionForward;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableActionModify;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry.SDN_WISE_WINDOWS_MAX;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableStats;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_BIGGER;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_EQUAL;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_EQUAL_OR_BIGGER;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_EQUAL_OR_LESS;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_LESS;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_NOT_EQUAL;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_PACKET;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_SIZE_0;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_SIZE_1;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_SIZE_2;
import com.github.sdnwiselab.sdnwise.function.FunctionInterface;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_ACCEPTED_ID_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_DFLT_CNT_BEACON_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_DFLT_CNT_REPORT_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_DFLT_CNT_UPDTABLE_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_DFLT_HDR_LEN;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_DFLT_RSSI_MIN;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_MAC_SEND_BROADCAST;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_MAC_SEND_UNICAST;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_NEIGHBORS_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_RLS_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_RL_TTL_DECR;
import com.github.sdnwiselab.sdnwise.packet.BeaconPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.GeoCoordinatesPacket;
import com.github.sdnwiselab.sdnwise.packet.GeoReportPacket;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.*;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_DST_H;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_DST_L;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_LEN;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_NET_ID;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_NXHOP_H;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_NXHOP_L;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_OPEN_PATH;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_RESPONSE;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_TTL;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_TYPE;
import com.github.sdnwiselab.sdnwise.packet.OpenPathPacket;
import com.github.sdnwiselab.sdnwise.packet.ReportPacket;
import com.github.sdnwiselab.sdnwise.packet.ResponsePacket;
import com.github.sdnwiselab.sdnwise.util.Neighbor;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author Sebastiano Milardo
 */
public abstract class Node implements Runnable {

    private static final String digits = "0123456789abcdef";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread th;

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        if (Security.getProvider("BC") == null) {
            System.out.println("Provider(\"BC\") not installed");
        } else {
            System.out.println("Provider(\"BC\") installed");
        }

        if (args.length == 7) {
            if (args[0].equals("SINK")) {
                th = new Thread(new SinkNode(
                        // its own id
                        (byte) Integer.parseInt(args[1]),
                        // its own address
                        new NodeAddress(args[2]),
                        // listener port
                        Integer.parseInt(args[3]),
                        // controller address
                        args[4],
                        // controller port
                        Integer.parseInt(args[5]),
                        // neigh file
                        args[6],
                        // security
                        false)
                );
                th.start();
            }
        } else if (args.length == 5) {
            if (args[0].equals("NODE")) {
                th = new Thread(new SensorNode(
                        // its own id
                        (byte) Integer.parseInt(args[1]),
                        // its own address
                        new NodeAddress(args[2]),
                        // listener port
                        Integer.parseInt(args[3]),
                        // neigh file
                        args[4], false)
                );
                th.start();
            }
        }
    }

    /**
     * Return length many bytes of the passed in byte array as a hex string.
     *
     * @param data the bytes to be converted.
     * @param length the number of bytes in the data block to be converted.
     * @return a hex representation of length bytes of data.
     */
    public static String toHex(byte[] data, int length) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i != length; i++) {
            int v = data[i] & 0xff;

            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0xf));
        }

        return buf.toString();
    }

    /**
     * Return the passed in byte array as a hex string.
     *
     * @param data the bytes to be converted.
     * @return a hex representation of data.
     */
    public static String toHex(byte[] data) {
        return toHex(data, data.length);
    }

    int port;
    DatagramSocket socket;

    final ArrayBlockingQueue<int[]> flowTableQueue;
    final ArrayBlockingQueue<int[]> txQueue;
    HashMap<NodeAddress, FakeInfo> neighbourList;
    String configNeighbourFilePath;

    private int num_hop_vs_sink;
    private int rssi_vs_sink;

    Battery battery;

    int semaphore,
            flow_table_free_pos,
            accepted_id_free_pos,
            neighbors_number;

    private final byte[] buf;

    ArrayList<Neighbor> neighborTable;
    ArrayList<FlowTableEntry> flowTable;
    ArrayList<NodeAddress> acceptedId;
    int[] statusRegister;
    HashMap<String, Object> adcRegister;

    HashMap<Integer, LinkedList<int[]>> functionBuffer = new HashMap<>();
    HashMap<Integer, FunctionInterface> functions = new HashMap<>();

    NodeAddress addr;
    int net_id;
    int cnt_beacon_max;
    int cnt_report_max;
    int cnt_updtable_max;
    int cnt_sleep_max;
    int ttl_max;
    int rssi_min;

    boolean isSecure = false;
    Cipher cipher;
    KeyPair pair;
    Key pubKey, privKey;
    PublicKey sinkPubKey;

    public Node(NodeAddress addr, byte net_id, int port, String configNeighbourFilePath, boolean isSecure) {
        buf = new byte[1024];

        statusRegister = new int[1024];
        neighborTable = new ArrayList<>(SDN_WISE_NEIGHBORS_MAX);
        acceptedId = new ArrayList<>(SDN_WISE_ACCEPTED_ID_MAX);
        flowTable = new ArrayList<>(SDN_WISE_RLS_MAX);

        flowTableQueue = new ArrayBlockingQueue<>(1024);
        txQueue = new ArrayBlockingQueue<>(1024);
        this.addr = addr;
        this.net_id = net_id;
        this.configNeighbourFilePath = configNeighbourFilePath;

        neighbourList = new HashMap<>();

        this.port = port;

        this.isSecure = isSecure;
        setup();
    }

    public void setNum_hop_vs_sink(int num_hop_vs_sink) {
        this.num_hop_vs_sink = num_hop_vs_sink;
    }

    public void setRssi_vs_sink(int rssi_vs_sink) {
        this.rssi_vs_sink = rssi_vs_sink;
    }

    public void setSemaphore(int semaforo) {
        this.semaphore = semaforo;
    }

    public int getNum_hop_vs_sink() {
        return num_hop_vs_sink;
    }

    public int getRssi_vs_sink() {
        return rssi_vs_sink;
    }

    public int getSemaphore() {
        return semaphore;
    }

    private void setup() {
        initSdnWise();
        initFlowTable();
        initNeighborTable();
        initAcceptedId();
        //boolean
        if (isSecure == true) {
            setupSecurity();
        }
    }

    public abstract void setupSecurity();

    // questi due metodi sono inglobati nel tx e rx Beacon
    public int[] sendPublicKey(int[] packet) {
        // TODO modificare con i pacchetti java

        int[] newPacket = new int[packet.length + sinkPubKey.getEncoded().length];
        System.arraycopy(packet, 0, newPacket, 0, packet.length);
        arraycopy(sinkPubKey.getEncoded(), 0, newPacket, packet.length, sinkPubKey.getEncoded().length);
        newPacket[SDN_WISE_LEN] = newPacket.length;
        return newPacket;
    }

    public void receivePublicKey(int[] allPacket, int keyLen) {

        byte[] sinkPubKeyArray = new byte[keyLen]; // lo sappiamo quanto di e'

        arraycopy(allPacket, allPacket.length - sinkPubKeyArray.length,
                sinkPubKeyArray, 0, sinkPubKeyArray.length);

        try {
            sinkPubKey = KeyFactory.getInstance("RSA").
                    generatePublic(new X509EncodedKeySpec(sinkPubKeyArray));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //
    public int[] sendSignedMessage(int[] packet) {
        if (packet.length > SDN_WISE_DFLT_HDR_LEN) {
            byte[] plainPacket = new byte[packet.length - SDN_WISE_DFLT_HDR_LEN];

            try {
                arraycopy(packet, SDN_WISE_DFLT_HDR_LEN,
                        plainPacket, 0, packet.length - SDN_WISE_DFLT_HDR_LEN);

                MessageDigest messageHash = MessageDigest.getInstance("SHA1", "BC");
                byte[] messageHashed = messageHash.digest(plainPacket);
                System.out.println("plainMessage: " + Arrays.toString(plainPacket));
                System.out.println("plainMessageString: " + toHex(plainPacket));
                System.out.println("messageHashed : " + Arrays.toString(messageHashed));
                System.out.println("messageHashedString : " + toHex(messageHashed));
                System.out.println("messageHashed Length: " + messageHashed.length);
                System.out.println("messageHashedString Length: " + (toHex(messageHashed)).length());

                cipher.init(Cipher.ENCRYPT_MODE, privKey);// ,random
                byte[] encryptedPart = cipher.doFinal(messageHashed);
                System.out.println("cipher: " + toHex(encryptedPart));
                System.out.println("cipher lenght: " + toHex(encryptedPart).length());
                System.out.println("cipher[] : " + Arrays.toString(encryptedPart));
                System.out.println("cipher[] lenght: " + encryptedPart.length);
                System.out.println("Encrypted Hashed Message Sent!");

                int[] newPacket = new int[packet.length + encryptedPart.length];
                System.arraycopy(packet, 0, newPacket, 0, packet.length);
                arraycopy(encryptedPart, 0, newPacket, packet.length, encryptedPart.length);

                return newPacket;
            } catch (NoSuchAlgorithmException |
                    NoSuchProviderException |
                    InvalidKeyException |
                    IllegalBlockSizeException |
                    BadPaddingException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return packet;
    }

    public int[] receiveSignedMessage(int[] allPacket) {
        byte[] encryptedPart = new byte[22];
        byte[] plainPart = new byte[allPacket.length - encryptedPart.length - SDN_WISE_DFLT_HDR_LEN];

        arraycopy(allPacket, SDN_WISE_DFLT_HDR_LEN, plainPart, 0, plainPart.length);
        arraycopy(allPacket, allPacket.length - encryptedPart.length,
                encryptedPart, 0, encryptedPart.length);

        try {
            // decryption step
            //Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, sinkPubKey);
            byte[] hashedPart = cipher.doFinal(encryptedPart);

            MessageDigest hashedPlainPart = MessageDigest.getInstance("SHA1", "BC");
            byte[] hastToVerify = hashedPlainPart.digest(plainPart);

            //verifica autenticita' messaggio
            if (Arrays.equals(hashedPart, hastToVerify)) {
                System.out.println("Signature Verification Correct");
                return allPacket;
            } else {
                System.out.println("Signature Verification Failed");
                return null;
            }

        } catch (InvalidKeyException |
                IllegalBlockSizeException |
                BadPaddingException |
                NoSuchAlgorithmException |
                NoSuchProviderException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void initSdnWise() {
        cnt_beacon_max = SDN_WISE_DFLT_CNT_BEACON_MAX;
        cnt_report_max = SDN_WISE_DFLT_CNT_REPORT_MAX;
        cnt_updtable_max = SDN_WISE_DFLT_CNT_UPDTABLE_MAX;
        rssi_min = SDN_WISE_DFLT_RSSI_MIN;
        ttl_max = SDN_WISE_DFLT_TTL_MAX;

        battery = new Battery();
        flow_table_free_pos = 1;
        accepted_id_free_pos = 0;

        InputStream is = this.getClass().getResourceAsStream("/" + configNeighbourFilePath);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split(",");
                if (tmp.length == 4) {
                    neighbourList.put(new NodeAddress(tmp[0]), new FakeInfo(
                            new InetSocketAddress(tmp[1],
                                    Integer.parseInt(tmp[2])),
                            Integer.parseInt(tmp[3]))
                    );
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SinkNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initFlowTable() {
        for (int i = 0; i < SDN_WISE_RLS_MAX; i++) {
            flowTable.add(i, new FlowTableEntry());
        }

        int i, k;
        
        flowTable.get(0).getWindows()[0]
                .setOperator(SDN_WISE_EQUAL)
                .setSize(SDN_WISE_SIZE_2)
                .setLocation(SDN_WISE_PACKET)
                .setPos(SDN_WISE_DST_H)
                .setValueHigh(0)
                .setValueLow(0);

        flowTable.get(0).getWindows()[1]
                .setOperator(SDN_WISE_NOT_EQUAL)
                .setSize(SDN_WISE_SIZE_1)
                .setLocation(SDN_WISE_PACKET)
                .setPos(SDN_WISE_TYPE)
                .setValueHigh(0)
                .setValueLow(0);

        for (k = 2; k < SDN_WISE_WINDOWS_MAX; k++) {
            flowTable.get(0).getWindows()[k] = new FlowTableWindow();
        }

        flowTable.get(0).setAction(new FlowTableActionForward()
                .setBroadcast(false)
                .setNextHop(new NodeAddress(0, 0))
                .setMultimatch(false)
        );

        flowTable.get(0).setStats(new FlowTableStats());

        for (i = 1; i < SDN_WISE_RLS_MAX; i++) {
            initRule(flowTable.get(i));
        }
    }

    public void initNeighborTable() {
        int i;
        for (i = 0; i < SDN_WISE_NEIGHBORS_MAX; i++) {
            neighborTable.add(i, new Neighbor());
        }
        neighbors_number = 0;
    }

    public void initAcceptedId() {
        int i;
        for (i = 0; i < SDN_WISE_ACCEPTED_ID_MAX; i++) {
            acceptedId.add(i, new NodeAddress(65535));
        }
    }

    public void initRule(FlowTableEntry rule) {
        int i;
        for (i = 0; i < SDN_WISE_WINDOWS_MAX; i++) {
            rule.getWindows()[i] = new FlowTableWindow();
        }
        rule.setAction(new FlowTableAction());
        rule.setStats(new FlowTableStats());
    }

    public int chooseNeighbor(int action_value_2_byte) {
        int i;
        for (i = 0; i < SDN_WISE_NEIGHBORS_MAX; i++) {
            if (action_value_2_byte == neighborTable.get(i).getAddr().getLow()) {
                return (neighborTable.get(i).getAddr().getHigh());
            }
        }
        return 254;
    }

    public void resetSemaphore() {
    }

    @Override
    public void run() {
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket = new DatagramSocket(port);
            new Timer().schedule(new Task(), 1000, 1000);
            new Thread(new PacketManager()).start();
            new Thread(new PacketSender()).start();

            while (true) {
                socket.receive(packet);
                int[] tmp = new int[packet.getData()[0]];

                for (int i = 0; i < packet.getLength(); i++) {
                    tmp[i] = packet.getData()[i] & 0xFF;
                }

                flowTableQueue.put(tmp);
            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void txBEACON() {
        BeaconPacket bp = new BeaconPacket();
        bp.setSrc(addr);
        bp.setNetId((byte)net_id);
        bp.setDist((byte)num_hop_vs_sink);
        bp.setBatt((byte)battery.getBatteryPercent());
        
        if (num_hop_vs_sink == 0) {
            bp.setNxhop(addr);
        } else {
            bp.setNxhop(new NodeAddress(
            flowTable.get(0).getWindows()[0].getValueHigh(),
            flowTable.get(0).getWindows()[0].getValueLow()));
        }

        if (this.isSecure) {
            NetworkPacket np = new NetworkPacket(sendPublicKey(bp.toIntArray()));
            radioTX(np.toIntArray(), SDN_WISE_MAC_SEND_BROADCAST);
        } else {
            radioTX(bp.toIntArray(), SDN_WISE_MAC_SEND_BROADCAST);
        }
        
    }

    void txREPORT() {

        ReportPacket rp = new ReportPacket();
        rp.setNetId((byte) net_id)
                .setSrc(addr)
                .setDst((byte) flowTable.get(0).getWindows()[0].getValueHigh(),
                        (byte) flowTable.get(0).getWindows()[0].getValueLow())
                .setNxhop(((FlowTableActionForward) flowTable.get(0).getAction()).getNextHop());
        rp.setBatt((byte) battery.getBatteryPercent());
        rp.setNeigh(neighbors_number);

        for (int j = 0; j < neighbors_number; j++) {
            rp.setNeighbourAddressAt(neighborTable.get(j).getAddr(), j);
            rp.setNeighbourWeightAt((byte) neighborTable.get(j).getRssi(), j);
        }
        initNeighborTable();
        controllerTX(rp.toIntArray());
    }

    void updateTable() {
        for (int i = 0; i < SDN_WISE_RLS_MAX; i++) {
            int ttl = flowTable.get(i).getStats().getTtl();
            if (ttl >= SDN_WISE_RL_TTL_DECR) {
                flowTable.get(i).getStats().setTtl((ttl - SDN_WISE_RL_TTL_DECR));
            } else {
                initRule(flowTable.get(i));
                if (i == 0) {
                    resetSemaphore();
                }
            }
        }
    }

    void rxGeoData(int[] packet) {
        if (isAcceptedIdPacket(packet)) {
            // TODO
            System.out.println("GEO DATA");
        } else if (isAcceptedIdAddress(
                packet[SDN_WISE_NXHOP_H],
                packet[SDN_WISE_NXHOP_L])) {
            runFlowMatch(packet);
        }
    }

    void rxGeoCoordinates(int[] packet) {
        if (isAcceptedIdPacket(packet)) {
            GeoCoordinatesPacket cgp = new GeoCoordinatesPacket(packet);
            int[] coord = cgp.getCoordinates();
            System.arraycopy(coord, 0, this.statusRegister, 0, 6);
        } else if (isAcceptedIdAddress(
                packet[SDN_WISE_NXHOP_H],
                packet[SDN_WISE_NXHOP_L])) {
            runFlowMatch(packet);
        }
    }

    void rxGeoReport(int[] packet) {
        if (isAcceptedIdPacket(packet)) {
            GeoReportPacket grp = new GeoReportPacket(packet);
            int n = grp.getPayloadSize();
            this.statusRegister[6] = grp.getNeighborsSize();
            for (int i = 7; i < n; i++) {
                this.statusRegister[i] = grp.getPayloadAt(i - 7);
            }
        } else if (isAcceptedIdAddress(
                packet[SDN_WISE_NXHOP_H],
                packet[SDN_WISE_NXHOP_L])) {
            runFlowMatch(packet);
        }
    }

    void rxData(int[] packet) {
        if (isAcceptedIdPacket(packet)) {
            SDN_WISE_Callback(packet);
        } else if (isAcceptedIdAddress(
                packet[SDN_WISE_NXHOP_H],
                packet[SDN_WISE_NXHOP_L])) {
            runFlowMatch(packet);
        }
    }

    void rxBeacon(int[] packet, int rssi) {
        BeaconPacket bp = new BeaconPacket(packet);
        int index = getNeighborIndex(bp.getSrc());

        if (index != (SDN_WISE_NEIGHBORS_MAX + 1)) {
            if (index != -1) {
                neighborTable.get(index).setRssi(rssi);
                neighborTable.get(index).setBatt(bp.getBatt());
            } else {
                neighborTable.get(neighbors_number).setAddr(bp.getSrc());
                neighborTable.get(neighbors_number).setRssi(rssi);
                neighborTable.get(neighbors_number).setBatt(bp.getBatt());
                neighbors_number++;
            }
        }
    }

    abstract void rxREPORT(int[] packet);

    void rxResponse(int[] packet) {
        if (isAcceptedIdPacket(packet)) {
            ResponsePacket rp = new ResponsePacket(packet);
            rp.getRule().setStats(new FlowTableStats());
            insertRule(rp.getRule(), searchRule(rp.getRule()));
        } else {
            runFlowMatch(packet);
        }
    }

    void rxOpenPath(int[] packet) {
        if (isAcceptedIdPacket(packet)) {
            OpenPathPacket opp = new OpenPathPacket(packet);
            List<NodeAddress> path = opp.getPath();

            for (int i = 0; i < path.size(); i++) {

                NodeAddress actual = path.get(i);
                // mi trovo
            
                if (isAcceptedIdAddress(actual.getHigh(), actual.getLow())) {
            
                    // se non sono il primo
                    if (i != 0) {
            
                        FlowTableEntry rule = new FlowTableEntry();
                        initRule(rule);

                        //regola per forwardare indietro
                        rule.getWindows()[0]
                                .setOperator(SDN_WISE_EQUAL)
                                .setSize(SDN_WISE_SIZE_2)
                                .setLocation(SDN_WISE_PACKET);

                        rule.getWindows()[0].setPos(SDN_WISE_DST_H)
                                .setValueHigh(path.get(0).getHigh())
                                .setValueLow(path.get(0).getLow());

                        rule.setAction(new FlowTableActionForward()
                                .setBroadcast(false)
                                .setNextHop(path.get(i - 1))
                        );
                        int p = searchRule(rule);
                        insertRule(rule, p);
                    }

                    if (i != (path.size()-1)) {
                        FlowTableEntry rule = new FlowTableEntry();
                        initRule(rule);

                        //regola per forwardare avanti
                        rule.getWindows()[0]
                                .setOperator(SDN_WISE_EQUAL)
                                .setSize(SDN_WISE_SIZE_2)
                                .setLocation(SDN_WISE_PACKET);

                        rule.getWindows()[0].setPos(SDN_WISE_DST_H)
                                .setValueHigh(path.get(path.size()-1).getHigh())
                                .setValueLow(path.get(path.size()-1).getLow());

                        rule.setAction(new FlowTableActionForward()
                                .setBroadcast(false)
                                .setNextHop(path.get(i + 1))
                        );

                        int p = searchRule(rule);
            
                        insertRule(rule, p);

                        //cambio next hop e dest del pacchetto
                        opp.setDst(path.get(i + 1));
                        opp.setNxhop(path.get(i + 1));

                        radioTX(opp.toIntArray(), SDN_WISE_MAC_SEND_UNICAST);
                        break;
                    }
                }
            }
        } else {
            runFlowMatch(packet);
        }
    }

    void runFlowMatch(int[] packet) {
        int j, i, found = 0;
        for (j = 0; j < SDN_WISE_RLS_MAX; j++) {

            i = getActualFlowIndex(j);

            if (matchRule(flowTable.get(i), packet) == 1) {
                found = 1;
                runAction(flowTable.get(i).getAction(), packet);
                flowTable.get(i).getStats()
                        .setCounter(flowTable.get(i).getStats().getCounter() + 1);
                if (!(flowTable.get(i).getAction().isMultimatch())) {
                    break;
                }
            }
        }
        if (found == 0) { //!found
            // It's necessary to send a rule/request if we have done the lookup
            // I must modify the source address with myself,
            NetworkPacket np = new NetworkPacket(packet)
                    .setSrc(addr)
                    .setRequestFlag()
                    .setTtl(SDN_WISE_DFLT_TTL_MAX)
                    .setNxhop(((FlowTableActionForward) flowTable.get(0)
                            .getAction()).getNextHop());
            controllerTX(np.toIntArray());
        }
    }

    abstract void rxConfig(int[] packet);

    void insertRule(FlowTableEntry rule, int pos) {
        if (pos >= SDN_WISE_RLS_MAX) {
            pos = flow_table_free_pos; // TODO controllare
            flow_table_free_pos++;
            if (flow_table_free_pos >= SDN_WISE_RLS_MAX) {
                flow_table_free_pos = 1;
            }
        }
        System.out.println("inserting rule " + rule + " at position " + pos);
        flowTable.set(pos, rule);
    }

    // Verifica che una condizione di una finestra di una regola è soddisfatta
    int matchWindow(FlowTableWindow window, int[] packet) {

        int size = window.getSize();
        int operatore = window.getOperator();
        int matchMemory = window.getLocation();
        int[] ptr;

        if (matchMemory != 0) {
            ptr = packet;
        } else {
            ptr = statusRegister;
        }

        switch (size) {
            case SDN_WISE_SIZE_2:
                return doOperation(operatore,
                        ptr[window.getPos()] * 256 + ptr[window.getPos() + 1],
                        window.getValueHigh() * 256 + window.getValueLow());
            case SDN_WISE_SIZE_1:
                return doOperation(operatore, ptr[window.getPos()], window.getValueLow());
            case SDN_WISE_SIZE_0:
                return 2;
            default:
                return 0;
        }
    }

    // Verifica che un pacchetto corrisponda a una regola
    int matchRule(FlowTableEntry rule, int[] packet) {
        int i;
        int sum = 0;
        for (i = 0; i < SDN_WISE_WINDOWS_MAX; i++) {
            int result = matchWindow(rule.getWindows()[i], packet);
            if (result != 0) {
                sum += result;
            } else {
                return 0;
            }
        }

        return (sum == SDN_WISE_WINDOWS_MAX * 2 ? 0 : 1);
    }

    // Esegue l'azione alla posizione r della tabella
    void runAction(FlowTableAction action, int[] packet) {

        int action_type = action.getType();
        NetworkPacket np = new NetworkPacket(packet);

        switch (action_type) {
            case SDN_WISE_FORWARD_UNICAST:
            case SDN_WISE_FORWARD_BROADCAST:
                np.decrementTtl();
                np.setNxhop(((FlowTableActionForward) action).getNextHop());
                radioTX(np.toIntArray(), action_type == SDN_WISE_FORWARD_BROADCAST);
                break;

            case SDN_WISE_DROP:
                FlowTableActionDrop ftad = (FlowTableActionDrop) action;
                int prob = ftad.getDropRate();
                // Se prob di drop=70% significa che se genero un numero,
                // droppo il pacchetto se il numero generato è inferiore a 70
                if ((Math.random() * 100) > prob) {

                    // il secondo  int indica il secondo  int
                    // dell'indirizzo del nodo a cui forwardare il pacchetto
                    // il primo  int è scelto casualmente, tra i vicini con
                    // il secondo  int dell'indirizzo posto uguale a quello
                    // contenuto nella rule
                    int first_byte_address = chooseNeighbor(0); // TODO sistemare
                    if (first_byte_address != 254) {
                        np.decrementTtl();
                        np.setNxhop((byte) first_byte_address, (byte) 0);
                        radioTX(np.toIntArray(), SDN_WISE_MAC_SEND_UNICAST);
                    }//else
                }//else
                break;
            //case 2
            case SDN_WISE_MODIFY:
                FlowTableActionModify ftam = (FlowTableActionModify) action;
                if (ftam.getLocation() != 0) {
                    // TODO problemi se si cambia un valore lungo un solo byte
                    // forse servirebbe un campo size nella modifica
                    int tmpAct1 = packet[ftam.getOffset()];
                    int tmpAct2 = packet[ftam.getOffset() + 1];
                    packet[ftam.getOffset()] = ftam.getValueHigh();
                    packet[ftam.getOffset() + 1] = ftam.getValueLow();
                    // TODO considerare il caso del multicas quando si inseriscono i  int[] nella flowTableQueue
                    flowTableQueue.add(packet);
                    packet[ftam.getOffset()] = tmpAct1;
                    packet[ftam.getOffset() + 1] = tmpAct2;
                } else {
                    // TODO problemi se si cambia un valore lungo un solo byte
                    // forse servirebbe un campo size nella modifica

                    statusRegister[ftam.getOffset()] = ftam.getValueHigh();
                    statusRegister[ftam.getOffset() + 1] = ftam.getValueLow();
                    flowTableQueue.add(packet);
                }
                break;
            case SDN_WISE_AGGREGATE:
                // TODO Ancora da definire
                break;
            case SDN_WISE_FORWARD_UP:
                FlowTableActionCallback ftac = (FlowTableActionCallback) action;
                FunctionInterface srvI = functions.get(ftac.getCallbackId());
                if (srvI != null) {
                    srvI.function(adcRegister,
                            flowTable,
                            neighborTable,
                            statusRegister,
                            acceptedId,
                            flowTableQueue,
                            txQueue,
                            ftac.getCallbackArgument(),
                            new NetworkPacket(packet)
                    );
                }
                break;
            default:
                break;
        }//switch
    }

    int doOperation(int operatore, int item1, int item2) {
        switch (operatore) {
            case SDN_WISE_EQUAL:
                return item1 == item2 ? 1 : 0;
            case SDN_WISE_NOT_EQUAL:
                return item1 != item2 ? 1 : 0;
            case SDN_WISE_BIGGER:
                return item1 > item2 ? 1 : 0;
            case SDN_WISE_LESS:
                return item1 < item2 ? 1 : 0;
            case SDN_WISE_EQUAL_OR_BIGGER:
                return item1 >= item2 ? 1 : 0;
            case SDN_WISE_EQUAL_OR_LESS:
                return item1 <= item2 ? 1 : 0;
            default:
                return 0;
        }
    }

    int searchRule(FlowTableEntry rule) {
        int i, j, sum, target;

        for (i = 0; i < SDN_WISE_RLS_MAX; i++) {
            sum = 0;
            target = SDN_WISE_WINDOWS_MAX;

            for (j = 0; j < SDN_WISE_WINDOWS_MAX; j++) {
                if (flowTable.get(i).getWindows()[j].equals(rule.getWindows()[j])) {
                    sum++;
                }
            }

            if (rule.getAction().isMultimatch()) {
                target++;
                if (flowTable.get(i).getAction().equals(rule.getAction())) {
                    sum++;
                }
            }

            if (sum == target) {
                return i;
            }
        }
        return SDN_WISE_RLS_MAX + 1;
    }

    int getNeighborIndex(NodeAddress addr) {
        int i;
        for (i = 0; i < SDN_WISE_NEIGHBORS_MAX; i++) {

            if (neighborTable.get(i).getAddr().equals(addr)) {
                return i;
            }
            if (neighborTable.get(i).getAddr().equals(new NodeAddress(255, 255))) { //se sono uguali
                return -1;
            }
        }
        return SDN_WISE_NEIGHBORS_MAX + 1;
    }

    int searchAcceptedId(int addr) {
        int i;
        for (i = 0; i < SDN_WISE_ACCEPTED_ID_MAX; i++) {
            if (acceptedId.get(i).intValue() == addr) {
                return i;
            }
        }
        return SDN_WISE_ACCEPTED_ID_MAX + 1;
    }

    boolean isAcceptedIdAddress(int addr_h, int addr_l) {
        return (addr_h == addr.getHigh()
                && addr_l == addr.getLow())
                || (addr_h == 255 && addr_l == 255)
                || (searchAcceptedId(new NodeAddress(addr_h, addr_l).intValue())
                != SDN_WISE_ACCEPTED_ID_MAX + 1);
    }

    boolean isAcceptedIdPacket(int[] packet) {
        return isAcceptedIdAddress(packet[SDN_WISE_DST_H], packet[SDN_WISE_DST_L]);
    }

    int getActualFlowIndex(int j) {
        //j = j % SDN_WISE_RLS_MAX;
        int i;
        if (j == 0) {
            i = 0;
        } else {
            i = flow_table_free_pos - j;
            if (i == 0) {
                i = SDN_WISE_RLS_MAX - 1;
            } else if (i < 0) {
                i = SDN_WISE_RLS_MAX - 1 + i;
            }
        }
        return i;
    }

    abstract void SDN_WISE_Callback(int[] packet);

    abstract void controllerTX(int[] packetInt);

    public void radioTX(int[] packetInt, boolean sdn_wise_mac_send_unicast) {
        battery.transmitRadio(packetInt.length);

        System.out.println("[N" + addr.toString() + "]: RTX " + Arrays.toString(packetInt));

        NetworkPacket np = new NetworkPacket(packetInt);

        NodeAddress tmpNxHop = np.getNxhop();
        NodeAddress tmpDst = np.getDst();

        if (tmpDst.equals(new NodeAddress("255.255"))) {
            for (FakeInfo isa : neighbourList.values()) {
                DatagramPacket pck = new DatagramPacket(np.toByteArray(), np.getLen(),
                        isa.inetAddress.getAddress(), isa.inetAddress.getPort());
                try {
                    socket.send(pck);

                } catch (IOException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            FakeInfo isa = neighbourList.get(tmpNxHop);
            if (isa != null) {
                try {
                    DatagramPacket pck = new DatagramPacket(np.toByteArray(), np.getLen(),
                            isa.inetAddress.getAddress(), isa.inetAddress.getPort());
                    socket.send(pck);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(SensorNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SensorNode.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public void arraycopy(int[] src, int srcPos, byte[] dst, int dstPos, int len) {
        for (int i = 0; i < len; i++) {
            dst[dstPos + i] = (byte) src[srcPos + i];
        }
    }

    public void arraycopy(byte[] src, int srcPos, int[] dst, int dstPos, int len) {
        for (int i = 0; i < len; i++) {
            dst[dstPos + i] = src[srcPos + i];
        }
    }

    public FunctionInterface createServiceInterface(byte[] classFile) {
        CustomClassLoader cl = new CustomClassLoader();
        FunctionInterface srvI = null;
        Class service = cl.defClass(classFile, classFile.length);
        try {
            srvI = (FunctionInterface) service.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
        return srvI;
    }

    private class FakeInfo {

        InetSocketAddress inetAddress;
        int rssi;

        FakeInfo(InetSocketAddress inetAddress, int rssi) {
            this.inetAddress = inetAddress;
            this.rssi = rssi;
        }
    }

    private class Task extends TimerTask {

        private int cntBeacon;
        private int cntReport;
        private int cntUpdTable;

        @Override
        public void run() {
            if (semaphore == 1) {
                battery.keepAlive(1);

                cntBeacon++;
                cntReport++;
                //cntUpdTable++; // TODO IMBRACCHIO!!!!!!

                if ((cntBeacon) >= cnt_beacon_max) {
                    cntBeacon = 0;
                    txBEACON();
                }

                if ((cntReport) >= cnt_report_max) {
                    cntReport = 0;
                    txREPORT();
                }

                if ((cntUpdTable) >= cnt_updtable_max) {
                    cntUpdTable = 0;
                    updateTable();
                }
            }
        }

    }

    private class PacketSender implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    radioTX(txQueue.take(), SDN_WISE_MAC_SEND_UNICAST);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class PacketManager implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    int rssi;
                    int[] tmp = flowTableQueue.take();
                    NetworkPacket tmpPacket = new NetworkPacket(tmp);
                    FakeInfo fk = neighbourList.get(tmpPacket.getSrc());
                    if (fk != null) {
                        rssi = fk.rssi;
                    } else {
                        rssi = 255;
                    }
                    battery.receiveRadio(tmp.length);
                    rxHandler(tmp, rssi);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void rxHandler(int[] packet, int rssi) throws InterruptedException {
            System.out.println("[N" + addr.toString() + "]: RRX " + Arrays.toString(packet));

            if (packet[SDN_WISE_LEN] > SDN_WISE_DFLT_HDR_LEN
                    && packet[SDN_WISE_NET_ID] == net_id
                    && packet[SDN_WISE_TTL] != 0) {

                switch (packet[SDN_WISE_TYPE]) {
                    case SDN_WISE_DATA:
                        rxData(packet);
                        break;

                    case SDN_WISE_BEACON:
                        rxBeacon(packet, rssi);
                        break;

                    case SDN_WISE_RESPONSE:
                        rxResponse(packet);
                        break;

                    case SDN_WISE_OPEN_PATH:
                        rxOpenPath(packet);
                        break;

                    case SDN_WISE_CONFIG:
                        rxConfig(packet);
                        break;

                    case SDN_WISE_DPID_CONNECTION:
                    case SDN_WISE_MULTICAST_GROUP_JOIN:
                    case SDN_WISE_MULTICAST_GROUP_LEAVE:
                        break;

                    case SDN_WISE_GEO_DATA:
                        rxGeoData(packet);
                        break;

                    case SDN_WISE_GEO_COORDINATES:
                        rxGeoCoordinates(packet);
                        break;

                    case SDN_WISE_GEO_REPORT:
                        rxGeoReport(packet);
                        break;

                    default:
                        rxREPORT(packet);
                        break;
                }// fine switch sul type
            }// fine if sull'address
        }
    }

    private class CustomClassLoader extends ClassLoader {

        public Class defClass(byte[] data, int len) {
            return defineClass(null, data, 0, len);
        }
    }

}
