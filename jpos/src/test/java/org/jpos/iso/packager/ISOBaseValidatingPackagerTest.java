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

package org.jpos.iso.packager;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.iso.ISOBaseValidator;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldValidator;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVMsg;
import org.jpos.iso.ISOValidator;
import org.jpos.iso.IVA_ALPHANUM;
import org.jpos.iso.IVA_ALPHANUMNOBLANK;
import org.jpos.iso.IVA_ALPHANUMNOZERO_NOBLANK;
import org.jpos.iso.validator.ISOVException;
import org.jpos.iso.validator.MSGTEST;
import org.jpos.iso.validator.MSGTEST02;
import org.jpos.iso.validator.TEST0100;
import org.junit.jupiter.api.Test;

public class ISOBaseValidatingPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        assertNull(iSOBaseValidatingPackager.getLogger(), "iSOBaseValidatingPackager.getLogger()");
        assertNull(iSOBaseValidatingPackager.getRealm(), "iSOBaseValidatingPackager.getRealm()");
    }

    @Test
    public void testSetFieldValidator() throws Throwable {
        ISOValidator[] fvlds = new ISOValidator[1];
        ISOBaseValidatingPackager cTCSubElementPackager = new CTCSubElementPackager();
        cTCSubElementPackager.setFieldValidator(fvlds);
        assertSame(fvlds, ((CTCSubElementPackager) cTCSubElementPackager).fldVld,
                "(CTCSubElementPackager) cTCSubElementPackager.fldVld");
    }

    @Test
    public void testSetMsgValidator() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[0];
        ISOBaseValidatingPackager cTCSubElementPackager = new CTCSubElementPackager();
        cTCSubElementPackager.setMsgValidator(msgVlds);
        assertSame(msgVlds, ((CTCSubElementPackager) cTCSubElementPackager).msgVld,
                "(CTCSubElementPackager) cTCSubElementPackager.msgVld");
    }

    @Test
    public void testValidate10() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new TEST0100(true);
        ISOValidator[] fvlds = new ISOValidator[0];
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOComponent m = new ISOMsg("testISOBaseValidatingPackagerMti");
        ISOMsg result = (ISOMsg) iSOBaseValidatingPackager.validate(m);
        assertSame(m, result, "result");
    }

    @Test
    public void testValidate12() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[1];
        msgVlds[0] = new MSGTEST02();
        ISOFieldValidator[] fvlds = new ISOFieldValidator[1];
        fvlds[0] = new IVA_ALPHANUM("testISOBaseValidatingPackagerDescription");
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOVMsg result = (ISOVMsg) iSOBaseValidatingPackager.validate(new ISOMsg("testISOBaseValidatingPackagerMti"));
        assertNotNull(result, "result");
    }

    @Test
    public void testValidate13() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[1];
        msgVlds[0] = new MSGTEST02();
        ISOValidator[] fvlds = new ISOFieldValidator[1];
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOVMsg result = (ISOVMsg) iSOBaseValidatingPackager.validate(new ISOMsg("testISOBaseValidatingPackagerMti"));
        assertNotNull(result, "result");
    }

    @Test
    public void testValidate14() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new TEST0100(true);
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOValidator[] fvlds = new ISOValidator[5];
        fvlds[1] = new ISOFieldValidator();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        ISOComponent m = new ISOMsg("testISOBaseValidatingPackagerMti");
        ISOMsg result = (ISOMsg) iSOBaseValidatingPackager.validate(m);
        assertSame(m, result, "result");
    }

    @Test
    public void testValidate18() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new TEST0100(true);
        msgVlds[1] = new MSGTEST();
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOValidator[] fvlds = new ISOValidator[5];
        fvlds[1] = new ISOFieldValidator();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        ISOVMsg result = (ISOVMsg) iSOBaseValidatingPackager.validate(new ISOMsg("testISOBaseValidatingPackagerMti"));
        assertNotNull(result, "result");
    }

    @Test
    public void testValidate3() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[1];
        msgVlds[0] = new TEST0100();
        ISOValidator[] fvlds = new ISOValidator[0];
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOMsg m = new ISOMsg("testISOBaseValidatingPackagerMti");
        m.setMTI("testISOBaseValidatingPackagerMti");
        ISOMsg result = (ISOMsg) iSOBaseValidatingPackager.validate(m);
        assertSame(m, result, "result");
    }

    @Test
    public void testValidate4() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOValidator[] fvlds = new ISOValidator[2];
        fvlds[0] = new ISOFieldValidator(100, 1000, "testISOBaseValidatingPackagerDescription");
        fvlds[1] = new IVA_ALPHANUMNOBLANK(true, "testISOBaseValidatingPackagerDescription");
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new ISOBaseValidator(true);
        msgVlds[1] = new MSGTEST();
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOVMsg result = (ISOVMsg) iSOBaseValidatingPackager.validate(new ISOMsg(100));
        assertNotNull(result, "result");
    }

    @Test
    public void testValidate9() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOValidator[] fvlds = new ISOValidator[2];
        fvlds[0] = new ISOFieldValidator(100, 1000, "testISOBaseValidatingPackagerDescription");
        fvlds[1] = new IVA_ALPHANUMNOBLANK(true, "testISOBaseValidatingPackagerDescription");
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new ISOBaseValidator(true);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOMsg result = (ISOMsg) iSOBaseValidatingPackager.validate(new ISOMsg(100));
        assertEquals(0, result.getDirection(), "result.getDirection()");
    }

    @Test
    public void testValidateThrowsClassCastException() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOValidator[] fvlds = new ISOValidator[2];
        fvlds[0] = new ISOBaseValidator();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals(ClassCastException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testValidateThrowsNullPOinterException1() throws Throwable {
        try {
            new ISOBaseValidatingPackager().validate(new ISOField(100, "testISOBaseValidatingPackagerv"));
            fail("Expected ClassCastException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(NullPointerException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testValidateThrowsClassCastException2() throws Throwable {
        ISOValidator[] fvlds = new ISOValidator[3];
        fvlds[1] = new ISOBaseValidator();
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals(ClassCastException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testValidateThrowsClassCastException3() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOValidator[] fvlds = new ISOValidator[3];
        fvlds[0] = new ISOFieldValidator("testISOBaseValidatingPackagerDescription");
        fvlds[1] = new ISOBaseValidator();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg("testISOBaseValidatingPackagerMti"));
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals(ClassCastException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testValidateThrowsClassCastException4() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOValidator[] fvlds = new ISOValidator[3];
        fvlds[0] = new ISOFieldValidator("testISOBaseValidatingPackagerDescription");
        fvlds[1] = new ISOBaseValidator();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals(ClassCastException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testValidateThrowsISOException() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[1] = new TEST0100();
        ISOValidator[] fvlds = new ISOValidator[0];
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("MTI not available", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOException1() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new TEST0100(true);
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOFieldValidator[] fvlds = new ISOFieldValidator[3];
        fvlds[0] = new IVA_ALPHANUM();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg(100));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("can't getMTI on inner message", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOException2() throws Throwable {
        ISOFieldValidator iVA_ALPHANUMNOBLANK = new IVA_ALPHANUMNOBLANK(true, "testISOBaseValidatingPackagerDescription");
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[0] = new MSGTEST(false);
        msgVlds[1] = new MSGTEST();
        msgVlds[2] = new TEST0100(true);
        ISOFieldValidator[] fvlds = new ISOFieldValidator[3];
        fvlds[0] = iVA_ALPHANUMNOBLANK;
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("MTI not available", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOException3() throws Throwable {
        ISOFieldValidator iVA_ALPHANUMNOBLANK = new IVA_ALPHANUMNOBLANK(true, "testISOBaseValidatingPackagerDescription");
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[2] = new TEST0100(true);
        ISOFieldValidator[] fvlds = new ISOFieldValidator[3];
        fvlds[0] = iVA_ALPHANUMNOBLANK;
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("MTI not available", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOException4() throws Throwable {
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[0] = new MSGTEST(false);
        msgVlds[1] = new MSGTEST();
        msgVlds[2] = new TEST0100(true);
        ISOValidator[] fvlds = new ISOFieldValidator[3];
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("MTI not available", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOException5() throws Throwable {
        ISOValidator[] fvlds = new ISOValidator[1];
        fvlds[0] = new IVA_ALPHANUMNOZERO_NOBLANK();
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[1] = new TEST0100();
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("MTI not available", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOException6() throws Throwable {
        ISOFieldValidator iVA_ALPHANUMNOBLANK = new IVA_ALPHANUMNOBLANK(true, "testISOBaseValidatingPackagerDescription");
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[0] = new MSGTEST(true);
        ISOFieldValidator[] fvlds = new ISOFieldValidator[3];
        fvlds[0] = iVA_ALPHANUMNOBLANK;
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        try {
            iSOBaseValidatingPackager.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Error on msg. ", ex.getMessage(), "ex.getMessage()");
            assertFalse(((ISOVException) ex).treated(), "ex.treated()");
            assertNotNull(((ISOVException) ex).getErrComponent(), "ex.getErrComponent()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsNullPointerException() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[2] = new MSGTEST02();
        ISOValidator[] fvlds = new ISOValidator[0];
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOMsg m = new ISOMsg();
        m.setMTI("testISOBaseValidatingPackagerMti");
        m.setRetransmissionMTI();
        try {
            iSOBaseValidatingPackager.validate(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.endsWith(String)\" because the return value of \"org.jpos.iso.ISOMsg.getString(int)\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testValidateThrowsNullPointerException1() throws Throwable {
        try {
            new ISOBaseValidatingPackager().validate(new ISOMsg("testISOBaseValidatingPackagerMti"));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"<local6>\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testValidateThrowsNullPointerException2() throws Throwable {
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[0] = new TEST0100();
        msgVlds[1] = new MSGTEST();
        msgVlds[2] = new MSGTEST02();
        ISOValidator[] fvlds = new ISOValidator[0];
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOMsg m = new ISOMsg();
        m.setMTI("testISOBaseValidatingPackagerMti");
        m.setRetransmissionMTI();
        try {
            iSOBaseValidatingPackager.validate(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.endsWith(String)\" because the return value of \"org.jpos.iso.ISOMsg.getString(int)\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testValidateThrowsNullPointerException3() throws Throwable {
        ISOVError FirstError = new ISOVError("testISOBaseValidatingPackagerDescription", "testISOBaseValidatingPackagerRejectCode");
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[1];
        msgVlds[0] = new MSGTEST02();
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOComponent m = new ISOVMsg(new ISOMsg(), FirstError);
        ISOValidator[] fvlds = new ISOValidator[0];
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        try {
            iSOBaseValidatingPackager.validate(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.endsWith(String)\" because the return value of \"org.jpos.iso.ISOMsg.getString(int)\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testValidateThrowsNullPointerException4() throws Throwable {
        ISOVError FirstError = new ISOVError("testISOBaseValidatingPackagerDescription", "testISOBaseValidatingPackagerRejectCode");
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[1];
        msgVlds[0] = new MSGTEST02();
        ISOFieldValidator[] fvlds = new ISOFieldValidator[1];
        fvlds[0] = new IVA_ALPHANUM("testISOBaseValidatingPackagerDescription");
        ISOBaseValidatingPackager iSOBaseValidatingPackager = new ISOBaseValidatingPackager();
        iSOBaseValidatingPackager.setFieldValidator(fvlds);
        iSOBaseValidatingPackager.setMsgValidator(msgVlds);
        ISOComponent m = new ISOVMsg(new ISOMsg(), FirstError);
        try {
            iSOBaseValidatingPackager.validate(m);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.endsWith(String)\" because the return value of \"org.jpos.iso.ISOMsg.getString(int)\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
