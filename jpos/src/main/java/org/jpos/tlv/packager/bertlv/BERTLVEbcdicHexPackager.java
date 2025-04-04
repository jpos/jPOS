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

package org.jpos.tlv.packager.bertlv;


import org.jpos.iso.BinaryInterpreter;
import org.jpos.iso.EbcdicHexInterpreter;
import org.jpos.iso.ISOException;


/**
 * Packager for BER TLV values. This packager does not require sub-field packagers
 *
 * @author Vishnu Pillai
 */

public class BERTLVEbcdicHexPackager extends DefaultICCBERTLVPackager {

    public BERTLVEbcdicHexPackager() throws ISOException {
        super();
    }

    @Override
    protected BinaryInterpreter getTagInterpreter() {
        return EbcdicHexInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getLengthInterpreter() {
        return EbcdicHexInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getValueInterpreter() {
        return EbcdicHexInterpreter.INSTANCE;
    }
}
