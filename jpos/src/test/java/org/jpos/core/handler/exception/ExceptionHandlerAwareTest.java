package org.jpos.core.handler.exception;

import org.jpos.core.InvalidCardException;
import org.jpos.core.handlers.exception.ExceptionHandler;
import org.jpos.core.handlers.exception.ExceptionHandlerAware;
import org.jpos.iso.ISOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ExceptionHandlerAwareTest implements ExceptionHandlerAware {

    @Before
    public void clearHandlers() {
        defaultHandlers.clear();
        specificHandlers.clear();
    }

    @Test
    public void testAddDefaultHandler() {
        addHandler(e -> null);
        assertEquals(1, defaultHandlers.size());
    }

    @Test
    public void testAddFirstSpecificHandler() {
        addHandler(e -> null, ISOException.class);
        assertEquals(1, specificHandlers.get(ISOException.class).size());
    }

    @Test
    public void testAddAdditionalSpecificHandler() {
        addHandler(e -> null, ISOException.class);
        addHandler(e -> null, ISOException.class);
        assertEquals(2, specificHandlers.get(ISOException.class).size());
    }

    @Test
    public void testRemoveDefaultHandler() {
        ExceptionHandler handler = e -> null;
        addHandler(handler);
        removeHandler(handler);
        assertEquals(0, defaultHandlers.size());
    }

    @Test
    public void testRemoveSpecificHandler() {
        ExceptionHandler handler = e -> null;
        addHandler(handler, ISOException.class);
        removeHandler(handler, ISOException.class);
        assertEquals(0, specificHandlers.get(ISOException.class).size());
    }

    @Test
    public void testRemoveSpecificExceptionHandlers() {
        ExceptionHandler handler = e -> null;
        addHandler(handler, ISOException.class);
        removeHandlers(ISOException.class);
        assertNull(specificHandlers.get(ISOException.class));
    }

    @Test(expected = ISOException.class)
    public void testFallbackHandler() throws Exception {
        try {
            throw new ISOException();
        } catch (Exception e) {
            handle(e);
        }
    }

    @Test
    public void testHandlerOrder() throws Exception {
        ExceptionHandler h1 = mock(ExceptionHandler.class);
        ExceptionHandler h2 = mock(ExceptionHandler.class);
        ExceptionHandler h3 = mock(ExceptionHandler.class);
        ExceptionHandler h4 = mock(ExceptionHandler.class);
        ExceptionHandler h5 = mock(ExceptionHandler.class);

        addHandler(h1);
        addHandler(h2);
        addHandler(h5, InvalidCardException.class);
        addHandler(h3, ISOException.class);
        addHandler(h4, ISOException.class);

        try {
            throw new ISOException();
        } catch (Exception e) {
            handle(e);
        }

        verifyZeroInteractions(h5);
        InOrder inOrder = Mockito.inOrder(h1,h2,h3,h4);

        inOrder.verify(h3).handle(any());
        inOrder.verify(h4).handle(any());
        inOrder.verify(h1).handle(any());
        inOrder.verify(h2).handle(any());
    }

    @Test
    public void testHandlerExecutionWhenExceptionThrown() throws Exception {
        ExceptionHandler h1 = mock(ExceptionHandler.class);
        ExceptionHandler h2 = mock(ExceptionHandler.class);
        ExceptionHandler h3 = mock(ExceptionHandler.class);
        ExceptionHandler h4 = mock(ExceptionHandler.class);
        ExceptionHandler h5 = mock(ExceptionHandler.class);

        when(h3.handle(any())).thenThrow(new ISOException("Hello"));

        addHandler(h1);
        addHandler(h2);
        addHandler(h5, InvalidCardException.class);
        addHandler(h3, ISOException.class);
        addHandler(h4, ISOException.class);

        try {
            throw new ISOException();
        } catch (Exception e) {
            try {
                handle(e);
                fail("Expected ISOException");
            } catch (ISOException e1) {
                assertEquals("Hello", e1.getMessage());
            }
        }

        verifyZeroInteractions(h1, h2, h4, h5);
        verify(h3).handle(any());
    }

}
