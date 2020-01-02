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

package org.jpos.core.handlers.exception;

/**
 * @author Alwyn Schoeman
 * @since 2.1.2
 */
public interface ExceptionHandler {

    /**
     * Implement custom exception handling that can be injected into jPos via configuration.
     *
     * @param e Exception
     * @return Same or modified exception.
     * @throws Exception In case the handler would like to throw an exception.  This should stop further handlers from processing.
     */
    Exception handle(Exception e) throws Exception;
}
