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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CurrencyTest {

    @Test
    public void testConstructor() throws Throwable {
        Currency currency = new Currency("testCurrencyAlphacode", 100, 1000);
        assertEquals("currency.isocode", 100, currency.isocode);
        assertEquals("currency.numdecimals", 1000, currency.numdecimals);
        assertEquals("currency.alphacode", "testCurrencyAlphacode", currency.alphacode);
    }

    @Test
    public void testGetAlphaCode() throws Throwable {
        String result = new Currency("testCurrencyAlphacode", 100, 1000).getAlphaCode();
        assertEquals("result", "testCurrencyAlphacode", result);
    }

    @Test
    public void testGetDecimals() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 100, 0).getDecimals();
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetDecimals1() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 100, 1000).getDecimals();
        assertEquals("result", 1000, result);
    }

    @Test
    public void testGetIsoCode() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 0, 100).getIsoCode();
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetIsoCode1() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 100, 1000).getIsoCode();
        assertEquals("result", 100, result);
    }
}
