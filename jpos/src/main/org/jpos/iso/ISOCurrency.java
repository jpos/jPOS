/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.iso;

import java.util.*;
import org.jpos.iso.ISOUtil;

/**
 * ISO Currency Conversion package 
 * @author salaman@teknos.com
 * @version $Id$
 */
public class ISOCurrency
{
    private static Hashtable currencies;

    static
    {
        currencies=new Hashtable();
        currencies.put("MDL",new Currency("MDL",498,2));
        currencies.put("498",new Currency("MDL",498,2));
        currencies.put("KZT",new Currency("KZT",398,2));
        currencies.put("398",new Currency("KZT",398,2));
        currencies.put("MNT",new Currency("MNT",496,2));
        currencies.put("496",new Currency("MNT",496,2));
        currencies.put("CVE",new Currency("CVE",132,2));
        currencies.put("132",new Currency("CVE",132,2));
        currencies.put("BGL",new Currency("BGL",100,2));
        currencies.put("100",new Currency("BGL",100,2));
        currencies.put("LTL",new Currency("LTL",440,2));
        currencies.put("440",new Currency("LTL",440,2));
        currencies.put("STD",new Currency("STD",678,0));
        currencies.put("678",new Currency("STD",678,0));
        currencies.put("TND",new Currency("TND",788,3));
        currencies.put("788",new Currency("TND",788,3));
        currencies.put("KES",new Currency("KES",404,2));
        currencies.put("404",new Currency("KES",404,2));
        currencies.put("AMD",new Currency("AMD",051,2));
        currencies.put("051",new Currency("AMD",051,2));
        currencies.put("SIT",new Currency("SIT",705,2));
        currencies.put("705",new Currency("SIT",705,2));
        currencies.put("CAD",new Currency("CAD",124,2));
        currencies.put("124",new Currency("CAD",124,2));
        currencies.put("IQD",new Currency("IQD",368,0));
        currencies.put("368",new Currency("IQD",368,0));
        currencies.put("MXN",new Currency("MXN",484,2));
        currencies.put("484",new Currency("MXN",484,2));
        currencies.put("DZD",new Currency("DZD",12,0));
        currencies.put("012",new Currency("DZD",12,0));
        currencies.put("ROL",new Currency("ROL",642,2));
        currencies.put("642",new Currency("ROL",642,2));
        currencies.put("AWG",new Currency("AWG",533,0));
        currencies.put("533",new Currency("AWG",533,0));
        currencies.put("DEM",new Currency("DEM",280,2));
        currencies.put("280",new Currency("DEM",280,2));
        currencies.put("ETB",new Currency("ETB",230,2));
        currencies.put("230",new Currency("ETB",230,2));
        currencies.put("UGX",new Currency("UGX",800,2));
        currencies.put("800",new Currency("UGX",800,2));
        currencies.put("GHC",new Currency("GHC",288,2));
        currencies.put("288",new Currency("GHC",288,2));
        currencies.put("CUP",new Currency("CUP",192,2));
        currencies.put("192",new Currency("CUP",192,2));
        currencies.put("DOP",new Currency("DOP",214,2));
        currencies.put("214",new Currency("DOP",214,2));
        currencies.put("TMM",new Currency("TMM",795,2));
        currencies.put("795",new Currency("TMM",795,2));
        currencies.put("GRD",new Currency("GRD",300,0));
        currencies.put("300",new Currency("GRD",300,0));
        currencies.put("ALL",new Currency("ALL",8,2));
        currencies.put("008",new Currency("ALL",8,2));
        currencies.put("YDD",new Currency("YDD",720,2));
        currencies.put("720",new Currency("YDD",720,2));
        currencies.put("NGN",new Currency("NGN",566,2));
        currencies.put("566",new Currency("NGN",566,2));
        currencies.put("MMK",new Currency("MMK",104,2));
        currencies.put("104",new Currency("MMK",104,2));
        currencies.put("ESP",new Currency("ESP",724,0));
        currencies.put("724",new Currency("ESP",724,0));
        currencies.put("KYD",new Currency("KYD",136,2));
        currencies.put("136",new Currency("KYD",136,2));
        currencies.put("SHP",new Currency("SHP",654,2));
        currencies.put("654",new Currency("SHP",654,2));
        currencies.put("VUV",new Currency("VUV",548,0));
        currencies.put("548",new Currency("VUV",548,0));
        currencies.put("TWD",new Currency("TWD",901,2));
        currencies.put("901",new Currency("TWD",901,2));
        currencies.put("MWK",new Currency("MWK",454,2));
        currencies.put("454",new Currency("MWK",454,2));
        currencies.put("PEN",new Currency("PEN",604,2));
        currencies.put("604",new Currency("PEN",604,2));
        currencies.put("ZRN",new Currency("ZRN",180,2));
        currencies.put("180",new Currency("ZRN",180,2));
        currencies.put("IEP",new Currency("IEP",372,2));
        currencies.put("372",new Currency("IEP",372,2));
        currencies.put("GQE",new Currency("GQE",226,2));
        currencies.put("226",new Currency("GQE",226,2));
        currencies.put("AAA",new Currency("AAA",000,0));
        currencies.put("000",new Currency("AAA",000,0));
        currencies.put("SRG",new Currency("SRG",740,2));
        currencies.put("740",new Currency("SRG",740,2));
        currencies.put("BZD",new Currency("BZD",84,2));
        currencies.put("084",new Currency("BZD",84,2));
        currencies.put("HKD",new Currency("HKD",344,2));
        currencies.put("344",new Currency("HKD",344,2));
        currencies.put("BEF",new Currency("BEF",56,0));
        currencies.put("056",new Currency("BEF",56,0));
        currencies.put("LRD",new Currency("LRD",430,2));
        currencies.put("430",new Currency("LRD",430,2));
        currencies.put("HUF",new Currency("HUF",348,2));
        currencies.put("348",new Currency("HUF",348,2));
        currencies.put("MVR",new Currency("MVR",462,2));
        currencies.put("462",new Currency("MVR",462,2));
        currencies.put("NPR",new Currency("NPR",524,2));
        currencies.put("524",new Currency("NPR",524,2));
        currencies.put("UZS",new Currency("UZS",860,2));
        currencies.put("860",new Currency("UZS",860,2));
        currencies.put("PYG",new Currency("PYG",600,0));
        currencies.put("600",new Currency("PYG",600,0));
        currencies.put("AUD",new Currency("AUD",036,2));
        currencies.put("036",new Currency("AUD",036,2));
        currencies.put("BOB",new Currency("BOB",68,2));
        currencies.put("068",new Currency("BOB",68,2));
        currencies.put("IDR",new Currency("IDR",360,2));
        currencies.put("360",new Currency("IDR",360,2));
        currencies.put("SGD",new Currency("SGD",702,2));
        currencies.put("702",new Currency("SGD",702,2));
        currencies.put("EGP",new Currency("EGP",818,2));
        currencies.put("818",new Currency("EGP",818,2));
        currencies.put("KMF",new Currency("KMF",174,0));
        currencies.put("174",new Currency("KMF",174,0));
        currencies.put("BDT",new Currency("BDT",050,2));
        currencies.put("050",new Currency("BDT",050,2));
        currencies.put("CSK",new Currency("CSK",200,2));
        currencies.put("200",new Currency("CSK",200,2));
        currencies.put("RWF",new Currency("RWF",646,0));
        currencies.put("646",new Currency("RWF",646,0));
        currencies.put("MAD",new Currency("MAD",504,2));
        currencies.put("504",new Currency("MAD",504,2));
        currencies.put("BYB",new Currency("BYB",112,0));
        currencies.put("112",new Currency("BYB",112,0));
        currencies.put("INR",new Currency("INR",356,2));
        currencies.put("356",new Currency("INR",356,2));
        currencies.put("NZD",new Currency("NZD",554,2));
        currencies.put("554",new Currency("NZD",554,2));
        currencies.put("ATS",new Currency("ATS",40,2));
        currencies.put("040",new Currency("ATS",40,2));
        currencies.put("FKP",new Currency("FKP",238,2));
        currencies.put("238",new Currency("FKP",238,2));
        currencies.put("KWD",new Currency("KWD",414,3));
        currencies.put("414",new Currency("KWD",414,3));
        currencies.put("HTG",new Currency("HTG",332,2));
        currencies.put("332",new Currency("HTG",332,2));
        currencies.put("MUR",new Currency("MUR",480,2));
        currencies.put("480",new Currency("MUR",480,2));
        currencies.put("MKD",new Currency("MKD",807,2));
        currencies.put("807",new Currency("MKD",807,2));
        currencies.put("UYU",new Currency("UYU",858,2));
        currencies.put("858",new Currency("UYU",858,2));
        currencies.put("GEK",new Currency("GEK",268,0));
        currencies.put("268",new Currency("GEK",268,0));
        currencies.put("NOK",new Currency("NOK",578,2));
        currencies.put("578",new Currency("NOK",578,2));
        currencies.put("CHF",new Currency("CHF",756,2));
        currencies.put("756",new Currency("CHF",756,2));
        currencies.put("BND",new Currency("BND",96,2));
        currencies.put("096",new Currency("BND",96,2));
        currencies.put("CRC",new Currency("CRC",188,2));
        currencies.put("188",new Currency("CRC",188,2));
        currencies.put("SZL",new Currency("SZL",748,2));
        currencies.put("748",new Currency("SZL",748,2));
        currencies.put("GYD",new Currency("GYD",328,2));
        currencies.put("328",new Currency("GYD",328,2));
        currencies.put("TTD",new Currency("TTD",780,2));
        currencies.put("780",new Currency("TTD",780,2));
        currencies.put("MTL",new Currency("MTL",470,3));
        currencies.put("470",new Currency("MTL",470,3));
        currencies.put("YUN",new Currency("YUN",890,0));
        currencies.put("890",new Currency("YUN",890,0));
        currencies.put("SEK",new Currency("SEK",752,2));
        currencies.put("752",new Currency("SEK",752,2));
        currencies.put("FJD",new Currency("FJD",242,2));
        currencies.put("242",new Currency("FJD",242,2));
        currencies.put("PLZ",new Currency("PLZ",616,2));
        currencies.put("616",new Currency("PLZ",616,2));
        currencies.put("BMD",new Currency("BMD",60,2));
        currencies.put("060",new Currency("BMD",60,2));
        currencies.put("BWP",new Currency("BWP",72,2));
        currencies.put("072",new Currency("BWP",72,2));
        currencies.put("SOS",new Currency("SOS",706,2));
        currencies.put("706",new Currency("SOS",706,2));
        currencies.put("DKK",new Currency("DKK",208,2));
        currencies.put("208",new Currency("DKK",208,2));
        currencies.put("GNF",new Currency("GNF",324,0));
        currencies.put("324",new Currency("GNF",324,0));
        currencies.put("PLN",new Currency("PLN",985,2));
        currencies.put("985",new Currency("PLN",985,2));
        currencies.put("EEK",new Currency("EEK",233,2));
        currencies.put("233",new Currency("EEK",233,2));
        currencies.put("ILS",new Currency("ILS",376,2));
        currencies.put("376",new Currency("ILS",376,2));
        currencies.put("SYP",new Currency("SYP",760,2));
        currencies.put("760",new Currency("SYP",760,2));
        currencies.put("ARS",new Currency("ARS",32,2));
        currencies.put("032",new Currency("ARS",32,2));
        currencies.put("HRK",new Currency("HRK",191,2));
        currencies.put("191",new Currency("HRK",191,2));
        currencies.put("JPY",new Currency("JPY",392,0));
        currencies.put("392",new Currency("JPY",392,0));
        currencies.put("BBD",new Currency("BBD",52,2));
        currencies.put("052",new Currency("BBD",52,2));
        currencies.put("FIM",new Currency("FIM",246,2));
        currencies.put("246",new Currency("FIM",246,2));
        currencies.put("XPF",new Currency("XPF",953,0));
        currencies.put("953",new Currency("XPF",953,0));
        currencies.put("SDP",new Currency("SDP",736,2));
        currencies.put("736",new Currency("SDP",736,2));
        currencies.put("XEU",new Currency("XEU",954,2));
        currencies.put("954",new Currency("XEU",954,2));
        currencies.put("LYD",new Currency("LYD",434,3));
        currencies.put("434",new Currency("LYD",434,3));
        currencies.put("SDA",new Currency("SDA",737,2));
        currencies.put("737",new Currency("SDA",737,2));
        currencies.put("PKR",new Currency("PKR",586,2));
        currencies.put("586",new Currency("PKR",586,2));
        currencies.put("PAB",new Currency("PAB",590,2));
        currencies.put("590",new Currency("PAB",590,2));
        currencies.put("GMD",new Currency("GMD",270,2));
        currencies.put("270",new Currency("GMD",270,2));
        currencies.put("GWP",new Currency("GWP",624,2));
        currencies.put("624",new Currency("GWP",624,2));
        currencies.put("DJF",new Currency("DJF",262,0));
        currencies.put("262",new Currency("DJF",262,0));
        currencies.put("THB",new Currency("THB",764,2));
        currencies.put("764",new Currency("THB",764,2));
        currencies.put("CZK",new Currency("CZK",203,2));
        currencies.put("203",new Currency("CZK",203,2));
        currencies.put("TRL",new Currency("TRL",792,2));
        currencies.put("792",new Currency("TRL",792,2));
        currencies.put("GBP",new Currency("GBP",826,2));
        currencies.put("826",new Currency("GBP",826,2));
        currencies.put("XOF",new Currency("XOF",952,0));
        currencies.put("952",new Currency("XOF",952,0));
        currencies.put("SCR",new Currency("SCR",690,2));
        currencies.put("690",new Currency("SCR",690,2));
        currencies.put("MRO",new Currency("MRO",478,2));
        currencies.put("478",new Currency("MRO",478,2));
        currencies.put("ZMK",new Currency("ZMK",894,2));
        currencies.put("894",new Currency("ZMK",894,2));
        currencies.put("NLG",new Currency("NLG",528,2));
        currencies.put("528",new Currency("NLG",528,2));
        currencies.put("COP",new Currency("COP",170,2));
        currencies.put("170",new Currency("COP",170,2));
        currencies.put("ECS",new Currency("ECS",218,2));
        currencies.put("218",new Currency("ECS",218,2));
        currencies.put("JOD",new Currency("JOD",400,3));
        currencies.put("400",new Currency("JOD",400,3));
        currencies.put("FRF",new Currency("FRF",250,2));
        currencies.put("250",new Currency("FRF",250,2));
        currencies.put("UAK",new Currency("UAK",804,2));
        currencies.put("804",new Currency("UAK",804,2));
        currencies.put("CYP",new Currency("CYP",196,2));
        currencies.put("196",new Currency("CYP",196,2));
        currencies.put("ZWD",new Currency("ZWD",716,2));
        currencies.put("716",new Currency("ZWD",716,2));
        currencies.put("MGF",new Currency("MGF",450,0));
        currencies.put("450",new Currency("MGF",450,0));
        currencies.put("AFA",new Currency("AFA",4,0));
        currencies.put("004",new Currency("AFA",4,0));
        currencies.put("NAD",new Currency("NAD",516,2));
        currencies.put("516",new Currency("NAD",516,2));
        currencies.put("CNY",new Currency("CNY",156,2));
        currencies.put("156",new Currency("CNY",156,2));
        currencies.put("KHR",new Currency("KHR",116,2));
        currencies.put("116",new Currency("KHR",116,2));
        currencies.put("PTE",new Currency("PTE",620,0));
        currencies.put("620",new Currency("PTE",620,0));
        currencies.put("ITL",new Currency("ITL",380,0));
        currencies.put("380",new Currency("ITL",380,0));
        currencies.put("LBP",new Currency("LBP",422,2));
        currencies.put("422",new Currency("LBP",422,2));
        currencies.put("VEB",new Currency("VEB",862,2));
        currencies.put("862",new Currency("VEB",862,2));
        currencies.put("AZM",new Currency("AZM",31,0));
        currencies.put("031",new Currency("AZM",31,0));
        currencies.put("SBD",new Currency("SBD",90,2));
        currencies.put("090",new Currency("SBD",90,2));
        currencies.put("KRW",new Currency("KRW",410,0));
        currencies.put("410",new Currency("KRW",410,0));
        currencies.put("SLL",new Currency("SLL",694,2));
        currencies.put("694",new Currency("SLL",694,2));
        currencies.put("WST",new Currency("WST",882,2));
        currencies.put("882",new Currency("WST",882,2));
        currencies.put("XCD",new Currency("XCD",951,2));
        currencies.put("951",new Currency("XCD",951,2));
        currencies.put("ZAR",new Currency("ZAR",710,2));
        currencies.put("710",new Currency("ZAR",710,2));
        currencies.put("AED",new Currency("AED",784,2));
        currencies.put("784",new Currency("AED",784,2));
        currencies.put("AON",new Currency("AON",24,0));
        currencies.put("024",new Currency("AON",24,0));
        currencies.put("SAR",new Currency("SAR",682,2));
        currencies.put("682",new Currency("SAR",682,2));
        currencies.put("TZS",new Currency("TZS",834,2));
        currencies.put("834",new Currency("TZS",834,2));
        currencies.put("LVL",new Currency("LVL",428,2));
        currencies.put("428",new Currency("LVL",428,2));
        currencies.put("SVC",new Currency("SVC",222,2));
        currencies.put("222",new Currency("SVC",222,2));
        currencies.put("TPE",new Currency("TPE",626,0));
        currencies.put("626",new Currency("TPE",626,0));
        currencies.put("KGS",new Currency("KGS",417,2));
        currencies.put("417",new Currency("KGS",417,2));
        currencies.put("BIF",new Currency("BIF",108,0));
        currencies.put("108",new Currency("BIF",108,0));
        currencies.put("ISK",new Currency("ISK",352,2));
        currencies.put("352",new Currency("ISK",352,2));
        currencies.put("LAK",new Currency("LAK",418,0));
        currencies.put("418",new Currency("LAK",418,0));
        currencies.put("MZM",new Currency("MZM",508,2));
        currencies.put("508",new Currency("MZM",508,2));
        currencies.put("JMD",new Currency("JMD",388,2));
        currencies.put("388",new Currency("JMD",388,2));
        currencies.put("PHP",new Currency("PHP",608,2));
        currencies.put("608",new Currency("PHP",608,2));
        currencies.put("SKK",new Currency("SKK",703,2));
        currencies.put("703",new Currency("SKK",703,2));
        currencies.put("LKR",new Currency("LKR",144,2));
        currencies.put("144",new Currency("LKR",144,2));
        currencies.put("VND",new Currency("VND",704,2));
        currencies.put("704",new Currency("VND",704,2));
        currencies.put("GTQ",new Currency("GTQ",320,2));
        currencies.put("320",new Currency("GTQ",320,2));
        currencies.put("BSD",new Currency("BSD",44,2));
        currencies.put("044",new Currency("BSD",44,2));
        currencies.put("TOP",new Currency("TOP",776,2));
        currencies.put("776",new Currency("TOP",776,2));
        currencies.put("HNL",new Currency("HNL",340,2));
        currencies.put("340",new Currency("HNL",340,2));
        currencies.put("MOP",new Currency("MOP",446,2));
        currencies.put("446",new Currency("MOP",446,2));
        currencies.put("IRR",new Currency("IRR",364,2));
        currencies.put("364",new Currency("IRR",364,2));
        currencies.put("NIO",new Currency("NIO",558,2));
        currencies.put("558",new Currency("NIO",558,2));
        currencies.put("ANG",new Currency("ANG",532,0));
        currencies.put("532",new Currency("ANG",532,0));
        currencies.put("LUF",new Currency("LUF",442,0));
        currencies.put("442",new Currency("LUF",442,0));
        currencies.put("BHD",new Currency("BHD",48,3));
        currencies.put("048",new Currency("BHD",48,3));
        currencies.put("GIP",new Currency("GIP",292,2));
        currencies.put("292",new Currency("GIP",292,2));
        currencies.put("MYR",new Currency("MYR",458,2));
        currencies.put("458",new Currency("MYR",458,2));
        currencies.put("CLP",new Currency("CLP",152,2));
        currencies.put("152",new Currency("CLP",152,2));
        currencies.put("KPW",new Currency("KPW",408,2));
        currencies.put("408",new Currency("KPW",408,2));
        currencies.put("BRL",new Currency("BRL",986,2));
        currencies.put("986",new Currency("BRL",986,2));
        currencies.put("OMR",new Currency("OMR",512,3));
        currencies.put("512",new Currency("OMR",512,3));
        currencies.put("IRA",new Currency("IRA",365,2));
        currencies.put("365",new Currency("IRA",365,2));
        currencies.put("USD",new Currency("USD",840,2));
        currencies.put("840",new Currency("USD",840,2));
        currencies.put("QAR",new Currency("QAR",634,2));
        currencies.put("634",new Currency("QAR",634,2));
        currencies.put("XAF",new Currency("XAF",950,0));
        currencies.put("950",new Currency("XAF",950,0));
        currencies.put("PGK",new Currency("PGK",598,2));
        currencies.put("598",new Currency("PGK",598,2));
        currencies.put("YER",new Currency("YER",886,2));
        currencies.put("886",new Currency("YER",886,2));
    }

