/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
class MCHIPCryptogramTest {

    @Test
    void testNotSupported(){
        assertThrows(Exception.class,()->new MChipCryptogram("FF"));
    }
    
    @Test
    void testMChipVersionBit00(){
        MChipCryptogram spec = new MChipCryptogram("10");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.MCHIP);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_1);
        assertTrue(spec.getDataBuilder() instanceof CVNMCDataBuilder);
    }

    @Test
    void testMChipVersionBit10(){
        MChipCryptogram spec = new MChipCryptogram("14");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.EMV_CSKD);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_1);
        assertTrue(spec.getDataBuilder() instanceof CVNMCDataBuilder);
    }
}
