/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVField;
import org.jpos.iso.ISOVMsg;
import org.jpos.util.Logger;
import org.junit.Test;

public class VErrorParserTest {

    @Test
    public void testConstructor() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        assertNull("vErrorParser.realm", vErrorParser.realm);
        assertNull("vErrorParser.logger", vErrorParser.logger);
    }

    @Test
    public void testDump() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        vErrorParser.dump(new PrintStream(new ByteArrayOutputStream(), true), "testVErrorParserIndent");
        assertNull("vErrorParser.getRealm()", vErrorParser.getRealm());
    }

    @Test
    public void testGetLogger() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        Logger logger = new Logger();
        vErrorParser.setLogger(logger, "testVErrorParserRealm");
        Logger result = vErrorParser.getLogger();
        assertSame("result", logger, result);
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new VErrorParser().getRealm();
        assertNull("result", result);
    }

    @Test
    public void testGetRealm1() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        vErrorParser.setLogger(new Logger(), "testVErrorParserRealm");
        String result = vErrorParser.getRealm();
        assertEquals("result", "testVErrorParserRealm", result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        Vector result = vErrorParser.getVErrors(new ISOVMsg(new ISOMsg("testVErrorParserMti")));
        assertEquals("result.size()", 0, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors1() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOVError FirstError = new ISOVError("testVErrorParserDescription");
        ISOComponent c = new ISOVField(new ISOField(), FirstError);
        Vector result = vErrorParser.getVErrors(c);
        assertEquals("result.size()", 1, result.size());
        assertSame("result.get(0)", FirstError, result.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors10() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOComponent c = new ISOMsg("testVErrorParserMti");
        Vector result = vErrorParser.getVErrors(c);
        assertEquals("result.size()", 0, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors2() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOVError FirstError = new ISOVError("testVErrorParserDescription", "testVErrorParserRejectCode");
        ISOComponent c = new ISOVMsg(new ISOMsg("testVErrorParserMti"), FirstError);
        Vector result = vErrorParser.getVErrors(c);
        assertEquals("result.size()", 1, result.size());
        assertSame("result.get(0)", FirstError, result.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors3() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOMsg c = new ISOMsg();
        c.set(100, "testVErrorParserValue");
        Vector result = vErrorParser.getVErrors(c);
        assertEquals("result.size()", 0, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors4() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOComponent c = new ISOMsg();
        Vector result = vErrorParser.getVErrors(c);
        assertEquals("(ISOMsg) c.getMaxField()", 0, ((ISOMsg) c).getMaxField());
        assertEquals("result.size()", 0, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors5() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        Vector result = vErrorParser.getVErrors(new ISOVMsg(new ISOMsg()));
        assertEquals("result.size()", 0, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors6() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOMsg Source = new ISOVMsg(new ISOMsg(), new ISOVError("testVErrorParserDescription"));
        Source.set(100, "testVErrorParserValue");
        Vector result = vErrorParser.getVErrors(new ISOVMsg(Source));
        assertEquals("result.size()", 0, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors7() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        Vector result = vErrorParser.getVErrors(new ISOVField(new ISOField(100)));
        assertEquals("result.size()", 0, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors8() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOVError FirstError = new ISOVError("testVErrorParserDescription");
        ISOComponent c = new ISOVMsg(new ISOMsg(), FirstError);
        Vector result = vErrorParser.getVErrors(c);
        assertEquals("result.size()", 1, result.size());
        assertSame("result.get(0)", FirstError, result.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetVErrors9() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        Vector result = vErrorParser.getVErrors(new ISOBinaryField());
        assertEquals("result.size()", 0, result.size());
    }

    @Test
    public void testGetVErrorsThrowsNullPointerException() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOVMsg c = new ISOVMsg(new ISOMsg(), new ISOVError("testVErrorParserDescription"));
        c.addISOVError(null);
        try {
            vErrorParser.getVErrors(c);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetVErrorsThrowsNullPointerException1() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        try {
            vErrorParser.getVErrors(new ISOVMsg(new ISOMsg(), null));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetVErrorsThrowsNullPointerException2() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOVField c = new ISOVField(new ISOField(), new ISOVError("testVErrorParserDescription"));
        c.addISOVError(null);
        try {
            vErrorParser.getVErrors(c);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetVErrorsThrowsNullPointerException3() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        ISOVField c = new ISOVField(new ISOField(100, "testVErrorParserv"));
        c.addISOVError(null);
        try {
            vErrorParser.getVErrors(c);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseXMLErrorList() throws Throwable {
        String result = new VErrorParser().parseXMLErrorList();
        assertEquals("result", "", result);
    }

    @Test
    public void testResetErrors() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        vErrorParser.resetErrors();
    }

    @Test
    public void testSetLogger() throws Throwable {
        VErrorParser vErrorParser = new VErrorParser();
        Logger logger = new Logger();
        vErrorParser.setLogger(logger, "testVErrorParserRealm");
        assertSame("vErrorParser.logger", logger, vErrorParser.logger);
        assertEquals("vErrorParser.realm", "testVErrorParserRealm", vErrorParser.realm);
    }
}
