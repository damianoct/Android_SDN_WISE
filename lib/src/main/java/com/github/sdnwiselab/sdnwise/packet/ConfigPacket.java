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
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * This class models a Configuration packet.
 * 
 * @author Sebastiano Milardo
 */
public class ConfigPacket extends NetworkPacket{
    // TODO this class will be divided into an ConfigPacket and many
    // ConfigAddressPacket, ConfigNetIdPacket, etc...
    public final static byte 
        SDN_WISE_CNF_READ = 0,
        SDN_WISE_CNF_WRITE = 1,
            
        SDN_WISE_CNF_ID_ADDR = 0,
        SDN_WISE_CNF_ID_NET_ID = 1,
        SDN_WISE_CNF_ID_CNT_BEACON_MAX = 2,
        SDN_WISE_CNF_ID_CNT_REPORT_MAX = 3,
        SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX = 4,
        SDN_WISE_CNF_ID_CNT_SLEEP_MAX = 5,
        SDN_WISE_CNF_ID_TTL_MAX = 6,
        SDN_WISE_CNF_ID_RSSI_MIN = 7,

        SDN_WISE_CNF_ADD_ACCEPTED = 8,
        SDN_WISE_CNF_REMOVE_ACCEPTED = 9,
        SDN_WISE_CNF_LIST_ACCEPTED = 10,

        SDN_WISE_CNF_ADD_RULE = 11,
        SDN_WISE_CNF_REMOVE_RULE = 12,
        SDN_WISE_CNF_REMOVE_RULE_INDEX = 13,
        SDN_WISE_CNF_GET_RULE_INDEX = 14,
        SDN_WISE_CNF_RESET = 15,
        SDN_WISE_CNF_ADD_FUNCTION = 16,
        SDN_WISE_CNF_REMOVE_FUNCTION = 17;
    private boolean isWrite = false;
    
    public ConfigPacket(byte[] data) {
        super(data);
    }

    public ConfigPacket() {
        super();
        this.setType(SDN_WISE_CONFIG);
    }

    public ConfigPacket(int[] data) {
        super(data);
    }
    
    private ConfigPacket setRead() {
        if (this.getPayloadSize()<1){
            this.setPayloadSize((byte)1);
        }
        isWrite = false;     
        setPayloadAt((byte)(this.getPayloadAt(0) | ( SDN_WISE_CNF_READ  << 7)), 0);
        setPayloadAt((byte)0, 1);
        setPayloadAt((byte)0, 2);
        return this;
    }

    private ConfigPacket setWrite() {
        if (this.getPayloadSize()<1){
            this.setPayloadSize((byte)1);
        }
        isWrite = true;
        setPayloadAt((byte)((this.getPayloadAt(0)) | ( SDN_WISE_CNF_WRITE  << 7)), 0);
        return this;
    }
    
    public boolean isWrite(){
        return this.getPayloadAt(0) >> 7 == 1;
    }
    
    public final byte getConfigId(){
        return (byte)(super.getPayloadAt((byte)0) & 0x7F);
    }
    
    private ConfigPacket setConfigId(int id){
        if (isWrite){
            setPayloadAt((byte)(id | (1<<7)), 0);
        } else {
            setPayloadAt((byte)(id), 0);    
        }
        return this;
    }
    
    private ConfigPacket setValue(byte high, byte low){
        super.setPayloadAt(high, 1);
        super.setPayloadAt(low, 2);
        return this;
    }

    private ConfigPacket setValue(int value){
        super.setPayloadAt((byte)(value >> 8), (byte)1);
        super.setPayloadAt((byte)(value & 0xFF), (byte)2);
        return this;
    }
    
