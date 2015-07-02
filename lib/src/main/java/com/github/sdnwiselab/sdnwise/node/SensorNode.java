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

import com.github.sdnwiselab.sdnwise.flowtable.FlowTableActionForward;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry.SDN_WISE_WINDOWS_MAX;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableStats;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_EQUAL;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_NOT_EQUAL;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_PACKET;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_SIZE_1;
import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_SIZE_2;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_ACCEPTED_ID_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_BEACON_HDR_LEN;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_CONFIG_HDR_LEN;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_DFLT_CNT_SLEEP_MAX;
import static com.github.sdnwiselab.sdnwise.node.Constants.SDN_WISE_MAC_SEND_UNICAST;
import com.github.sdnwiselab.sdnwise.packet.BeaconPacket;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ADD_ACCEPTED;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ADD_FUNCTION;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_GET_RULE_INDEX;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_ADDR;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_CNT_BEACON_MAX;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_CNT_REPORT_MAX;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_CNT_SLEEP_MAX;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_NET_ID;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_RSSI_MIN;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_ID_TTL_MAX;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_LIST_ACCEPTED;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_REMOVE_ACCEPTED;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_REMOVE_FUNCTION;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_REMOVE_RULE;
import static com.github.sdnwiselab.sdnwise.packet.ConfigPacket.SDN_WISE_CNF_REMOVE_RULE_INDEX;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.ConfigPacket;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_DST_H;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_DST_L;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_LEN;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_NET_ID;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_NXHOP_H;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_NXHOP_L;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_SRC_H;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_SRC_L;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_TTL;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_TYPE;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author Sebastiano Milardo
 */
public class SensorNode extends Node {

    public SensorNode(byte net_id, NodeAddress node,
            int port,
            String configNeighbourFilePath, boolean isSecure) {
        super(node, net_id, port, configNeighbourFilePath, isSecure);
        setSemaphore(0);
        setNum_hop_vs_sink(ttl_max + 1);
        setRssi_vs_sink(0);

        cnt_sleep_max = SDN_WISE_DFLT_CNT_SLEEP_MAX;
    }

    @Override
    public void resetSemaphore() {
        setSemaphore(0);
        setNum_hop_vs_sink(255);
    }

    @Override
    void SDN_WISE_Callback(int[] packet) {
        if (this.functions.get(1) == null) {
            packet[SDN_WISE_SRC_H] = addr.getHigh();
            packet[SDN_WISE_SRC_L] = addr.getLow();
            packet[SDN_WISE_DST_H] = flowTable.get(0).getWindows()[0].getValueHigh();
            packet[SDN_WISE_DST_L] = flowTable.get(0).getWindows()[0].getValueLow();
            packet[SDN_WISE_TTL] = ttl_max;
            runFlowMatch(packet);
        } else {
            this.functions.get(1).function(adcRegister, 
                    flowTable, 
                    neighborTable, 
                    statusRegister, 
                    acceptedId, 
                    flowTableQueue,
                    txQueue,
                    0,
                    new NetworkPacket(packet));         
        }
    }

    @Override
    public void setupSecurity() {
    }

    @Override
    void rxREPORT(int[] packet) {
        runFlowMatch(packet);
    }

