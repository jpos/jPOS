/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.iso;

/**
 * Marker interface for packagers that encode and decode dataset payloads.
 */
public interface ISODatasetPackager extends ISOPackager, ISOSubFieldPackager {
    /**
     * Indicates whether datasets handled by this packager use the standard
     * dataset identifier and length envelope.
     *
     * @return {@code true} when the standard dataset envelope is present
     */
    default boolean hasDatasetEnvelope() {
        return true;
    }
}
