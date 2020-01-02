/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.space;

@SuppressWarnings("unchecked")
public class SpaceTap implements SpaceListener {
    LocalSpace ssp;
    LocalSpace dsp;
    Object key;
    Object tapKey;
    long tapTimeout;

    /**
     * @param sp space
     * @param key key to monitor
     * @param tapKey key to use when copying 
     * @param tapTimeout copy timeout in millis
     */
    public SpaceTap (LocalSpace sp, Object key, Object tapKey, long tapTimeout) {
        this (sp, sp, key, tapKey, tapTimeout);
    }
    /**
     * @param ssp source space
     * @param dsp destination space
     * @param key key to monitor
     * @param tapKey key to use when copying 
     * @param tapTimeout copy timeout in millis
     */
    public SpaceTap (LocalSpace ssp, LocalSpace dsp, Object key, Object tapKey, long tapTimeout) {
        super();
        this.ssp = ssp;
        this.dsp = dsp;
        this.key = key;
        this.tapKey = tapKey;
        this.tapTimeout = tapTimeout;
        if (key.equals (tapKey) && ssp == dsp)
            throw new IllegalArgumentException ("Possible deadlock - key equals tap-key within same space");
        ssp.addListener (key, this);
    }
    public void notify (Object key, Object value) {
        dsp.out (tapKey, value, tapTimeout);
    }
   
    public void close() {
        if (ssp != null) {
            ssp.removeListener (key, this);
            ssp = null;
        }
    }
}

