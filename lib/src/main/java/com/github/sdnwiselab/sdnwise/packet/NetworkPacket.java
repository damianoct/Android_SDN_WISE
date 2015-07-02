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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a generic SDN-WISE message.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class NetworkPacket implements Cloneable{

    /**
     * The maximum number of hops allowed in the network.
     */
     public final static byte SDN_WISE_DFLT_TTL_MAX = 20;

    /**
     * The maximum length of a NetworkPAcket.
     */
     public final static byte SDN_WISE_MAX_LEN = 116;
    
     // TODO these values won't be public in the future
     public final static byte SDN_WISE_LEN = 0,
            SDN_WISE_NET_ID = 1,
            SDN_WISE_SRC_H = 2,
            SDN_WISE_SRC_L = 3,
            SDN_WISE_DST_H = 4,
            SDN_WISE_DST_L = 5,
            SDN_WISE_TYPE = 6,
            SDN_WISE_TTL = 7,
            SDN_WISE_NXHOP_H = 8,
            SDN_WISE_NXHOP_L = 9;
            
    // TODO these values won't be public in the future
    
// packet types
    public final static byte SDN_WISE_DATA = 0,
            SDN_WISE_BEACON = 1,
            SDN_WISE_REPORT = 2,
            SDN_WISE_REQUEST = 3,
            SDN_WISE_RESPONSE = 4,
            SDN_WISE_OPEN_PATH = 5,
            SDN_WISE_CONFIG = 6,
            SDN_WISE_DPID_CONNECTION = 7,
            SDN_WISE_MULTICAST_GROUP_JOIN = 8,
            SDN_WISE_MULTICAST_GROUP_LEAVE = 9,
            SDN_WISE_GEO_DATA = 10,
            SDN_WISE_GEO_COORDINATES = 11,
            SDN_WISE_GEO_REPORT = 12;
            
            
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final byte SDN_WISE_DFLT_HDR_LEN = 10;
    
    
    private final byte[] data = new byte[SDN_WISE_MAX_LEN];;

    /**
     * Returns a NetworkPacket given a byte array.
     *
     * @param data the data contained in the NetworkPacket
     */
    public NetworkPacket(byte[] data) {
        setArray(data);
    }

     /**
     * Creates an empty NetworkPacket. The TTL and LEN values are set to 
     * default.
     */
    public NetworkPacket() {       
        this.setTtl(SDN_WISE_DFLT_TTL_MAX);
        this.setLen(SDN_WISE_DFLT_HDR_LEN);
    }

     /**
     * Returns a NetworkPacket given a int array. Integer values will be 
     * truncated to byte.
     *
     * @param data the data contained in the NetworkPacket
     */
    public NetworkPacket(int[] data){
        setArray(fromIntArrayToByteArray(data));
    }
    
    private byte[] fromIntArrayToByteArray(int[] array){
        byte[] dataToByte = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            dataToByte[i] = (byte) array[i];
        }
        return dataToByte;
    }
    
    private void setArray(byte[] array) {
        if (array.length <= SDN_WISE_MAX_LEN && array.length >= 
                SDN_WISE_DFLT_HDR_LEN){         
            this.setLen(array[SDN_WISE_LEN]);
            this.setNetId(array[SDN_WISE_NET_ID]);
            this.setSrc(array[SDN_WISE_SRC_H],array[SDN_WISE_SRC_L]);
            this.setDst(array[SDN_WISE_DST_H],array[SDN_WISE_DST_L]);
            this.setType(array[SDN_WISE_TYPE]);
            this.setTtl(array[SDN_WISE_TTL]);
            this.setNxhop(array[SDN_WISE_NXHOP_H],array[SDN_WISE_NXHOP_L]);
            this.setPayload(Arrays.copyOfRange(array, SDN_WISE_DFLT_HDR_LEN,
                    this.getLen()));
        } else {
            throw new IllegalArgumentException("Invalid array size: " + array.length);
        }
    }

    /**
     * Returns the length of the message.
     *
     * @return an integer representing the length of the message
     */
    public final int getLen() {
        return data[0] & 0xFF;
    }

    /**
     * Sets the length of the message.
     *
     * @param value an integer representing the length of the message.
     * @return the packet itself.
     */
    public final NetworkPacket setLen(byte value) {
        if (value <= SDN_WISE_MAX_LEN && value > 0){
            data[0] = value;
        } else {
            throw new IllegalArgumentException("Invalid length");
        }
        return this;
    }

    /**
     * Returns the NetworkId of the message.
     *
     * @return an integer representing the NetworkId of the message
     */
    public final int getNetId() {
        return data[1] & 0xFF;
    }

    /**
     * Sets the NetworkId of the message.
     *
     * @param value the networkId of the packet.
     * @return the packet itself.
     */
    public final NetworkPacket setNetId(byte value) {
        data[1] = value;
        return this;
    }

    /**
     * Returns the address of the source node.
     *
     * @return the NodeAddress of the source node
     */
    public final NodeAddress getSrc() {
        return new NodeAddress(data[2], data[3]);
    }

    /**
     * Sets the address of the source node.
     *
     * @param valueH the high byte of the address.
     * @param valueL the low byte of the address.
     * @return the packet itself.
     */
    public final NetworkPacket setSrc(byte valueH, byte valueL) {
        data[2] = valueH;
        data[3] = valueL;
        return this;
    }

    /**
     * Sets the address of the source node.
     *
     * @param address the NodeAddress of the source node.
     * @return the packet itself.
     */
    public NetworkPacket setSrc(NodeAddress address) {
        setSrc(address.getHigh(), address.getLow());
        return this;
    }

    /**
     * Sets the address of the source node.
     *
     * @param address a String representing the address of the source node.
     * @return the packet itself.
     */
    public final NetworkPacket setSrc(String address) {
        setSrc(new NodeAddress(address));
        return this;
    }

    /**
     * Returns the address of the destination node.
     *
     * @return the NodeAddress of the destination node
     */
    public final NodeAddress getDst() {
        return new NodeAddress(data[4], data[5]);
    }

    /**
     * Set the address of the destination node.
     *
     * @param valueH high value of the address of the destination. 
     * @param valueL low value of the address of the destination.
     * @return the packet itself.
     */
    public final NetworkPacket setDst(byte valueH, byte valueL) {
        data[4] = valueH;
        data[5] = valueL;
        return this;
    }

    /**
     * Set the address of the destination node.
     *
     * @param address the NodeAddress value of the destination. 
     * @return the packet itself.
     */
    public final NetworkPacket setDst(NodeAddress address) {
        setDst(address.getHigh(), address.getLow());
        return this;
    }

    /**
     * Set the address of the destination node.
     *
     * @param address a String representing the destination address. 
     * @return the packet itself.
     */
    public final NetworkPacket setDst(String address) {
        setDst(new NodeAddress(address));
        return this;
    }

    /**
     * Returns the type of the message.
     *
     * @return an integer representing the type of the message
     */
    public final int getType() {
        return data[6] & 0xFF;
    }

    /**
     * Sets the type of the message.
     *
     * @param type an integer representing the type of the message
     * @return
     */
    public final NetworkPacket setType(byte type) {
        data[6] = type;
        return this;
    }

    /**
     * Returns the Time To Live of the message. When the TTL of a packet
     * reaches 0 the receiving node will drop the packet.
     *
     * @return an integer representing the Time To Live of the message
     */
    public final int getTtl() {
        return data[7] & 0xFF;
    }

    /**
     * Sets the Time To Live of the message. When the TTL of a packet
     * reaches 0 the receiving node will drop the packet.
     *
     * @param value an integer representing the Time To Live of the message.
     * @return the packet itself.
     */
    public final NetworkPacket setTtl(byte value) {
        data[7] = value;
        return this;
    }

    /**
     * Decrements the Time To Live of the message by 1. When the TTL of a packet
     * reaches 0 the receiving node will drop the packet.
     *
     * @return the packet itself.
     */
    public final NetworkPacket decrementTtl() {
        if (data[7]>0){
            data[7]--;
        }
        return this;
    }
    
    /**
     * Returns the NodeAddress of the next hop towards the destination.
     *
     * @return the NodeAddress of the the next hop towards the destination node
     */
    public final NodeAddress getNxhop() {
        return new NodeAddress(data[8], data[9]);
    }

    /**
     * Sets the NodeAddress of the next hop towards the destination.
     *
     * @param valueH high value of the address of the next hop. 
     * @param valueL low value of the address of the next hop.
     * @return packet itself.
     */
    public final NetworkPacket setNxhop(byte valueH, byte valueL) {
        data[8] = valueH;
        data[9] = valueL;
        return this;
    }

    /**
     * Sets the NodeAddress of the next hop towards the destination.
     *
     * @param address the NodeAddress address of the next hop. 
     * @return packet itself.
     */
    public final NetworkPacket setNxhop(NodeAddress address) {
        setNxhop(address.getHigh(), address.getLow());
        return this;
    }

    /**
     * Sets the NodeAddress of the next hop towards the destination.
     *
     * @param address a string representing the address of the next hop. 
     * @return packet itself.
     */
    public final NetworkPacket setNxhop(String address) {
        setNxhop(new NodeAddress(address));
        return this;
    }

    /**
     * Returns the payload of the packet as a byte array.
     *
     * @return the payload of the packet
     */
    public byte[] getPayload() {
        return Arrays.copyOfRange(data, SDN_WISE_DFLT_HDR_LEN, 
                this.getLen());
    }

     /**
     * Sets the payload of the packet from a byte array.
     *
     * @param payload the payload of the packet.
     * @return the payload of the packet.
     */
    public NetworkPacket setPayload(byte[] payload) {
        if (payload.length + SDN_WISE_DFLT_HDR_LEN <= SDN_WISE_MAX_LEN){
                System.arraycopy(payload, 0, data, SDN_WISE_DFLT_HDR_LEN, payload.length);
                this.setLen((byte) (payload.length + SDN_WISE_DFLT_HDR_LEN));
        } else {
            throw new IllegalArgumentException("Payload exceeds packet size");
        }
        return this;
    }

    /**
     * Sets the payload size of the packet.
     *
     * @param size the payload size.
     * @return the packet itself.
     */
    public NetworkPacket setPayloadSize(int size) {
        if (SDN_WISE_DFLT_HDR_LEN + size <= SDN_WISE_MAX_LEN){
            this.setLen((byte)(SDN_WISE_DFLT_HDR_LEN + size));
        } else {
            throw new IllegalArgumentException("Index cannot be greater than "
                    + "the maximum payload size: "+ size);
        }
        return this;
    }
 
    /**
     * Gets the payload size of the packet.
     *
     * @return the packet payload size.
     */ 
    public int getPayloadSize() {
        return (this.getLen() - SDN_WISE_DFLT_HDR_LEN);
    }
    
    /**
     * Sets a single payload byte.
     *
     * @param index the index of the payload. The first byte of the payload is 0.
     * @param newData the new data to be set.
     * @return the packet itself.
     */
    public NetworkPacket setPayloadAt(byte newData, int index) {
        if (SDN_WISE_DFLT_HDR_LEN + index < SDN_WISE_MAX_LEN){
            data[SDN_WISE_DFLT_HDR_LEN + index] = newData;
            if ((index + SDN_WISE_DFLT_HDR_LEN) >= this.getLen()){
                this.setLen((byte)(SDN_WISE_DFLT_HDR_LEN + index + 1));
            }
        } else {
            throw new IllegalArgumentException("Index cannot be greater than "
                    + "the maximum payload size");
        }
        return this;
    }
    
    /**
     * Sets a part of the payload of the NetworkPacket. Differently from
     * copyPayload this method updates also the length of the packet
     *
     * @param src the new data to be set.
     * @param srcPos starting from this byte of src.
     * @param payloadPos copying to this byte of payload.
     * @param length this many bytes.
     * @return the packet itself.
     */
    
    public NetworkPacket setPayload(byte[] src, byte srcPos, 
            byte payloadPos, byte length) {
        this.copyPayload(src, srcPos, payloadPos, length);
        this.setPayloadSize(length + payloadPos);
        return this;
    }
    
    /**
     * Copy a part of the payload of the NetworkPacket.  Differently from
     * copyPayload this method does not update the length of the packet
     *
     * @param src the new data to be set.
     * @param srcPos starting from this byte of src.
     * @param payloadPos copying to this byte of payload.
     * @param length this many bytes.
     * @return the packet itself.
     */
    
    public NetworkPacket copyPayload(byte[] src, byte srcPos, 
            byte payloadPos, byte length) {
        for (int i = 0; i<length; i++){
            setPayloadAt(src[i+srcPos],i+payloadPos);
        }
        return this;
    }
    
    /**
     * Gets a byte from the payload of the packet at position index.
     * 
     * @param index the offset of the byte.
     * @return the byte of the payload.
     */

    public byte getPayloadAt(int index) {
        if (index + SDN_WISE_DFLT_HDR_LEN < this.getLen()){
                return data[SDN_WISE_DFLT_HDR_LEN + index];
        } else {
            throw new IllegalArgumentException("Index cannot be greater than "
                    + "the maximum payload size");
        }
    }
    
    /**
     * Gets a part of the payload of the packet from position start, to position 
     * end.
     * 
     * @param start start the copy from this byte.
     * @param end to this byte.
     * @return a byte[] part of the payload.
     */
    
    public byte[] copyPayloadOfRange(int start, int end){
            return Arrays.copyOfRange(data, SDN_WISE_DFLT_HDR_LEN + start, 
                    SDN_WISE_DFLT_HDR_LEN + end);
    }
    
    
    private String bytesToHex(byte[] bytes, int start, int stop) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = start; j < stop; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars).trim();
    }
    
    /**
     * Returns a String representation of the NetworkPacket.
     *
     * @return a String representation of the NetworkPacket
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[");      
        str.append(bytesToHex(data,0,this.getLen()));
        str.append("]");
        return str.toString();
    }

    /**
     * Returns a byte array representation of the NetworkPacket.
     *
     * @return a byte array representation of the NetworkPacket
     */
    public byte[] toByteArray() {
        return Arrays.copyOf(data, this.getLen());
    }

    /**
     * Returns an int array representation of the NetworkPacket.
     *
     * @return a int array representation of the NetworkPacket
     */
    public int[] toIntArray() {
        int[] tmp = new int[this.getLen()];
        for (int i = 0; i<tmp.length;i++){
            tmp[i] = data[i] & 0xFF;
        }
        return tmp;
    }
    
    @Override
    public NetworkPacket clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(NetworkPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new NetworkPacket(data.clone());
    }
    
    /**
     * Set the request flag of the packet. This flag is set when a node cannot
     * manage this kind of packet. A node receiving a packet with the flag
     * raised will send it to the control plane.
     * 
     * @return the packet itself.
     */
    public NetworkPacket setRequestFlag(){
        if (getType() < 128){
            setType((byte) (getType()+128));
        }
        return this;
    }

    /**
     * Unset the request flag of the packet. This flag is set when a node cannot
     * manage this kind of packet. A node receiving a packet with the flag
     * raised will send it to the control plane.
     * 
     * @return the packet itself.
     */
    public NetworkPacket unsetRequestFlag(){
        if (getType() > 127){
            setType((byte) (getType()-128));
        }
        return this;
    }

    /**
     * Returns if the request flag of the packet is raised or not. 
     * This flag is set when a node cannot
     * manage this kind of packet. A node receiving a packet with the flag
     * raised will send it to the control plane.
     * 
     * @return the packet itself.
     */
    public boolean isRequest(){
        return getType() > 127; 
    }
}
