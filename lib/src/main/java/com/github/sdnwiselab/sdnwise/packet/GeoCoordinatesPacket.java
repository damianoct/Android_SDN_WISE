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

/**
 *
 * @author Sebastiano Milardo
 */
public class GeoCoordinatesPacket extends NetworkPacket {

    /**
     * This constructor initialize a geo coordinates packet starting from a byte
     * array.
     *
     * @param data the byte array representing the geo coordinates packet.
     */
    public GeoCoordinatesPacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a geo coordinates packet. The type of the
     * packet is set to SDN_WISE_GEO_COORDINATES.
     */
    public GeoCoordinatesPacket() {
        super();
        this.setType(SDN_WISE_GEO_COORDINATES);
    }

    /**
     * This constructor initialize a geo coordinates packet starting from a int
     * array.
     *
     * @param data the int array representing the geo coordinates packet, all
     * int are casted to byte.
     */
    public GeoCoordinatesPacket(int[] data) {
        super(data);
    }

    public GeoCoordinatesPacket setCoordinates(int x, int y, int z) {
        setPayloadAt((byte) x, 0);
        setPayloadAt((byte) (x >> 8), 1);

        setPayloadAt((byte) y, 2);
        setPayloadAt((byte) (y >> 8), 3);

        setPayloadAt((byte) z, 4);
        setPayloadAt((byte) (z >> 8), 5);

        return this;
    }

    public int[] getCoordinates() {
        int[] coordinates = new int[3];
        coordinates[0] = this.getPayloadAt(0) + this.getPayloadAt(1) * 256;
        coordinates[1] = this.getPayloadAt(2) + this.getPayloadAt(3) * 256;
        coordinates[2] = this.getPayloadAt(4) + this.getPayloadAt(5) * 256;
        return coordinates;
    }
}
