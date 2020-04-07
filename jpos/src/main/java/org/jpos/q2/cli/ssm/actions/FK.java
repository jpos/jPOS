package org.jpos.q2.cli.ssm.actions;

import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.ssm.SsmActionBase;
import org.jpos.security.SMException;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Form a key from clear components.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class FK extends SsmActionBase {
    private boolean prompt = false;

    @Override
    protected boolean checkUsage(CLIContext cli, String[] strings) {
        // This doesn't cover all combinations, but it is assumed the operation will fail elsewhere.
        if (strings.length == 4) {
            if (!strings[3].equals("-prompt")) {
                cli.println("Usage: FK KeyLength keyType -prompt");
                cli.println("Usage: FK KeyLength keyType component1 component2 component3");
                return false;
            } else {
                prompt = true;
                return true;
            }
        }
        if (strings.length < 6) {
            cli.println("Usage: FK KeyLength keyType -prompt");
            cli.println("Usage: FK KeyLength keyType component1 component2 component3");
            return false;
        }

        return true;
    }

    @Override
    protected void doCommand(
            CLIContext cli,
            JCESecurityModule sm,
            short keyLength,
            String[] strings)
            throws SMException {
        if (prompt) {
            String key1 = readKeyComponent(cli, 1);
            String key2 = readKeyComponent(cli, 2);
            String key3 = readKeyComponent(cli, 3);
            sm.formKEYfromThreeClearComponents(
                    keyLength, strings[2].toUpperCase(), key1, key2, key3);
        } else {
            sm.formKEYfromThreeClearComponents(
                    keyLength, strings[2].toUpperCase(), strings[3], strings[4], strings[5]);
        }
    }

    private String readKeyComponent(CLIContext cli, int kcNumber) {
        boolean validComponent = false;
        String key = null;
        cli.println("Key component " + kcNumber + ":");
        while (!validComponent) {
            while (true) {
                key = cli.readSecurely("Please enter key component:");
                if (key != null && key.length() == 32) break;
                cli.println("Key component must be 32 hexadecimal characters.");
            }
            String second;
            while (true) {
                second = cli.readSecurely("Please re-enter key component:");
                if (second != null && second.length() == 32) break;
                cli.println("Key component must be 32 hexadecimal characters.");
            }
            validComponent = key.equals(second);
            if (!validComponent) {
                cli.println("Entered key components don't match.");
            }
        }
        return key;
    }
}
