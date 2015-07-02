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
 * FlowTableWindow is part of the structure of the Entry of a FlowTable.
 * This Class implements FlowTableInterface.
 * 
 * @author Sebastiano Milardo
 */
public class FlowTableWindow implements FlowTableInterface {

    public final static byte SIZE = 4;
    public final static byte COPY_SIZE = 4;

    // memory
    public final static byte SDN_WISE_PACKET = 1;
    public final static byte SDN_WISE_STATUS = 0;

    // size
    public final static byte SDN_WISE_SIZE_0 = 0;
    public final static byte SDN_WISE_SIZE_1 = 2;
    public final static byte SDN_WISE_SIZE_2 = 4;

    // operators
    public final static byte SDN_WISE_EQUAL = 8;
    public final static byte SDN_WISE_NOT_EQUAL = 16;
    public final static byte SDN_WISE_BIGGER = 24;
    public final static byte SDN_WISE_LESS = 32;
    public final static byte SDN_WISE_EQUAL_OR_BIGGER = 40;
    public final static byte SDN_WISE_EQUAL_OR_LESS = 48;

    private final static byte operationIndex = 0;
    private final static byte offsetIndex = 1;
    private final static byte highValueIndex = 2;
    private final static byte lowValueIndex = 3;
    private final byte[] window = new byte[SIZE];

    /**
     * Simple constructor for the FlowTableWindow object.
     * 
     * Set window[] values at zero.
     */
    public FlowTableWindow() {
        window[operationIndex] = 0;
        window[offsetIndex] = 0;
        window[highValueIndex] = 0;
        window[lowValueIndex] = 0;
    }

    /**
     * Constructor for the FlowTableWindow object.
     * 
     * @param value byte array contains value to copy in actions[]
     */
    public FlowTableWindow(byte[] value) {
        if (value.length == 4) {
            window[operationIndex] = value[0];
            window[offsetIndex] = value[1];
            window[highValueIndex] = value[2];
            window[lowValueIndex] = value[3];
        } else {
            window[operationIndex] = 0;
            window[offsetIndex] = 0;
            window[highValueIndex] = 0;
            window[lowValueIndex] = 0;
        }
    }
  
    /**
     * Constructor for the FlowTableWindow object.
     * 
     * @param op value to insert in window[]
     * @param pos value to insert in window[]
     * @param value_h High value to insert in window[]
     * @param value_l Low value to insert in window[]
     */
    public FlowTableWindow(int op, int pos, int value_h, int value_l) {
        this.window[operationIndex] = (byte) op;
        this.window[offsetIndex] = (byte) pos;
        this.window[highValueIndex] = (byte) value_h;
        this.window[lowValueIndex] = (byte) value_l;
    }

    /**
     * Getter method to obtain Size.
     * 
     * @return an int value of size.
     */
    public int getSize() {
        return (window[operationIndex] & 0x06) & 0xFF;
    }

    /**
     * Getter method to obtain Operator.
     * 
     * @return  an int value of operator.
     */
    public int getOperator() {
        return (window[operationIndex] & 0xF8) & 0xFF;
    }

    /**
     * Getter method to obtain Location.
     * 
     * @return an int value of location.
     */
    public int getLocation() {
        return (window[operationIndex] & 0x01) & 0xFF;
    }

    /**
     * Setter method to set operationIndex of window[].
     * 
     * @param value value to set
     * @return this FlowTableWindow
     */
    public FlowTableWindow setSize(int value) {
        window[operationIndex] = (byte) ((window[operationIndex] & ~(0x06)) | value);
        return this;
    }

    /**
     * Setter method to set operationIndex of window[].
     * 
     * @param value value to set
     * @return this FlowTableWindow
     */
    public FlowTableWindow setOperator(int value) {
        window[operationIndex] = (byte) ((window[operationIndex] & ~(0xF8)) | value);
        return this;
    }

    /**
     * Setter method to set operationIndex of window[].
     * 
     * @param value value to set
     * @return this FlowTableWindow
     */
    public FlowTableWindow setLocation(int value) {
        window[operationIndex] = (byte) ((window[operationIndex] & ~(0x01)) | value);
        return this;
    }

