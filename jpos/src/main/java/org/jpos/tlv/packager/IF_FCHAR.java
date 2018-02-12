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

package org.jpos.tlv.packager;

import org.jpos.iso.AsciiInterpreter;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOStringFieldPackager;
import org.jpos.iso.NullPadder;
import org.jpos.iso.Prefixer;

/**
 *
 * Fully consuming packager
 *
 * @author Vishnu Pillai
 */
public class IF_FCHAR extends ISOStringFieldPackager {

    public IF_FCHAR() {
        super(NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, FullyConsumingPrefixer.INSTANCE);
    }

    public static class FullyConsumingPrefixer implements Prefixer {

        private static final FullyConsumingPrefixer INSTANCE = new FullyConsumingPrefixer();

        private FullyConsumingPrefixer() {
        }

        @Override
        public void encodeLength(int length, byte[] bytes) throws ISOException {

        }

        @Override
        public int decodeLength(byte[] bytes, int offset) throws ISOException {
            return bytes.length - offset;
        }

        @Override
        public int getPackedLength() {
            return 0;
        }
    }
}
