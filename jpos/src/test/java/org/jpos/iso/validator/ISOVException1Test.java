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
