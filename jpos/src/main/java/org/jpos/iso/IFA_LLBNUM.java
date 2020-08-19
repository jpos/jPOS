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
import java.io.IOException;
import java.io.InputStream;

/**
 * Length is represented in ASCII (as in IFA_LL*)
 * Value is represented in BCD
 * ISOFieldPackager Binary LLNUM
 *
 * @author Mladen Mrkic <mmrkic@arius.co.yu>
 *
 * @see ISOComponent
 */
public class IFA_LLBNUM extends ISOFieldPackager {
    private Interpreter interpreter;
    private Prefixer prefixer;
    
    public IFA_LLBNUM () {
        super();
        interpreter = BCDInterpreter.LEFT_PADDED;
        prefixer = AsciiPrefixer.LL;
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public  IFA_LLBNUM (int len, String description, boolean pad) {
        super(len, description);
        this.pad = pad;
        interpreter = pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED;
        prefixer = AsciiPrefixer.LL;
    }

    public void setPad(boolean pad)
    {
        this.pad = pad;
        interpreter = pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED;
    }

    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        String s = (String) c.getValue();
        int len = s.length();
        if (len > getLength() || len>99)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing IFA_LLBNUM field " + c.getKey()
            );

        byte[] b = new byte[2 + ((len+1) >> 1)];
        prefixer.encodeLength(len + 1 >> 1 << 1, b);
        interpreter.interpret(s, b, 2);
        return b;
    }

    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        int len = prefixer.decodeLength(b, offset);
        c.setValue (interpreter.uninterpret(b, offset + 2, len));
        return 2 + (++len >> 1);
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        int len = prefixer.decodeLength(readBytes (in, 2), 2);
        c.setValue (interpreter.uninterpret(readBytes (in, len+2 >> 1), 0, len));
    }
    public int getMaxPackedLength() {
        return 1 + (getLength()+1 >> 1);
    }
}
