/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

package org.jpos.iso;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * ISO Currency Conversion package 
 * @author salaman@teknos.com
 * @author Jonathan.O'Connor@xcom.de
 * @version $Id$
 * @see http://www.evertype.com/standards/iso4217/iso4217-en.html
 *      http://www.iso.org/iso/en/prods-services/popstds/currencycodeslist.html
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

        ResourceBundle r = ResourceBundle.getBundle(ISOCurrency.class.getName());
        Enumeration en = r.getKeys();

        // property example:
        //   DKK=208 2
        while (en.hasMoreElements()) {
            String alphaCode  = (String)en.nextElement();
            String[] tmp = ((String)r.getString(alphaCode)).split(" ");
            String isoCode = tmp[0];
            int numDecimals = Integer.parseInt(tmp[1]);
            put(alphaCode, isoCode, numDecimals);
        }
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
    public static Currency getCurrency (String code) throws ISOException {
        return (Currency) currencies.get (ISOUtil.zeropad (code, 3));
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
