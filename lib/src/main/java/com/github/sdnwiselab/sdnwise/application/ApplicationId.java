/*
 * Copyright (C) 2015 Seb
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

import com.github.sdnwiselab.sdnwise.controller.ControllerId;

/**
 * Application Identification Class for FlowVisor.
 * This class describes methods for manage port and address used from the 
 * Sensors Application. This class extends ControllerId Class.
 * 
 * @author Sebastiano Milardo
 */
public class ApplicationId extends ControllerId {

    /**
     * Constructor for this Class.
     * 
     * @param address Application Address Used
     * @param port Application Port Used 
     */
    public ApplicationId(String address, int port) {
        super(address, port);
    }
}