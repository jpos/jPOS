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

package org.jpos.space;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class SpaceInterceptorTest {

    @Test
    public void testConstructor() throws Throwable {
        Space sp = mock(Space.class);
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(sp);
        assertSame("spaceInterceptor.sp", sp, spaceInterceptor.sp);
    }

    @Test
    public void testInThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.in("");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }

    @Test
    public void testOut() throws Throwable {
        TSpace sp = mock(TSpace.class);
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(sp);
        sp.out("testString", "1", 0L);
        spaceInterceptor.out("testString", "1", 0L);
        assertSame("spaceInterceptor.sp", sp, spaceInterceptor.sp);
        verify(sp, times(2)).out("testString", "1", 0L);
    }

    @Test
    public void testOutThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.out(Integer.valueOf(-1), "1", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }

    @Test
    public void testRdp() throws Throwable {
        Space sp = SpaceFactory.getSpace();
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(sp);
        Object result = spaceInterceptor.rdp(sp);
        assertNull("result", result);
        assertSame("spaceInterceptor.sp", sp, spaceInterceptor.sp);
    }

    @Test
    public void testRdpThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.rdp("testString");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }

    @Test
    public void testRdThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.rd("", -1L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }
}
