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
 * org.jpos.space.Space operations do not declare any checked exceptions.
 * 
 * Persistent space implementations can raise some exceptions that 
 * are wrapped around this unchecked SpaceError that we recommend to
 * catch.
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 1.4.7
 */
public class SpaceError extends Error {

    private static final long serialVersionUID = 2478239452513511965L;
    public SpaceError() {
        super();
    }
    public SpaceError(String message) {
        super(message);
    }
    public SpaceError(String message, Throwable cause) {
        super(message, cause);
    }
    public SpaceError(Throwable cause) {
        super(cause);
    }
}

