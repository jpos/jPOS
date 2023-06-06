/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;

/**
 * M/Chip Cryptogram Specification 
 * 
 * @author Rainer Reyes
 */
public class MChipCryptogram implements CryptogramSpec {

    private final CVNMCDataBuilder dataBuilder;
    private final SKDMethod skdMethod;


    public MChipCryptogram(String cryptogramVersionNumber) {
        byte data = (byte) Integer.parseInt(cryptogramVersionNumber, 16);
        this.dataBuilder = new CVNMCDataBuilder((data & 1) == 1); // byte 1 = 1
        // byte 3-2
        if ((data >> 1 & 0x03) == 0x00) { // bits = 00
            // byte 3-2 = 00
            skdMethod = SKDMethod.MCHIP;
        } else if ((data >> 1 & 0x03) == 0x02) { // bits = 10
            // byte 3-2 = 10
            skdMethod = SKDMethod.EMV_CSKD;
        } else {
            throw new IllegalArgumentException("Cryptogram version not supported");
        }
    }

    @Override
    public MKDMethod getMKDMethod() {
        return MKDMethod.OPTION_A;
    }

    @Override
    public SKDMethod getSKDMethod() {
        return skdMethod;
    }

    @Override
    public ARPCMethod getARPCMethod() {
        return ARPCMethod.METHOD_1;
    }

    @Override
    public CryptogramDataBuilder getDataBuilder() {
        return dataBuilder;
    }
}
