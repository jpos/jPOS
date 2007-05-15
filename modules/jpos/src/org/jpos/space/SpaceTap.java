/*
 * jPOS Project
 * Copyright (c) 2007 Alejandro Revilla and Contributors
 * jPOS.org (http://jpos.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jpos.space;

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