    public final ConfigPacket setNodeAddressValue(NodeAddress newAddr){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_ADDR)
            .setValue(newAddr.getHigh(),newAddr.getLow());
        return this;
    }
    
    public final ConfigPacket setNetworkIdValue(byte id){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_NET_ID)
            .setValue((byte)0,id);
        return this;
    }
    
    public final ConfigPacket setBeaconPeriodValue(int period){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_CNT_BEACON_MAX)
            .setValue(period);
        return this;
    }
    
    public final ConfigPacket setReportPeriodValue(int period){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_CNT_REPORT_MAX)
            .setValue(period);
        return this;        
    }
    
    public final ConfigPacket setUpdateTablePeriodValue(int period){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX)
            .setValue(period);
        return this;
    }
    
    public final ConfigPacket setSleepIntervalValue(int period){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_CNT_SLEEP_MAX)
            .setValue(period);
        return this;
    }
    
    public final ConfigPacket setDefaultTtlMaxValue(byte ttl){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_TTL_MAX)
            .setValue((byte)0,ttl);
        return this;
    }
    
    public final ConfigPacket setDefaultRssiMinValue(byte rssi){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ID_RSSI_MIN)
            .setValue((byte)0,rssi);
        return this;
    }
    
    public final ConfigPacket setAddAcceptedAddressValue(NodeAddress addr){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ADD_ACCEPTED)
            .setValue(addr.getHigh(),addr.getLow());
        return this;
    }
    
    public final ConfigPacket setRemoveAcceptedAddressValue(NodeAddress addr){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_REMOVE_ACCEPTED)
            .setValue(addr.getHigh(),addr.getLow());
        return this;
    }
    
    public final ConfigPacket setReadAcceptedAddressesValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_LIST_ACCEPTED);
        return this;
    }
    
    public final ConfigPacket setAddRuleValue(FlowTableEntry flow){
        byte[] flowArray = flow.toByteArray();
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ADD_RULE)    
            .setPayload(flowArray,(byte)0,(byte)1,(byte)flowArray.length);
        return this;
    }

    public final ConfigPacket setAddRuleAtPositionValue(FlowTableEntry flow, 
            int index){
        byte[] flowArray = flow.toByteArray();
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ADD_RULE)   
            .setValue(index)
            .setPayload(flowArray,(byte)0,(byte)3,(byte)flowArray.length);
        return this;
    }
    
    public final ConfigPacket setAddFunctionAtPositionValue(int index, 
            byte[] payload){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_ADD_FUNCTION)   
            .setValue(index)
            .setPayload(payload,(byte)0,(byte)3,(byte)payload.length);
        return this;
    }
    
    public final ConfigPacket setRemoveFunctionAtPositionValue(int index){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_REMOVE_FUNCTION)   
            .setValue(index);
        return this;
    }
    
    public final ConfigPacket setRemoveRuleValue(FlowTableEntry flow){
        byte[] flowArray = flow.toByteArray();
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_REMOVE_RULE)    
            .setPayload(flowArray,(byte)0,(byte)1,(byte)flowArray.length);
        return this;
    }
    
    public final ConfigPacket setRemoveRuleAtPositionValue(int index){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_REMOVE_RULE_INDEX)    
            .setValue(index);
        return this;
    }
    
    public final ConfigPacket setReadRuleAtPositionValue(int index){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_GET_RULE_INDEX)
            .setValue(index);
        return this;
    }

    public final ConfigPacket setResetValue(){
        this.setWrite()
            .setConfigId(SDN_WISE_CNF_RESET);
        return this;
    }
    
    //--------------------------------------------------------------------------
    
    public final ConfigPacket setReadNodeAddressValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_ADDR);
        return this;
    }
    
    public final ConfigPacket setReadNetworkIdValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_NET_ID);
        return this;
    }
    
    public final ConfigPacket setReadBeaconPeriodValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_CNT_BEACON_MAX);
        return this;
    }
    
    public final ConfigPacket setReadReportPeriodValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_CNT_REPORT_MAX);
        return this;
    }
    
    public final ConfigPacket setReadUpdateTablePeriodValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX);
        return this;
    }
    
    public final ConfigPacket setReadSleepIntervalValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_CNT_SLEEP_MAX);
        return this;
    }
    
    public final ConfigPacket setReadDefaultTtlMaxValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_TTL_MAX);
        return this;
    }
    
    public final ConfigPacket setReadDefaultRssiMinValue(){
        this.setRead()
            .setConfigId(SDN_WISE_CNF_ID_RSSI_MIN);
        return this;
    }
    
    //--------------------------------------------------------------------------

    public final NodeAddress getNodeAddress(){
        if (getConfigId() == SDN_WISE_CNF_ID_ADDR){
            return new NodeAddress(this.getPayloadAt((byte)1),
                    this.getPayloadAt(2));
        } else {
            return null;
        }
    }
    
    public final int getNetworkIdValue(){
        if (getConfigId() == SDN_WISE_CNF_ID_NET_ID){
            return getPayloadAt(2);
        } else {
            return -1;
        }
    }
    
    public final int getBeaconPeriodValue(){
        if (getConfigId() == SDN_WISE_CNF_ID_CNT_BEACON_MAX){
            return ((getPayloadAt(1)<<8) + getPayloadAt(2));
        } else {
            return -1;
        }
    }
    
    public final int getReportPeriodValue(){
        if (getConfigId() == SDN_WISE_CNF_ID_CNT_REPORT_MAX){
            return ((getPayloadAt(1)<<8) + getPayloadAt(2));
        } else {
            return -1;
        }
    }
    
    public final int getUpdateTablePeriodValue(){
        if (getConfigId() == SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX){
            return getPayloadAt(1)<<8 + getPayloadAt(2);
        } else {
            return -1;
        }
    }
    
    public final int getSleepIntervalValue(){
        if (getConfigId() == SDN_WISE_CNF_ID_CNT_SLEEP_MAX){
            return getPayloadAt(1)<<8 + getPayloadAt(2);
        } else {
            return -1;
        }
    }
    
    public final int getDefaultTtlMaxValue(){
        if (getConfigId() == SDN_WISE_CNF_ID_TTL_MAX){
            return getPayloadAt(2) & 0xFF;
        } else {
            return -1;
        }
    }
    
    public final int getDefaultRssiMinValue(){
        if (getConfigId() == SDN_WISE_CNF_ID_RSSI_MIN){
            return getPayloadAt(2) & 0xFF;
        } else {
            return -1;
        }
    }  
    
    public List<NodeAddress> getAcceptedAddressesValues(){
        LinkedList<NodeAddress> list = new LinkedList<>();
        if (getConfigId() == SDN_WISE_CNF_LIST_ACCEPTED){
            for (int i = 1; i < getPayloadSize(); i += 2) {
                if (getPayloadAt(i) != -1 && getPayloadAt(i + 1) != -1) {
                    list.add(new NodeAddress(
                            getPayloadAt(i) & 0xFF,
                            getPayloadAt(i + 1) & 0xFF)
                    );
                }
            }
        }
        return list;
    }
    
    public FlowTableEntry getRule(){
        FlowTableEntry rule = null;
        if (getConfigId() == SDN_WISE_CNF_GET_RULE_INDEX){            
            rule = new FlowTableEntry(this.copyPayloadOfRange(3, 
                    3 + FlowTableEntry.SDN_WISE_RULE_COPY_LEN));
        }
        return rule;
    }
}
