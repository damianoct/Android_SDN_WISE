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
package com.github.sdnwiselab.sdnwise.function;

import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.packet.GeoDataPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.util.Neighbor;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author Sebastiano Milardo
 */
public class GeoRouting implements FunctionInterface {

    @Override
    public void function(
            HashMap<String, Object> adcRegister, 
            ArrayList<FlowTableEntry> flowTable, 
            ArrayList<Neighbor> neighborTable, 
            int[] statusRegister, 
            ArrayList<NodeAddress> acceptedId, 
            ArrayBlockingQueue<int[]> flowTableQueue,
            ArrayBlockingQueue<int[]> txQueue,
            int value,
            NetworkPacket np
    ) {
        
        GeoDataPacket gdp = new GeoDataPacket(np.toByteArray());
        NodeAddress dest = gdp.getCurrentMulticastNodeAddress();
        int nNeigh = statusRegister[6];
        
        HashMap<NodeAddress, int[]> map = new HashMap<>();
        for (int i = 0; i<nNeigh; i++){
            NodeAddress tmp = new NodeAddress(statusRegister[6+(i*8)],
                                    statusRegister[7+(i*8)]);
            int[] tmpCoord = Arrays.copyOfRange
                                (statusRegister, 8 + (i*8), 14 + (i*8));
            
            map.put(tmp, tmpCoord);
                    
        }
        
        
        
        // TODO add stuff in here
        
    }   
    
    
    private class Distance implements Comparable<Distance> {
        double distance;
        NodeAddress address;

        public Distance(double distance, NodeAddress address) {
            this.distance = distance;
            this.address = address;
        }

        @Override
        public int compareTo(Distance o) {
            return distance < o.distance ? -1 : distance > o.distance ? 1 : 0;
        }
    }

    private NodeAddress getClosestToDest(HashMap<NodeAddress, int[]> neigh, int[] dest){
        List<Distance> distances = new ArrayList<>();

        for (Entry<NodeAddress, int[]> n : neigh.entrySet()){
            distances.add(
                    new Distance(
                            getDistance(n.getValue(),dest),n.getKey()
                    ));
        }
        
        Collections.sort(distances);
        return distances.get(0).address;
    }
    
    private double getDistance(int[] a , int[] b){
        double result = Double.POSITIVE_INFINITY;
        if (a.length == 3 && b.length == 3){
            double deltaX = a[0] - b[0];
            double deltaY = a[1] - b[1];
            double deltaZ = a[2] - b[2];
            result = Math.sqrt(
                    deltaX*deltaX + 
                    deltaY*deltaY + 
                    deltaZ*deltaZ);
        }
        return result; 
    }
}