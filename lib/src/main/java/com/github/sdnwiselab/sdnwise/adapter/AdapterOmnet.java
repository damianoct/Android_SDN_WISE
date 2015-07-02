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

import java.util.Map;

/**
 * The adapter class for the communication with the OMNet++ simulator .
 * Configuration data are passed using a Map<String,String> which contains all
 * the options needed in the constructor of the class.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class AdapterOmnet extends Adapter {

    /**
     * Creates an AdapterOmnet object. The conf map is used to pass the
     * configuration settings.
     *
     * @param conf contains the configuration data.
     */
    public AdapterOmnet(Map<String, String> conf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Opens this adapter.
     *
     * @return a boolean indicating the correct ending of the operation
     */
    @Override
    public boolean open() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Closes this adapter.
     *
     * @return a boolean indicating the correct ending of the operation
     */
    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sends a byte array using this adapter.
     *
     * @param data the array to be sent
     */
    @Override
    public void send(byte[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

//package sdwn.adaptation;
//
//import java.io.BufferedReader;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.net.*;
//
//import java.io.*;
//
//public class AdaptationSimulator {
//
//    public static void main(String argv[]) throws Exception {
//        DatagramSocket receiver = new DatagramSocket(9998);
//
//        Socket socket = new Socket();
//        Connect c = new Connect(receiver);
//        System.out.println("Creating/Formatting Files to Rules Response");
//
//        PrintWriter pw1 = null;
//        pw1 = new PrintWriter(new FileWriter(new File("C:/SDWN_Simulator/rule1.txt")));
//
//        PrintWriter pw2 = null;
//        pw2 = new PrintWriter(new FileWriter(new File("C:/SDWN_Simulator/rule2.txt")));
//
//        pw1.close();
//        pw2.close();
//
//        SimulatorWriter sw = new SimulatorWriter();
//        sw.run();
//
//    }
//}
//
//class Connect extends Thread {
//
//    private DatagramSocket client = null;
//    BufferedReader in = null;
//    PrintStream out = null;
//
//    byte[] buffer = new byte[255];
//    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//
//    public Connect() {
//    }
//
//    public Connect(DatagramSocket clientSocket) {
//        client = clientSocket;
//        try {
////in = new BufferedReader(new InputStreamReader(client.getInputStream()));
////out = new PrintStream(client.getOutputStream(), true);
//        } catch (Exception e1) {
//            try {
//                client.close();
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//            return;
//        }
//        this.start();
//    }
//
//    public void run() {
//
//        try {
//
//            while (true) {
//
//                System.out.println("In attesa di messaggi...");
//                client.receive(packet);
//                System.out.println("Dopo receive packet");
//
//                byte buffer[] = packet.getData();
//
//                String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
//
//                System.out.println("Messaggio ricevuto dall'host " + packet.getAddress() + " su porta " + packet.getPort() + ": " + message);
//
//                for (int i = 0; i < 70; i++) {
//                    System.out.println("Byte[" + i + "=" + buffer[i]);
//                }
//
//                //Aggiunto da me
//                //Comunico via socket il pacchetto ricevuto al server per l'elaborazione
//                System.out.println("---Pacchetto in spedizione su socket---");
//                DatagramSocket sender = new DatagramSocket();
//                InetAddress IP = InetAddress.getByName("localhost");
//     	   //DatagramPacket packet = new DatagramPacket(bufferFilter, bufferFilter.length, IP, 1200);
//                //DatagramPacket packet = new DatagramPacket(bufferFilter, bufferFilter.length, IP, 9999);
//
//                //ADD new change on Dec 18
//                //Send the rule request directly to the Controll Layer
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, IP, 9999);
//
//                byte[] packet_byte = packet.getData();
//
//                if (packet_byte[6] == 3) {
//                    //2 ottobre nuovo controller 
//                    try {
//
//                        //TCP
//                        Socket clientSocket = new Socket("127.0.0.1", 1111);
//                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
//                        //byte packet_byte[]= UnsignedByte.toByteArray(array);
//                        outToServer.write(packet_byte);
//
//                        //UDP transmission
//                  /*
//                         DatagramSocket socket = new DatagramSocket();
//                         //UnsignedByte[] buf = response;
//                         DatagramPacket packetToController = new DatagramPacket(packet_byte, packet_byte.length, InetAddress.getByName("127.0.0.1"), new Integer ("1111"));
//                         socket.send(packetToController);
//                         * */
//                        System.out.println("---Packet 3 is sent ---");
//                        clientSocket.close();
//                        //socket.close();
//
//                    } catch (Exception ex) {
//
//                    }
//                } else {
//                    System.out.println("---Packet 2 is sent ---");
//                    sender.send(packet);
//                    sender.close();
//                }
//
//                //Fine aggiunto da me
//            }//while true
//
//        } catch (Exception e) {
//        }
//    }//end run
//
//}
//
////////////////////////////////////////7
//class SimulatorWriter implements Runnable {
//
//    OutputStream out;
//
//    static int count = 0;
//
//    public SimulatorWriter() {
//
//    }
//
//    /**
//     * ********* New run *******************
//     */
//    public void run() {
//        try {
//
//            //byte[] buffer = new byte[255];
//            //DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//            DatagramSocket client = new DatagramSocket(1100);
//
//            //DatagramSocket client = new DatagramSocket(9999);
//            while (true) {
//
//                //the following three lines has moved from line 154-156 on October 30
//                byte[] buffer = new byte[255];
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                //DatagramSocket client = new DatagramSocket(1100);
//
//                System.out.println("Wait for a message from the Controller...");
//                client.receive(packet);
//
//                byte[] receiveBuffer = packet.getData();
//
//                System.out.println("I received a message from the host " + packet.getAddress() + " su porta " + packet.getPort());
//
//                //Comunico via socket il pacchetto ricevuto al server per l'elaborazione
//                System.out.println("---Write the file with the packet received by the Controller---");
//
//                for (int i = 0; i < 70; i++) {
//                    System.out.println("Byte[" + i + "=" + receiveBuffer[i]);
//                }
//
//                PrintWriter pw1 = null;
//                PrintWriter pw2 = null;
//
//                String pathnameFile1 = "C:/SDWN_Simulator/rule1.txt";
//                String pathnameFile2 = "C:/SDWN_Simulator/rule2.txt";
//
//                boolean append1;
//                boolean append2;
//
//                if (count < 10) {
//                    try {
//      // created as a separate variable to emphasize that I'm appending to this file
//
//                        // = true;
//                        if (count == 0) {
//                            append1 = false;
//
//                            pw2 = new PrintWriter(new FileWriter(new File("C:/SDWN_Simulator/rule2.txt")));
//
//                        } else {
//                            append1 = true;
//                        }
//
//                        pw1 = new PrintWriter(new FileWriter(new File("C:/SDWN_Simulator/rule1.txt"), append1));
//                        // a print writer gives you many more methods to write with
//                        pw1.println("Count=" + count);
//
//                        for (int i = 0; i < 70; i++) {
//                            pw1.println(receiveBuffer[i]);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        // deal with the exception
//                    } finally {
//                        pw1.close();
//                    }
//
//                } /**
//                 * ****** fine count < 10
//                 * ***********************************************
//                 */
//                else {
//                    try {
//                        // created as a separate variable to emphasize that I'm appending to this file
//
//                        if (count == 10) {
//                            append2 = false;
//                        } else {
//                            append2 = true;
//                        }
//
//                        pw2 = new PrintWriter(new FileWriter(new File("C:/SDWN_Simulator/rule2.txt"), append2));
//                        // a print writer gives you many more methods to write with
//                        pw2.println("Count=" + count);
//
//                        for (int i = 0; i < 70; i++) {
//                            pw2.println(receiveBuffer[i]);
//                        }
//
//                        if (count == 20) {
//                            //count=0; Removed on Dec 19, due to the fact that then the count is incremented, so it will be=0
//                            count = -1;
//                            pw1 = new PrintWriter(new FileWriter(new File("C:/SDWN_Simulator/rule1.txt")));
//                            /*
//                             File f = new File(pathnameFile1);
//                             boolean success = f.delete();
//
//                             if (!success)
//                             throw new IllegalArgumentException("Delete: deletion failed");
//                             */
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        // deal with the exception
//                    } finally {
//                        pw2.close();
//                    }
//
//                }
//
//                count++;
//                /*DatagramSocket sender = new DatagramSocket();
//                 InetAddress IP = InetAddress.getByName("localhost");
//  	  
//                 DatagramPacket packetToSimulator = new DatagramPacket(receiveBuffer, receiveBuffer.length, IP, 1200);
//                 sender.send(packetToSimulator);
//                 System.out.println("---Messaggio Spedito---");
//         
//                 */
//                //Fine aggiunto da me
//
//            }
//
//        } catch (Exception e) {
//        }
//    }
//
//}
