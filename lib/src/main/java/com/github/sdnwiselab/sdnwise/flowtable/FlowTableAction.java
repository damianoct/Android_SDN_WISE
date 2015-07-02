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

import static com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow.SDN_WISE_STATUS;
import java.util.Arrays;

/**
 * FlowTableAction is part of the structure of the Entry of a FlowTable.
 * This Class implements FlowTableInterface.
 * 
 * @author Sebastiano Milardo
 */
public class FlowTableAction implements FlowTableInterface{
    public final static byte SIZE = 4;
    public final static byte COPY_SIZE = 4;
    
    // multimatch
    private final static byte SDN_WISE_MULTI = 2;
    private final static byte SDN_WISE_NOT_MULTI = 0;

    // actions
    public final static byte SDN_WISE_FORWARD_UNICAST = 4;
    public final static byte SDN_WISE_FORWARD_BROADCAST = 8;
    public final static byte SDN_WISE_DROP = 12;
    public final static byte SDN_WISE_MODIFY = 16;
    public final static byte SDN_WISE_AGGREGATE = 20;
    public final static byte SDN_WISE_FORWARD_UP = 24;
    
    private final static byte actionIndex = 0;
    private final static byte offsetIndex = 1;
    private final static byte highValueIndex = 2;
    private final static byte lowValueIndex = 3; 
    
    private final byte[] action = new byte[SIZE];

    /**
     * Constructor for the FlowTableAction object.
     * 
     * @param act the action that will be executed.
     * @param pos the position where the action will be executed.
     * @param valueHigh the high byte of the action.
     * @param valueLow the low byte of the action.
     */
    public FlowTableAction(int act, int pos, int valueHigh, int valueLow) {
        this.action[actionIndex]= (byte) act;
        this.action[offsetIndex]= (byte) pos;
        this.action[highValueIndex]= (byte) valueHigh;
        this.action[lowValueIndex]= (byte) valueLow;
    }

    /**
     * Constructor for the FlowTableAction object.
     * 
     * @param value byte array contains values to copy in actions[]
     */
    public FlowTableAction(byte[] value) {
        if (value.length == 4) {
            action[actionIndex]= value[0];
            action[offsetIndex]= value[1];
            action[highValueIndex]= value[2];
            action[lowValueIndex]= value[3];
        } else {
            action[actionIndex]= 0;
            action[offsetIndex]= 0;
            action[highValueIndex]= 0;
            action[lowValueIndex]= 0;
        }
    }

    /**
     * Simple constructor for the FlowTableEntry object.
     * 
     * Set action[] values at zero.
     */
    public FlowTableAction() {
        action[actionIndex]= 0;
        action[offsetIndex]= 0;
        action[highValueIndex]= 0;
        action[lowValueIndex]= 0;
    }

    /**
     * Getter method to obtain the multimatch value. An entry in the FlowTable
     * with multimatch set to true will not block the search in the table
     * for other matching rules and vice versa.
     * 
     * @return a boolean value.
     */
    public boolean isMultimatch() {
        return (((action[actionIndex]& SDN_WISE_MULTI) >> 1) == 1);
    }

    /**
     * Getter method to obtain the Type of Action. The possible types of actions
     * are SDN_WISE_FORWARD_UNICAST, SDN_WISE_FORWARD_BROADCAST, SDN_WISE_DROP,
     * SDN_WISE_MODIFY, SDN_WISE_AGGREGATE, SDN_WISE_FORWARD_UP.
     * 
     * @return value of the type action.
     */
    public int getType() {
        return (action[actionIndex]& 0xFC) & 0xFF;
    }

    /**
     * Getter method to obtain the Location where the Action will be executed.
     * 
     * @return a 1 for the SDN_WISE_PACKET or 0 for the SDN_WISE_STATUS.
     */
    int getLocation() {
        return (action[actionIndex]& 0x01) & 0xFF;
    }

    /**
     * Setter method to set the multimatch value. An entry in the FlowTable
     * with multimatch set to true will not block the search in the table
     * for other matching rules and vice versa.
     * 
     * @param value the multimatch value.
     * @return this FlowTableAction.
     */
    public FlowTableAction setMultimatch(boolean value) {
        action[actionIndex]= (byte) ((action[actionIndex]& ~(SDN_WISE_MULTI)) | 
                ((value ? SDN_WISE_MULTI : SDN_WISE_NOT_MULTI)));
        return this;
    }

    /**
     * Setter method to set the type of Action. The possible types of actions
     * are SDN_WISE_FORWARD_UNICAST, SDN_WISE_FORWARD_BROADCAST, SDN_WISE_DROP,
     * SDN_WISE_MODIFY, SDN_WISE_AGGREGATE, SDN_WISE_FORWARD_UP.
     * 
     * @param value will be set.
     * @return this FlowTableAction.
     */
    FlowTableAction setType(int value) {
        action[actionIndex]= (byte) ((action[actionIndex]& ~(0xFC)) | value);
        return this;
    }

