package org.jpos.iso.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVMsg;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MSGTEST1Test {

    @Test
    public void testConstructor() throws Throwable {
        MSGTEST mSGTEST = new MSGTEST();
        assertNull("mSGTEST.getRealm()", mSGTEST.getRealm());
        assertFalse("mSGTEST.breakOnError()", mSGTEST.breakOnError());
        assertNull("mSGTEST.getLogger()", mSGTEST.getLogger());
    }

    @Test
    public void testConstructor1() throws Throwable {
        MSGTEST mSGTEST = new MSGTEST(true);
        assertNull("mSGTEST.getRealm()", mSGTEST.getRealm());
        assertTrue("mSGTEST.breakOnError()", mSGTEST.breakOnError());
        assertNull("mSGTEST.getLogger()", mSGTEST.getLogger());
    }

    @Test
    public void testValidate() throws Throwable {
        MSGTEST mSGTEST = new MSGTEST();
        final ISOVMsg m = mock(ISOVMsg.class);
        given(m.getComposite()).willReturn(m);
        given(m.hasFields(new int[] { 3, 7, 11 })).willReturn(false);
        given(m.addISOVError(isA(ISOVError.class))).willReturn(true);

        ISOVMsg result = (ISOVMsg) mSGTEST.validate(m);
        assertSame("result", m, result);
    }

    @Test
    public void testValidate1() throws Throwable {
        ISOVMsg result = (ISOVMsg) new MSGTEST().validate(new ISOMsg("testMSGTESTMti"));
        assertNotNull("result", result);
    }

    @Test
    public void testValidateThrowsISOException() throws Throwable {
        try {
            new MSGTEST(true).validate(new ISOBinaryField());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "Can't call validate on non Composite", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testValidateThrowsISOVException() throws Throwable {
        try {
            new MSGTEST(true).validate(new ISOMsg("testMSGTESTMti"));
            fail("Expected ISOVException to be thrown");
        } catch (ISOVException ex) {
            assertEquals("ex.getMessage()", "Error on msg. ", ex.getMessage());
            assertFalse("ex.treated", ex.treated);
            assertNotNull("ex.errComponent", ex.errComponent);
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testValidateThrowsISOVException1() throws Throwable {
        ISOComponent m = new ISOVMsg(new ISOMsg());
        try {
            new MSGTEST(true).validate(m);
            fail("Expected ISOVException to be thrown");
        } catch (ISOVException ex) {
            assertEquals("ex.getMessage()", "Error on msg. ", ex.getMessage());
            assertFalse("ex.treated", ex.treated);
            assertSame("ex.errComponent", m, ex.errComponent);
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testValidateThrowsNullPointerException() throws Throwable {
        new MSGTEST().validate(null);
    }
}
