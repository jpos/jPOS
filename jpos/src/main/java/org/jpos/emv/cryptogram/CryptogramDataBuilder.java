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

import org.jpos.emv.EMVStandardTagType;
import org.jpos.emv.IssuerApplicationData;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;

import java.util.*;


/**
 * Interface that provides methods to build strings for ARPC and ARQC generation
 *
 * @author Rainer Reyes
 */
public interface CryptogramDataBuilder {

    final PaddingMethod NO_PADDING = data -> data;

    /**
     * ISO/IEC 9797-1 padding method 1
     * for Block size 8,  n = 64
     */
    final PaddingMethod ISO9797Method1 = data -> data.isEmpty() ?
            "0000000000000000" :
            ISOUtil.zeropadRight(data, data.length() % 16 == 0 ? data.length() : data.length() + 16 - data.length() % 16);

    /**
     * ISO/IEC 9797-1 padding method 2
     * for Block size 8,  n = 64
     */
    final PaddingMethod ISO9797Method2 = data -> ISO9797Method1.apply(data + "80");

    /**
     * ISO/IEC 9797-1 padding method 3
     * for Block size 8,  n = 64
     */
    final PaddingMethod ISO9797Method3 = data -> {
        StringBuilder sb = new StringBuilder();
        String D = ISO9797Method1.apply(data);
        String Ld = ISOUtil.byte2hex(ISOUtil.int2byte(data.length() / 2));
        String Lp = ISO9797Method1.apply(Ld);
        Lp = Ld.length() % 16 == 0 ? "" : Lp.substring(Ld.length());
        return sb.append(Lp).append(Ld).append(D).toString();
    };


    /**
     * Method that selects the  minimum set of data elements recommended for
     * the generation of application cryptograms described in EMV Book 2 sec 8.1.1
     *
     * @param data ICC data
     * @return Minimum Set of Data Elements for Application Cryptogram Generation
     */
    static List<String> minimumSetOfDataElement(TLVList data) {
        return Arrays.asList(
                data.getString(EMVStandardTagType.AMOUNT_AUTHORISED_NUMERIC_0x9F02.getTagNumber()),
                Optional.ofNullable(data.getString(EMVStandardTagType.AMOUNT_OTHER_NUMERIC_0x9F03.getTagNumber()))
                        .orElse("000000000000"),
                data.getString(EMVStandardTagType.TERMINAL_COUNTRY_CODE_0x9F1A.getTagNumber()),
                data.getString(EMVStandardTagType.TERMINAL_VERIFICATION_RESULTS_0x95.getTagNumber()),
                data.getString(EMVStandardTagType.TRANSACTION_CURRENCY_CODE_0x5F2A.getTagNumber()),
                data.getString(EMVStandardTagType.TRANSACTION_DATE_0x9A.getTagNumber()),
                data.getString(EMVStandardTagType.TRANSACTION_TYPE_0x9C.getTagNumber()),
                data.getString(EMVStandardTagType.UNPREDICTABLE_NUMBER_0x9F37.getTagNumber()),
                data.getString(EMVStandardTagType.APPLICATION_INTERCHANGE_PROFILE_0x82.getTagNumber()),
                data.getString(EMVStandardTagType.APPLICATION_TRANSACTION_COUNTER_0x9F36.getTagNumber())
        );
    }


    /**
     * Method that returns default issuer response data (ARC or CSU)
     *
     * @param approved true if transaction was approved, otherwise false
     * @return String representing  default issuer response data that will be used to generate the ARPC
     */
    String getDefaultARPCRequest(boolean approved);

    /**
     * Select necessary data elements and create the string used to generate the ARQC with no padding
     * <p>
     *
     * @param data ICC data received
     * @param iad  Issuer application Data
     * @return String used to generate the ARQC
     */
    String buildARQCRequest(TLVList data, IssuerApplicationData iad);


    /**
     * Select necessary data elements and create the string used to generate the ARQC with padding
     * <p>
     *
     * @param data          ICC data received
     * @param iad           Issuer application Data
     * @param paddingMethod Padding method to use
     * @return String used to generate the ARQC
     */
    String buildARQCRequest_padded(TLVList data, IssuerApplicationData iad, PaddingMethod paddingMethod);

    /**
     * Padding Method Interface
     */
    interface PaddingMethod {
        String apply(String data);
    }
}
