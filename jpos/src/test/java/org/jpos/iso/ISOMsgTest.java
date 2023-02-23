/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * @author apr
 */
public class ISOMsgTest {
    @Test
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
    
    @Test
    public void testFPath() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        
        String mynull = null;
        
        assertFalse(m.hasField("63"));
        assertFalse(m.hasField(63));
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
        m.set("63.2.5",mynull);  // null
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
        
        assertFalse(m.hasField("99"));
        assertFalse(m.hasField(99));
        m.set("99.99","value99");
        m.set("99.98",mynull);
        
        assertEquals(true, m.hasField("99.99"));
        assertEquals("value99", m.getString("99.99"));
        
        assertFalse(m.hasField("100"));
        assertFalse(m.hasField(100));
        m.set("100.100",mynull);
        assertFalse(m.hasField("100.100"));
        assertFalse(m.hasField("100"));  // Not added unnecessarily
        assertFalse(m.hasField(100));  // Still not added unnecessarily
        

    }
    
    @Test
    public void testFPathISOComponent() throws Exception {
        ISOMsg m = new ISOMsg("0100");

        ISOField f100 = new ISOField(100,"value100");
        
        assertFalse(m.hasField("100"));
        assertFalse(m.hasField(100));
        m.set(f100);
        assertTrue(m.hasField("100"));
        assertTrue(m.hasField(100));
        assertEquals(f100,m.getComponent("100"));
        
        assertFalse(m.hasField("101"));
        assertFalse(m.hasField(101));
        m.set("101.101","value101");
        assertTrue(m.hasField("101.101"));
        assertTrue(m.hasField("101"));
        assertTrue(m.hasField(101));
        assertEquals(101,m.getComponent("101.101").getKey());
        assertEquals("value101", m.getComponent("101.101").getValue());
        m.set("101.102","value102");
        assertTrue(m.hasField("101.102"));
        assertEquals(102,m.getComponent("101.102").getKey());
        assertEquals("value102", m.getComponent("101.102").getValue());

        ISOMsg f102 = new ISOMsg(102);
        f102.set("1","value1");
        f102.set("2","value2");
        
        assertFalse(m.hasField("102"));
        m.set(f102);
        assertTrue(m.hasField("102.1"));
        assertTrue(m.hasField("102.2"));
        
        ISOMsg copy = new ISOMsg();
        copy.set("101",m.getComponent("101"));
        assertTrue(copy.hasField("101"));
        assertTrue(copy.hasField(101));
        assertTrue(copy.hasField("101.101"));
        assertTrue(copy.hasField("101.102"));
        assertEquals("value101",copy.getString("101.101"));
        assertEquals("value102",copy.getString("101.102"));
        
        copy.set("102.1", m.getComponent("102.1"));
        assertTrue(copy.hasField("102"));
        assertTrue(copy.hasField(102));
        assertTrue(copy.hasField("102.1"));
        assertEquals("value1",copy.getString("102.1"));
        assertFalse(copy.hasField("102.2"));
        assertNull(copy.getString("102.2"));
    }
    
    @Test
    public void testFPathISOComponent_2() throws Exception {
        ISOAmount comp = new ISOAmount();
        comp.setValue("840"+"2"+"000055555533");
        assertEquals(-1, comp.getFieldNumber());

        ISOMsg m = new ISOMsg();
        m.set(44, "hello44");
        m.set("95.2.8", comp);

        assertTrue(m.hasField(95));
        assertTrue(m.hasField("95"));
        assertNotNull(m.getComponent(95));

        assertTrue(m.hasField("95.2"));
        assertTrue(m.hasField("95.2.8"));

        ISOComponent extractedComp = m.getComponent("95.2.8");
        assertEquals(comp, extractedComp);
        assertEquals(8, extractedComp.getFieldNumber());
        assertEquals("840", ((ISOAmount)extractedComp).getCurrencyCodeAsString());

        ISOAmount comp2 = new ISOAmount(99);
        comp2.setValue("858"+"2"+"000011111177");

        assertEquals(99, comp2.getFieldNumber());
        m.set("95.2.10", comp2);

        // the set method above should have changed comp2's inner field number to the last value in the path
        assertEquals(10, comp2.getFieldNumber());

        assertNotNull(m.getComponent("95.2.10"));
        assertEquals(comp2, m.getComponent("95.2.10"));

        assertEquals(10, m.getComponent("95.2").getMaxField());
        assertEquals( 2, m.getComponent("95.2").getChildren().size());
        assertEquals(858, ((ISOAmount)m.getComponent("95.2.10")).getCurrencyCode());

        m.set("95.2.8", (String)null);
        assertEquals(1, m.getComponent("95.2").getChildren().size());
        assertEquals(858, ((ISOAmount)m.getComponent("95.2.10")).getCurrencyCode());

        m.unset("95.2.10");
        // The way ISOMsg#unset(fpath) works, is that if the parent of the unset sub-subfield becomes
        // empty, it will also delete the parent!  But the behaviour doesn't propagate up the hierarchy...
        assertFalse(m.hasField("95.2"));

        // ...therefore, 95 should still exist (along with the root ISOMsg, of course)
        assertTrue(m.hasField("95"));
        assertTrue(m.getComponent("95").getChildren().isEmpty());
    }

    @Test
    public void testFPathISOEcho() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        
        ISOField f100 = new ISOField(100,"value100");
        m.set(f100);
        
        m.set("101.101","value101");
        m.set("101.102","value102");

        ISOMsg f102 = new ISOMsg(102);
        f102.set("1","value1");
        f102.set("2","value2");
        m.set(f102);
        
        ISOMsg f103 = new ISOMsg(103);
        f103.set("97","value1");
        f103.set("98","value2");
        f103.set("99.1","99.1");
        m.set(f103);

        m.set(10,"10");
        m.set(11,"11");
        m.set(12,"12");
        m.set(13,"13");
        m.set(14,"14");
        m.set(15,"15");
        m.set(16,"16");
        
        String[] echoList = new String[] {"10","11","16","14","101.102","103","911.911","999"};
        
        ISOMsg copyMsg = new ISOMsg();
        
        for (String fpath : echoList) {
            copyMsg.set(fpath, m.getComponent(fpath));
        }
        
        /*
         * Check fields in echoMsg
         */
        assertEquals(true,copyMsg.hasField("10"));
        assertEquals(true,copyMsg.hasField(10));
        assertEquals(true,copyMsg.hasField("11"));
        assertEquals(true,copyMsg.hasField(11));
        assertEquals(true,copyMsg.hasField("14"));
        assertEquals(true,copyMsg.hasField(14));
        assertEquals(true,copyMsg.hasField("16"));
        assertEquals(true,copyMsg.hasField(16));
        assertEquals(true,copyMsg.hasField("101"));
        assertEquals(true,copyMsg.hasField(101));
        assertEquals(true,copyMsg.hasField("101.102"));
        assertEquals(true,copyMsg.hasField("103"));
        assertEquals(true,copyMsg.hasField(103));       // Whole of 103 copied
        assertEquals(true,copyMsg.hasField("103.97"));  // Whole of 103 copied
        assertEquals(true,copyMsg.hasField("103.98"));  // Whole of 103 copied
        
        
        /*
         * Check fields echod but *not* present in m are not somehow present in copyMsg
         */
        assertFalse(copyMsg.hasField("911"));
        assertFalse(copyMsg.hasField(911));
        assertFalse(copyMsg.hasField("911.911"));
        assertFalse(copyMsg.hasField("999"));
        assertFalse(copyMsg.hasField(999));
        
        /*
         * Check fields present in m but not required to be copied are not present in copyMsg
         */
        assertFalse(copyMsg.hasField("12"));
        assertFalse(copyMsg.hasField(12));
        assertFalse(copyMsg.hasField("13"));
        assertFalse(copyMsg.hasField(13));
        assertFalse(copyMsg.hasField("15"));
        assertFalse(copyMsg.hasField(15));
        assertFalse(copyMsg.hasField("100"));
        assertFalse(copyMsg.hasField(100));
        assertFalse(copyMsg.hasField("101.101"));
        assertFalse(copyMsg.hasField("102"));
        assertFalse(copyMsg.hasField(102));
        assertFalse(copyMsg.hasField("102.1"));
        assertFalse(copyMsg.hasField("102.2"));
    }
}
