/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import java.io.PrintStream;
import java.util.Objects;

import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

/**
 * CVR parser based on A.19 CVR, M/Chip 4 Issuer Guide to Debit and Credit Parameter Management, December 2004
 */
public class CVRMastercard implements Loggeable {

    private final byte[] cvr;

    /**
     * 
     * @param cvr Byte array containing the CVR value.
     */
    public CVRMastercard(byte[] cvr) {
        Objects.requireNonNull("cvr", "CVR cannot be null.");
        if (cvr.length != 6)
            throw new IllegalArgumentException(
                    String.format("Invalid length. Expected = 6, actual = %s.", cvr.length));
        this.cvr = cvr;
    }

    /**
     * 
     * @param cvr Hexadecimal string representation of the CVR value.
     */
    public CVRMastercard(String cvr) {
        Objects.requireNonNull("cvr", "CVR cannot be null.");
        cvr = cvr.trim();
        if (cvr.length() != 12)
            throw new IllegalArgumentException(
                    String.format("Invalid length. Expected = 12, actual = %s.", cvr.length()));
        this.cvr = ISOUtil.hex2byte(cvr);
    }

    public boolean aacReturnedInSecondGenerateAC() {
        return !isBitOn(cvr[0], 8) && !isBitOn(cvr[0], 7);
    }

    public boolean aacReturnedInFirstGenerateAC() {
        return !isBitOn(cvr[0], 6) && !isBitOn(cvr[0], 5);
    }

    public boolean tcReturnedInSecondGenerateAC() {
        return !isBitOn(cvr[0], 8) && isBitOn(cvr[0], 7);
    }

    public boolean arqcReturnedInFirstGenerateAC() {
        return isBitOn(cvr[0], 6) && !isBitOn(cvr[0], 5);
    }

    public boolean tcReturnedInFirstGenerateAC() {
        return !isBitOn(cvr[0], 6) && isBitOn(cvr[0], 5);
    }
        
    public boolean offlinePINVerificationPerformed() {
        return isBitOn(cvr[0], 3);
    }

    public boolean offlinePINVerificationNotPerformed() {
        return isBitOn(cvr[3], 6);
    }    
    
    public boolean ddaReturned() {
        return isBitOn(cvr[1], 8);
    }

    public boolean combinedDDAACGenerationReturnedInFirstGenerateAC() {
        return isBitOn(cvr[1], 7);
    }

    public boolean combinedDDAACGenerationReturnedInSecondGenerateAC() {
        return isBitOn(cvr[1], 6);
    }

    public boolean issuerAuthenticationFailed() {
        return isBitOn(cvr[4], 3);
    }

    public boolean scriptReceived() {
        return isBitOn(cvr[4], 2);
    }

    public boolean scriptFailed() {
        return isBitOn(cvr[4], 1);
    }    

    public boolean ciacDefaultSkippedOnCAT3() {
        return isBitOn(cvr[1], 4);
    }

    public boolean matchFoundInAdditionalCheckTable() {
        return isBitOn(cvr[5], 2);
    }

    public boolean noMatchFoundInAdditionalCheckTable() {
        return isBitOn(cvr[5], 1);
    }    

    public int rightNibbleOfScriptCounter() {
        StringBuilder sb = new StringBuilder();
        sb.append(isBitOn(cvr[2], 8) ? "1" : "0");
        sb.append(isBitOn(cvr[2], 7) ? "1" : "0");
        sb.append(isBitOn(cvr[2], 6) ? "1" : "0");
        sb.append(isBitOn(cvr[2], 5) ? "1" : "0");
        return Integer.parseInt(sb.toString(), 2);
    }

    public int rightNibbleOfPINTryCounter() {
        StringBuilder sb = new StringBuilder();
        sb.append(isBitOn(cvr[2], 4) ? "1" : "0");
        sb.append(isBitOn(cvr[2], 3) ? "1" : "0");
        sb.append(isBitOn(cvr[2], 2) ? "1" : "0");
        sb.append(isBitOn(cvr[2], 1) ? "1" : "0");
        return Integer.parseInt(sb.toString(), 2);
    }
    
    public boolean offlinePINVerificationFailed() {
        return isBitOn(cvr[0], 2);
    }

    public boolean ptlExceeded() {
        return isBitOn(cvr[0], 2);
    }
    
    public boolean internationalTransaction() {
        return isBitOn(cvr[3], 3);
    }

    public boolean domesticTransaction() {
        return isBitOn(cvr[3], 2);
    }
    
    public boolean terminalErroneouslyConsidersOfflinePINOK() {
        return isBitOn(cvr[3], 1);
    }

    public boolean lowerConsecutiveOfflineLimitExceeded() {
        return isBitOn(cvr[4], 8);
    }

    public boolean upperConsecutiveOfflineLimitExceeded() {
        return isBitOn(cvr[4], 7);
    }

    public boolean lowerCumulativeOfflineLimitExceeded() {
        return isBitOn(cvr[4], 6);
    }

    public boolean upperCumulativeOfflineLimitExceeded() {
        return isBitOn(cvr[4], 5);
    }    

    public boolean goOnlineOnNextTransactionSet() {
        return isBitOn(cvr[4], 4);
    }
         
    public boolean unableToGoOnline() {
        return isBitOn(cvr[3], 7);
    }    
    
    public boolean secondGenerateACNotRequested() {
        return isBitOn(cvr[0], 8) && !isBitOn(cvr[0], 7);
    }

    public boolean issuerAuthenticationPerformed() {
        return isBitOn(cvr[1], 5);
    }