    /**
     * Getter method to obtain Pos.
     * 
     * @return an int value of pos.
     */
    public int getPos() {
        return window[offsetIndex] & 0xFF;
    }

    /**
     * Setter method to set offsetIndex of window[].
     * 
     * @param pos value to set
     * @return this FlowTableWindow
     */
    public FlowTableWindow setPos(int pos) {
        this.window[offsetIndex] = (byte) pos;
        return this;
    }

    /**
     * Getter method to obtain High Value.
     * 
     * @return an int value of high value.
     */
    public int getValueHigh() {
        return window[highValueIndex] & 0xFF;
    }

    /**
     * Setter method to set highValueIndex of window[].
     * 
     * @param valueHigh value to set
     * @return this FlowTableWindow
     */
    public FlowTableWindow setValueHigh(int valueHigh) {
        this.window[highValueIndex] = (byte) valueHigh;
        return this;
    }

    /**
     * Getter method to obtain Low Value.
     * 
     * @return an int value of low value.
     */
    public int getValueLow() {
        return window[lowValueIndex] & 0xFF;
    }

    /**
     * Setter method to set lowValueIndex of window[].
     * 
     * @param valueLow value to set
     * @return this FlowTableWindow
     */
    public FlowTableWindow setValueLow(int valueLow) {
        this.window[lowValueIndex] = (byte) valueLow;
        return this;
    }

    /**
     * Getter method to obtain Operation.
     * 
     * @return an int value of operation.
     */
    public int getOp() {
        return window[operationIndex] & 0xFF;
    }

    /**
     * Setter method to set operation.
     * 
     * @param op operation to set
     */
    public void setOp(int op) {
        this.window[operationIndex] = (byte) op;
    }

    @Override
    public String toString() {
        return (window[operationIndex] & 0xFF) + ","
                + (window[offsetIndex] & 0xFF) + ","
                + (window[highValueIndex] & 0xFF) + ","
                + (window[lowValueIndex] & 0xFF) + " ";
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(window, SIZE);
    }

    /**
     * Getter method to obtain Operator in String.
     * 
     * @return a string of operator.
     */
    public String getOperatorToString() {
        if (getSize() != SDN_WISE_SIZE_0) {
            switch (getOperator()) {
                case (8):
                    return "=";
                case (16):
                    return "!=";
                case (24):
                    return ">";
                case (32):
                    return "<";
                case (40):
                    return ">=";
                case (48):
                    return "<=";
            }
        }
        return "";
    }

    /**
     * Getter method to obtain Size in string.
     * 
     * @return a string in size.
     */
    public String getSizeToString() {
        if (getSize() != SDN_WISE_SIZE_0) {
            return (getSize() / 2) + "";
        }
        return "";
    }

    /**
     * Getter method to obtain memory in string.
     * 
     * @return a string value of memory.
     */
    public String getMemoryToString() {
        if (getSize() != SDN_WISE_SIZE_0) {
            return getLocation() == SDN_WISE_STATUS ? "STATUS_REG" : "PACKET";
        }
        return "";
    }

    /**
     * Getter method to obtain address in string.
     * 
     * @return a string value of address.
     */
    public String getAddressToString() {
        if (getSize() != SDN_WISE_SIZE_0) {
            if (getLocation() == SDN_WISE_STATUS) {
                return getPos() + "";
            } else {
                switch (getPos()) {
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
                        return getPos() + "";
                }
            }
        }
        return "";
    }

    /**
     * Getter method to obtain value in string.
     * 
     * @return a string value.
     */
    public String getValueToString() {
        if (getSize() != 0) {
            return Integer.toHexString(window[highValueIndex] * 256
                    + window[lowValueIndex]);
        } else {
            return "";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlowTableWindow other = (FlowTableWindow) obj;
        return Arrays.equals(other.window, window); 
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Arrays.hashCode(this.window);
        return hash;
    }
    
    
}
