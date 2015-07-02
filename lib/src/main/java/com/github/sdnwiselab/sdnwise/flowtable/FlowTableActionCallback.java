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

import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_STATUS;

/**
 *
 * @author Seb
 */
public class FlowTableActionCallback extends FlowTableAction{
    
    public FlowTableActionCallback(){
        super();
        this.setType(SDN_WISE_FORWARD_UP)
            .setLocation(SDN_WISE_STATUS);
    }
    
    
    public FlowTableActionCallback setCallbackId(int id){ 
        this.setOffset(id);
        return this;
    }
    
    public FlowTableActionCallback setCallbackArgument(int argument){
        setValueHigh(argument>>8);
        setValueLow(argument);
        return this;
    }
    
    public int getCallbackId(){
        return this.getOffset();
    }
    
    public int getCallbackArgument(){
        return this.getValueHigh()<<8 + this.getValueLow();
    }
}
