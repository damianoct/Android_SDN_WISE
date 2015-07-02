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
package com.github.sdnwiselab.sdnwise.util;

/**
 * This Class represents the Neighbor of a NodeAddress,
 * specifying its rssi and level battery value.
 * 
 * @author Sebastiano Milardo
 */
public class Neighbor {

    private NodeAddress addr;
    private int rssi;
    private int batt;

    /**
     * Constructor method for this class by following attributes.
     * 
     * @param addr NodeAddress Object.
     * @param rssi rssi of the NodeAddress.
     * @param batt battery value of the NodeAddress.
     */
    public Neighbor(NodeAddress addr, int rssi, int batt) {
        this.addr = addr;
        this.rssi = rssi;
        this.batt = batt;
    }

     /**
     * Simple Constructor method for this class.
     * 
     */
    public Neighbor() {
        this.addr = new NodeAddress(255, 255);
        this.rssi = 255;
        this.batt = 255;
    }

    /**
     * Getter method to obtain NodeAddress object.
     * 
     * @return a NodeAddress.
     */
    public NodeAddress getAddr() {
        return addr;
    }

    /**
     * Setter method to set NodeAddress.
     * 
     * @param addr the NodeAddress will be set.
     */
    public void setAddr(NodeAddress addr) {
        this.addr = addr;
    }

    /**
     * Getter method to obtain rssi of a NodeAddress object.
     * 
     * @return int value rssi of the NodeAddress.
     */
    public int getRssi() {
        return rssi;
    }
    
    /**
     * Setter method to set rssi for NodeAddress object.
     * 
     * @param rssi the rssi of NodeAddress will be set.
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * Getter method to obtain battery value of a NodeAddress object.
     * 
     *  @return int value battery of the NodeAddress.
     */
    public int getBatt() {
        return batt;
    }
    
    /**
     * Setter method to set battery for NodeAddress object.
     * 
     * @param batt the battery value of NodeAddress will be set.
     */
    public void setBatt(int batt) {
        this.batt = batt;
    }
}
