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

package org.jpos.iso.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVMsg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MSGTEST1Test {

    @Test
    public void testConstructor() throws Throwable {
        MSGTEST mSGTEST = new MSGTEST();
        assertNull(mSGTEST.getRealm(), "mSGTEST.getRealm()");
        assertFalse(mSGTEST.breakOnError(), "mSGTEST.breakOnError()");
        assertNull(mSGTEST.getLogger(), "mSGTEST.getLogger()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        MSGTEST mSGTEST = new MSGTEST(true);
        assertNull(mSGTEST.getRealm(), "mSGTEST.getRealm()");
        assertTrue(mSGTEST.breakOnError(), "mSGTEST.breakOnError()");
        assertNull(mSGTEST.getLogger(), "mSGTEST.getLogger()");
    }

    @Test
    public void testValidate() throws Throwable {
        MSGTEST mSGTEST = new MSGTEST();
        final ISOVMsg m = mock(ISOVMsg.class);
        given(m.getComposite()).willReturn(m);
        given(m.hasFields(new int[] { 3, 7, 11 })).willReturn(false);
        given(m.addISOVError(isA(ISOVError.class))).willReturn(true);

        ISOVMsg result = (ISOVMsg) mSGTEST.validate(m);
        assertSame(m, result, "result");
    }

    @Test
    public void testValidate1() throws Throwable {
        ISOVMsg result = (ISOVMsg) new MSGTEST().validate(new ISOMsg("testMSGTESTMti"));
        assertNotNull(result, "result");
    }

    @Test
    public void testValidateThrowsISOException() throws Throwable {
        try {
            new MSGTEST(true).validate(new ISOBinaryField());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Can't call validate on non Composite", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOVException() throws Throwable {
        try {
            new MSGTEST(true).validate(new ISOMsg("testMSGTESTMti"));
            fail("Expected ISOVException to be thrown");
        } catch (ISOVException ex) {
            assertEquals("Error on msg. ", ex.getMessage(), "ex.getMessage()");
            assertFalse(ex.treated, "ex.treated");
            assertNotNull(ex.errComponent, "ex.errComponent");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsISOVException1() throws Throwable {
        ISOComponent m = new ISOVMsg(new ISOMsg());
        try {
            new MSGTEST(true).validate(m);
            fail("Expected ISOVException to be thrown");
        } catch (ISOVException ex) {
            assertEquals("Error on msg. ", ex.getMessage(), "ex.getMessage()");
            assertFalse(ex.treated, "ex.treated");
            assertSame(m, ex.errComponent, "ex.errComponent");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testValidateThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            new MSGTEST().validate(null);
        });
    }
}
