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
package com.github.sdnwiselab.sdnwise.adapter;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.logging.Level;

/**
 * This is the interface for RMI communication.
 */
interface AdapterRmiInterface extends Remote {

    /**
     * Opens this adapter.
     *
     * @param IN_PORT the port for the incoming RMI requests
     * @param IN_ADDRESS the address for the incoming RMI requests, in the form
     * //hostname:port/name
     * @return a boolean indicating the correct ending of the operation
     * @throws RemoteException
     */
    public abstract boolean open(int IN_PORT, String IN_ADDRESS)
            throws RemoteException;

    /**
     * Closes this adapter.
     *
     * @return a boolean indicating the correct ending of the operation
     * @throws RemoteException
     */
    public abstract boolean close() throws RemoteException;

    /**
     * Sends a byte array using this adapter.
     *
     * @param data the array to be sent
     * @param OUT_ADDRESS the address for the outgoing RMI requests, in the form
     * //hostname:port/name
     * @throws RemoteException
     */
    public abstract void send(byte[] data, String OUT_ADDRESS)
            throws RemoteException;

    /**
     * Receives a byte array.
     *
     * @param data
     * @throws RemoteException
     */
    public void receive(byte[] data) throws RemoteException;
}

/**
 * The adapter class for communicating using RMI protocol. Configuration data
 * are passed using a Map<String,String> which contains all the options needed
 * in the constructor of the class.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class AdapterRmi extends Adapter{

    private static final long serialVersionUID = 1L;
    private final String IN_ADDRESS;
    private final String OUT_ADDRESS;
    private final int IN_PORT;

    private final RmiServer server;

    /**
     * Creates an AdapterRmi object. The conf map is used to pass the
     * configuration settings for the serial port as strings. Specifically
     * needed parameters are:
     * <ol>
     * <li>IN_ADDRESS: //hostname:port/name</li>
     * <li>OUT_ADDRESS: //hostname:port/name</li>
     * <li>IN_PORT: port</li>
     * </ol>
     *
     * @param conf contains the serial port configuration data.
     * @throws RemoteException
     */
    public AdapterRmi(Map<String, String> conf) throws RemoteException {
        this.IN_ADDRESS = conf.get("IN_ADDRESS");
        this.OUT_ADDRESS = conf.get("OUT_ADDRESS");
        this.IN_PORT = Integer.parseInt(conf.get("IN_PORT"));
        this.server = new RmiServer();
    }

    /**
     * Opens this adapter.
     *
     * @return a boolean indicating the correct completition of the operation
     */
    @Override
    public boolean open() {
        try {
            return server.open(IN_PORT, IN_ADDRESS);
        } catch (RemoteException ex) {
            log(Level.SEVERE, ex.toString());
        }
        return false;
    }

    /**
     * Closes this adapter.
     *
     * @return a boolean indicating the correct completition of the operation
     */
    @Override
    public boolean close() {
        try {
            return server.close();
        } catch (RemoteException ex) {
            log(Level.SEVERE, ex.toString());
            return false;
        }
    }

    /**
     * Sends a byte array using this adapter.
     *
     * @param data the array to be sent
     */
    @Override
    public void send(byte[] data) {
        try {
            server.send(data, OUT_ADDRESS);
        } catch (RemoteException ex) {
            log(Level.SEVERE, ex.toString());
        }
    }

    /**
     * Receives a byte array using this adapter.
     *
     * @param data the array received
     * @throws RemoteException
     */
    public void receive(byte[] data) throws RemoteException {
        setChanged();
        notifyObservers(data);
    }

    /**
     * An inner clas for extending UnicastRemoteObject class.
     */
    private class RmiServer extends UnicastRemoteObject
            implements AdapterRmiInterface {

        private static final long serialVersionUID = 1L;

        public RmiServer() throws RemoteException {
            super();
        }

        /**
         * Opens this adapter.
         *
         * @param IN_PORT the port for the incoming RMI requests
         * @param IN_ADDRESS the address for the incoming RMI requests, in the
         * form //hostname:port/name
         * @return a boolean indicating the correct completition of the
         * operation
         * @throws RemoteException
         */
        @Override
        public boolean open(int IN_PORT, String IN_ADDRESS)
                throws RemoteException {
            try {
                LocateRegistry.createRegistry(IN_PORT);
                Naming.rebind(IN_ADDRESS, this);
            } catch (RemoteException | MalformedURLException ex) {
                log(Level.SEVERE, ex.toString());
                return false;
            }
            return true;
        }

        /**
         * Closes this adapter.
         *
         * @return a boolean indicating the correct completition of the
         * operation
         * @throws RemoteException
         */
        @Override
        public boolean close() throws RemoteException {
            try {
                Naming.unbind(IN_ADDRESS);
                return UnicastRemoteObject.unexportObject(this, true);
            } catch (NotBoundException | MalformedURLException ex) {
                log(Level.SEVERE, ex.toString());
                return false;
            }
        }

        /**
         * Sends a byte array using this adapter.
         *
         * @param data
         * @param OUT_ADDRESS the address for the outgoing RMI requests, in the
         * form //hostname:port/name
         * @throws RemoteException
         */
        @Override
        public void send(byte[] data, String OUT_ADDRESS)
                throws RemoteException {
            try {
                AdapterRmiInterface adp = (AdapterRmiInterface) Naming.lookup(OUT_ADDRESS);
                adp.receive(data);
            } catch (NotBoundException | MalformedURLException |
                    RemoteException ex) {
                log(Level.SEVERE, ex.toString());
            }
        }

        /**
         * Receives a byte array.
         *
         * @param data
         * @throws RemoteException
         */
        @Override
        public void receive(byte[] data) throws RemoteException {
            AdapterRmi.this.receive(data);
        }
    }

}
