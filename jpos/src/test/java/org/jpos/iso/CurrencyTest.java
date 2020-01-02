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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CurrencyTest {

    @Test
    public void testConstructor() throws Throwable {
        Currency currency = new Currency("testCurrencyAlphacode", 100, 1000);
        assertEquals(100, currency.isocode, "currency.isocode");
        assertEquals(1000, currency.numdecimals, "currency.numdecimals");
        assertEquals("testCurrencyAlphacode", currency.alphacode, "currency.alphacode");
    }

    @Test
    public void testGetAlphaCode() throws Throwable {
        String result = new Currency("testCurrencyAlphacode", 100, 1000).getAlphaCode();
        assertEquals("testCurrencyAlphacode", result, "result");
    }

    @Test
    public void testGetDecimals() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 100, 0).getDecimals();
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetDecimals1() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 100, 1000).getDecimals();
        assertEquals(1000, result, "result");
    }

    @Test
    public void testGetIsoCode() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 0, 100).getIsoCode();
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetIsoCode1() throws Throwable {
        int result = new Currency("testCurrencyAlphacode", 100, 1000).getIsoCode();
        assertEquals(100, result, "result");
    }
}
