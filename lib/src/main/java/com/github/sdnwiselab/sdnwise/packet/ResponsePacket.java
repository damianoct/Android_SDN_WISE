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

import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import java.util.Arrays;

/**
 * This class models an Response packet.
 * 
 * @author Sebastiano Milardo
 */
public class ResponsePacket extends NetworkPacket{

    /**
     * This constructor initialize a response packet starting from a byte array.
     * 
     * @param data the byte array representing the response packet.
     */
    public ResponsePacket(byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a response packet. The type of the packet
     * is set to SDN_WISE_RESPONSE.
     */
    public ResponsePacket() {
        super();
        this.setType(SDN_WISE_RESPONSE);
    }
    
    /**
     * This constructor initialize a response packet starting from a int array.
     * 
     * @param data the int array representing the response packet, all int are
     * casted to byte.
     */
    public ResponsePacket(int[] data){
        super(data);
    }
    
    /**
     * Setter for the rule in the response packet. 
     * 
     * @param rule the FlowTableEntry item used in the NetworkPacket.
     * @return the packet itself.
     */
    public ResponsePacket setRule(FlowTableEntry rule){
        byte[] tmp = rule.toByteArray();
        // the last byte is for stats so it is useless to send in a response
        this.setPayload(Arrays.copyOf(tmp,tmp.length-1));
        return this;
    }
    
    /**
     * Getter for the rule in the response packet. 
     * 
     * @return the rule as a FlowTableEntry..
     */
    public FlowTableEntry getRule() {
        FlowTableEntry rule = new FlowTableEntry(this.getPayload());
        return rule;
    }    
}
