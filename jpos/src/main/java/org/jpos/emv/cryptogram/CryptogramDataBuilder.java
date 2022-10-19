package org.jpos.emv.cryptogram;

import org.jpos.emv.EMVStandardTagType;
import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Interface that provides methods to build strings for ARPC and ARQC generation
 *
 * @author Rainer Reyes
 */
public interface CryptogramDataBuilder {

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
     * Select necessary data elements and create the string used to generate the ARQC
     * <p>
     *
     * @param data ICC data received
     * @param iad  Issuer application Data
     * @return String used to generate the ARQC
     */
    String buildARQCRequest(TLVList data, IssuerApplicationData iad);

}
