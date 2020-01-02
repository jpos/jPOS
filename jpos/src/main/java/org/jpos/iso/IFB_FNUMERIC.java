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
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id: IFB_FNUMERIC.java 2706 2009-03-05 11:24:43Z apr $
 * @see ISOComponent
 */
public class IFB_FNUMERIC extends ISOStringFieldPackager {
    public IFB_FNUMERIC() {
        super(LeftPadder.ZERO_PADDER, BCDInterpreter.RIGHT_PADDED_F, NullPrefixer.INSTANCE);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_FNUMERIC(int len, String description, boolean isLeftPadded) {
        super(len, description, LeftPadder.ZERO_PADDER,
                isLeftPadded ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED_F,
                NullPrefixer.INSTANCE);
    }

    /** Must override ISOFieldPackager method to set the Interpreter correctly */
    public void setPad(boolean pad)
    {
        setInterpreter(pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED_F);
        this.pad = pad;
    }
}
