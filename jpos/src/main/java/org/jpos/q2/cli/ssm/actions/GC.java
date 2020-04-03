package org.jpos.q2.cli.ssm.actions;

import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.ssm.SsmActionBase;
import org.jpos.security.SMException;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Generate clear key component.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class GC extends SsmActionBase {

    @Override
    protected boolean checkUsage(CLIContext cli, String[] strings) {
        if (strings.length < 2) {
            cli.println("Usage: GC KeyLength");
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
        sm.generateClearKeyComponent(keyLength);
    }

}