    /** 
     * Converts from an ISO Amount (12 digit string) to a double taking in 
     * consideration the number of decimal digits according to currency
     * 
     * @param isoamount - The ISO amount to be converted (eg. ISOField 4)
     * @param currency  - The ISO currency to be converted (eg. ISOField 49)
     * @return result - A double representing the converted field
     * @exception IllegalArgumentException
     */
    public static double convertFromIsoMsg(String isoamount, String currency) throws IllegalArgumentException
    {
        double d=0;                                                                                           
        try
        {
            Currency c=(Currency)currencies.get(currency.toUpperCase());
            int decimals=c.getDecimals();
            double m=1; if(decimals>0) for(int x=1;x<=decimals;x++) m*=10;
            d=new Double(isoamount).doubleValue();
            d/=m;
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Failed convertFromIsoMsg");
        }
        return d;
    }

    /** 
     * Converts an amount to an ISO Amount taking in consideration
     * the number of decimal digits according to currency
     * 
     * @param amount - The amount to be converted 
     * @param currency  - The ISO currency to be converted (eg. ISOField 49)
     * @return result - An iso amount representing the converted field
     * @exception IllegalArgumentException
     */
    public static String convertToIsoMsg(double amount,String currency) throws IllegalArgumentException
    {
        String z=null;
        try
        {
            Currency c=(Currency)currencies.get(currency.toUpperCase());
            if(c==null) throw new IllegalArgumentException("Bad currency parameter");
            int decimals=c.getDecimals();
            double m=1; if(decimals>0) for(int x=1;x<=decimals;x++) m*=10;
            amount*=m;
            z=ISOUtil.zeropad(Long.toString((long)amount),12);
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Failed convertToIsoMsg");
        }
        return z;
    }
    
