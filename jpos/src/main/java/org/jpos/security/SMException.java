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

package  org.jpos.security;

import org.jpos.iso.ISOException;

/**
 * Signals that a Security Module exception of some sort has occurred.
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public class SMException extends ISOException {

    private static final long serialVersionUID = 6419380899728561889L;
    Exception nested = null;

    public SMException () {
        super();
    }

    public SMException (String s) {
        super(s);
    }

    public SMException (Exception e) {
        super(e);
    }

    public SMException (String s, Exception e) {
        super(s, e);
    }

    /**
     * put your documentation comment here
     * @return tag name
     */
    protected String getTagName () {
        return  "security-module-exception";
    }
}



