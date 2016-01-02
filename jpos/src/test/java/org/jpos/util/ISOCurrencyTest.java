/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

package org.jpos.util;

import org.jpos.iso.ISOCurrency;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class ISOCurrencyTest {
    @Test
    public void testparseFromISO87String () {
        assertEquals("2 decimals",  new BigDecimal("12.34"), ISOCurrency.parseFromISO87String("000000001234", "840"));
        assertEquals("3 decimals",  new BigDecimal("1.234"), ISOCurrency.parseFromISO87String("000000001234", "048"));
        assertEquals("no decimals", new BigDecimal("1234"),  ISOCurrency.parseFromISO87String("000000001234", "020"));
    }
    @Test
    public void testtoISO87String () {
        assertEquals("2 decimals",  "000000001234", ISOCurrency.toISO87String(new BigDecimal("12.34"), "840"));
        assertEquals("3 decimals",  "000000001234", ISOCurrency.toISO87String(new BigDecimal("1.234"), "048"));
        assertEquals("no decimals", "000000001234", ISOCurrency.toISO87String(new BigDecimal("1234"),  "020"));
    }
}
