/*
 * This file is part of JQL.
 *
 * JQL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JQL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jql.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.faul.jql.utils;

/**
 * A simple timer class. To use, instantiate it, then call elapsedTime().
 * @author Ben Faul
 *
 */
public class Timer {
    private long start;

    /**
     * Constructor, and sets the initial time.
     */
    public Timer() {
    	resetTimer();
    }
    
    /**
     * Resets the initial time. Use after you want to measure a new stopwatch. 
     */
    public void resetTimer() {
        start = System.currentTimeMillis();
    }

    /**
     * Returns the number of elapsed milliseconds since construction, or resetTimer();
     * @return long. The number of milliseconds or restTimer() call.
     */
    public long getElapsed() {
        return System.currentTimeMillis() - start;
    }
}
