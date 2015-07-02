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
package com.github.sdnwiselab.sdnwise.packet;

import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * This class models a Report packet.
 * 
 * @author Sebastiano Milardo
 */
public class ReportPacket extends BeaconPacket {

    public final static byte SDN_WISE_MAX_NEIG = 35;
    public final static int SDN_WISE_NEIGH = 12;

     
    /**
     * This constructor initialize a report packet starting from a byte array.
     * 
     * @param data the byte array representing the report packet.
     */
    public ReportPacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a report packet. The type of the packet
     * is set to SDN_WISE_REPORT.
     */
    public ReportPacket() {
        super();
        this.setType(SDN_WISE_REPORT);
    }

    /**
     * This constructor initialize a report packet starting from a int array.
     * 
     * @param data the int array representing the report packet, all int are
     * casted to byte.
     */
    public ReportPacket(int[] data) {
        super(data);
    }

    /**
     * Getter for the number of neighbors of the source node. 
     *
     * @return the number of neighbors.
     */
    public int getNeigh() {
        return this.getPayloadAt(2) & 0xFF;
    }

    /**
     * Setter for the number of neighbors of the source node.
     * 
     * @param value the number of neighbors.
     * @return the packet itself.
     */
    public NetworkPacket setNeigh(int value) {
        if (value <= SDN_WISE_MAX_NEIG){
            this.setPayloadAt((byte)value, 2);
            this.setPayloadSize((byte)(3 + value * 3));
        }else{
            throw new IllegalArgumentException("Too many neighbors");
        }
        return this;
    }

    /**
     * Getter for the NodeAddress of the i-th node in the neighbor list. 
     *
     * @param i the i-th node in the neighbors list
     * @return the NodeAddress of the i-th node in the neighbors list
     */
    public NodeAddress getNeighbourAddress(int i) {
        if (i <= SDN_WISE_MAX_NEIG){
        return new NodeAddress(
                this.getPayloadAt(3 + i*3),
                this.getPayloadAt(4 + (i*3)));
        } else {
            throw new IllegalArgumentException("Index exceeds max number of neighbors");
        }
    }

    /**
     * Setter for the NodeAddress of the i-th node in the neighbor list. 
     *
     * @param addr the address of the i-th NodeAddress.
     * @param i the position where the NodeAddress will be inserted.
     * @return 
     */
    public ReportPacket setNeighbourAddressAt(NodeAddress addr, int i) {
        if (i <= SDN_WISE_MAX_NEIG){
            this.setPayloadAt(addr.getHigh(),(byte)(3+ i*3));
            this.setPayloadAt(addr.getLow(),(byte)(4 + (i*3)));
            if (this.getNeigh() < i){
                this.setNeigh(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException("Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Getter for the rssi value between the i-th node in the neighbor list and the
     * source node. 
     * 
     * @param i the i-th node in the neighbors list
     * @return the rssi value
     */
    public int getNeighbourWeight(int i) {
        if (i <= SDN_WISE_MAX_NEIG){
            return this.getPayloadAt(5 + (i*3));
        } else {
            throw new IllegalArgumentException("Index exceeds max number of neighbors");
        }
    }

    /**
     * Setter for the rssi value between the i-th node in the neighbor list and the
     * source node. 
     * 
     * @param i the i-th node in the neighbors list.
     * @param value the weight of the link.
     * @return the packet itself.
     */
    public ReportPacket setNeighbourWeightAt(byte value, int i) {
        if (i <= SDN_WISE_MAX_NEIG){
            this.setPayloadAt(value, (byte)(5 + i*3));
            if (this.getNeigh() < i){
                this.setNeigh(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException("Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Gets the list of Neighbors.
     *
     * @return an HashMap filled with the neighbors and their weights.
     */
    public HashMap<NodeAddress, Byte> getNeighborsHashMap() {
        HashMap<NodeAddress, Byte> map = new HashMap<>();
        int nNeig = this.getNeigh();
        for (int i = 0; i < nNeig; i++) {
            map.put(this.getNeighbourAddress(i),
                    (byte)this.getNeighbourWeight(i));
        }
        return map;
    }

    /**
     * Sets the list of Neighbors.
     *
     * @param map the map of neighbors to be set
     * @return
     */
    public NetworkPacket setNeighborsHashMap(HashMap<NodeAddress, Byte> map) {
        int i = 0;
        for (Map.Entry<NodeAddress, Byte> entry : map.entrySet()) {
            this.setNeighbourAddressAt(entry.getKey(),i);
            this.setNeighbourWeightAt(entry.getValue(), i);
            i++;
        }
        this.setNeigh((byte) map.size());
        return this;
    }

}
