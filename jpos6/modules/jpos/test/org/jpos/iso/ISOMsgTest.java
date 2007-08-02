/*
 * Copyright (c) 2007 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
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

        assertEquals ("000000", m.getString (3));
        assertEquals ("000000", new String(m.getBytes(3)));
        assertEquals ("CAFEBABE", new String(m.getBytes(52)));
    }
}