    public boolean offlineEncryptedPINVerificationPerformed() {
        return isBitOn(cvr[0], 2);
    }

    private boolean isBitOn(byte value, int position) {
        return ((value >> (position - 1)) & 1) == 1;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        String inner2 = inner + "  ";
        StringBuilder sb = new StringBuilder();
        p.printf("%s<cvr-mastercard value='%s'>%s%n", indent, ISOUtil.hexString(cvr), sb.toString());

        p.printf("%sBYTE 1:%n", inner);
        if (aacReturnedInSecondGenerateAC())
            p.printf("%sACC RETURNED IN SECOND GENERATE AC%n", inner2);
        if (tcReturnedInSecondGenerateAC())
            p.printf("%sTC RETURNED IN SECOND GENERATE AC%n", inner2);
        if (secondGenerateACNotRequested())
            p.printf("%sSECOND GENERATE AC NOT REQUESTED%n", inner2);
        if (aacReturnedInFirstGenerateAC())
            p.printf("%sACC RETURNED IN FIRST GENERATE AC%n", inner2);
        if (tcReturnedInFirstGenerateAC())
            p.printf("%sTC RETURNED IN FIRST GENERATE AC%n", inner2);
        if (arqcReturnedInFirstGenerateAC())
            p.printf("%sARQC RETURNED IN FIRST GENERATE AC%n", inner2);
        if (offlinePINVerificationPerformed())
            p.printf("%sOFFLINE PIN VERIFICATION PERFORMED%n", inner2);
        if (offlineEncryptedPINVerificationPerformed())
            p.printf("%sOFFLINE ENCRYPTED PIN VERIFICATION PERFORMED%n", inner2);
            
        p.printf("%n%sBYTE 2:%n", inner);
        if (ddaReturned())
            p.printf("%sDDA RETURNED%n", inner2);
        if (combinedDDAACGenerationReturnedInFirstGenerateAC())
            p.printf("%sCOMBINED DDA/AC GENERATION RETURNED IN FIRST GENERATE AC%n", inner2);
        if (combinedDDAACGenerationReturnedInSecondGenerateAC())
            p.printf("%sCOMBINED DDA/AC GENERATION RETURNED IN SECOND GENERATE AC%n", inner2);
        if (issuerAuthenticationPerformed())
            p.printf("%sISSUER AUTHENTICATION PEFORMED%n", inner2);
        if (ciacDefaultSkippedOnCAT3())
            p.printf("%sCIAC-DEFAULT SKIPPED ON CAT3%n", inner2);            

        p.printf("%n%sBYTE 3:%n", inner);
        p.printf("%sRIGHT NIBBLE OF SCRIPT COUNTER = %s%n", inner2, rightNibbleOfScriptCounter());
        p.printf("%sRIGHT NIBBLE OF PIN TRY COUNTER = %s%n", inner2, rightNibbleOfPINTryCounter());

        p.printf("%n%sBYTE 4:%n", inner);               
        if (unableToGoOnline())
            p.printf("%sUNABLE TO GO ONLINE INDICATED%n", inner2);
        if (offlinePINVerificationNotPerformed())
            p.printf("%sOFFLINE PIN VERIFICATION NOT PERFORMED%n", inner2);
        if (offlinePINVerificationFailed())
            p.printf("%sOFFLINE PIN VERIFICATION FAILED%n", inner2);
        if (ptlExceeded())
            p.printf("%sPTL EXCEEDED%n", inner2);
        if (internationalTransaction())
            p.printf("%sINTERNATIONAL TRANSACTION%n", inner2);            
        if (domesticTransaction())
            p.printf("%sDOMESTIC TRANSACTION%n", inner2);            
        if (terminalErroneouslyConsidersOfflinePINOK())
            p.printf("%sTERMINAL ERRONEOUSLY CONSIDERS OFFLINE PIN OK%n", inner2);   

        p.printf("%n%sBYTE 5:%n", inner);
        if (lowerConsecutiveOfflineLimitExceeded())
            p.printf("%sLOWER CONSECUTIVE OFFLINE LIMIT EXCEEDED%n", inner2);   
        if (upperConsecutiveOfflineLimitExceeded())
            p.printf("%sUPPER CONSECUTIVE OFFLINE LIMIT EXCEEDED%n", inner2);   
        if (lowerCumulativeOfflineLimitExceeded())
            p.printf("%sLOWER CUMULATIVE OFFLINE LIMIT EXCEEDED%n", inner2);   
        if (upperCumulativeOfflineLimitExceeded())
            p.printf("%sUPPER CUMULATIVE OFFLINE LIMIT EXCEEDED%n", inner2);
        if (goOnlineOnNextTransactionSet())
            p.printf("%sGO ONLINE ON NEXT TRANSACTION WAS SET%n", inner2);
        if (issuerAuthenticationFailed())
            p.printf("%sISSUER AUTHENTICATION FAILED%n", inner2);
        if (scriptReceived())
            p.printf("%sSCRIPT RECEIVED%n", inner2);
        if (scriptFailed())
            p.printf("%sSCRIPT FAILED%n", inner2);
            
        p.printf("%n%sBYTE 6:%n", inner);
        if (matchFoundInAdditionalCheckTable())
            p.printf("%sMATCH FOUND IN ADDITIONAL CHECK TABLE%n", inner2);
        if (noMatchFoundInAdditionalCheckTable())
            p.printf("%sNO MATCH FOUND IN ADDITIONAL CHECK TABLE%n", inner2);          
            
        p.printf("%s</cvr-mastercard>%n", indent);        
    }
}