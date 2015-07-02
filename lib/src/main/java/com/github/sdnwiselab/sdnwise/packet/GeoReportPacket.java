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
public class GeoReportPacket extends NetworkPacket{
    
    private static int BLOCK_SIZE = 8;
    
    /**
     * This constructor initialize a geo report packet starting from a byte
     * array.
     *
     * @param data the byte array representing the geo report packet.
     */
    public GeoReportPacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a geo report packet. The type of the
     * packet is set to SDN_WISE_GEO_COORDINATES.
     */
    public GeoReportPacket() {
        super();
        this.setType(SDN_WISE_GEO_COORDINATES);
    }

    /**
     * This constructor initialize a geo report packet starting from a int
     * array.
     *
     * @param data the int array representing the geo coordinates packet, all
     * int are casted to byte.
     */
    public GeoReportPacket(int[] data) {
        super(data);
    }

    public GeoReportPacket setNodeAddressAt(
            NodeAddress addr,
            int index){     
        setPayloadAt((byte) addr.getHigh(),(BLOCK_SIZE*index));
        setPayloadAt((byte) addr.getLow(), 1 + (BLOCK_SIZE*index));
        return this;
    }
    
    public GeoReportPacket setCoordinatesAt(
            int x, int y, int z,
            int index) 
    {     
        setPayloadAt((byte) x, 2 + (BLOCK_SIZE*index));
        setPayloadAt((byte) (x >> 8), 3 + (BLOCK_SIZE*index));

        setPayloadAt((byte) y, 4 + (BLOCK_SIZE*index));
        setPayloadAt((byte) (y >> 8), 5 + (BLOCK_SIZE*index));

        setPayloadAt((byte) z, 6 + (BLOCK_SIZE*index));
        setPayloadAt((byte) (z >> 8), 7 + (BLOCK_SIZE*index));
        return this;
    }

    public int[] getCoordinatesAt(int index) 
    {
        int[] coordinates = new int[3];
        coordinates[0] = this.getPayloadAt(2+(index*BLOCK_SIZE)) + 
                this.getPayloadAt(3+(index*BLOCK_SIZE)) * 256;
        coordinates[1] = this.getPayloadAt(4+(index*BLOCK_SIZE)) + 
                this.getPayloadAt(5+(index*BLOCK_SIZE)) * 256;
        coordinates[2] = this.getPayloadAt(6+(index*BLOCK_SIZE)) + 
                this.getPayloadAt(7) * 256;
        return coordinates;
    }
    
    public NodeAddress getNodeAddressAt(int index) 
    {
        return new NodeAddress(
                this.getPayloadAt(0+(index*BLOCK_SIZE)),
                this.getPayloadAt(1+(index*BLOCK_SIZE))
        );     
    }
    
    public int getNeighborsSize(){
        return this.getPayloadSize() / BLOCK_SIZE;
    }
}
