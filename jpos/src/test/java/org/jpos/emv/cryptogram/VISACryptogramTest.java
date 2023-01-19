/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Rainer Reyes
 */
class VISACryptogramTest {

    @Test
    void testNotSupported() {
        assertThrows(Exception.class, () -> new VISACryptogram("99"));
    }

    @Test
    void testCVN10() {
        VISACryptogram spec = new VISACryptogram("0A");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.VSDC);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_1);
        assertTrue(spec.getDataBuilder() instanceof CVN10DataBuilder);
    }

    @Test
    void testCVN18() {
        VISACryptogram spec = new VISACryptogram("12");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.EMV_CSKD);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_2);
        assertTrue(spec.getDataBuilder() instanceof CVN18DataBuilder);
    }

    @Test
    void testCVN22() {
        VISACryptogram spec = new VISACryptogram("22");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.EMV_CSKD);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_2);
        assertTrue(spec.getDataBuilder() instanceof CVN22DataBuilder);
    }

}
