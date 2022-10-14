package org.jpos.emv.cryptogram;

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;

import static org.jpos.emv.cryptogram.CryptogramDataBuilder.minimumSetOfDataElement;

/**
 * 
 * Visa CVN 10 - Data Builder
 *
 * @author Rainer Reyes
 */
public class CVN10DataBuilder implements CryptogramDataBuilder {
    
    @Override
    public String getDefaultARPCRequest(boolean approved) {
        return approved ? "0000" : "9900";
    }


    @Override
    public String buildARQCRequest(TLVList data, IssuerApplicationData iad) {
        StringBuilder sb = new StringBuilder();
        minimumSetOfDataElement(data).stream().forEach(sb::append);
        sb.append(iad.getCardVerificationResults());
        return sb.toString();
    }
}
