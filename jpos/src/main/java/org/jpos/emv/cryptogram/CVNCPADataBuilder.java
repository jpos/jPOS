package org.jpos.emv.cryptogram;

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;
import static org.jpos.emv.cryptogram.CryptogramDataBuilder.minimumSetOfDataElement;

public class CVNCPADataBuilder implements CryptogramDataBuilder {

    @Override
    public String getDefaultARPCRequest(boolean approved) {
        return approved ? "3030" : "3031";
    }

    @Override
    public String buildARQCRequest(TLVList data, IssuerApplicationData iad) {
        StringBuilder sb = new StringBuilder();
        minimumSetOfDataElement(data).stream().forEach(sb::append);
        sb.append(iad.toString());
        return sb.toString();
    }

    @Override
    public String buildARQCRequest_padded(TLVList data, IssuerApplicationData iad, PaddingMethod paddingMethod) {
        return paddingMethod.apply(buildARQCRequest(data, iad));
    }
}
