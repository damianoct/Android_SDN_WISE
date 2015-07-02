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
package com.github.sdnwiselab.sdnwise.controller;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableAction;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableStats;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow;
import com.github.sdnwiselab.sdnwise.topology.SocketIoNetworkGraph;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages event with SocketIO and communication with Node.js Server.
 * 
 * @author Sebastiano Milardo
 */
public class ControllerSocketIo {

    private Controller controller;
    private Socket socket;

    /**
     * Constructor Method for this Class.
     * 
     * @param ctr The Controller manages event with SocketIO.
     * @param address String Address to create a connection.
     */
    public ControllerSocketIo(Controller ctr, String address) {
        this.controller = ctr;

        socket = null;
        try {
            socket = IO.socket(address);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("subscribeAsControllerGui");
                }

            }).on("requestTable", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    String[] idArray = ((String) args[0]).split("\\.");
                    byte netId = (byte)Integer.parseInt(idArray[0]);
                    NodeAddress addr = new NodeAddress(idArray[1] + "." + idArray[2]);
                    List<FlowTableEntry> fTable = controller.getRules(netId, addr);

                    String flowTableHtml = "<table class=\"tg\">\n"
                            + "			  <tr>\n"
                            + "				<th class=\"tg-s6z1b\" colspan=\"5\">Matching Rule</th>\n"
                            + "				<th class=\"tg-s6z1b\" colspan=\"5\">Matching Rule</th>\n"
                            + "				<th class=\"tg-s6z1b\" colspan=\"5\">Matching Rule</th>\n"
                            + "				<th class=\"tg-s6z2b\" colspan=\"5\">Action</th>\n"
                            + "				<th class=\"tg-s6z3b\" colspan=\"2\">Statistics</th>\n"
                            + "			  </tr>"
                            + " <tr>\n"
                            + "				<td class=\"tg-s6z1\">Location</td>\n"
                            + "				<td class=\"tg-s6z1\">Offset</td>\n"
                            + "				<td class=\"tg-s6z1\">Size</td>\n"
                            + "				<td class=\"tg-s6z1\">Operator</td>\n"
                            + "				<td class=\"tg-s6z1\">Value</td>\n"
                            + "				<td class=\"tg-s6z1\">Location</td>\n"
                            + "				<td class=\"tg-s6z1\">Offset</td>\n"
                            + "				<td class=\"tg-s6z1\">Size</td>\n"
                            + "				<td class=\"tg-s6z1\">Operator</td>\n"
                            + "				<td class=\"tg-s6z1\">Value</td>\n"
                            + "				<td class=\"tg-s6z1\">Location</td>\n"
                            + "				<td class=\"tg-s6z1\">Offset</td>\n"
                            + "				<td class=\"tg-s6z1\">Size</td>\n"
                            + "				<td class=\"tg-s6z1\">Operator</td>\n"
                            + "				<td class=\"tg-s6z1\">Value</td>\n"
                            + "				<td class=\"tg-s6z2\">Type</td>\n"
                            + "				<td class=\"tg-s6z2\">Location</td>\n"
                            + "				<td class=\"tg-s6z2\">Offset</td>\n"
                            + "				<td class=\"tg-s6z2\">Value</td>\n"
                            + "				<td class=\"tg-s6z2\">MultiMatch</td>\n"
                            + "				<td class=\"tg-s6z3\">TTL</td>\n"
                            + "				<td class=\"tg-s6z3\">Count</td>\n"
                            + "			  </tr>";

                    StringBuilder sb = new StringBuilder(flowTableHtml);
                    int j = 0;
                    for (FlowTableEntry ft : fTable) {
                        if (ft != null && ft.getWindows()[0].getOperator() != 0) {
                            j++;
                            sb.append(convertFlowTableEntryToHtml(ft, (j % 2 == 0) ? "even" : "odd"));
                        }
                    }
                    sb.append("</table>");

                    socket.emit("receiveTable", args[0], args[1], sb.toString());
                }

            }).on("requestSettings", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    String[] idArray = ((String) args[0]).split("\\.");
                    byte netId = (byte)Integer.parseInt(idArray[0]);
                    NodeAddress addr = new NodeAddress(idArray[1] + "." + idArray[2]);

                    String flowTableHtml = "<table class=\"ts\">"
                            + "<tr>"
                            + "	<td class=\"ts-odd2\">Beacon Period</td>"
                            + "<td class=\"ts-odd1\">" + controller.getNodeBeaconPeriod(netId, addr) + "</td>"
                            + "<td class=\"ts-odd1\">s</td>"
                            + "</tr><tr>"
                            + "	<td class=\"ts-even2\">Report Period</td>"
                            + "<td class=\"ts-even1\">" + controller.getNodeReportPeriod(netId, addr) + "</td>"
                            + "<td class=\"ts-even1\">s</td>"
                            + "</tr><tr>"
                            + "	<td class=\"ts-odd2\">TTL Max</td>"
                            + "<td class=\"ts-odd1\">" + controller.getNodeTtlMax(netId, addr) + "</td>"
                            + "<td class=\"ts-odd1\">n. of hops</td>"
                            + "</tr><tr>"
                            + "	<td class=\"ts-even2\">RSSI Min</td>"
                            + "<td class=\"ts-even1\">" + controller.getNodeRssiMin(netId, addr) + "</td>"
                            + "<td class=\"ts-even1\">dBm</td>"
                            + "</tr>";

                    StringBuilder sb = new StringBuilder(flowTableHtml);

                    sb.append("</table>");

                    socket.emit("receiveSettings", args[0], args[1], sb.toString());
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    socket.emit("unsubscribeAsControllerGui");
                }
            });
            socket.connect();

        } catch (URISyntaxException ex) {
            Logger.getLogger(SocketIoNetworkGraph.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is used to Convert FlowTableEntry in HTML code.
     * 
     * @param ft FlowTableEntry to represent.
     * @param parity string value to identify odd or even value of the 
     * FlowTableEntry.
     * @return string value of FlowTableEntry in HTML.
     */
    public String convertFlowTableEntryToHtml(FlowTableEntry ft, String parity) {
        StringBuilder row = new StringBuilder("<tr>");
        for (FlowTableWindow w : ft.getWindows()) {
            row.append("<td class=\"tg-").append(parity).append("1\">").append(w.getMemoryToString()).append("</td>");
            row.append("<td class=\"tg-").append(parity).append("1\">").append(w.getAddressToString()).append("</td>");
            row.append("<td class=\"tg-").append(parity).append("1\">").append(w.getSizeToString()).append("</td>");
            row.append("<td class=\"tg-").append(parity).append("1\">").append(w.getOperatorToString()).append("</td>");
            row.append("<td class=\"tg-").append(parity).append("1\">").append(w.getValueToString()).append("</td>");
        }

        FlowTableAction fa = ft.getAction();
        row.append("<td class=\"tg-").append(parity).append("2\">").append(fa.getTypeToString()).append("</td>");
        row.append("<td class=\"tg-").append(parity).append("2\">").append(fa.getLocationToString()).append("</td>");
        row.append("<td class=\"tg-").append(parity).append("2\">").append(fa.getOffsetToString()).append("</td>");
        row.append("<td class=\"tg-").append(parity).append("2\">").append(fa.getValueToString()).append("</td>");
        row.append("<td class=\"tg-").append(parity).append("2\">").append(fa.isMultimatch()).append("</td>");

        FlowTableStats fs = ft.getStats();
        row.append("<td class=\"tg-").append(parity).append("3\">").append(fs.getTtl()).append("</td>");
        row.append("<td class=\"tg-").append(parity).append("3\">").append(fs.getCounter()).append("</td>");
        row.append("</tr>");
        return row.toString();
    }
}
