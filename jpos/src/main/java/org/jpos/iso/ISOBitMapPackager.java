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

package org.jpos.iso;

/**
 * IF*_BITMAP classes extends this class instead of ISOFieldPackager
 * so packagers can check if field-1 ISOFieldPackager is an instance
 * of an ISOBitMapPackager and handle differences between ANSI X9.2
 * and ISO-8583 packaging schemes.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 *
 * @see ISOFieldPackager
 */
public abstract class ISOBitMapPackager extends ISOFieldPackager {
    public ISOBitMapPackager() {
        super();
    }
    public ISOBitMapPackager(int len, String description) {
        super(len, description);
    }
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBitMap (fieldNumber);
    }
}
