/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

import org.jpos.iso.ISOUtil;
import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.ssm.SsmActionBase;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Import a key from encryption under a KEK to under the LMK.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class IK extends SsmActionBase {

    @Override
    protected boolean checkUsage(CLIContext cli, String[] strings) {
        if (strings.length < 8) {
            cli.println("Usage: IK keyLength keyType KEYunderKEK kekLength kekType KEKunderLMK KEKcheckValue");
            return false;
        }
        return true;
    }

    @Override
    protected void doCommand(CLIContext cli, JCESecurityModule sm, short keyLength, String[] strings) throws SMException {
        SecureDESKey KEKunderLMK = new SecureDESKey((short) Integer.parseInt(strings[4]),
                strings[5].toUpperCase(), strings[6], strings[7]);
        sm.importKey(keyLength, strings[2].toUpperCase(),
                ISOUtil.hex2byte(strings[3]), KEKunderLMK, true);
    }
}
