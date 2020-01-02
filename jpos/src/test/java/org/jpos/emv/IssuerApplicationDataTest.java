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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jpos.emv.IssuerApplicationData.Format;
import org.junit.jupiter.api.Test;

public class IssuerApplicationDataTest {

    @Test
    public void testConstructorWithInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IssuerApplicationData("9383");
        });
    }

    @Test
    public void testConstructorWithBlankArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IssuerApplicationData(new String(new char[64]));
        });
    }    

    @Test
    public void testConstructorWithNullArgument() {
        assertThrows(NullPointerException.class, () -> {
            new IssuerApplicationData((String) null);
        });
    }
    
    @Test
    public void testConstructorWithEmptyByteArray() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IssuerApplicationData(new byte[] {});
        });
    }
    
    @Test
    public void testToString() {
        String hexIAD = "0110250000044000DAC10000000000000000";
        IssuerApplicationData iad = new IssuerApplicationData(hexIAD);
        assertEquals(hexIAD, iad.toString());
    }

    @Test
    public void testMChip4_1() {
        String hexIAD = "0110250000044000DAC10000000000000000";
        IssuerApplicationData iad = new IssuerApplicationData(hexIAD);
        iad.dump(System.out, "");
        assertEquals(Format.M_CHIP, iad.getFormat());
        assertEquals("250000044000", iad.getCardVerificationResults(), "CVR matches.");
        assertEquals("01", iad.getDerivationKeyIndex(), "KDI matches.");
        assertEquals("10", iad.getCryptogramVersionNumber(), "CVN matches.");
        assertEquals("DAC1", iad.getDAC(), "DAC/ICC dynamic number matches.");
    }
    
    @Test
    public void testMChip4_2() {
        IssuerApplicationData iad = new IssuerApplicationData("0210A00000000000000000000000000000FF");
        iad.dump(System.out, "");
        assertEquals(Format.M_CHIP, iad.getFormat());
        assertEquals("A00000000000", iad.getCardVerificationResults(), "CVR matches.");
        assertEquals("00000000000000FF", iad.getCounters(), "Plaintext/Encrypted counters matches.");
        assertEquals("02", iad.getDerivationKeyIndex(), "KDI matches.");
        assertEquals("0000", iad.getDAC(), "DAC/ICC dynamic number matches.");

    }
    
    @Test
    public void testRandom() {
        IssuerApplicationData iad = new IssuerApplicationData("0110201009248400000000000000000029ff");
        iad.dump(System.out, "");    
        assertEquals("201009248400", iad.getCardVerificationResults(), "CVR matches.");
        assertEquals("00000000000029ff", iad.getCounters(), "Plaintext/Encrypted counters matches.");        
    }
        
    @Test
    public void testVIS15_1() {
        IssuerApplicationData iad = new IssuerApplicationData("06010A03A40000");
        iad.dump(System.out, "");
        assertEquals(Format.VIS, iad.getFormat());        
        assertEquals("03A40000", iad.getCardVerificationResults(), "CVR matches.");
        assertEquals("0A", iad.getCryptogramVersionNumber(), "CVN matches.");
        assertEquals("01", iad.getDerivationKeyIndex(), "KDI matches.");
    }
    
    @Test
    public void testVIS15_2() {
        IssuerApplicationData iad = new IssuerApplicationData(
                "1F43010020000000000000000007717300000000000000000000000000000000");
        iad.dump(System.out, "");
        assertEquals(Format.VIS, iad.getFormat());
        assertEquals("01", iad.getDerivationKeyIndex(), "KDI matches.");
        assertEquals("43", iad.getCryptogramVersionNumber(), "CVN matches.");
        assertEquals("0020000000", iad.getCardVerificationResults(), "CVR matches.");
        assertEquals("000000000007717300000000000000000000000000000000", iad.getIssuerDiscretionaryData(), "CVN matches.");
    }
    
    @Test
    public void testVIS15_3() {
        String hexIAD = "06010A03A020000F04000000000000000000006232E4F9";
        IssuerApplicationData iad = new IssuerApplicationData(hexIAD);
        iad.dump(System.out, "");
        assertEquals(Format.VIS, iad.getFormat());
        assertEquals("03A02000", iad.getCardVerificationResults(), "CVR matches.");
        assertEquals("01", iad.getDerivationKeyIndex(), "KDI matches.");
        assertEquals("0A", iad.getCryptogramVersionNumber(), "CVN matches.");
        assertEquals("00F04000000000000000000006232E4F9", iad.getIssuerDiscretionaryData(),
                "Issuer discretionary data matches.");
    }
        
    @Test
    public void testEMVFormatA() {
        IssuerApplicationData iad = new IssuerApplicationData("0FA506A231C0020000000000000000000F110601010000000000000000000000");
        iad.dump(System.out, "");
        assertEquals(Format.EMV_FORMAT_A, iad.getFormat());        
        assertEquals("06", iad.getDerivationKeyIndex(), "KDI matches.");        
        assertEquals("A231C00200", iad.getCardVerificationResults(), "CVR matches.");
        assertEquals("110601010000000000000000000000", iad.getIssuerDiscretionaryData(), "CVN matches.");
    }       
}