    public static Object[] decomposeComposedCurrency(String incurr) throws IllegalArgumentException
    {
        Object[] outcurr=null;
        try
        {   
            StringTokenizer st=new StringTokenizer(incurr);
            String curr=st.nextToken();
            Double amount=new Double(st.nextToken());
            outcurr=new Object[2];
            outcurr[0]=curr;
            outcurr[1]=amount;
        }
        catch(Exception e)
        {   
            throw new IllegalArgumentException("Failed decompose");
        }
        return outcurr;
    }
    
    public static String getIsoCodeFromAlphaCode(String alphacode) throws IllegalArgumentException
    {
        String isocode=null;
        try
        {
            Currency c=(Currency)currencies.get(alphacode.toUpperCase());
            isocode=ISOUtil.zeropad(Integer.toString(c.getIsoCode()),3);
            if(isocode==null) throw new IllegalArgumentException("AlphaCode not found, or incorrectly specified");
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Failed getIsoCodeFromAlphaCode");
        }
        return  isocode;
    }

    public static Currency getCurrency (int code) throws ISOException {
	return (Currency) 
	    currencies.get (ISOUtil.zeropad (Integer.toString (code), 3));
    }

    static public void main(String[] args)
    {
        try
        {
            String cychamount="uyu 25049.00";
            Object[] d=decomposeComposedCurrency(cychamount);
            String curr=(String)d[0];
            double amount=((Double)d[1]).doubleValue();
            System.out.println("Cych: "
		+cychamount+" , CURR="+curr+" , AMOUNT="+amount);
            String isocurr=getIsoCodeFromAlphaCode(curr);
            String a1=convertToIsoMsg(amount,isocurr);
            double d2=convertFromIsoMsg(a1,isocurr);
            System.out.println("ISOCUrr: "+a1);
            System.out.println(amount+"="+d2);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
