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
import org.bouncycastle.util.Arrays;
import org.jpos.iso.*;
import org.jpos.tlv.ISOTaggedField;

import org.junit.jupiter.api.*;

public class Bug568BERTLVAsciiHexPackager {

    final String HEX_ASCII = "9f2610e18d2a69a45dd09a9f360401839f3708ab34b0bd";

    @Test
    public void testLenInterpretation() throws ISOException {
        final ISOMsg ICC_DATA = new ISOMsg(55);
        final BERTLVAsciiHexPackager packager = new BERTLVAsciiHexPackager();
        final IFA_LLABINARY fieldPackager = new IFA_LLABINARY();


        packager.setFieldPackager(new org.jpos.iso.ISOFieldPackager[]{fieldPackager});
        ICC_DATA.setPackager(packager);

        ICC_DATA.unpack(HEX_ASCII.getBytes());

        ICC_DATA.getChildren().values().forEach
                (e -> {
                    try {
                        ISOTaggedField field = (ISOTaggedField) e;
                        final byte[] value = (byte[]) field.getValue();
                        System.out.println("tag " + field.getTag() + " value " + ISOUtil.hexString(value));
                        Assertions.
                                assertFalse(Arrays.isNullOrEmpty(value));

                    } catch (ISOException ex) {
                        throw new RuntimeException(ex);
                    }
                });


    }

}