    @Override
    void rxBeacon(int[] packet, int rssi) {
        BeaconPacket bp = new BeaconPacket(packet);
        int keyLen = packet.length - SDN_WISE_BEACON_HDR_LEN;
        if (keyLen > 0) {
            receivePublicKey(packet, keyLen);
        }
        if (rssi > rssi_min) {

            // Il nodo è abilitato a trasmettere report solo quando semaphore==1
            // Adesso devo capire a che distanza dal sink è il nodo mittente
            // TODO controllare nel nodo fisico se quello sotto è un < o <=
            if (bp.getDist() < this.getNum_hop_vs_sink() && (rssi >= getRssi_vs_sink())) {
                this.setSemaphore(1);
                // Il mittente mi garantisce una distanza minore, scelgo lui come next_hop
                // e aggiorno la regola relativa all'inoltro
                int k;

                // Questa regola dice:
                // Se ha per destinazione il sink e non è un data packet forwarda al nodo
                // che mi garantisce il next hop migliore
                flowTable.get(0).getWindows()[0]
                        .setOperator(SDN_WISE_EQUAL)
                        .setSize(SDN_WISE_SIZE_2)
                        .setLocation(SDN_WISE_PACKET);
                flowTable.get(0).getWindows()[0].setPos(SDN_WISE_DST_H);
                flowTable.get(0).getWindows()[0].setValueHigh(packet[SDN_WISE_NXHOP_H]);
                flowTable.get(0).getWindows()[0].setValueLow(packet[SDN_WISE_NXHOP_L]);

                flowTable.get(0).getWindows()[1]
                        .setOperator(SDN_WISE_NOT_EQUAL)
                        .setSize(SDN_WISE_SIZE_1)
                        .setLocation(SDN_WISE_PACKET);
                flowTable.get(0).getWindows()[1].setPos(SDN_WISE_TYPE);
                flowTable.get(0).getWindows()[1].setValueHigh(0);
                flowTable.get(0).getWindows()[1].setValueLow(0);

                for (k = 2; k < SDN_WISE_WINDOWS_MAX; k++) {
                    flowTable.get(0).getWindows()[k] = new FlowTableWindow();
                }
                
                flowTable.get(0).setAction(new FlowTableActionForward()
                        .setBroadcast(false)
                        .setNextHop(bp.getSrc())
                        );
                
                flowTable.get(0).setStats(new FlowTableStats());
                
                setNum_hop_vs_sink(bp.getDist() + 1);
            } else if (
                    bp.getDist() == this.getNum_hop_vs_sink()
                    && ((FlowTableActionForward)flowTable.get(0).getAction())
                            .getNextHop().equals(bp.getSrc())) 
            {
                flowTable.get(0).getStats().setTtl(255);
            }
            super.rxBeacon(packet, rssi);
        }
    }

