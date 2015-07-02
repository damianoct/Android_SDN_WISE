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

/**
 * This class models a Data packet.
 * 
 * @author Sebastiano Milardo
 */
public class DataPacket extends NetworkPacket{

    /**
     * This constructor initialize a data packet starting from a byte array.
     * 
     * @param data the byte array representing the data packet.
     */
    public DataPacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a data packet. The type of the packet
     * is set to SDN_WISE_DATA.
     */
    public DataPacket() {
        super();
        this.setType(SDN_WISE_DATA);
    }

    /**
     * This constructor initialize a data packet starting from a int array.
     * 
     * @param data the int array representing the data packet, all int are
     * casted to byte.
     */
    public DataPacket(int[] data) {
        super(data);
    }
    
}
