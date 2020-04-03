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