    @Override //funzione richiamata in runFlowMatch
    void controllerTX(int[] packet) {
        radioTX(packet, SDN_WISE_MAC_SEND_UNICAST);
    }
    
    
    @Override
    void rxConfig(int[] packet) {
        NodeAddress dest = new NodeAddress(packet[SDN_WISE_DST_H],
                packet[SDN_WISE_DST_L]);

        if (!dest.equals(addr)) {
            runFlowMatch(packet);
        } else {

            int toBeSent = 0;
            int pos;
            int ii = 1;

            int isWrite = packet[SDN_WISE_CONFIG_HDR_LEN] >> 7;
            int id = packet[SDN_WISE_CONFIG_HDR_LEN] & ~(1 << 7);
            int value = new NodeAddress(packet[SDN_WISE_CONFIG_HDR_LEN + 1],
                    packet[SDN_WISE_CONFIG_HDR_LEN + 2]).intValue();

            if (isWrite != 0) {
                switch (id) {
                    case SDN_WISE_CNF_ID_ADDR:
                        addr = new NodeAddress(value);
                        break;
                    case SDN_WISE_CNF_ID_NET_ID:
                        net_id = packet[SDN_WISE_CONFIG_HDR_LEN + 2];
                        break;
                    case SDN_WISE_CNF_ID_CNT_BEACON_MAX:
                        cnt_beacon_max = value;
                        break;
                    case SDN_WISE_CNF_ID_CNT_REPORT_MAX:
                        cnt_report_max = value;
                        break;
                    case SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX:
                        cnt_updtable_max = value;
                        break;
                    case SDN_WISE_CNF_ID_CNT_SLEEP_MAX:
                        cnt_sleep_max = value;
                        break;
                    case SDN_WISE_CNF_ID_TTL_MAX:
                        ttl_max = packet[SDN_WISE_CONFIG_HDR_LEN + 2];
                        break;
                    case SDN_WISE_CNF_ID_RSSI_MIN:
                        rssi_min = packet[SDN_WISE_CONFIG_HDR_LEN + 2];
                        break;
                    case SDN_WISE_CNF_ADD_ACCEPTED:
                        pos = searchAcceptedId(value);
                        if (pos == (SDN_WISE_ACCEPTED_ID_MAX + 1)) {
                            pos = searchAcceptedId(65535);
                            acceptedId.set(pos,new NodeAddress(value));
                        }
                        break;
                    case SDN_WISE_CNF_REMOVE_ACCEPTED:
                        pos = searchAcceptedId(value);
                        if (pos != (SDN_WISE_ACCEPTED_ID_MAX + 1)) {
                            acceptedId.set(pos,new NodeAddress(65535));
                        }
                        break;
                    case SDN_WISE_CNF_REMOVE_RULE_INDEX:
                        if (value != 0) {
                            initRule(flowTable.get(getActualFlowIndex(value)));
                        }
                        break;
                    case SDN_WISE_CNF_REMOVE_RULE:
                        //TODO
                        break;
                    case SDN_WISE_CNF_ADD_FUNCTION:
                        if (functionBuffer.get(value) == null) {
                            functionBuffer.put(value, new LinkedList<int[]>());
                        }
                        functionBuffer.get(value).add(Arrays.copyOfRange(
                                packet, SDN_WISE_CONFIG_HDR_LEN + 5,
                                packet.length));

                        if (functionBuffer.get(value).size() == packet[SDN_WISE_CONFIG_HDR_LEN + 4]) {
                            int total = 0;
                            for (int[] n : functionBuffer.get(value)) {
                                total += (n.length);
                            }

                            int pointer = 0;

                            byte[] func = new byte[total];

                            for (int[] n : functionBuffer.get(value)) {
                                for (int j = 0; j < n.length; j++) {
                                    func[pointer] = (byte) n[j];
                                    pointer++;
                                }
                            }

                            functions.put(value, createServiceInterface(func));
                            System.out.println("[N"+ addr.toString() +"]: New Function Added - " + value);
                            functionBuffer.remove(value);
                        }

                        break;
                    case SDN_WISE_CNF_REMOVE_FUNCTION:
                        functions.remove(value);
                        break;
                    default:
                        break;

                }
            } else {
                toBeSent = 1;
                int[] packetList = new int[116]; // TODO lunghezza giusta
                int iii;
                switch (id) {
                    case SDN_WISE_CNF_ID_ADDR:
                        //e adesso che non ci sono piu' H e L per addr???
                        packet[SDN_WISE_CONFIG_HDR_LEN + 1] = addr.getHigh();
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = addr.getLow();
                        break;
                    case SDN_WISE_CNF_ID_NET_ID:
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = net_id;
                        break;
                    case SDN_WISE_CNF_ID_CNT_BEACON_MAX:
                        packet[SDN_WISE_CONFIG_HDR_LEN + 1] = (cnt_beacon_max >> 8);
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = cnt_beacon_max;
                        break;
                    case SDN_WISE_CNF_ID_CNT_REPORT_MAX:
                        packet[SDN_WISE_CONFIG_HDR_LEN + 1] = (cnt_report_max >> 8);
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = cnt_report_max;
                        break;
                    case SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX:
                        packet[SDN_WISE_CONFIG_HDR_LEN + 1] = (cnt_updtable_max >> 8);
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = cnt_updtable_max;
                        break;
                    case SDN_WISE_CNF_ID_CNT_SLEEP_MAX:
                        packet[SDN_WISE_CONFIG_HDR_LEN + 1] = (cnt_sleep_max >> 8);
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = cnt_sleep_max;
                        break;
                    case SDN_WISE_CNF_ID_TTL_MAX:
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = ttl_max;
                        break;
                    case SDN_WISE_CNF_ID_RSSI_MIN:
                        packet[SDN_WISE_CONFIG_HDR_LEN + 2] = rssi_min;
                        break;
                    case SDN_WISE_CNF_LIST_ACCEPTED:
                        toBeSent = 0;
                        packetList[SDN_WISE_NET_ID] = net_id;
                        packetList[SDN_WISE_SRC_H] = packet[SDN_WISE_DST_H];
                        packetList[SDN_WISE_SRC_L] = packet[SDN_WISE_DST_L];
                        packetList[SDN_WISE_DST_H] = packet[SDN_WISE_SRC_H];
                        packetList[SDN_WISE_DST_L] = packet[SDN_WISE_SRC_L];
                        packetList[SDN_WISE_TYPE] = packet[SDN_WISE_TYPE];
                        packetList[SDN_WISE_TTL] = ttl_max;
                        packetList[SDN_WISE_NXHOP_H] = ((FlowTableActionForward)flowTable.get(0).getAction()).getNextHop().getHigh();
                        packetList[SDN_WISE_NXHOP_L] = ((FlowTableActionForward)flowTable.get(0).getAction()).getNextHop().getLow();

                        packetList[SDN_WISE_CONFIG_HDR_LEN] = SDN_WISE_CNF_LIST_ACCEPTED;

                        for (int jj = 0; jj < SDN_WISE_ACCEPTED_ID_MAX; jj++) {
                            if (acceptedId.get(jj) != new NodeAddress(65535)) {
                                packetList[SDN_WISE_CONFIG_HDR_LEN + ii]
                                        = (acceptedId.get(jj).getHigh());
                                ii++;
                                packetList[SDN_WISE_CONFIG_HDR_LEN + ii]
                                        = (acceptedId.get(jj).getLow());
                                ii++;
                            }
                        }
                        packetList[SDN_WISE_LEN] = (ii
                                + SDN_WISE_CONFIG_HDR_LEN);

                        controllerTX(packetList);

                        break;
                    case SDN_WISE_CNF_GET_RULE_INDEX:
                        toBeSent = 0;

                        System.out.println(Arrays.toString(packetList));

                        packetList[SDN_WISE_NET_ID] = net_id;
                        packetList[SDN_WISE_SRC_H] = packet[SDN_WISE_DST_H];
                        packetList[SDN_WISE_SRC_L] = packet[SDN_WISE_DST_L];
                        packetList[SDN_WISE_DST_H] = packet[SDN_WISE_SRC_H];
                        packetList[SDN_WISE_DST_L] = packet[SDN_WISE_SRC_L];
                        packetList[SDN_WISE_TYPE] = packet[SDN_WISE_TYPE];
                        packetList[SDN_WISE_TTL] = ttl_max;
                        packetList[SDN_WISE_NXHOP_H] = ((FlowTableActionForward)flowTable.get(0).getAction()).getNextHop().getHigh();
                        packetList[SDN_WISE_NXHOP_L] = ((FlowTableActionForward)flowTable.get(0).getAction()).getNextHop().getLow();

                        packetList[SDN_WISE_CONFIG_HDR_LEN] = SDN_WISE_CNF_GET_RULE_INDEX;

                        ii = SDN_WISE_CONFIG_HDR_LEN;
                        ii++;
                        packetList[ii] = packet[ii];
                        ii++;
                        packetList[ii] = packet[ii];

                        int jj = getActualFlowIndex(packet[ii]);
                        packetList[ii] = packet[ii];
                        ii++;
                        
                        byte[] tmp = flowTable.get(jj).toByteArray();
                            
                            for (int x = 0; x<tmp.length; x++){
                                packetList[ii] = tmp[x];
                                ii++;
                            }
                            
                            packetList[SDN_WISE_LEN] = ii;

                        controllerTX(Arrays.copyOf(packetList, packetList[0]));
                        break;
                    default:
                        break;
                }
            }

            if (toBeSent != 0) {
                packet[SDN_WISE_SRC_H] = addr.getHigh();
                packet[SDN_WISE_SRC_L] = addr.getLow();
                packet[SDN_WISE_DST_H] = flowTable.get(0).getWindows()[0].getValueHigh();
                packet[SDN_WISE_DST_L] = flowTable.get(0).getWindows()[0].getValueLow();
                packet[SDN_WISE_TTL] = ttl_max;
                runFlowMatch(packet);
            }
        }
    }
}