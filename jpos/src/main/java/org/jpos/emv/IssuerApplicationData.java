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

package org.jpos.emv;

import org.jpos.emv.cryptogram.MChipCryptogram;
import org.jpos.emv.cryptogram.CryptogramSpec;
import org.jpos.emv.cryptogram.VISACryptogram;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Issuer Application Data parser (IAD, tag 0x9F10) with support for the following formats:
 * 
 * - VIS 1.5
 * - M/CHIP 4
 * - EMV Format A (as per the EMV 4.3 Book 3 spec)
 */
public final class IssuerApplicationData implements Loggeable {

    private final String iad;
    private String cvn;
    private String cvr;
    private String dki;
    private String idd;
    private String dac;
    private String counters;
    private String lastOnlineATC;
    private Format format;
    private String cci;

    private CryptogramSpec cryptogramSpec = null;
    /**
     * 
     * @param hexIAD Hexadecimal (String) representation of the IAD.
     */
    public IssuerApplicationData(String hexIAD) {

        Objects.requireNonNull(hexIAD, "IAD data cannot be null.");

        iad = hexIAD.trim();

        if (iad.length() < 14)
            throw new IllegalArgumentException("Invalid IAD length.");

        format = Format.UNKNOWN;

        int len = iad.length() / 2; // length in bytes

        if (Arrays.asList(18, 20, 26, 28).contains(len)) {
            //Therefore, the length of the Issuer Application Data is 18 bytes and for 
            // M/Chip Advance it may be 18, 20, 26, or 28.
            unpackMCHIP(iad);
        } else if (len == 32 && iad.startsWith("0F") && iad.startsWith("0F", 32)) {
            // EMV_v4.3_Book_3
            // C7.2 Issuer Application Data for Format Code 'A'
            unpackEMVFormatA(iad);
        } else if ((len >= 7 && len <= 23) || len == 32) {
            // IAD Format 0/1/3 or IAD Format 2
            unpackVIS(iad);
        } else {
            unpackOther(iad);
        }
    }

    /**
     * 
     * @param iad Byte array representation of the IAD.
     */
    public IssuerApplicationData(byte[] iad) {

        this(ISOUtil.byte2hex(Objects.requireNonNull(iad, "IAD data cannot be null.")));
    }

    private void unpackMCHIP(String data) {
        
        int length = data.length();
        
        format = Format.M_CHIP;
        dki = data.substring(0, 2);
        cvn = data.substring(2, 4);
        cryptogramSpec = new MChipCryptogram(cvn);
        cvr = data.substring(4, 16);
        dac = data.substring(16, 20);

        //Counters (Plaintext or Encrypted)/Accumulators
        if (length <= 40) { //20 bytes
            counters = data.substring(20, 36);
        } else {
            counters = data.substring(20, 52);
        }

        // Last Online ATC (Only for M/Chip Advance)
        if (length == 40) {
            lastOnlineATC = data.substring(36, 40);
        } else if (length == 56) {
            lastOnlineATC = data.substring(52, 56);
        }
    }

    private void unpackVIS(String iad) {

        format = Format.VIS;
        boolean format2 = iad.length() == 64;

        if (format2) {
            cvn = iad.substring(2, 4);
            dki = iad.substring(4, 6);
            cvr = iad.substring(6, 16);
            idd = iad.substring(16);
        }
        else {
            dki = iad.substring(2, 4);
            cvn = iad.substring(4, 6);
            cvr = iad.substring(6, 14);

            if (iad.length() > 14)
                idd = iad.substring(13);
        }
        cryptogramSpec = new VISACryptogram(cvn);
    }

    private void unpackEMVFormatA(String data) {

        format = Format.EMV_FORMAT_A;
        cci = data.substring(2, 4);
        dki = data.substring(4, 6);
        cvr = data.substring(6, 16);
        idd = data.substring(34);
    }  

    private void unpackOther(String data) {

        format = Format.OTHER;
        dki = data.substring(2, 4);
        cvn = data.substring(4, 6);

        if (data.length() == 64) {            
            String bridge = dki;
            dki = cvn;
            cvn = bridge;            
        }

        int cvrLength = Integer.parseInt(data.substring(0, 2), 16);
        cvr = data.substring(8, 8 + cvrLength);

        if ((8 + 2 + cvrLength) < data.length())
            idd = data.substring(8 + 2 + cvrLength);        
    }    

    /**
     * 
     * @return A hexadecimal representation of the Derivation Key Index (DKI).
     */
    public String getDerivationKeyIndex() {
        return dki;
    }

    /**
     * 
     * @return A hexadecimal representation of the Cryptogram Version Number (CVN).
     */
    public String getCryptogramVersionNumber() {
        return cvn;
    }

    /**
     * 
     * @return A hexadecimal representation of the Common Core Identifier (CCI)
     */
    public String getCommonCoreIdentifier() {
        return cci;
    }
    
    /**
     * 
     * @return A hexadecimal representation of the Card Verification Results (CVR).
     */
    public String getCardVerificationResults() {
        return cvr;
    }

    /**
     * 
     * @return A hexadecimal representation of the DAC/ICC dynamic number.
     */
    public String getDAC() {
        return dac;
    }

    /**
     * 
     * @return A hexadecimal representation of multiple counters, depending on the format.
     */
    public String getCounters() {
        return counters;
    }

    /**
     * 
     * @return The format of the IAD.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * 
     * @return The Isser Discretionary Data (IDD).
     */
    public String getIssuerDiscretionaryData() {
        return idd;
    }
    
    public String getLastOnlineATC() {
        return lastOnlineATC;
    }

    public CryptogramSpec getCryptogramSpec() {
        return Objects.requireNonNull(cryptogramSpec, "CryptogramSpec not implemented");
    }
    @Override
    public String toString() {
        return iad;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        
        String inner = indent + "  ";
        
        p.printf("%s<iad format='%s' value='%s'>", indent, getFormat().toString(), iad);

        if (cci != null)
            p.printf("%n%sCommon core identifier: '%s'", inner, getCommonCoreIdentifier());

        if (dki != null)
            p.printf("%n%sKey derivation index: '%s'", inner, getDerivationKeyIndex());

        if (cvn != null)
            p.printf("%n%sCryptogram version number: '%s'", inner, getCryptogramVersionNumber());

        if (cvr != null)
            p.printf("%n%sCard verification results: '%s'", inner, getCardVerificationResults());

        if (idd != null)
            p.printf("%n%sIssuer discretionary data: '%s'", inner, getIssuerDiscretionaryData());            

        if (dac != null)
            p.printf("%n%sDAC/ICC dynamic number: '%s'", inner, getDAC());            

        if (counters != null)
            p.printf("%n%sPlaintext/Encrypted counters: '%s'", inner, getCounters());
        if (lastOnlineATC != null)
            p.printf("%n%sLast ATC Online: '%s'", inner, getLastOnlineATC());
        p.printf("%n%s</iad>%n", indent);  
    }

    public enum Format {
        UNKNOWN,
        OTHER,
        M_CHIP,
        EMV_FORMAT_A,
        VIS
    }
}
