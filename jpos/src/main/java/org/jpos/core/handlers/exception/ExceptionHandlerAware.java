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

package org.jpos.core.handlers.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Interface that modifies an implementing class to add an exception handling pipeline.
 *
 * <p>
 *     The main pipeline consists of multiple sub-pipelines:
 *     <ul>
 *         <li>A pipeline for which handlers are called regardless of the type of exception handled.</li>
 *         <li>A pipeline per targeted exception type.</li>
 *     </ul>
 *     The targeted pipeline always executes before the default pipeline.
 * </p>
 * <p>
 *     In the event that both of the pipelines are empty, the default behavior is to rethrow the initial exception.
 * </p>
 * <p>
 *     There is no need to implement the methods unless you override the default behavior.
 * </p>
 *
 * @author  Alwyn Schoeman
 * @since 2.1.2
 */
public interface ExceptionHandlerAware {

    /**
     *
     * @return A list of exception handlers that are always executed.
     */
    List<ExceptionHandler> getDefaultExceptionHandlers();

    /**
     *
     * @return A map of exception classes to exception handlers.  These handlers only execute if the exception matches.
     */
    Map<Class<? extends Exception>, List<ExceptionHandler>> getTargetedExceptionHandlers();

    /**
     * Add a handler to the default pipeline.
     * @param handler ExceptionHandler to add.
     */
    default void addHandler(ExceptionHandler handler) {
        getDefaultExceptionHandlers().add(handler);
    }

    /**
     * Add a handler to an exception specific pipeline.
     * @param handler ExceptionHandler to add.
     * @param clazz Exception handler pipeline to add it to.
     */
    default void addHandler(ExceptionHandler handler, Class<? extends Exception> clazz) {
        List<ExceptionHandler> handlers = getTargetedExceptionHandlers().computeIfAbsent(clazz, f -> new ArrayList<>() );
        if (handler != null) {
            handlers.add(handler);
        }
    }

    /**
     * Remove a handler from the default pipeline.
     * @param handler ExceptionHandler to remove.
     */
    default void removeHandler(ExceptionHandler handler) {
        getDefaultExceptionHandlers().remove(handler);
    }

    /**
     * Remove a handler from an exception specific handler pipeline.
     * @param handler ExceptionHandler to remove.
     * @param clazz Exception pipeline to remove it from.
     */
    default void removeHandler(ExceptionHandler handler, Class<? extends Exception> clazz) {
        final List<ExceptionHandler> exceptionHandlers = getTargetedExceptionHandlers().get(clazz);
        if (exceptionHandlers != null) {
            exceptionHandlers.remove(handler);
        }
    }

    /**
     * Remove all handler for a specific exception handling pipeline.
     * @param clazz Exception pipeline to remove.
     */
    default void removeHandlers(Class<? extends Exception> clazz) {
        getTargetedExceptionHandlers().remove(clazz);
    }

    /**
     * Execute the pipeline by starting with the specific pipeline for the exception
     * followed by the default pipeline.
     * <br>
     * In the event of both pipelines being empty, the original exception is rethrown.
     * @param e Initial exception.
     * @return Same, modified or new exception.
     * @throws Exception In the event of a handler throwing an exception.  Processing by further handlers would be cancelled.
     */
    default Exception handle(Exception e) throws Exception {
        int strike = 1;
        Exception exception = e;
        final List<ExceptionHandler> exceptionHandlers = getTargetedExceptionHandlers().get(e.getClass());

        if (exceptionHandlers != null) {
            for (ExceptionHandler handler : exceptionHandlers) {
                exception = handler.handle(exception);
            }
        } else {
            strike++;
        }

        if (getDefaultExceptionHandlers().isEmpty() && (++strike == 3)) {
            throw e;
        }

        for (ExceptionHandler handler : getDefaultExceptionHandlers()) {
            exception = handler.handle(exception);
        }

        return exception;
    }
}
