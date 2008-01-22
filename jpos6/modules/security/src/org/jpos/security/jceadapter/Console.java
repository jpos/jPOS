/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

package  org.jpos.security.jceadapter;

import java.util.Properties;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;


/**
 * A simple application for sending critical commands to the JCE Security Module.
 * The functionalities available from this console, are not available programmatically (via API's),
 * for security reasons, because most of them involve clear (non encrypted) keys.
 * Those commands are package protected in the JCE Security Module.
 * @author Hani Samuel Kirollos
 * @version $Revision$ $Date$
 */
public class Console {

    public Console () {
    }

    /**
     * @param args
     */
    public static void main (String[] args) {
        JCESecurityModule sm = new JCESecurityModule();
        Logger logger = new Logger();
        logger.addListener(new SimpleLogListener(System.out));
        sm.setLogger(logger, "jce-security-module");
        Properties cfgProps = new Properties();
        SimpleConfiguration cfg = new SimpleConfiguration(cfgProps);
        String commandName = null;
        String[] commandParams = new String[7];                 // 7 is Maximum number of paramters for a command
        System.out.println("Welcome to JCE Security Module console commander!");
        if (args.length == 0) {
            System.out.println("Usage: Console [-options] command [commandparameters...]");
            System.out.println("\nwhere options include:");
            System.out.println("    -lmk <filename>");
            System.out.println("                  to specify the Local Master Keys file");
            System.out.println("    -rebuildlmk   to rebuild new Local Master Keys");
            System.out.println("                  WARNING: old Local Master Keys gets overwritten");
            System.out.println("    -jce <provider classname>");
            System.out.println("                  to specify a JavaTM Cryptography Extension 1.2.1 provider");
            System.out.println("\nWhere command include: ");
            System.out.println("    GC <keyLength>");
            System.out.println("                  to generate a clear key component.");
            System.out.println("    FK <keyLength> <keyType> <component1> <component2> <component2>");
            System.out.println("                  to form a key from three clear components.");
            System.out.println("                  and returns the key encrypted under LMK");
            System.out.println("                  Odd parity is be forced before encryption under LMK");
            System.out.println("    CK <keyLength> <keyType> <KEYunderLMK>");
            System.out.println("                  to generate a key check value for a key encrypted under LMK.");
        }
        else {
            int argsCounter = 0;
            for (int j = 0; j < 10; j++) {
                if ((argsCounter < args.length) &&
                    (args[argsCounter].toLowerCase().compareTo("-lmk") == 0)
                ) {
                    argsCounter++;
                    cfgProps.setProperty("lmk", args[argsCounter++]);
                }
                if ((argsCounter < args.length) &&
                    (args[argsCounter].toLowerCase().compareTo("-jce") == 0)
                ) {
                    argsCounter++;
                    cfgProps.setProperty("jce", args[argsCounter++]);
                }
                if ((argsCounter < args.length) &&
                    (args[argsCounter].toLowerCase().compareTo("-rebuildlmk") == 0)
                ) {
                    argsCounter++;
                    cfgProps.setProperty("rebuildlmk", "true");
                }
            }
            if (argsCounter < args.length) {
                commandName = args[argsCounter++];
                int i = 0;
                while (argsCounter < args.length) {
                    commandParams[i++] = args[argsCounter++];
                }
            }
            // Configure JCE Security Module
            try {
                sm.setConfiguration(cfg);
            } catch (ConfigurationException e) {
                e.printStackTrace();
                System.exit(0);
            }
            // Execute Command
            if (commandName != null) {
                try {
                    short keyLength = (short)Integer.parseInt(commandParams[0]);
                    if (commandName.toUpperCase().compareTo("GC") == 0) {
                        String clearKeyComponenetHexString = sm.generateClearKeyComponent(keyLength);
                    }
                    else if (commandName.toUpperCase().compareTo("FK") == 0) {
                        SecureDESKey KEYunderLMK = sm.formKEYfromThreeClearComponents(keyLength,
                                commandParams[1].toUpperCase(), commandParams[2], commandParams[3], commandParams[4]);
                    }
                    else if (commandName.toUpperCase().compareTo("CK") == 0) {
                        SecureDESKey KEYunderLMK = sm.generateKeyCheckValue(keyLength,
                                commandParams[1].toUpperCase(), commandParams[2]);
                    }
                    else {
                        System.err.println("Unknown command: " + commandName);
                    }
                } catch (SMException e) {
                //e.printStackTrace();
                } catch (java.lang.NumberFormatException e) {
                    System.err.println("Invalid KeyLength");
                }
            }
            else {
                System.err.println("No command specified");
            }
        }
    }
}



