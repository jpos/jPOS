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
