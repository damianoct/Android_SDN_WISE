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
import com.github.sdnwiselab.sdnwise.adapter.AdapterCom;
import com.github.sdnwiselab.sdnwise.adapter.AdapterOmnet;
import com.github.sdnwiselab.sdnwise.adapter.AdapterTcp;
import com.github.sdnwiselab.sdnwise.adapter.AdapterUdp;
import com.github.sdnwiselab.sdnwise.configuration.ConfigAdaptation;
import java.util.Map;

/**
 * AdaptationFactory creates an Adaptation object given the specifications
 * contained in a ConfigAdaptation object. This class implements the factory
 * object pattern.
 * <p>
 * This class is used in the initialization phase of the network in order to
 * create an adaptation object. The different types of adapter are chosen using
 * the option TYPE of the configuration file provided to the ConfigAdaptation
 * class.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class AdaptationFactory {

    private static ConfigAdaptation conf;

    /**
     * Returns an adapter depending on the options specified. The supported
     * types at the moment are "UDP/TCP" for udp/tcp communication and 
     * "COM" for serial port communication. "OMNET" adapter is still under 
     * development.
     * Details regarding the adapters are contained in the config map.
     *
     * @param config the type of adapter that will be instantiated.
     * @return an adapter object
     */
    private static Adapter getAdapter(Map<String, String> config)
             {
        switch (config.get("TYPE")) {
            case "UDP":
                return new AdapterUdp(config);
            case "COM":
                return new AdapterCom(config);
            case "OMNET":
                return new AdapterOmnet(config);
            case "TCP":
                return new AdapterTcp(config);
            default:
                throw new UnsupportedOperationException
            ("Error in configuration file: Unsupported Adapter of type " +
                    config.get("TYPE"));
        }
    }

    /**
     * Returns an adaptation object given a configAdaptation object. If one of
     * the adapter cannot be instantiated then this method throws an
     * UnsupportedOperationException.
     *
     * @param config contains the configurations for the adaptation object
     * @return an adaptation object
     */
    public final static Adaptation getAdaptation(ConfigAdaptation config)
    {
        conf = config;
        Adapter lower = getAdapter(conf.getLower());
        Adapter upper = getAdapter(conf.getUpper());
        return new Adaptation(lower, upper);
    }

    private AdaptationFactory() {
    }
}
