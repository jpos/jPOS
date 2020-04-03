package org.jpos.q2.cli.ssm;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.SSM;
import org.jpos.security.SMException;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Provides base for most SSM based commands.  The exec method wraps the processing for derived classes.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public abstract class SsmActionBase implements CLICommand {

    protected abstract boolean checkUsage(CLIContext cli, String[] strings);

    protected abstract void doCommand(
            CLIContext cli,
            JCESecurityModule sm,
            short keyLength,
            String[] strings)
            throws SMException;

    @Override
    public void exec(CLIContext cli, String[] strings) throws Exception {
        if (!checkUsage(cli, strings)) return;
        try {
            short keyLength = (short) Integer.parseInt(strings[1]);
            JCESecurityModule securityModule = SSM.getSecurityModule(cli);
            if (securityModule == null) {
                cli.println("No security module initialized.  Use the INIT command to initialize one.");
                return;
            }
            doCommand(cli, securityModule, keyLength, strings);
        } catch (SMException sme) {
            cli.printThrowable(sme);
        } catch (NumberFormatException nfe) {
            cli.println("Invalid KeyLength");
        }
    }
}
