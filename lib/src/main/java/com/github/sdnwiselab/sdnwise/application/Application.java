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
package com.github.sdnwiselab.sdnwise.application;

import com.github.sdnwiselab.sdnwise.adapter.Adapter;
import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.topology.NetworkGraph;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.nio.charset.Charset;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds a representation of the sensor network and resolves all the
 * requests coming from the network itself. This abstract class has two main
 * private methods. managePacket and graphUpdate(abstract). The first is called 
 * when a request is coming from the network while the latter is called when 
 * something in the topology of the network changes.
 * <p>
 * There are send and receive(abstract) methods to and from the Adaptation Layer 
 *
 * @author Sebastiano Milardo
 */
public abstract class Application implements Observer, Runnable {

    private Adapter lower;
    final Controller controller;
    private final ArrayBlockingQueue<NetworkPacket> bQ;
    final Scanner scanner;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    
    private boolean isStopped;
    
    /**
     * Constructor method for Application Abstract Class.
     * 
     * @param controller the controller to be set
     * @param lower the adapter to be set
     */
    public Application(Controller controller, Adapter lower) {
        this.lower = lower;
        this.controller = controller;
        bQ = new ArrayBlockingQueue<>(1000);
        scanner = new Scanner(System.in, "UTF-8");
        isStopped = false;
    }

    public abstract void receivePacket(DataPacket data);

    public abstract void graphUpdate();

    private void managePacket(NetworkPacket data) {
        if (data.getType() == DataPacket.SDN_WISE_DATA){
            receivePacket(new DataPacket(data.toByteArray()));
        }
    }

    /**
     * This methods manages updates coming from the lower adapter or the network
     * representation. When a message is received from the lower adapter it is
     * inserted in a ArrayBlockingQueue and then the method managePacket it is
     * called on it. While for updates coming from the network representation
     * the method graphUpdate is invoked.
     *
     * @param o
     * @param arg
     */
    @Override
    public final void update(Observable o, Object arg){
        if (o.equals(getLower())) {
            try {
                bQ.put(new NetworkPacket((byte[]) arg));
            } catch (InterruptedException ex) {
                Logger.getLogger(Application.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        } 
    }

    /**
     * Starts the working thread that manages incoming requests and it listens
     * to messages coming from the standard input.
     */
    @Override
    public final void run() {
        if (getLower().open()) {
            Thread th = new Thread(new Worker(bQ));
            th.start();
            getLower().addObserver(this);
            while (!isStopped) {
                if (scanner.nextLine().equals("exit -l Controller")) {
                    isStopped = true;
                }
            }
        }
    }
    
    /**
     * Stops the working thread that manages incoming requests. 
     */
    public final void stop(){
        isStopped = true;
    }

    /**
     * This method sends a generic message to a node. The message is represented
     * by an array of bytes.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param message the content of the message to be sent
     */
    public final void sendMessage(byte netId, NodeAddress destination, byte[] message) {
        
        DataPacket dp = new DataPacket();
        dp.setNetId(netId)
                .setDst(destination)
                .setSrc("0.0")
                .setNxhop("0.0")
                .setPayload(message);
        lower.send(dp.toByteArray());
    }
    
    /**
     * This method sends a generic message to a node. 
     * The message is represented by string.
     * 
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param message the content of the message to be sent
     */
    public final void sendMessage(byte netId, NodeAddress destination, String message) {
        if (message != null && !message.isEmpty()) {
            this.sendMessage(netId, destination, message.getBytes(UTF8_CHARSET));
        }
    }  
    
    /**
     * Setter method to set a Lower Adapter 
     * 
     * @param lower the adapter to be set
     */
    public void setLower(Adapter lower){
        this.lower = lower;
    }

     /**
     * Getter method to return lower Adapter 
     * 
     * @return the lower Adapter
     */
    public Adapter getLower() {
        return this.lower;
    }

    /**
     * Getter method to obtain the Network Graph of this Controller. 
     * 
     * @return the controller network graph.
     */
    public NetworkGraph getNetworkGraph() {
        return controller.getNetworkGraph();
    }

    private class Worker implements Runnable {
        
        private final ArrayBlockingQueue<NetworkPacket> bQ;
        boolean isStopped = false;

        Worker(ArrayBlockingQueue<NetworkPacket> bQ) {
            this.bQ = bQ;
        }

        @Override
        public void run() {
            while (!isStopped) {
                try {
                    managePacket(bQ.take());
                } catch (InterruptedException ex) {
                    isStopped = true;
                }
            }
        }
    }
}
