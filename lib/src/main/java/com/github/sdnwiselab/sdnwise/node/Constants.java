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
package com.github.sdnwiselab.sdnwise.node;

/**
 * Contains all the constants for the correct simulation of a SDN-WISE node.
 * 
 * @author Sebastiano Milardo
 */
public class Constants {
   
    // the default netId of a node
    public final static int SDN_WISE_DFLT_NET_ID = 1;

    
    // packet header length
    public final static int SDN_WISE_PACKET_LEN = 128;
    public final static int SDN_WISE_DFLT_HDR_LEN = 10;
    public final static int SDN_WISE_BEACON_HDR_LEN = SDN_WISE_DFLT_HDR_LEN + 2;
    public final static byte SDN_WISE_REPORT_HDR_LEN = SDN_WISE_DFLT_HDR_LEN + 3;
    public final static int SDN_WISE_RESPONSE_HDR_LEN = SDN_WISE_DFLT_HDR_LEN;
    public final static int SDN_WISE_OPEN_PATH_HDR_LEN = SDN_WISE_DFLT_HDR_LEN;
    public final static int SDN_WISE_CONFIG_HDR_LEN = SDN_WISE_DFLT_HDR_LEN;

    // routing
    public final static int SDN_WISE_DFLT_RSSI_MIN = 180;

    // tables
    public final static byte SDN_WISE_RLS_MAX = 16;
    public final static byte SDN_WISE_NEIGHBORS_MAX = 15;
    public final static byte SDN_WISE_ACCEPTED_ID_MAX = 10;
    public final static byte SDN_WISE_RL_TTL_DECR = 10;

    // timers
    public final static byte SDN_WISE_DFLT_CNT_DATA_MAX = 10;
    public final static byte SDN_WISE_DFLT_CNT_BEACON_MAX = 10;
    public final static byte SDN_WISE_DFLT_CNT_REPORT_MAX = 2 * SDN_WISE_DFLT_CNT_BEACON_MAX;
    public final static byte SDN_WISE_DFLT_CNT_UPDTABLE_MAX = 6;   // TTL = 150s
    public final static byte SDN_WISE_DFLT_CNT_SLEEP_MAX = 100;

    // status register
    public final static int SDN_WISE_STATUS_LEN = 128/*SDN_WISE_PACKET_LEN*/;

    // COM ports
    public final static int SDN_WISE_COM_START_BYTE = 0x7A;
    public final static int SDN_WISE_COM_STOP_BYTE = 0x7E;

    // send
    public final static boolean SDN_WISE_MAC_SEND_UNICAST = false;
    public final static boolean SDN_WISE_MAC_SEND_BROADCAST = true;

    private Constants() {
    }

}
