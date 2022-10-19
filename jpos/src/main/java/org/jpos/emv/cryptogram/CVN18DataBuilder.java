package org.jpos.emv.cryptogram;

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;

import static org.jpos.emv.cryptogram.CryptogramDataBuilder.minimumSetOfDataElement;

/**
 * Visa CVN 18 - Data Builder
 * @author Rainer Reyes
 */
public class CVN18DataBuilder implements CryptogramDataBuilder {
    
    @Override
    public String getDefaultARPCRequest(boolean approved) {
        /* for success:
         * 00830000
         * Byte 6, bit 1: Issuer Approves Online Transaction
         * Byte 6, bits 7–8: Update Counters:
         * • 11 = Add transaction to offline counter
         */
        return approved ? "00830000" : "00000000";
    }

    @Override
    public String buildARQCRequest(TLVList data, IssuerApplicationData iad) {
        StringBuilder sb = new StringBuilder();
        minimumSetOfDataElement(data).stream().forEach(sb::append);
        sb.append(iad.toString());
        return sb.toString();
    }
}
