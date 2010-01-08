/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

import junit.framework.TestCase;

/**
 * @author apr
 */
public class ISOMsgTest extends TestCase
{
    public void testGetBytes() throws Exception {
        ISOMsg m = new ISOMsg("0800");
        m.set (3, "000000");
        m.set (52, "CAFEBABE".getBytes());
        m.set ("63.2.3", "Field 63.2.3");
        m.set ("63.2.4", m.getBytes (52));

        assertEquals ("000000", m.getString (3));
        assertEquals ("000000", new String(m.getBytes(3)));
        assertEquals ("CAFEBABE", new String(m.getBytes(52)));
        assertEquals ("Field 63.2.3", m.getString ("63.2.3"));
        assertEquals ("CAFEBABE", new String(m.getBytes("63.2.4")));
    }
    
    public void testFPath() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        
        assertFalse(m.hasField("63"));
        assertFalse(m.hasField("63.2"));
        assertFalse(m.hasField("63.2.3"));
        
        m.set("63.2.3","value3");
        m.set("63.2.4","value4");
        
        /*
         * Check null processing on set matches existing processing (setting to null same as unset).
         * 
         * Please note m.set(fPathString,null) is amiguous and cannot be distinguished from
         *  m.set(fieldNumberString, byte[]), so take care.
         */
        String b = null;
        m.set("63.2.5",b);  // null
        m.set("63.2.6",m.getString("99"));  // null as well
        assertFalse(m.hasField("63.2.5"));
        assertFalse(m.hasField("63.2.6"));
                
        assertEquals(true,m.hasField("63.2.3"));
        assertEquals("value3", m.getString("63.2.3"));
        assertEquals(true,m.hasField("63.2.4"));
        assertEquals("value4", m.getString("63.2.4"));
        
        assertFalse(m.hasField("63.2.999"));
        assertFalse(m.hasField("63.2.4.999"));
                
        m.unset("63.2.3");
        
        assertFalse(m.hasField("63.2.3"));
        
        assertEquals(true,m.hasField("63.2"));
        m.unset("63.2.4");  // Removal of last remaining field triggers removal of immediate parent.
        assertFalse(m.hasField("63.2"));
        
        m.unset("63.2.4.999");  // No problem removing non-existant fields.

    }
}
