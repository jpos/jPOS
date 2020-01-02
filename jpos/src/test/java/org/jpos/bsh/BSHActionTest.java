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

package org.jpos.bsh;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.event.ActionEvent;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BSHActionTest {

    @Disabled("test fails - needs a real action file")
    @Test
    public void testActionPerformed() throws Throwable {
        new BSHAction().actionPerformed(new ActionEvent("testString", 100, "testBSHActionParam3", 100L, 1000));
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testActionPerformedThrowsNullPointerException() throws Throwable {
        try {
            new BSHAction().actionPerformed(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new BSHAction();
        assertTrue(true, "Test completed without Exception");
    }

}
