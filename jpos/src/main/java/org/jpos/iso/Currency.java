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
    String alphacode;
    int isocode;
    int numdecimals;

    public Currency(String alphacode, int isocode, int numdecimals)
    {
        this.alphacode = alphacode;
        this.isocode = isocode;
        this.numdecimals = numdecimals;
    }

    public int getDecimals()
    {
        return numdecimals;
    }

    public int getIsoCode()
    {
        return isocode;
    }

    public String getAlphaCode()
    {
        return alphacode;
    }

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

    public double parseAmountFromISOMsg(String isoamount)
    {
        return new Double(isoamount)/Math.pow(10, getDecimals());
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
