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
package com.github.sdnwiselab.sdnwise.flowtable;

import java.util.Arrays;
import java.util.Objects;

/**
 * FlowTableEntry represents the structure of the Entry of a FlowTable.
 * It is made of Window[], Action and Statistics.
 * This Class implements FlowTableInterface.
 * 
 * @author Sebastiano Milardo
 */
public class FlowTableEntry implements FlowTableInterface{
    
    public final static byte SDN_WISE_WINDOWS_MAX = 3;
    public final static byte SDN_WISE_RULE_COPY_LEN = SDN_WISE_WINDOWS_MAX * 
            FlowTableWindow.COPY_SIZE + 
            FlowTableAction.COPY_SIZE + 
            FlowTableStats.COPY_SIZE;
     public final static byte SDN_WISE_RULE_LEN = SDN_WISE_WINDOWS_MAX * 
            FlowTableWindow.SIZE + 
            FlowTableAction.SIZE + 
            FlowTableStats.SIZE;
    
    private final FlowTableWindow[] window = new FlowTableWindow[SDN_WISE_WINDOWS_MAX];
    private FlowTableAction action;
    private FlowTableStats stats = new FlowTableStats();

    /**
     * Simple constructor for the FlowTableEntry object.
     * 
     * It creates new FlowTableWindow instances setting all the values to 0.
     */
    public FlowTableEntry() {
        for (int i = 0; i < window.length; i++) {
            window[i] = new FlowTableWindow();
        }
        action = new FlowTableAction();
    }
    
    /**
     * Constructor for the FlowTableEntry object.
     * It initializes new FlowTableWindow[], FlowTableAction and FlowTableStats 
     * instances.
     * 
     * @param entry From byte array to FlowTableEntry
     */
    public FlowTableEntry(byte[] entry) {
        if (entry.length == SDN_WISE_RULE_LEN || 
                entry.length == SDN_WISE_RULE_COPY_LEN){
            for (int i = 0; i < SDN_WISE_WINDOWS_MAX; i++) {
                window[i] = (new FlowTableWindow(Arrays.copyOfRange(entry,
                        (FlowTableWindow.SIZE) * i, (i + 1) * 
                                FlowTableWindow.SIZE)));
            }
            action = new FlowTableAction(Arrays.copyOfRange(entry,
                    (FlowTableWindow.SIZE) * SDN_WISE_WINDOWS_MAX, 
                    ((FlowTableWindow.SIZE) * SDN_WISE_WINDOWS_MAX)
                    + FlowTableAction.SIZE));

            stats = new FlowTableStats(Arrays.copyOfRange(entry,
                    (FlowTableWindow.SIZE) * SDN_WISE_WINDOWS_MAX + 
                            FlowTableAction.SIZE,
                    entry.length));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("[");

        for (FlowTableWindow w : getWindows()) {
            out.append(w.toString());
        }
        out.append("]").append("[").append(getAction()).append("]" + "[").append(getStats()).append("]");

        return out.toString();
    }

   /**
     * Getter method to obtain the window array of the FlowTable entry.
     * 
     * @return the window[] of the FlowTable
     */
    public FlowTableWindow[] getWindows() {
        return Arrays.copyOf(window, window.length);
    }  
    
    /**
     * Setter method to set window array of the FlowTable entry.
     * 
     * @param window the window[] to set
     */
    public void setWindows(FlowTableWindow[] window) {
        if (window.length == this.window.length){
            System.arraycopy(window, 0, this.window, 0, this.window.length);
        }
    }

   /**
     * Getter method to obtain the Action part of the FlowTable entry.
     * 
     * @return the action of the FlowTable
     */
    public FlowTableAction getAction() {
        return action;
    }

   /**
     * Setter method to set the Action part of the FlowTable entry.
     * 
     * @param action the action to set
     */
    public void setAction(FlowTableAction action) {
        this.action = action;
    }

    /**
     * Getter method to obtain the Statistics of the FlowTable entry.
     * 
     * @return the statistics of the FlowTable entry.
     */
    public FlowTableStats getStats() {
        return stats;
    }

    /**
     * Setter method to set statistics of the FlowTable entry.
     * 
     * @param stats the statistics will be set.
     */
    public void setStats(FlowTableStats stats) {
        this.stats = stats;
    }

    @Override
    public byte[] toByteArray() {
        int i = 0;

        byte[] flowTableEntry = new byte[FlowTableWindow.SIZE * SDN_WISE_WINDOWS_MAX
                + FlowTableAction.SIZE + FlowTableStats.SIZE];
            for (FlowTableWindow fw : window) {
                if (fw != null){
                    System.arraycopy(fw.toByteArray(),
                            0,
                            flowTableEntry,
                            i,
                            FlowTableWindow.SIZE);
                } 
                i += FlowTableWindow.SIZE;
            }
         

        System.arraycopy(action.toByteArray(),
                0,
                flowTableEntry,
                SDN_WISE_WINDOWS_MAX * FlowTableWindow.SIZE,
                FlowTableAction.SIZE);

        System.arraycopy(stats.toByteArray(),
                0,
                flowTableEntry,
                SDN_WISE_WINDOWS_MAX * FlowTableWindow.SIZE
                + FlowTableAction.SIZE,
                FlowTableStats.SIZE);

        return flowTableEntry;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Arrays.deepHashCode(this.window);
        hash = 59 * hash + Objects.hashCode(this.action);
        hash = 59 * hash + Objects.hashCode(this.stats);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlowTableEntry other = (FlowTableEntry) obj;
        if (!Arrays.deepEquals(this.window, other.window)) {
            return false;
        }
        return Objects.equals(this.action, other.action);
    }
}

