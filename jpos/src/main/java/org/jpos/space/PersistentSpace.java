/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

package org.jpos.space;

/**
 * Marker interface for spaces that utilize a persistent store.
 *
 * <p>
 * Implementations of this interface, such as {@code JDBMSpace} and {@code JESpace},
 * are designed to persist data to a durable storage medium. This ensures that data
 * can survive application restarts or failures, making it suitable for use cases
 * requiring long-term storage and reliability.
 * </p>
 *
 * <p>
 * Being a marker interface, {@code PersistentSpace} does not define any additional
 * methods but serves as a way to identify and group implementations that provide
 * persistent storage capabilities.
 * </p>
 *
 * @see JDBMSpace
 * @see JESpace
 */
public interface PersistentSpace { }
