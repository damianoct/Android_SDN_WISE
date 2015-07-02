/*
 * Copyright (C) 2015 Seb
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
 *
 * @author Seb
 */
public class GeoDataPacket extends NetworkPacket {

    public final static byte SDN_WISE_GEO_GROUP_H = 10,
            SDN_WISE_GEO_GROUP_L = 11,
            SDN_WISE_GEO_INIT_H = 12,
            SDN_WISE_GEO_INIT_L = 13,
            SDN_WISE_GEO_PREV_H = 14,
            SDN_WISE_GEO_PREV_L = 15,
            SDN_WISE_GEO_CUR_H = 16,
            SDN_WISE_GEO_CUR_L = 17;

    /**
     * This constructor initialize a geo coordinates packet starting from a byte
     * array.
     *
     * @param data the byte array representing the geo coordinates packet.
     */
    public GeoDataPacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a geo coordinates packet. The type of the
     * packet is set to SDN_WISE_GEO_COORDINATES.
     */
    public GeoDataPacket() {
        super();
        this.setType(SDN_WISE_GEO_DATA);
    }

    /**
     * This constructor initialize a geo coordinates packet starting from a int
     * array.
     *
     * @param data the int array representing the geo coordinates packet, all
     * int are casted to byte.
     */
    public GeoDataPacket(int[] data) {
        super(data);
    }

    public NodeAddress getGroupAddress() {
        return new NodeAddress(this.getPayloadAt(SDN_WISE_GEO_GROUP_H),
                this.getPayloadAt(SDN_WISE_GEO_GROUP_L));
    }

    public GeoDataPacket setGroupAddress(NodeAddress addr) {
        super.setPayloadAt(SDN_WISE_GEO_GROUP_H, addr.getHigh());
        super.setPayloadAt(SDN_WISE_GEO_GROUP_L, addr.getLow());
        return this;
    }

    public NodeAddress getInitiatorAddress() {
        return new NodeAddress(this.getPayloadAt(SDN_WISE_GEO_INIT_H),
                this.getPayloadAt(SDN_WISE_GEO_INIT_L));
    }

    public GeoDataPacket setInitiatorAddress(NodeAddress addr) {
        super.setPayloadAt(SDN_WISE_GEO_INIT_H, addr.getHigh());
        super.setPayloadAt(SDN_WISE_GEO_INIT_L, addr.getLow());
        return null;
    }

    public NodeAddress getPreviousMulticastNodeAddress() {
        return new NodeAddress(this.getPayloadAt(SDN_WISE_GEO_PREV_H),
                this.getPayloadAt(SDN_WISE_GEO_PREV_L));
    }

    public GeoDataPacket setPreviousMulticastNodeAddress(NodeAddress addr) {
        super.setPayloadAt(SDN_WISE_GEO_PREV_H, addr.getHigh());
        super.setPayloadAt(SDN_WISE_GEO_PREV_L, addr.getLow());
        return null;
    }

    public NodeAddress getCurrentMulticastNodeAddress() {
        return new NodeAddress(this.getPayloadAt(SDN_WISE_GEO_CUR_H),
                this.getPayloadAt(SDN_WISE_GEO_CUR_L));
    }

    public GeoDataPacket setCurrentMulticastNodeAddress(NodeAddress addr) {
        super.setPayloadAt(SDN_WISE_GEO_CUR_H, addr.getHigh());
        super.setPayloadAt(SDN_WISE_GEO_CUR_L, addr.getLow());
        return null;
    }
}
