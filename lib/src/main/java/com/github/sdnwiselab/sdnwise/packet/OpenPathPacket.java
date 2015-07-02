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
import java.util.LinkedList;
import java.util.List;

/**
 * This class models an Open Path packet.
 * 
 * @author Sebastiano Milardo
 */
public class OpenPathPacket extends NetworkPacket{

    /**
     * This constructor initialize an open path packet starting from a byte array.
     * 
     * @param data the byte array representing the open path packet.
     */
    public OpenPathPacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize an open path packet. The type of the packet
     * is set to SDN_WISE_OPEN_PATH.
     */
    public OpenPathPacket() {
        super();
        this.setType(SDN_WISE_OPEN_PATH);
    }

    /**
     * This constructor initialize an open path packet starting from a int array.
     * 
     * @param data the int array representing the open path packet, all int are
     * casted to byte.
     */
    public OpenPathPacket(int[] data) {
        super(data);
    }
    
    /**
     * Setter for the path in the Open Path packet. A path is a list of 
     * NodeAddress objects. Each node receiving this method will learn two 
     * rules. One to reach the first node in the path and one to reach the last 
     * one in the path.
     * 
     * @param path a list containing all the node in a path.
     * @return the packet itself.
     */
    public OpenPathPacket setPath(List<NodeAddress> path){
        byte i = 0;
        for (NodeAddress addr : path) {
            this.setPayloadAt(addr.getHigh(), i);
            i++;
            this.setPayloadAt(addr.getLow(), i);
            i++;
        }
        return this;
    }
    
    /**
     * Getter for the path in the Open Path packet. A path is a list of 
     * NodeAddress objects. Each node receiving this method will learn two 
     * rules. One to reach the first node in the path and one to reach the last 
     * one in the path.
     * 
     * @return the list of NodeAddress in the path.
     */
    public List<NodeAddress> getPath(){
        LinkedList<NodeAddress> list = new LinkedList<>();
        byte[] payload = this.getPayload();
        for (int i = 0 ; i<payload.length-1; i+=2){
            list.add(new NodeAddress(payload[i], payload[i+1]));
        }
        return list;
    }
    
}
