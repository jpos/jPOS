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

package org.jpos.q2.cli.ssm.actions;

import org.jpos.iso.ISODate;
import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.ssm.SsmActionBase;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Calculate CVV
 */
public class CVV extends SsmActionBase {
    @Override
    protected boolean checkUsage(CLIContext cli, String[] strings) {
        if (strings.length < 9) {
            cli.println("Usage: cvv keyLength CVK-A-underLMK KeyCheck-A CVK-B-underLMK KeyCheck-B PAN EXP ServiceCode");
            return false;
        }
        return true;
    }

    @Override
    protected void doCommand(CLIContext cli, JCESecurityModule sm, short keyLength, String[] args) throws SMException {
        SecureDESKey cvkAUnderLmk = new SecureDESKey (keyLength, "CVK", args[2], args[3]);
        SecureDESKey cvkBUnderLmk = new SecureDESKey (keyLength, "CVK", args[4], args[5]);
        String pan = args[6];
        String exp = args[7];
        String serviceCode = args[8];
        sm.calculateCVV(pan, cvkAUnderLmk, cvkBUnderLmk, ISODate.parseISODate(exp + "01000000"), serviceCode);
    }
}
