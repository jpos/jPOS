package org.jpos.q2.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.q2.Q2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        assertEquals("taskAdaptor.getLog().getRealm()", "org.jpos.q2.iso.TaskAdaptor", taskAdaptor.getLog().getRealm());
        assertEquals("taskAdaptor.getState()", -1, taskAdaptor.getState());
        assertTrue("taskAdaptor.isModified()", taskAdaptor.isModified());
    }

    @Test
    public void testGetObject() throws Throwable {
        Object result = new TaskAdaptor().getObject();
        assertNull("result", result);
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        try {
            taskAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("taskAdaptor.task", taskAdaptor.task);
            assertTrue("taskAdaptor.isModified()", taskAdaptor.isModified());
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("taskAdaptor.task", taskAdaptor.task);
            assertFalse("taskAdaptor.isModified()", taskAdaptor.isModified());
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        try {
            taskAdaptor.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertTrue("taskAdaptor.isModified()", taskAdaptor.isModified());
        }
    }

    @Test
    public void testStopService() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        taskAdaptor.setName("testTaskAdaptorName");
        taskAdaptor.stopService();
        assertNull("taskAdaptor.task", taskAdaptor.task);
        assertEquals("taskAdaptor.getName()", "testTaskAdaptorName", taskAdaptor.getName());
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        TaskAdaptor taskAdaptor = new TaskAdaptor();
        try {
            taskAdaptor.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
