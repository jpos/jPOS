package org.jpos.emv.cryptogram;

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;

/**
 * M/CHIP Data builder
 *
 * @author Rainer Reyes
 */
public class CVNMCDataBuilder implements CryptogramDataBuilder {
    // counter is used only in M/CHIP advance
    private final boolean includeCounters;

    public CVNMCDataBuilder(boolean includeCounters) {
        this.includeCounters = includeCounters;
    }


    @Override
    public String buildARQCRequest(TLVList data, IssuerApplicationData iad) {

        StringBuilder sb = new StringBuilder();
        CryptogramDataBuilder.minimumSetOfDataElement(data).stream().forEach(sb::append);
        sb.append(iad.getCardVerificationResults());

        if (includeCounters) {
            sb.append(iad.getCounters());
        }
        return sb.toString();
    }

    @Override
    public String getDefaultARPCRequest(boolean approved) {
        return approved ? "0012" : "9900";
    }
    
}
