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
import com.github.sdnwiselab.sdnwise.util.NodeAddress;

/**
 *
 * @author Seb
 */
public class FlowTableActionDrop extends FlowTableAction{
    
    public FlowTableActionDrop(){
        this.setType(SDN_WISE_DROP);
        this.setLocation(SDN_WISE_PACKET);
    }
    
    public FlowTableActionDrop setDropRate(int percent){
        this.setValueHigh(percent);
        return this;
    }
    
    public int getDropRate(){
        return this.getValueHigh();
    }
    
    public FlowTableActionDrop setAlternativeNextHop(NodeAddress addr){
        this.setValueLow(addr.getLow()); // TODO dovrei mettere l'intero indirizzo
        return this;
    }
}
