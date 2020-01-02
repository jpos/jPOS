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

package org.jpos.emv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

public class CVRMastercardTest {

    @Test
    public void testConstructorWithInvalidByteLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CVRMastercard(new byte[8]);
        });
    }

    @Test
    public void testConstructorWithInvalidHexLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CVRMastercard("0000");
        });
    }

    @Test
    public void testSuccessfulHexConstructor() {
        CVRMastercard cvr = new CVRMastercard("250000044000");
        cvr.dump(System.out, "");
        assertTrue(cvr.aacReturnedInSecondGenerateAC());
        assertTrue(cvr.arqcReturnedInFirstGenerateAC());
        assertTrue(cvr.offlinePINVerificationPerformed());
        assertTrue(cvr.internationalTransaction());
        assertTrue(cvr.upperConsecutiveOfflineLimitExceeded());
        assertFalse(cvr.upperCumulativeOfflineLimitExceeded());
        assertFalse(cvr.domesticTransaction(), "Transaction is not domestic.");
        assertEquals(0, cvr.rightNibbleOfScriptCounter());
        assertEquals(0, cvr.rightNibbleOfPINTryCounter());
    }

    @Test
    public void testSuccessfulByteArrayConstructor() {
        CVRMastercard cvr = new CVRMastercard(ISOUtil.hex2byte("250000044000"));
        cvr.dump(System.out, "");
        assertTrue(cvr.aacReturnedInSecondGenerateAC());
        assertTrue(cvr.arqcReturnedInFirstGenerateAC());
        assertTrue(cvr.offlinePINVerificationPerformed());
        assertTrue(cvr.internationalTransaction());
        assertTrue(cvr.upperConsecutiveOfflineLimitExceeded());
        assertFalse(cvr.upperCumulativeOfflineLimitExceeded());
        assertFalse(cvr.domesticTransaction(), "Transaction is not domestic.");
        assertEquals(0, cvr.rightNibbleOfScriptCounter());
        assertEquals(0, cvr.rightNibbleOfPINTryCounter());
    }    
}