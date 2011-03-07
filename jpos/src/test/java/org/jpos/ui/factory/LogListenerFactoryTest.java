package org.jpos.ui.factory;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogListenerFactoryTest {

    @Test
    public void testConstructor() throws Throwable {
        new LogListenerFactory();
        assertTrue("Test completed without Exception", true);
    }

}
