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

package org.jpos.core.handler.exception;

import org.jpos.core.InvalidCardException;
import org.jpos.core.handlers.exception.ExceptionHandler;
import org.jpos.core.handlers.exception.ExceptionHandlerAware;
import org.jpos.iso.ISOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ExceptionHandlerAwareTest implements ExceptionHandlerAware {

    private final Map<Class<? extends Exception>,List<ExceptionHandler>> exceptionHandlers = new HashMap<>();

    @BeforeEach
    public void clearHandlers() {
        getExceptionHandlers().clear();
    }

    @Test
    public void testAddDefaultHandler() {
        addHandler(e -> null);
        assertEquals(1, getExceptionHandlers().get(null).size());
    }

    @Test
    public void testAddFirstTargetedHandler() {
        addHandler(e -> null, ISOException.class);
        assertEquals(1, getExceptionHandlers().get(ISOException.class).size());
    }

    @Test
    public void testAddAdditionalTargetedHandler() {
        addHandler(e -> null, ISOException.class);
        addHandler(e -> null, ISOException.class);
        assertEquals(2, getExceptionHandlers().get(ISOException.class).size());
    }

    @Test
    public void testRemoveDefaultHandler() {
        ExceptionHandler handler = e -> null;
        addHandler(handler);
        removeHandler(handler);
        assertEquals(0, this.getExceptionHandlers().size());
    }

    @Test
    public void testRemoveTargetedHandler() {
        ExceptionHandler handler = e -> null;
        addHandler(handler, ISOException.class);
        addHandler(e -> null, ISOException.class);
        assertEquals(2, getExceptionHandlers().get(ISOException.class).size());
        removeHandler(handler, ISOException.class);
        assertEquals(1, getExceptionHandlers().get(ISOException.class).size());
    }

    @Test
    public void testRemoveTargetedExceptionHandlers() {
        ExceptionHandler handler = e -> null;
        addHandler(handler, ISOException.class);
        removeHandlers(ISOException.class);
        assertNull(getExceptionHandlers().get(ISOException.class));
    }

    @Test
    public void testFallbackHandler() throws Exception {
        assertThrows(ISOException.class, () -> {
            try {
                throw new ISOException();
            } catch (Exception e) {
                handle(e);
            }
        });
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

    @Override
    public Map<Class<? extends Exception>, List<ExceptionHandler>> getExceptionHandlers() {
        return exceptionHandlers;
    }
}
