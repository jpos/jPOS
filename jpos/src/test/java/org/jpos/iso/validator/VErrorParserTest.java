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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVField;
import org.jpos.iso.ISOVMsg;
import org.jpos.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class VErrorParserTest {

    Throwable thrown;

    List<ISOVError> result;

    VErrorParser vErrorParser;

    @BeforeEach
    void setUp() {
        vErrorParser = new VErrorParser();
    }

    @Test
    void testConstructor() {
        assertNull(vErrorParser.realm, "vErrorParser.realm");
        assertNull(vErrorParser.logger, "vErrorParser.logger");
    }

    @Test
    void testDump() {
        vErrorParser.dump(new PrintStream(new ByteArrayOutputStream(), true), "testVErrorParserIndent");
        assertNull(vErrorParser.getRealm(), "vErrorParser.getRealm()");
    }

    @Test
    void testGetLogger() {
        Logger logger = new Logger();
        vErrorParser.setLogger(logger, "testVErrorParserRealm");

        assertSame(logger, vErrorParser.getLogger());
    }

    @Test
    void testGetRealm() {
        assertNull(vErrorParser.getRealm());
    }

    @Test
    void testGetRealm1() {
        vErrorParser.setLogger(new Logger(), "testVErrorParserRealm");

        assertEquals("testVErrorParserRealm", vErrorParser.getRealm());
    }

    @Test
    void testGetVErrors() {
        result = vErrorParser.getVErrors(new ISOVMsg(new ISOMsg("testVErrorParserMti")));
        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrors1() {
        ISOVError FirstError = new ISOVError("testVErrorParserDescription");
        ISOComponent c = new ISOVField(new ISOField(), FirstError);
        result = vErrorParser.getVErrors(c);
        assertEquals(1, result.size(), "result.size()");
        assertSame(FirstError, result.get(0), "result.get(0)");
    }

    @Test
    void testGetVErrors10() {
        ISOComponent c = new ISOMsg("testVErrorParserMti");
        result = vErrorParser.getVErrors(c);
        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrors2() {
        ISOVError FirstError = new ISOVError("testVErrorParserDescription", "testVErrorParserRejectCode");
        ISOComponent c = new ISOVMsg(new ISOMsg("testVErrorParserMti"), FirstError);
        result = vErrorParser.getVErrors(c);
        assertEquals(1, result.size(), "result.size()");
        assertSame(FirstError, result.get(0), "result.get(0)");
    }

    @Test
    void testGetVErrors3() {
        ISOMsg c = new ISOMsg();
        c.set(100, "testVErrorParserValue");
        result = vErrorParser.getVErrors(c);
        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrors4() {
        ISOComponent c = new ISOMsg();
        result = vErrorParser.getVErrors(c);

        assertEquals(0, c.getMaxField(), "(ISOMsg) c.getMaxField()");
        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrors5() {
        result = vErrorParser.getVErrors(new ISOVMsg(new ISOMsg()));

        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrors6() {
        ISOMsg Source = new ISOVMsg(new ISOMsg(), new ISOVError("testVErrorParserDescription"));
        Source.set(100, "testVErrorParserValue");
        result = vErrorParser.getVErrors(new ISOVMsg(Source));

        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrors7() {
        result = vErrorParser.getVErrors(new ISOVField(new ISOField(100)));

        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrors8() {
        ISOVError FirstError = new ISOVError("testVErrorParserDescription");
        ISOComponent c = new ISOVMsg(new ISOMsg(), FirstError);
        result = vErrorParser.getVErrors(c);

        assertEquals(1, result.size(), "result.size()");
        assertSame(FirstError, result.get(0), "result.get(0)");
    }

    @Test
    void testGetVErrors9() {
        result = vErrorParser.getVErrors(new ISOBinaryField());

        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    void testGetVErrorsThrowsNullPointerException() {
        ISOVMsg c = new ISOVMsg(new ISOMsg(), new ISOVError("testVErrorParserDescription"));
        c.addISOVError(null);

        thrown = assertThrows(NullPointerException.class,
            () -> vErrorParser.getVErrors(c)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testGetVErrorsThrowsNullPointerException1() {
        thrown = assertThrows(NullPointerException.class,
            () -> vErrorParser.getVErrors(new ISOVMsg(new ISOMsg(), null))
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testGetVErrorsThrowsNullPointerException2() {
        ISOVField c = new ISOVField(new ISOField(), new ISOVError("testVErrorParserDescription"));
        c.addISOVError(null);

        thrown = assertThrows(NullPointerException.class,
            () -> vErrorParser.getVErrors(c)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testGetVErrorsThrowsNullPointerException3() {
        ISOVField c = new ISOVField(new ISOField(100, "testVErrorParserv"));
        c.addISOVError(null);

        thrown = assertThrows(NullPointerException.class,
            () -> vErrorParser.getVErrors(c)
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testParseXMLErrorList() {
        assertEquals("", vErrorParser.parseXMLErrorList());
    }

    @Test
    void testResetErrors() {
        vErrorParser.resetErrors();
    }

    @Test
    void testSetLogger() {
        Logger logger = new Logger();
        vErrorParser.setLogger(logger, "testVErrorParserRealm");
        assertSame(logger, vErrorParser.logger, "vErrorParser.logger");
        assertEquals("testVErrorParserRealm", vErrorParser.realm, "vErrorParser.realm");
    }

}
