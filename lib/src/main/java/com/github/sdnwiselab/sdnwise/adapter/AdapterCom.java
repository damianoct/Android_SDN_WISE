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

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.TooManyListenersException;
import java.util.logging.Level;

/**
 * The adapter class for serial port communication. Configuration data are
 * passed using a Map<String,String> which contains all the options needed in
 * the constructor of the class.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class AdapterCom extends Adapter {

    final byte START_BYTE;
    final byte STOP_BYTE;
    private final String PORT_NAME;
    private final int BAUD_RATE;
    private final int DATA_BITS;
    private final int STOP_BITS;
    private final int PARITY;
    private final int MAX_PAYLOAD;

    private InputStream in;
    private BufferedOutputStream out;
    private SerialPort serialPort;

    /**
     * Creates an AdapterCom object. The conf map is used to pass the
     * configuration settings for the serial port as strings. Specifically
     * needed parameters are:
     * <ol>
     * <li>PARITY</li>
     * <li>STOP_BITS</li>
     * <li>DATA_BITS</li>
     * <li>BAUD_RATE</li>
     * <li>PORT_NAME</li>
     * <li>STOP_BYTE</li>
     * <li>START_BYTE</li>
     * <li>MAX_PAYLOAD</li>
     * </ol>
     *
     * @param conf contains the serial port configuration data.
     */
    public AdapterCom(Map<String, String> conf) {
        this.PARITY = Integer.parseInt(conf.get("PARITY"));
        this.STOP_BITS = Integer.parseInt(conf.get("STOP_BITS"));
        this.DATA_BITS = Integer.parseInt(conf.get("DATA_BITS"));
        this.BAUD_RATE = Integer.parseInt(conf.get("BAUD_RATE"));
        this.PORT_NAME = conf.get("PORT_NAME");
        this.STOP_BYTE = Byte.parseByte(conf.get("STOP_BYTE"));
        this.START_BYTE = Byte.parseByte(conf.get("START_BYTE"));
        this.MAX_PAYLOAD = Integer.parseInt(conf.get("MAX_PAYLOAD"));
    }

    /**
     * Opens this adapter.
     *
     * @return a boolean indicating the correct ending of the operation
     */
    @Override
    public final boolean open() {
        CommPortIdentifier portId;
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                String portName = portId.getName();
                this.log(Level.INFO, "Serial Port Found: " + portName);
                if (portName.equals(PORT_NAME)) {
                    try {
                        log(Level.INFO, "SINK");
                        serialPort = (SerialPort) portId.open("AdapterCOM", 2000);
                        break;
                    } catch (PortInUseException ex) {
                        log(Level.SEVERE, ex.toString());
                    }
                }
            }
        }

        if (serialPort != null) {
            try {
                in = serialPort.getInputStream();
                out = new BufferedOutputStream(serialPort.getOutputStream());
                SerialListener sl = new SerialListener(in);
                sl.addObserver(this);
                serialPort.setSerialPortParams(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
                serialPort.addEventListener(sl);
                serialPort.notifyOnDataAvailable(true);
                return true;

            } catch (IOException | UnsupportedCommOperationException | TooManyListenersException ex) {
                log(Level.SEVERE, "Terminating " + ex.toString());
                return false;
            }
        } else {
            log(Level.SEVERE, "Unable to open serial port");
            return false;
        }
    }

    /**
     * Sends a byte array using this adapter.
     *
     * @param data the array to be sent
     */
    @Override
    public final void send(byte[] data) {
        try {
            int len = data[0] & 0xFF;
            if (len <= MAX_PAYLOAD) { // MAX 802.15.4 DATA FRAME PAYLOAD = 116
                this.out.write(START_BYTE);
                this.out.write(data);
                this.out.write(STOP_BYTE);
                this.out.flush();
            }
        } catch (IOException ex) {
            log(Level.SEVERE, ex.toString());
        }
    }

    /**
     * Closes this adapter.
     *
     * @return a boolean indicating the correct ending of the operation
     */
    @Override
    public final boolean close() {
        try {
            serialPort.close();
            in.close();
            return true;
        } catch (IOException ex) {
            log(Level.SEVERE, ex.toString());
            return false;
        }
    }

    private class SerialListener extends Observable implements SerialPortEventListener {

        boolean startFlag = false;
        int expected = 0;
        int b;
        byte a;
        final LinkedList<Byte> receivedBytes;
        final LinkedList<Byte> packet;
        InputStream in;

        SerialListener(InputStream in) {
            this.packet = new LinkedList<>();
            this.receivedBytes = new LinkedList<>();
            this.in = in;
        }

        /*
         * Manage an event that occurs during serial port communications. 
         * If the data are available, receive operations will occur.
         * 
         * @param event the event to manage 
         */
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    for (int i = 0; i < in.available(); i++) {
                        b = in.read();
                        if (b > -1) {
                            receivedBytes.add((byte) b);
                        }
                    }

                    while (!receivedBytes.isEmpty()) {
                        a = receivedBytes.poll();
                        if (!startFlag && a == START_BYTE) {
                            startFlag = true;
                            packet.add(a);
                        } else if (startFlag && expected == 0) {
                            expected = a & 0xFF;
                            packet.add(a);
                        } else if (startFlag && expected > 0 && packet.size() < expected + 1) {
                            packet.add(a);
                        } else if (startFlag && expected > 0 && packet.size() == expected + 1) {
                            packet.add(a);
                            if (a == STOP_BYTE) {
                                packet.removeFirst();
                                packet.removeLast();
                                byte[] bytePacket = new byte[packet.size()];
                                for (int i = 0; i < bytePacket.length; i++) {
                                    bytePacket[i] = packet.poll();
                                }
                                setChanged();
                                notifyObservers(bytePacket);
                            } else {
                                while (!packet.isEmpty()) {
                                    receivedBytes.addFirst(packet.removeLast());
                                }
                                receivedBytes.poll();
                            }
                            startFlag = false;
                            expected = 0;
                        }
                    }
                } catch (IOException e) {
                    log(Level.SEVERE, e.toString());
                }
            }
        }
    }
}
