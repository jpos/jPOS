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

package org.jpos.q2.qbean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import org.jdom2.Element;
import org.jpos.space.SpaceError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpaceLetTest {

    @Test
    public void testConstructor() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        assertNotNull(spaceLet, "java default constructor");
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testInp() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        Element persist = mock(Element.class);
        spaceLet.setPersist(persist);
        spaceLet.initService();
        Object result = spaceLet.inp(Integer.valueOf(0));
        assertNull(result);
    }

    @Test
    public void testInpThrowsSpaceError() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.inp("testString");
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testInThrowsSpaceError() throws Throwable {
        assertThrows(SpaceError.class, () -> {
            SpaceLet spaceLet = new SpaceLet();
            spaceLet.in("");
        });
    }

    @Test
    public void testInThrowsSpaceError1() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.in("", 100L);
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testOutThrowsSpaceError1() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.out("testString", "testString");
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testOutThrowsSpaceError3() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.out("testString", "", 100L);
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testRdpThrowsSpaceError() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.rdp("");
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testRdThrowsSpaceError() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.rd(Integer.valueOf(0));
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testRdThrowsSpaceError1() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.rd(";1", 100L);
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

}
