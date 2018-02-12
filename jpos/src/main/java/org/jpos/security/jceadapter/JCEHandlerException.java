/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

package  org.jpos.security.jceadapter;

/**
 * Signals that a JCE Handler exception of some sort has occurred.
 * @author Hani Samuel Kirollos
 * @version $Revision$ $Date$
 */
import org.jpos.security.SMException;


public class JCEHandlerException extends SMException {

    public JCEHandlerException () {
        super();
    }

    public JCEHandlerException (String s) {
        super(s);
    }

    public JCEHandlerException (Exception e) {
        super(e);
    }

    public JCEHandlerException (String s, Exception e) {
        super(s, e);
    }
}



