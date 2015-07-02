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
package com.github.sdnwiselab.sdnwise.flowtable;

import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_PACKET;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_NXHOP_H;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;

/**
 *
 * @author Seb
 */
public class FlowTableActionForward extends FlowTableAction{
    
    public FlowTableActionForward(){
        super();
        this.setType(SDN_WISE_FORWARD_UNICAST);
        this.setLocation(SDN_WISE_PACKET);
        this.setOffset(SDN_WISE_NXHOP_H);
    }
    
    public FlowTableActionForward setNextHop(NodeAddress addr){
        this.setValueHigh(addr.getHigh());
        this.setValueLow(addr.getLow());
        return this;
    }
    
    public NodeAddress getNextHop(){
        return new NodeAddress(this.getValueHigh(),this.getValueLow());
    }
    
    public FlowTableActionForward setBroadcast(boolean isBroadcast){
        if (isBroadcast){
            this.setType(SDN_WISE_FORWARD_BROADCAST);
        } else {
            this.setType(SDN_WISE_FORWARD_UNICAST);
        }
        return this;     
    }
    
    public boolean isBroadcast(){
        return this.getType() == SDN_WISE_FORWARD_BROADCAST;
    }
}
