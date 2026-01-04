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

package org.jpos.emv.cryptogram;

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Rainer Reyes
 */
class CVN18DataBuilderTest {
    private final CVN18DataBuilder builder = new CVN18DataBuilder();

    @Test
    void testBuildARQCRequest() {

        // Visa Smart Debit/Credit (VSDC) Contact & Contactless Issuer Implementation Guide - October 2018 - Page 172 H.1 Example 1


        TLVList data = new TLVList();
        data.append(0x9F02, "000000000100");
        data.append(0x9F03, "000000000000");
        data.append(0x9F1A, "0840");
        data.append(0x95, "0000000000");
        data.append(0x5F2A, "0840");
        data.append(0x9A, "181231");
        data.append(0x9C, "01");
        data.append(0x9F37, "ABCDEF10");
        data.append(0x82, "1800");
        data.append(0x9F36, "0001");
        data.append(0x9f10, "06011203000000");

        IssuerApplicationData iad = new IssuerApplicationData(data.getString(0x9f10));

        assertEquals(
                "00000000010000000000000008400000000000084018123101ABCDEF101800000106011203000000",
                builder.buildARQCRequest(data, iad)
        );
        assertEquals(
                "00000000010000000000000008400000000000084018123101ABCDEF1018000001060112030000008000000000000000",
                builder.buildARQCRequest_padded(data, iad)
        );
    }
}
