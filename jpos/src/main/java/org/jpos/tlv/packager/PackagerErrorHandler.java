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

package org.jpos.tlv.packager;


import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;


/**
 * Callback interface invoked when a tagged-sequence packager encounters a packing/unpacking error.
 * @author Vishnu Pillai
 */
public interface PackagerErrorHandler {

    /**
     * Invoked when packing {@code m} fails.
     *
     * @param m component being packed
     * @param e failure raised by the packager
     */
    void handlePackError(ISOComponent m, ISOException e);

    /**
     * Invoked when unpacking {@code msg} into {@code isoComponent} fails.
     *
     * @param isoComponent destination component
     * @param msg raw bytes being unpacked
     * @param e failure raised by the packager
     */
    void handleUnpackError(ISOComponent isoComponent, byte[] msg, ISOException e);
}
