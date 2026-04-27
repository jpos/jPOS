/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.core;


/**
 *
 * Multipurpose sequencer.<br>
 * CardAgents requires persistent sequence number<br>
 * Sequencer interface isolate from particular DB implementations
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 */
public interface Sequencer {
    /**
     * Increments {@code counterName} by 1 and returns its new value.
     *
     * @param counterName name of the counter
     * @return the counter's new value
     */
    int get(String counterName);
    /**
     * Increments {@code counterName} by {@code add} and returns its new value.
     *
     * @param counterName name of the counter
     * @param add increment to apply
     * @return the counter's new value
     */
    int get(String counterName, int add);
    /**
     * Replaces the value of {@code counterName}.
     *
     * @param counterName name of the counter
     * @param value new value
     * @return the previous value
     */
    int set(String counterName, int value);
}
