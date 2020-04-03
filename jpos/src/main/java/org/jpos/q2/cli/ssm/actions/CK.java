package org.jpos.q2.cli.ssm.actions;

import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.ssm.SsmActionBase;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Generate check value for key.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class CK extends SsmActionBase {

    @Override
    protected boolean checkUsage(CLIContext cli, String[] strings) {
        if (strings.length < 4) {
            cli.println("Usage: CK KeyLength KeyType KeyUnderLMK");
            return false;
        }
        return true;
    }

    @Override
    protected void doCommand(
            CLIContext cli,
            JCESecurityModule sm,
            short keyLength,
            String[] strings) throws SMException {
        sm.generateKeyCheckValue(
                new SecureDESKey(keyLength, strings[2].toUpperCase(), strings[3], ""));
    }

}
