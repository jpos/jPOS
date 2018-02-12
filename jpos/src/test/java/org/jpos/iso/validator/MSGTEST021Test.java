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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVMsg;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MSGTEST021Test {

    @Test
    public void testConstructor() throws Throwable {
        MSGTEST02 mSGTEST02 = new MSGTEST02(true);
        assertNull("mSGTEST02.getRealm()", mSGTEST02.getRealm());
        assertTrue("mSGTEST02.breakOnError()", mSGTEST02.breakOnError());
        assertNull("mSGTEST02.getLogger()", mSGTEST02.getLogger());
    }

    @Test
    public void testConstructor1() throws Throwable {
        MSGTEST02 mSGTEST02 = new MSGTEST02();
        assertNull("mSGTEST02.getRealm()", mSGTEST02.getRealm());
        assertFalse("mSGTEST02.breakOnError()", mSGTEST02.breakOnError());
        assertNull("mSGTEST02.getLogger()", mSGTEST02.getLogger());
    }

    @Test
    public void testValidate() throws Throwable {
        ISOVMsg result = (ISOVMsg) new MSGTEST02().validate(new ISOMsg("testMSGTEST02Mti"));
        assertNotNull("result", result);
    }

    @Test
    public void testValidate1() throws Throwable {
        MSGTEST02 mSGTEST02 = new MSGTEST02();
        ISOVMsg m = mock(ISOVMsg.class);
        given(m.getComposite()).willReturn(m);
        given(m.hasFields(new int[] { 0, 1 })).willReturn(false);
        given(m.addISOVError(isA(ISOVError.class))).willReturn(true);
        given(m.getString(0)).willReturn("");
        ISOVMsg result = (ISOVMsg) mSGTEST02.validate(m);
        assertSame("result", m, result);
    }

    @Test
    public void testValidate2() throws Throwable {
        ISOMsg m = new ISOVMsg(new ISOMsg("testMSGTEST02Mti"), new ISOVError("testMSGTEST02Description"));
        m.set(1, "testMSGTEST02Value");
        ISOMsg result = (ISOMsg) new MSGTEST02().validate(m);
        assertSame("result", m, result);
    }

    @Test
    public void testValidateThrowsISOException() throws Throwable {
        try {
            new MSGTEST02().validate(new ISOField());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "Can't call validate on non Composite", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testValidateThrowsISOVException() throws Throwable {
        try {
            new MSGTEST02(true).validate(new ISOMsg(100));
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
        ISOComponent m = new ISOVMsg(new ISOMsg("testMSGTEST02Mti"));
        try {
            new MSGTEST02(true).validate(m);
            fail("Expected ISOVException to be thrown");
        } catch (ISOVException ex) {
            assertEquals("ex.getMessage()", "Error on msg. ", ex.getMessage());
            assertFalse("ex.treated", ex.treated);
            assertSame("ex.errComponent", m, ex.errComponent);
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException() throws Throwable {
        ISOMsg Source = new ISOMsg("testMSGTEST02Mti");
        Source.setMTI("testMSGTEST02Mti");
        Source.setRetransmissionMTI();
        ISOComponent m = new ISOVMsg(Source);
        try {
            new MSGTEST02().validate(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException1() throws Throwable {
        try {
            new MSGTEST02().validate(new ISOMsg());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException2() throws Throwable {
        ISOComponent m = new ISOVMsg(new ISOMsg(100));
        try {
            new MSGTEST02().validate(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException3() throws Throwable {
        ISOMsg m = new ISOMsg("testMSGTEST02Mti");
        m.setMTI("testMSGTEST02Mti");
        m.setRetransmissionMTI();
        try {
            new MSGTEST02().validate(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
