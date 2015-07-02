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

/**
 * This class models a Beacon packet.
 * 
 * @author Sebastiano Milardo
 */
public class BeaconPacket extends NetworkPacket{

    public final static NodeAddress BROADCAST_ADDR = new NodeAddress("255.255");
    public final static int SDN_WISE_DIST = 10,
                            SDN_WISE_BATT = 11;
    /**
     * This constructor initialize a beacon packet starting from a byte array.
     * 
     * @param data the byte array representing the beacon packet.
     */
    public BeaconPacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a beacon packet. The type of the packet
     * is set to SDN_WISE_BEACON and the destination address is BROADCAST_ADDR.
     */
    public BeaconPacket() {
        super();
        this.setType(SDN_WISE_BEACON);
        this.setDst(BROADCAST_ADDR);
    }

    /**
     * This constructor initialize a beacon packet starting from a int array.
     * 
     * @param data the int array representing the beacon packet, all int are
     * casted to byte.
     */
    public BeaconPacket(int[] data) {
        super(data);
    }
    
    
    /**
     * Getter for the number of hops between the source node and the sink. 
     *
     * @return the number of hops between the source node and the sink.
     */
    public int getDist() {
        return this.getPayloadAt(0) & 0xFF;
    }

    /**
     * Setter for the number of hops between the source node and the sink. 
     *
     * @param value the number of hops.
     * @return the packet itself.
     */
    public NetworkPacket setDist(byte value) {
        this.setPayloadAt(value, 0);
        return this;
    }

    /**
     * Returns an estimation of the residual charge of the batteries of the
     * node. The possible values are: [0-255] 0 = no charge,
     * 255 = full charge.
     *
     * @return an estimation of the residual charge of the batteries of the node
     */
    public int getBatt() {
        return this.getPayloadAt(1) & 0xFF;
    }

    /**
     * Set the battery level in the packet. 
     * The possible values are: [0-FF] 0 = no charge,FF = full charge.
     * 
     * @param value the value of the battery level.
     * @return the packet itself.
     */
    public NetworkPacket setBatt(byte value) {
        this.setPayloadAt(value, 1);
        return this;
    }
    
    /**
     * Set the address of the Sink to which this node is connected. 
     * 
     * @param addr the address of the Sink.
     * @return the packet itself.
     */
    public NetworkPacket setSinkAddress(NodeAddress addr) {
        this.setNxhop(addr);
        return this;
    }
    
    /**
     * Get the address of the Sink to which this node is connected. 
     * 
     * @return the address of the Sink.
     */
    public NodeAddress getSinkAddress() {
        return this.getNxhop();
    }
    
}
