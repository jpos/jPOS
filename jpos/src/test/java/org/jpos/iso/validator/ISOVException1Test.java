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

package org.jpos.iso.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOMsg;
import org.junit.Test;

public class ISOVException1Test {

    @Test
    public void testConstructor() throws Throwable {
        ISOComponent errComponent = new ISOMsg("testISOVExceptionMti");
        ISOVException iSOVException = new ISOVException("testISOVExceptionDescription", errComponent);
        assertSame("iSOVException.errComponent", errComponent, iSOVException.errComponent);
        assertEquals("iSOVException.getMessage()", "testISOVExceptionDescription", iSOVException.getMessage());
        assertFalse("iSOVException.treated", iSOVException.treated);
        assertNull("iSOVException.getNested()", iSOVException.getNested());
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOVException iSOVException = new ISOVException("testISOVExceptionDescription");
        assertEquals("iSOVException.getMessage()", "testISOVExceptionDescription", iSOVException.getMessage());
        assertFalse("iSOVException.treated", iSOVException.treated);
        assertNull("iSOVException.getNested()", iSOVException.getNested());
    }

    @Test
    public void testGetErrComponent() throws Throwable {
        ISOComponent errComponent = new ISOMsg("testISOVExceptionMti");
        ISOComponent result = new ISOVException("testISOVExceptionDescription", errComponent).getErrComponent();
        assertSame("result", errComponent, result);
    }

    @Test
    public void testSetErrComponent() throws Throwable {
        ISOVException iSOVException = new ISOVException("testISOVExceptionDescription");
        ISOComponent c = new ISOMsg();
        iSOVException.setErrComponent(c);
        assertSame("iSOVException.errComponent", c, iSOVException.errComponent);
    }

    @Test
    public void testSetTreated() throws Throwable {
        ISOVException iSOVException = new ISOVException("testISOVExceptionDescription");
        iSOVException.setTreated(true);
        assertTrue("iSOVException.treated", iSOVException.treated);
    }

    @Test
    public void testTreated() throws Throwable {
        boolean result = new ISOVException("testISOVExceptionDescription", new ISOMsg("testISOVExceptionMti")).treated();
        assertFalse("result", result);
    }
}
