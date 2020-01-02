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

/**
 * Request adds to TinySpace a few helper methods suitable for
 * exchanging requests and responses over an outter [Transient|Tiny]Space.
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 1.4.7
 */
@SuppressWarnings("deprecation")
public class Request extends TinySpace {
    public static String REQUEST  = "$REQ";
    public static String RESPONSE = "$RESP";
    public static String ERROR    = "$ERR";
    
    public Request () {
        super();
    }
    public Request (Object value) {
        out (REQUEST, value);
    }

    public Object getResponse () {
        return rd (RESPONSE);
    }
    public Object getResponse (long timeout) {
        return rd (RESPONSE, timeout);
    }
    public void setResponse (Object o) {
        out (RESPONSE, o);
    }

    public Object getRequest () {
        return rd (REQUEST);
    }
    public Object getRequest (long timeout) {
        return rd (REQUEST, timeout);
    }

    public void addError (Object o) {
        out (ERROR, o);
    }
    public Object[] getErrors () {
        return SpaceUtil.inpAll(this, ERROR);
    }
    public Object getError () {
        return inp (ERROR);
    }
}

