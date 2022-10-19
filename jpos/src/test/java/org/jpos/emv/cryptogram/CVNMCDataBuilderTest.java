package org.jpos.emv.cryptogram;

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Rainer Reyes
 */
class CVNMCDataBuilderTest {

    @Test
    void buildARPCRequest() {

        CVNMCDataBuilder builder = new CVNMCDataBuilder(false);

        TLVList data = new TLVList()
                .append(0x9f02, "000000010000")
                .append(0x9f03, "000000001000")
                .append(0x9f1A, "0840")
                .append(0x95, "0000001080")
                .append(0x5f2A, "0840")
                .append(0x9a, "980704")
                .append(0x9c, "00")
                .append(0x9f37, "11111111")
                .append(0x82, "5800")
                .append(0x9f36, "3456")
                .append(0x9f10, "0110608003220000B6A400000000000000FF");

        IssuerApplicationData iad = new IssuerApplicationData(data.getString(0x9f10));

        assertEquals(
                "000000010000000000001000084000000010800840980704001111111158003456608003220000",
                builder.buildARQCRequest(data, iad)
        );

    }


}
