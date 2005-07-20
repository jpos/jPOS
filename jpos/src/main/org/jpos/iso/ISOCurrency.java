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

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * ISO Currency Conversion package 
 * @author salaman@teknos.com
 * @author Jonathan.O'Connor@xcom.de
 * @version $Id$
 * @see http://www.evertype.com/standards/iso4217/iso4217-en.html
 */

public class ISOCurrency
{
    private static Hashtable currencies;

    /** Should be called like this: put("ALL", "008", 2);
     * Note: the second parameter is zero padded to three digits
     */
    private static void put(String alphaCode, String isoCode, int numDecimals) {
        Currency ccy = new Currency(alphaCode, Integer.parseInt(isoCode), numDecimals);
        currencies.put(alphaCode, ccy);
        currencies.put(isoCode, ccy);
    }

    static
    {
        currencies=new Hashtable();
        put("AAA", "000", 0);//???
        put("AED", "784", 2);   // UAE Dirham
        put("AFA", "004", 0);   // Afghanistan Afghani
        put("ALL", "008", 2);   // Albanian Lek
        put("AMD", "051", 2);   // Armenian Dram
        put("ANG", "532", 0);   // Dutch Antilles Guilder
        put("AON", "024", 0);   // Angolan New Kwanza
        put("ARS", "032", 2);   // Argentine Peso
        put("ATS", "040", 2);   // Austrian Schilling
        put("AUD", "036", 2);   // Australian Dollar
        put("AWG", "533", 0);   // Aruban Guilder
        put("AZM", "031", 0);   // Azerbaijan Manat
        put("BBD", "052", 2);   // Barbados Dollar
        put("BDT", "050", 2);   // Bangladesh Taka
        put("BEF", "056", 0);   // Belgian Franc
        put("BGL", "100", 2);   // Bulgarian Lev
        put("BHD", "048", 3);   // Bahraini Dinar
        put("BIF", "108", 0);   // Burundi Franc
        put("BMD", "060", 2);   // Bermudan Dollar
        put("BND", "096", 2);   // Brunei Dollar
        put("BOB", "068", 2);   // Bolivian Boliviano
        put("BRL", "986", 2);   // Brazilian Real
        put("BSD", "044", 2);   // Bahamian Dollar
        put("BWP", "072", 2);   // Botswana Pula
        put("BYB", "112", 0);   // Belarussian Ruble
        put("BZD", "084", 2);   // Belize Dollar
        put("CAD", "124", 2);   // Canadian Dollar
        put("CHF", "756", 2);   // Swiss Franc
        put("CLP", "152", 2);   // Chilean Peso
        put("CNY", "156", 2);   // Chinese Yuan Renminbi
        put("COP", "170", 2);   // Columbian Peso
        put("CRC", "188", 2);   // Costa Rican Colon
        put("CSK", "200", 2);// ???
        put("CUP", "192", 2);   // Cuban Peso
        put("CVE", "132", 2);   // Cape Verde Escudo
        put("CYP", "196", 2);   // Cyprus Pound
        put("CZK", "203", 2);   // Czech Koruna
        put("DEM", "280", 2);   // Deutsche Mark
        put("DJF", "262", 0);   // Djibouti Franc
        put("DKK", "208", 2);   // Danish Krone
        put("DOP", "214", 2);   // Dominican Peso
        put("DZD", "012", 0);   // Algerian Dinar
        put("ECS", "218", 2);   // Ecuador Sucre
        put("EEK", "233", 2);   // Estonian Kroon
        put("EGP", "818", 2);   // Egyptian Pound
        put("ESP", "724", 0);   // Spanish Peseta
        put("ETB", "230", 2);   // Ethiopian Birr
        put("EUR", "978", 2);   // Euro
        put("FIM", "246", 2);   // Finnish Markka
        put("FJD", "242", 2);   // Fiji Dollar
        put("FKP", "238", 2);   // Falkland Islands Pound
        put("FRF", "250", 2);   // French Franc
        put("GBP", "826", 2);   // Pound Sterling
        put("GEK", "268", 0);//??? Georgian ???
        put("GHC", "288", 2);   // Ghanian Cedi 
        put("GIP", "292", 2);   // Gibraltar Pound
        put("GMD", "270", 2);   // Gambian Dalasi
        put("GNF", "324", 0);   // Guinean Franc
        put("GQE", "226", 2);//???
        put("GRD", "300", 0);   // Greek Drachma
        put("GTQ", "320", 2);   // Guatemalan Quetzal
        put("GWP", "624", 2);   // Guinea Bissau Peso
        put("GYD", "328", 2);   // Guyana Dollar
        put("HKD", "344", 2);   // Hong Kong Dollar
        put("HNL", "340", 2);   // Honduran Lempira
        put("HRK", "191", 2);   // Croatian Kuna
        put("HTG", "332", 2);   // Haiti Gourde
        put("HUF", "348", 2);   // Hungarian Forint
        put("IDR", "360", 2);   // Indonesian Rupiah
        put("IEP", "372", 2);   // Irish Pound
        put("ILS", "376", 2);   // Israeli Shekel
        put("INR", "356", 2);   // Indian Rupee
        put("IQD", "368", 0);   // Iraqi Dinar
        put("IRA", "365", 2);
        put("IRR", "364", 2);
        put("ISK", "352", 2);
        put("ITL", "380", 0);
        put("JMD", "388", 2);
        put("JOD", "400", 3);
        put("JPY", "392", 0);
        put("KES", "404", 2);
        put("KGS", "417", 2);
        put("KHR", "116", 2);
        put("KMF", "174", 0);
        put("KPW", "408", 2);
        put("KRW", "410", 0);
        put("KWD", "414", 3);
        put("KYD", "136", 2);
        put("KZT", "398", 2);
        put("LAK", "418", 0);
        put("LBP", "422", 2);
        put("LKR", "144", 2);
        put("LRD", "430", 2);
        put("LTL", "440", 2);
        put("LUF", "442", 0);
        put("LVL", "428", 2);
        put("LYD", "434", 3);
        put("MAD", "504", 2);
        put("MDL", "498", 2);
        put("MGF", "450", 0);
        put("MKD", "807", 2);
        put("MMK", "104", 2);
        put("MNT", "496", 2);
        put("MOP", "446", 2);
        put("MRO", "478", 2);
        put("MTL", "470", 3);
        put("MUR", "480", 2);
        put("MVR", "462", 2);
        put("MWK", "454", 2);
        put("MXN", "484", 2);
        put("MYR", "458", 2);
        put("MZM", "508", 2);
        put("NAD", "516", 2);
        put("NGN", "566", 2);
        put("NIO", "558", 2);
        put("NLG", "528", 2);
        put("NOK", "578", 2);
        put("NPR", "524", 2);
        put("NZD", "554", 2);
        put("OMR", "512", 3);
        put("PAB", "590", 2);
        put("PEN", "604", 2);
        put("PGK", "598", 2);
        put("PHP", "608", 2);
        put("PKR", "586", 2);
        put("PLN", "985", 2);
        put("PLZ", "616", 2);//???
        put("PTE", "620", 0);
        put("PYG", "600", 0);
        put("QAR", "634", 2);
        put("ROL", "642", 2);
        put("RON", "946", 2);
        put("RUB", "643", 2);
        put("RUR", "810", 2);
        put("RWF", "646", 0);
        put("SAR", "682", 2);
        put("SBD", "090", 2);
        put("SCR", "690", 2);
        put("SDA", "737", 2);
        put("SDP", "736", 2);
        put("SEK", "752", 2);
        put("SGD", "702", 2);
        put("SHP", "654", 2);
        put("SIT", "705", 2);
        put("SKK", "703", 2);
        put("SLL", "694", 2);
        put("SOS", "706", 2);
        put("SRG", "740", 2);
        put("STD", "678", 0);
        put("SVC", "222", 2);
        put("SYP", "760", 2);
        put("SZL", "748", 2);
        put("THB", "764", 2);
        put("TMM", "795", 2);
        put("TND", "788", 3);
        put("TOP", "776", 2);
        put("TPE", "626", 0);
        put("TRL", "792", 2); 
        put("TRY", "949", 2); // since 1 January 2005 New Turkish Lira replaces Turkish Lira (TRL)]
        put("TTD", "780", 2);
        put("TWD", "901", 2);
        put("TZS", "834", 2);
        put("UAK", "804", 2);//???
        put("UGX", "800", 2);
        put("USD", "840", 2);
        put("UYU", "858", 2);
        put("UZS", "860", 2);
        put("VEB", "862", 2);
        put("VND", "704", 2);
        put("VUV", "548", 0);
        put("WST", "882", 2);
        put("XAF", "950", 0);
        put("XCD", "951", 2);
        put("XEU", "954", 2);
        put("XOF", "952", 0);
        put("XPF", "953", 0);
        put("YDD", "720", 2);//???
        put("YER", "886", 2);
        put("YUN", "890", 2); // Yugoslav dina
        put("ZAR", "710", 2);
        put("ZMK", "894", 2);
        put("ZRN", "180", 2);
        put("ZWD", "716", 2);
        put("CSD", "891", 2);
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
            z=ISOUtil.zeropad(Long.toString(Math.round (amount)),12);
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

/*
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
*/
}
