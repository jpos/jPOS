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

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.ssm.SsmActionBase;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Translate a key from encryption under LMK to KEK.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class KE extends SsmActionBase implements CLICommand {

    @Override
    protected boolean checkUsage(CLIContext cli, String[] strings) {
        if (strings.length < 9) {
            cli.println("Usage: KE keyLength keyType KEYunderLMK KEYcheckValue kekLength kekType KEKunderLMK KEKcheckValue");
            return false;
        }
        return true;
    }

    @Override
    protected void doCommand(CLIContext cli, JCESecurityModule sm, short keyLength, String[] strings) throws SMException {
        SecureDESKey KEKunderLMK = new SecureDESKey((short) Integer.parseInt(strings[5]),
                strings[6].toUpperCase(), strings[7], strings[8]);
        SecureDESKey KEYunderLMK = new SecureDESKey(keyLength, strings[2].toUpperCase(),
                strings[3], strings[4]);
        sm.exportKey(KEYunderLMK, KEKunderLMK);
    }
}
