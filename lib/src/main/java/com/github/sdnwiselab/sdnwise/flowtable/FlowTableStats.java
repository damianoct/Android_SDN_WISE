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
package com.github.sdnwiselab.sdnwise.flowtable;

import java.util.Arrays;

/**
 * FlowTableStats is part of the structure of the Entry of a FlowTable.
 * This Class implements FlowTableInterface.
 * 
 * @author Sebastiano Milardo
 */
public class FlowTableStats implements FlowTableInterface {

    public final static byte SIZE = 2;
    public final static byte COPY_SIZE = 1;
    
    private final static byte ttlIndex = 0;
    private final static byte countIndex = 1;
    private final byte[] stats = new byte[SIZE];

    /**
     * Simple constructor for the FlowTableStats object.
     * 
     * It sets the statistical fields to the default values.
     */
    public FlowTableStats() {
        stats[ttlIndex] = (byte) 255;
        stats[countIndex] = 0;
    }

    /**
     * Constructor for the FlowTableStats object.
     * 
     * @param value byte array to copy into the statistical part.
     */
    public FlowTableStats(byte[] value) {
        if (value.length == 2) {
            stats[ttlIndex] = value[ttlIndex];
            stats[countIndex] = value[countIndex];
        } else if (value.length == 1){
            stats[ttlIndex] = value[ttlIndex];
            stats[countIndex] = 0;
        } else {
            stats[ttlIndex] = (byte) 255;
            stats[countIndex] = 0;
        }
    }

    /**
     * Constructor for the FlowTableStats object.
     * 
     * @param ttl byte value to insert in the statistical part.
     * @param count byte value to insert in the statistical part.
     */
    public FlowTableStats(int ttl, int count) {
        this.stats[ttlIndex] = (byte) ttl;
        this.stats[ttlIndex] = (byte) count; //al posto di ttlIndex non va countIndex?????
    }

    /**
     * Getter Method to obtain the ttl value. When the TTL of an entry is equal
     * to 0 the entry is remove from the FlowTable.
     * 
     * @return value of ttl of stats[].
     */
    public int getTtl() {
        return stats[ttlIndex] & 0xFF;
    }

    /**
     * Setter Method to set the ttl value. When the TTL of an entry is equal
     * to 0 the entry is remove from the FlowTable.
     * 
     * @param ttl to be set
     * @return this FlowTableStats
     */
    public FlowTableStats setTtl(int ttl) {
        this.stats[ttlIndex] = (byte) ttl;
        return this;
    }

    /**
     * Getter Method to obtain count value. The count value represent the number
     * of times an entry has been executed in the FlowTable. This value is not
     * sent to a node.
     * 
     * @return value of count of stats[].
     */
    public int getCounter() {
        return stats[countIndex] & 0xFF;
    }

    /**
     * Setter Method to set count value. The count value represent the number
     * of times an entry has been executed in the FlowTable. This value is not
     * sent to a node.
     * 
     * @param count to be set
     * @return this FlowTableStats
     */
    public FlowTableStats setCounter(int count) {
        this.stats[countIndex] = (byte) count;
        return this;
    }

    @Override
    public String toString() {
        return (stats[ttlIndex] & 0xFF) + "," + (stats[countIndex] & 0xFF) + ' ';
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(stats, SIZE);
    }
}
