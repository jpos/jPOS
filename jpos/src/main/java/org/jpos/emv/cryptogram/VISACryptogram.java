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

package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;

/**
 * VISA Cryptogram Specification
 *
 * @author Rainer Reyes
 */
public class VISACryptogram implements CryptogramSpec {

    private final Integer number;
    private final CryptogramDataBuilder dataBuilder;
    private final ARPCMethod arpcMethod;
    private final SKDMethod skdMethod;

    public VISACryptogram(String cryptogramVersionNumber) {
        this.number = Integer.parseInt(cryptogramVersionNumber, 16);
        if (number == 10) {
            this.dataBuilder = new CVN10DataBuilder();
            this.arpcMethod = ARPCMethod.METHOD_1;
            this.skdMethod = SKDMethod.VSDC;
        } else if (number == 18) { // CVN 18
            this.dataBuilder = new CVN18DataBuilder();
            this.arpcMethod = ARPCMethod.METHOD_2;
            this.skdMethod = SKDMethod.EMV_CSKD;
        } else if (number == 34) {//CVN '22'
            this.dataBuilder = new CVN22DataBuilder();
            this.arpcMethod = ARPCMethod.METHOD_2;
            this.skdMethod = SKDMethod.EMV_CSKD;
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
        return arpcMethod;
    }

    @Override
    public CryptogramDataBuilder getDataBuilder() {
        return dataBuilder;
    }
}
