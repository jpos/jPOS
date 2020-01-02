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

package org.jpos.q2;


/**
 * An interface describing a Q2 service MBean.
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 * @see QPersist
 */
public interface QBean {

    // State
    int STOPPED    = 0;
    int STOPPING   = 1;
    int STARTING   = 2;
    int STARTED    = 3;
    int FAILED     = 4;
    int DESTROYED  = 5;

    String stateString[] = {
        "Stopped", "Stopping", "Starting", "Started", "Failed", "Destroyed"
    };

    /**
     * init the service
     * @throws Exception on error
     */
    void init () throws Exception;

    /**
     * start the service
     * @throws Exception on error
     */
    void start () throws Exception;

    /**
     * stop the service
     * @throws Exception on error
     */
    void stop () throws Exception;

    /**
     * destroy the service
     * @throws Exception on error
     */
    void destroy () throws Exception;

    /**
     * @return state (STARTING, STARTED, FAILED, DESTROYED ...)
     */
    int getState ();

    /**
     * @return state (STARTING, STARTED, FAILED, DESTROYED ...)
     */
    String getStateAsString ();
}
