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

package org.jpos.q2.iso;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.q2.Q2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        assertEquals("org.jpos.q2.iso.TaskAdaptor", taskAdaptor.getLog().getRealm(), "taskAdaptor.getLog().getRealm()");
        assertEquals(-1, taskAdaptor.getState(), "taskAdaptor.getState()");
        assertTrue(taskAdaptor.isModified(), "taskAdaptor.isModified()");
    }

    @Test
    public void testGetObject() throws Throwable {
        Object result = new TaskAdaptor().getObject();
        assertNull(result, "result");
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        try {
            taskAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getFactory()\" because the return value of \"org.jpos.q2.iso.TaskAdaptor.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(taskAdaptor.task, "taskAdaptor.task");
            assertTrue(taskAdaptor.isModified(), "taskAdaptor.isModified()");
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        String[] args = new String[0];
        taskAdaptor.setServer(q2);
        try {
            taskAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildTextTrim(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(taskAdaptor.task, "taskAdaptor.task");
            assertFalse(taskAdaptor.isModified(), "taskAdaptor.isModified()");
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        try {
            taskAdaptor.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getFactory()\" because the return value of \"org.jpos.q2.iso.TaskAdaptor.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertTrue(taskAdaptor.isModified(), "taskAdaptor.isModified()");
        }
    }

    @Test
    public void testStopService() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        taskAdaptor.setName("testTaskAdaptorName");
        taskAdaptor.stopService();
        assertNull(taskAdaptor.task, "taskAdaptor.task");
        assertEquals("testTaskAdaptorName", taskAdaptor.getName(), "taskAdaptor.getName()");
    }

}
