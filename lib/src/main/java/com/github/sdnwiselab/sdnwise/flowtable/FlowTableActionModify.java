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

/**
 *
 * @author Seb
 */
public class FlowTableActionModify extends FlowTableAction{
    
    public FlowTableActionModify(){
        this.setType(SDN_WISE_MODIFY);
    }

    @Override
    public FlowTableAction setOffset(int pos) {
        return super.setOffset(pos); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getOffset() {
        return super.getOffset(); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    @Override
    public FlowTableAction setLocation(int value) {
        return super.setLocation(value); 
    }

    @Override
    public int getLocation() {
        return super.getLocation();
    }

    @Override
    public FlowTableAction setValueLow(int valueLow) {
        return super.setValueLow(valueLow); 
    }

    @Override
    public int getValueLow() {
        return super.getValueLow(); 
    }

    @Override
    public FlowTableAction setValueHigh(int valueHigh) {
        return super.setValueHigh(valueHigh);
    }

    @Override
    public int getValueHigh() {
        return super.getValueHigh(); 
    }
    
    
    
}