    /**
     * Setter method to obtain the Location where the Action will be executed.
     * The possible values are SDN_WISE_PACKET or SDN_WISE_STATUS.
     * 
     * @param value will be set
     * @return this FlowTableAction
     */
    FlowTableAction setLocation(int value) {
        action[actionIndex]= (byte) ((action[actionIndex]& ~(0x01)) | value);
        return this;
    }

    /**
     * Getter method to get the whole Position byte.
     * 
     * @return value of the position byte in action[].
     */
    int getOffset() {
        return action[offsetIndex]& 0xFF;
    }

    /**
     * Setter method to set the whole Position byte.
     * 
     * @param pos set the position byte in action[]
     * @return this FlowTableAction
     */
    FlowTableAction setOffset(int pos) {
        this.action[offsetIndex]= (byte) pos;
        return this;
    }

    /**
     * Getter method to obtain the high byte of the Action. An action can use
     * as parameter two bytes. This is the high one.
     * 
     * @return value of the high byte.
     */
    int getValueHigh() {
        return action[highValueIndex]& 0xFF;
    }

    /**
     * Setter method to set the high byte of the Action. An action can use
     * as parameter two bytes. This is the high one.
     * 
     * @param valueHigh will be set.
     * @return this FlowTableAction.
     */
    FlowTableAction setValueHigh(int valueHigh) {
        this.action[highValueIndex]= (byte) valueHigh;
        return this;
    }

    /**
     * Getter method to obtain the low byte of the Action. An action can use
     * as parameter two bytes. This is the low one.
     * 
     * @return value of the low byte.
     */
    int getValueLow() {
        return action[lowValueIndex]& 0xFF;
    }

    /**
     * Setter method to set the low byte of the Action. An action can use
     * as parameter two bytes. This is the low one.
     * 
     * @param valueLow will be set
     * @return this FlowTableAction
     */
    FlowTableAction setValueLow(int valueLow) {
        this.action[lowValueIndex]= (byte) valueLow;
        return this;
    }

    /**
     * Getter method to obtain the whole action byte. This method is useful to
     * reduce the number of call when comparing two rules.
     * 
     * @return value of the action byte.
     */
    int getAct() {
        return action[actionIndex]& 0xFF;
    }

    /**
     * Setter method to set the whole action byte. This method is useful to
     * reduce the number of call when setting up a rules.
     * 
     * @param act action byte that will be set.
     */
    void setAct(int act) {
        this.action[actionIndex]= (byte) act;
    }

    @Override
    public String toString() {
        return (action[actionIndex]& 0xFF)
                + "," + (action[offsetIndex]& 0xFF) + "," + 
                (action[highValueIndex]& 0xFF) + "," + 
                (action[lowValueIndex]& 0xFF) + " ";
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(action,SIZE);
    }
    
    /**
     * Returns a String representation of the type of Action.
     * 
     * @return a string.
     */
    public String getTypeToString() {
        switch (getType()) {
            case (4):
                return "FORWARD_UNICAST";
            case (8):
                return "FORWARD_BROADCAST";
            case (12):
                return "DROP";
            case (16):
                return "MODIFY";
            case (20):
                return "AGGREGATE";
            case (24):
                return "FORWARD_TO_APP";
        }
        return "";
    }
    
     /**
     * Returns a String representation of the location where the Action will be 
     * evaluated.
     * 
     * @return string value of getLocation().
     */
    public String getLocationToString() {
        return getLocation() == 0 ? "STATUS_REG" : "PACKET";
    }
    
    /**
     * Getter method to obtain a string representation to the address where 
     * the Action will be evaluated. This value is an offset in the packet or in
     * the status register.
     * 
     * @return string value of the address.
     */
    public String getOffsetToString() {
        if (getLocation() == SDN_WISE_STATUS) {
            return action[offsetIndex] + "";
        } else {
            switch (action[offsetIndex]) {
                case (0):
                    return "LENGTH";
                case (1):
                    return "NET_ID";
                case (2):
                    return "SRC_HIGH";
                case (3):
                    return "SRC_LOW";
                case (4):
                    return "DST_HIGH";
                case (5):
                    return "DST_LOW";
                case (6):
                    return "TYPE";
                case (7):
                    return "TTL";
                case (8):
                    return "NEXT_HOP_HIGH";
                case (9):
                    return "NEXT_HOP_LOW";
                default:
                    return action[offsetIndex] + "";
            }
        }
    }
    
     /**
     * Getter method to obtain a string representation of where the Action will
     * be evaluated.
     * 
     * @return string value of the address.
     */
    public String getValueToString() {
        return Integer.toHexString(action[highValueIndex] * 256 + 
                action[lowValueIndex]);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlowTableAction other = (FlowTableAction) obj;
        return Arrays.equals(other.action, action); 
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Arrays.hashCode(this.action);
        return hash;
    }
}
