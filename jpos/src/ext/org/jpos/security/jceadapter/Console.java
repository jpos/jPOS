/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
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



