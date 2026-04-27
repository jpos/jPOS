/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import java.io.Serializable;


/**
 * ISO Currency Conversion package
 *
 * @author salaman@teknos.com
 * @version $Id$
 */
public class Currency implements Serializable
{
    /** ISO alpha currency code (e.g. "USD"). */
    String alphacode;
    /** ISO numeric currency code. */
    int isocode;
    /** Number of decimal places for this currency. */
    int numdecimals;

    /**
     * Creates a Currency with the given code and decimal count.
     * @param alphacode ISO alpha code (e.g. "USD")
     * @param isocode ISO numeric code
     * @param numdecimals number of decimal places
     */
    public Currency(String alphacode, int isocode, int numdecimals)
    {
        this.alphacode = alphacode;
        this.isocode = isocode;
        this.numdecimals = numdecimals;
    }

    /**
     * Returns the number of decimal places for this currency.
     * @return decimal count
     */
    public int getDecimals()
    {
        return numdecimals;
    }

    /**
     * Returns the ISO numeric currency code.
     * @return ISO numeric code
     */
    public int getIsoCode()
    {
        return isocode;
    }

    /**
     * Returns the ISO alpha currency code.
     * @return alpha code (e.g. "USD")
     */
    public String getAlphaCode()
    {
        return alphacode;
    }

    /**
     * Formats an amount for inclusion in an ISO message (zero-padded, 12 digits).
     * @param amount the amount to format
     * @return 12-character zero-padded string
     */
    public String formatAmountForISOMsg(double amount)
    {
        try
        {
            double m = Math.pow(10, getDecimals()) * amount;
            return ISOUtil.zeropad(String.valueOf(Math.round(m)), 12);
        }
        catch (ISOException e)
        {
            throw new IllegalArgumentException("Failed to convert amount",e);
        }
    }

    /**
     * Parses an ISO amount string into a double by applying the currency's decimal shift.
     * @param isoamount the ISO-formatted amount string (no decimal point)
     * @return the decimal amount value
     */
    public double parseAmountFromISOMsg(String isoamount)
    {
        return Double.parseDouble(isoamount)/Math.pow(10, getDecimals());
    }

    @Override
    public String toString()
    {
        return "Currency{" +
               "alphacode='" + alphacode + '\'' +
               ", isocode=" + isocode +
               ", numdecimals=" + numdecimals +
               '}';
    }
}
