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
package com.github.sdnwiselab.sdnwise.adaptation;

import com.github.sdnwiselab.sdnwise.adapter.Adapter;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

/**
 * Adaptation is the class that incorporates the communication adapters for
 * connecting the controller to the sensor networks and vice versa.
 * <p>
 * This class is implemented as an Observer, so it has an update method that is
 * called every time a new message is received by one of the two adapters. This
 * class also implements runnable and it works on a separate thread.
 * <p>
 * The behavior of this class is equal to a transparent proxy that send messages 
 * coming from the lower adapter to the upper adapter and from the upper 
 * adapter to the lower.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class Adaptation implements Observer, Runnable {

    private final Adapter lower;
    private final Adapter upper;
    private final Scanner scanner;
    private boolean isStopped;

    /**
     * Creates an adaptation object given two adapters.
     *
     * @param lower the adapter that receives messages from the sensor network
     * @param upper the adapter that receives messages from the controller
     */
    Adaptation(Adapter lower, Adapter upper) {
        this.lower = lower;
        this.upper = upper;
        scanner = new Scanner(System.in, "UTF-8");
        isStopped = false;
    }

    /**
     * This method is called for each message coming from the adapters. Messages
     * coming from the lower adapter are sent to the upper one and vice versa.
     *
     * @param o the adapter that has received the message
     * @param arg the message received as a byte array
     */
    @Override
    public final void update(Observable o, Object arg) {
        if (o.equals(lower)) {
            System.out.println("[ADP]: ↑ " + Arrays.toString((byte[]) arg));
            upper.send((byte[]) arg);
        } else if (o.equals(upper)) {
            System.out.println("[ADP]: ↓ " + Arrays.toString((byte[]) arg));
            lower.send((byte[]) arg);
        }
    }

    /**
     * Starts the adaptation thread and checks if both adapters have been opened
     * correctly. This method is listening for incoming closing messages from
     * the standard input.
     */
    @Override
    public final void run() {
        if (lower.open() && upper.open()) {
            lower.addObserver(this);
            upper.addObserver(this);
            while (!isStopped) {
                if (scanner.nextLine().equals("exit -l Adaptation")) {
                    isStopped = true;
                }
            }
            lower.close();
            upper.close();
        }
    }
}
