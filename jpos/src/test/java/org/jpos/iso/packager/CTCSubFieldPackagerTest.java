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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.iso.IFA_LLLLCHAR;
import org.jpos.iso.IFE_CHAR;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOValidator;
import org.jpos.util.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CTCSubFieldPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        assertNull(cTCSubFieldPackager.getLogger(), "cTCSubFieldPackager.getLogger()");
        assertNull(cTCSubFieldPackager.getRealm(), "cTCSubFieldPackager.getRealm()");
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        boolean result = cTCSubFieldPackager.emitBitMap();
        assertFalse(result, "result");
    }

    @Test
    public void testPack1() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        cTCSubFieldPackager.setFieldPackager(fld);
        byte[] result = cTCSubFieldPackager.pack(new ISOField());
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testPackThrowsISOException() throws Throwable {
        try {
            new CTCSubFieldPackager().pack(null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("null: null", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("null: Cannot invoke \"org.jpos.iso.ISOComponent.getChildren()\" because \"c\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"org.jpos.iso.ISOComponent.getChildren()\" because \"c\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
        }
    }

    @Test
    public void testPackThrowsISOException1() throws Throwable {
        try {
            new CTCSubFieldPackager().pack(new ISOBinaryField(100));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("null: null", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("null: Cannot read the array length because \"this.fld\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot read the array length because \"this.fld\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
        }
    }

    @Test
    public void testPackThrowsISOException2() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        cTCSubFieldPackager.setFieldPackager(fld);
        try {
            cTCSubFieldPackager.pack(new ISOMsg("testCTCSubFieldPackagerMti"));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("null: null", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("null: Cannot invoke \"org.jpos.iso.ISOFieldPackager.pack(org.jpos.iso.ISOComponent)\" because \"this.fld[i]\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"org.jpos.iso.ISOFieldPackager.pack(org.jpos.iso.ISOComponent)\" because \"this.fld[i]\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
        }
    }

    @Test
    public void testUnpack() throws Throwable {
        byte[] b = new byte[0];
        int result = new CTCSubFieldPackager().unpack(new ISOField(), b);
        assertEquals(0, result, "result");
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[6];
        fld[0] = new IFA_LLLLCHAR(100, "testCTCSubFieldPackagerDescription");
        cTCSubFieldPackager.setFieldPackager(fld);
        byte[] b = new byte[4];
        try {
            cTCSubFieldPackager.unpack(new ISOField(), b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("org.jpos.iso.IFA_LLLLCHAR: Problem unpacking field 0", ex.getMessage(), "ex.getMessage()");
            assertEquals("Invalid character found. Expected digit.", ex.getNested().getMessage(), "ex.getNested().getMessage()");
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOFieldPackager[] fld = new ISOFieldPackager[4];
            fld[0] = new IFE_CHAR();
            CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
            cTCSubFieldPackager.setFieldPackager(fld);
            byte[] b = new byte[2];
            cTCSubFieldPackager.unpack(null, b);
        });
    }

    @Test
    public void testUnpackThrowsNullPointerException2() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            new CTCSubFieldPackager().unpack(new ISOBinaryField(100), (byte[]) null);
        });
    }

    @Test
    public void testValidateThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            new CTCSubFieldPackager().validate(null);
        });
    }

    @Test
    public void testValidateThrowsNullPointerException2() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            new CTCSubFieldPackager().validate(new ISOMsg());
        });
    }

    @Test
    public void testValidateThrowsNullPointerException3() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            CTCSubFieldPackager cTCSubFieldPackager = new CTCSubFieldPackager();
            cTCSubFieldPackager.setLogger(Logger.getLogger(""), "testCTCSubFieldPackagerRealm");
            ISOValidator[] fvlds = new ISOValidator[2];
            cTCSubFieldPackager.setFieldValidator(fvlds);
            cTCSubFieldPackager.validate(new ISOMsg("testCTCSubFieldPackagerMti"));
        });
    }
}
