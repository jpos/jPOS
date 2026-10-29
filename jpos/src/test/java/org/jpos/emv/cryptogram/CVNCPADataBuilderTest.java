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

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CVNCPADataBuilderTest {
    private final CVNCPADataBuilder builder = new CVNCPADataBuilder();

    /**
     *  Test Data is based of EMV Issuer and Application Security Guidelines v3.0  Annex A.3.3 - Example of ARQC Generation
     */
    @Test
    void testBuildARQCRequest() {

        TLVList data = new TLVList();
        data.append(0x9F02, "000000010000");
        data.append(0x9F03, "000000001000");
        data.append(0x9F1A, "0840");
        data.append(0x95, "0000001080");
        data.append(0x5F2A, "0840");
        data.append(0x9A, "980704");
        data.append(0x9C, "00");
        data.append(0x9F37, "11111111");
        data.append(0x82, "5800");
        data.append(0x9F36, "3456");
        data.append(0x9f10, "0FA500A03800000000000000000000000F010000000000000000000000000000");

        IssuerApplicationData iad = new IssuerApplicationData(data.getString(0x9f10));

        assertEquals(
                "0000000100000000000010000840000000108008409807040011111111580034560FA500A03800000000000000000000000F010000000000000000000000000000",
                builder.buildARQCRequest(data, iad)
        );
        assertEquals(
                "0000000100000000000010000840000000108008409807040011111111580034560FA500A03800000000000000000000000F01000000000000000000000000000080000000000000",
                builder.buildARQCRequest_padded(data, iad)
        );
    }
}
