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

package org.jpos.ui.action;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;
import java.util.NoSuchElementException;

import org.junit.Test;

public class RedirectTest {

    @Test
    public void testActionPerformedThrowsNoSuchElementException() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent("", 100, "", 100L, 1000));
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testActionPerformedThrowsNullPointerException() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent(Long.valueOf(0L), 100, null, 100L, 1000));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testActionPerformedThrowsNullPointerException1() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent("", 100, "testRedirectParam3", 100L, 1000));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testActionPerformedThrowsNullPointerException2() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent("", 100, "testRedirect\rParam3", 100L, 1000));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new Redirect();
        assertTrue("Test completed without Exception", true);
    }

}
