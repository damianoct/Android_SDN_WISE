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
package com.github.sdnwiselab.sdnwise.topology;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.sdnwiselab.sdnwise.graphStream.Edge;
import com.github.sdnwiselab.sdnwise.graphStream.Node;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds a org.graphstream.graph.Graph object which represent the
 * topology of the wireless sensor network. The method updateMap is invoked when
 * a message with topology updates is sent to the controller. This is a web
 * version of the NetworkGraph class.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class SocketIoNetworkGraph extends NetworkGraph {

    /**
     * This constructor returns the VisualNetworkGraph object. It requires a
     * time to live for each node in the network and a value representing the
     * RSSI resolution in order to consider a change of the RSSI value a change
     * in the network. This web version requires the address of the Socket.IO
     * server in the form "http://address:port"
     *
     * @param timeout the time to live for a node in seconds.
     * @param rssiResolution the RSSI resolution.
     * @param address address of the Socket.IO server.
     */
    private Socket socket;

    /**
     * Constructor method to create a representation of SocketIoNetworkGraph.
     * 
     * @param timeout the time to live for a node in seconds.
     * @param rssiResolution the RSSI resolution.
     * @param address address of the Socket.IO server.
     */
    public SocketIoNetworkGraph(int timeout, int rssiResolution, String address) {
        super(timeout, rssiResolution);
        socket = null;
        try {
            socket = IO.socket(address);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("subscribeAsController");
                }

            }).on("resume", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    for (Node n : graph.getNodeSet()) {
                        socket.emit("addNode",
                                n.getId(),
                                (((int) n.getAttribute("battery")) * 100 / 255) + "%",
                                String.valueOf(new Date(((long) n.getAttribute("lastSeen")))),
                                String.valueOf(n.getAttribute("netId")),
                                String.valueOf(n.getAttribute("nodeAddress"))
                        );
                    }

                    for (Edge e : graph.getEdgeSet()) {
                        socket.emit("addEdge", e.getId(),
                                e.getSourceNode().getId(),
                                e.getTargetNode().getId(),
                                ((byte) (255 - (int) e.getAttribute("length"))) - 72 + " dBm"
                        );
                    }

                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    socket.emit("unsubscribeAsController");
                }

            });
            socket.connect();

        } catch (URISyntaxException ex) {
            Logger.getLogger(SocketIoNetworkGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    void setupNode(Node node, int batt, long now, int netId, NodeAddress addr) {
        super.setupNode(node, batt, now, netId, addr);
        socket.emit("updateNode",
                node.getId(),
                (batt * 100 / 255) + "%",
                String.valueOf(new Date(now).toString()),
                String.valueOf(netId),
                String.valueOf(addr)
        );
    }

    @Override
    void updateNode(Node node, int batt, long now) {
        super.updateNode(node, batt, now);
        socket.emit("updateNode",
                node.getId(),
                (batt * 100 / 255) + "%",
                String.valueOf(new Date(now).toString()),
                String.valueOf(node.getAttribute("netId")),
                String.valueOf(node.getAttribute("nodeAddress"))
        );
    }

    @Override
    void setupEdge(Edge edge, int newLen) {
        super.setupEdge(edge, newLen);
        socket.emit("updateEdge",
                edge.getId(),
                ((byte) (255 - newLen)) - 72 + " dBm"
        );
    }

    @Override
    void updateEdge(Edge edge, int newLen) {
        super.updateEdge(edge, newLen);
        socket.emit("updateEdge",
                edge.getId(),
                ((byte) (255 - newLen)) - 72 + " dBm"
        );
    }

    @Override
    <T extends Node> T removeNode(Node node) {
        T tmp = super.removeNode(node);
        socket.emit("removeNode", node.getId());
        return tmp;
    }

    @Override
    <T extends Edge> T removeEdge(Edge edge) {
        T tmp = super.removeEdge(edge);
        socket.emit("removeEdge", edge.getId());
        return tmp;
    }

    @Override
    <T extends Edge> T addEdge(String id, String from, String to, boolean directed) {
        T tmp = super.addEdge(id, from, to, directed);
        socket.emit("addEdge", id, from, to, "");
        return tmp;
    }

    @Override
    <T extends Node> T addNode(String id) {
        T tmp = super.addNode(id);
        socket.emit("addNode",
                tmp.getId(),
                String.valueOf(tmp.getAttribute("battery")),
                String.valueOf(tmp.getAttribute("lastSeen")),
                String.valueOf(tmp.getAttribute("netId")),
                String.valueOf(tmp.getAttribute("nodeAddress"))
        );
        return tmp;
    }
}
