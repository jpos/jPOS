/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * ISO Currency Conversion package
 *
 * @author vsalaman@gmail.com
 * @author Jonathan.O'Connor@xcom.de
 * @version $Id$
 * @see "http://www.evertype.com/standards/iso4217/iso4217-en.html"
 *      "http://www.iso.org/iso/en/prods-services/popstds/currencycodeslist.html"
 */
public class ISOCurrency
{
    private static Map<String, Currency> currencies;
    private static final Object mutex = new Object();

    // Avoid creation of instances.
    private ISOCurrency()
    {
    }

    static
    {
        synchronized (mutex)
        {
            currencies = new HashMap<String, Currency>();
        }
        addBundle(ISOCurrency.class.getName());
        loadPropertiesFromClasspath("META-INF/org/jpos/config/ISOCurrency.properties");
    }

    @SuppressWarnings({"EmptyCatchBlock"})
    public static void loadPropertiesFromClasspath(String base)
    {
        InputStream in=loadResourceAsStream(base);
        try
        {
            if(in!=null)
            {
                addBundle(new PropertyResourceBundle(in));
            }
        }
        catch (IOException e)
        {
        }
        finally
        {
            if(in!=null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    /**
     * Converts from an ISO Amount (12 digit string) to a double taking in
     * consideration the number of decimal digits according to currency
     *
     * @param isoamount - The ISO amount to be converted (eg. ISOField 4)
     * @param currency  - The ISO currency to be converted (eg. ISOField 49)
     * @return result - A double representing the converted field
     * @throws IllegalArgumentException if we fail to convert the amount
     */
    public static double convertFromIsoMsg(String isoamount, String currency) throws IllegalArgumentException
    {
        Currency c = findCurrency(currency);
        return c.parseAmountFromISOMsg(isoamount);
    }

    public static void addBundle(ResourceBundle r)
    {
        synchronized (mutex)
        {
            Enumeration en = r.getKeys();
            while (en.hasMoreElements())
            {
                String alphaCode = (String) en.nextElement();
                String[] tmp = r.getString(alphaCode).split(" ");
                String isoCode = tmp[0];
                int numDecimals = Integer.parseInt(tmp[1]);
                addCurrency(alphaCode, isoCode, numDecimals);
            }
        }
    }

    public static void addBundle(String bundleName)
    {
        ResourceBundle r = ResourceBundle.getBundle(bundleName);
        addBundle(r);
    }

    /**
     * Converts an amount to an ISO Amount taking in consideration
     * the number of decimal digits according to currency
     *
     * @param amount   - The amount to be converted
     * @param currency - The ISO currency to be converted (eg. ISOField 49)
     * @return result - An iso amount representing the converted field
     * @throws IllegalArgumentException if we fail to convert the amount
     */
    public static String convertToIsoMsg(double amount, String currency) throws IllegalArgumentException
    {
        return findCurrency(currency).formatAmountForISOMsg(amount);
    }

    public static Object[] decomposeComposedCurrency(String incurr) throws IllegalArgumentException
    {
        final String[] strings = incurr.split(" ");
        if (strings.length != 2)
        {
            throw new IllegalArgumentException("Invalid parameter: " + incurr);
        }
        return new Object[]{strings[0], Double.valueOf(strings[1])};
    }

    public static String getIsoCodeFromAlphaCode(String alphacode) throws IllegalArgumentException
    {
        try
        {
            Currency c = findCurrency(alphacode);
            return ISOUtil.zeropad(Integer.toString(c.getIsoCode()), 3);
        }
        catch (ISOException e)
        {
            throw new IllegalArgumentException("Failed getIsoCodeFromAlphaCode/ zeropad failed?", e);
        }
    }

    public static Currency getCurrency(int code) throws ISOException
    {
        final String isoCode = ISOUtil.zeropad(Integer.toString(code), 3);
        return findCurrency(isoCode);
    }

    public static Currency getCurrency(String code) throws ISOException
    {
        final String isoCode = ISOUtil.zeropad(code, 3);
        return findCurrency(isoCode);
    }

    private static InputStream loadResourceAsStream(String name)
    {
        InputStream in = null;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null)
        {
            in = contextClassLoader.getResourceAsStream(name);
        }
        if (in == null)
        {
            in = ISOCurrency.class.getClassLoader().getResourceAsStream(name);
        }
        return in;
    }

    /**
     * Should be called like this: put("ALL", "008", 2);
     * Note: the second parameter is zero padded to three digits
     *
     * @param alphaCode   An alphabetic code such as USD
     * @param isoCode     An ISO code such as 840
     * @param numDecimals the number of implied decimals
     */
    private static void addCurrency(String alphaCode, String isoCode, int numDecimals)
    {
        // to allow a clean replacement from a more specific resource bundle we
        // require clearing instead of overriding.
        if(currencies.containsKey(alphaCode) || currencies.containsKey(isoCode))
        {
            currencies.remove(alphaCode);
            currencies.remove(isoCode);
        }
        Currency ccy = new Currency(alphaCode, Integer.parseInt(isoCode), numDecimals);
        currencies.put(alphaCode, ccy);
        currencies.put(isoCode, ccy);
    }

    private static Currency findCurrency(String currency)
    {
        final Currency c = currencies.get(currency.toUpperCase());
        if (c == null)
        {
            throw new IllegalArgumentException("Currency with key '" + currency + "' was not found");
        }
        return c;
    }
}
