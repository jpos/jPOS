package org.jpos.ui.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.swing.event.ChangeEvent;

import org.junit.Test;

public class JTabbedPaneFactoryTest {

    @Test
    public void testConstructor() throws Throwable {
        JTabbedPaneFactory jTabbedPaneFactory = new JTabbedPaneFactory();
        assertEquals("jTabbedPaneFactory.actions.size()", 0, jTabbedPaneFactory.actions.size());
    }

    @Test
    public void testStateChanged() throws Throwable {
        JTabbedPaneFactory jTabbedPaneFactory = new JTabbedPaneFactory();
        jTabbedPaneFactory.stateChanged(new ChangeEvent(Integer.valueOf(0)));
        assertEquals("jTabbedPaneFactory.actions.size()", 0, jTabbedPaneFactory.actions.size());
        assertNull("jTabbedPaneFactory.p", jTabbedPaneFactory.p);
        assertNull("jTabbedPaneFactory.ui", jTabbedPaneFactory.ui);
    }
}
