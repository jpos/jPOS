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

package  org.jpos.security;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleMsg;


/**
 * <p>
 * Provides base functionality for the actual Security Module Adapter.
 * </p>
 * <p>
 * You adapter needs to override the methods that end with "Impl"
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public class BaseSMAdapter
        implements SMAdapter, ReConfigurable, LogSource {
    protected Logger logger = null;
    protected String realm = null;
    protected Configuration cfg;
    private String name;

    public BaseSMAdapter () {
        super();
    }

    public BaseSMAdapter (Configuration cfg, Logger logger, String realm) throws ConfigurationException
    {
        super();
        setLogger(logger, realm);
        setConfiguration(cfg);
    }

    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    public Logger getLogger () {
        return  logger;
    }

    public String getRealm () {
        return  realm;
    }

    /**
     * associates this SMAdapter with a name using NameRegistrar
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName (String name) {
        this.name = name;
        NameRegistrar.register("s-m-adapter." + name, this);
    }

    /**
     * @return this SMAdapter's name ("" if no name was set)
     */
    public String getName () {
        return  this.name;
    }

    /**
     * @param name
     * @return SMAdapter instance with given name.
     * @throws NotFoundException
     * @see NameRegistrar
     */
    public static SMAdapter getSMAdapter (String name) throws NameRegistrar.NotFoundException {
        return  (SMAdapter)NameRegistrar.get("s-m-adapter." + name);
    }

    public SecureDESKey generateKey (short keyLength, String keyType) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key Length", keyLength), new SimpleMsg("parameter",
                    "Key Type", keyType)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate Key", cmdParameters));
        SecureDESKey result = null;
        try {
            result = generateKeyImpl(keyLength, keyType);
            evt.addMessage(new SimpleMsg("result", "Generated Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public SecureDESKey importKey (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key Length", keyLength), new SimpleMsg("parameter",
                    "Key Type", keyType), new SimpleMsg("parameter", "Encrypted Key",
                    encryptedKey), new SimpleMsg("parameter", "Key-Encrypting Key", kek), new SimpleMsg("parameter", "Check Parity", checkParity)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import Key", cmdParameters));
        SecureDESKey result = null;
        try {
            result = importKeyImpl(keyLength, keyType, encryptedKey, kek, checkParity);
            evt.addMessage(new SimpleMsg("result", "Imported Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public byte[] exportKey (SecureDESKey key, SecureDESKey kek) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key", key), new SimpleMsg("parameter", "Key-Encrypting Key",
                    kek),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Export Key", cmdParameters));
        byte[] result = null;
        try {
            result = exportKeyImpl(key, kek);
            evt.addMessage(new SimpleMsg("result", "Exported Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN encryptPIN (String pin, String accountNumber) throws SMException {
        accountNumber = EncryptedPIN.extractAccountNumberPart(accountNumber);
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "clear pin", pin), new SimpleMsg("parameter", "account number",
                    accountNumber)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Encrypt Clear PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = encryptPINImpl(pin, accountNumber);
            evt.addMessage(new SimpleMsg("result", "PIN under LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public String decryptPIN (EncryptedPIN pinUnderLmk) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Decrypt PIN", cmdParameters));
        String result = null;
        try {
            result = decryptPINImpl(pinUnderLmk);
            evt.addMessage(new SimpleMsg("result", "clear PIN", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN importPIN (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1), new SimpleMsg("parameter",
                    "Data Key 1", kd1),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = importPINImpl(pinUnderKd1, kd1);
            evt.addMessage(new SimpleMsg("result", "PIN under LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN translatePIN (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1), new SimpleMsg("parameter",
                    "Data Key 1", kd1), new SimpleMsg("parameter", "Data Key 2", kd2),
                    new SimpleMsg("parameter", "Destination PIN Block Format", destinationPINBlockFormat)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate PIN from Data Key 1 to Data Key 2",
                cmdParameters));
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderKd1, kd1, kd2, destinationPINBlockFormat);
            evt.addMessage(new SimpleMsg("result", "PIN under Data Key 2", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN importPIN (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Derived Unique Key", pinUnderDuk), new SimpleMsg("parameter",
                    "Key Serial Number", ksn), new SimpleMsg("parameter", "Base Derivation Key",
                    bdk)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = importPINImpl(pinUnderDuk, ksn, bdk);
            evt.addMessage(new SimpleMsg("result", "PIN under LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN translatePIN (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Derived Unique Key", pinUnderDuk), new SimpleMsg("parameter",
                    "Key Serial Number", ksn), new SimpleMsg("parameter", "Base Derivation Key",
                    bdk), new SimpleMsg("parameter", "Data Key 2", kd2), new SimpleMsg("parameter",
                    "Destination PIN Block Format", destinationPINBlockFormat)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderDuk, ksn, bdk, kd2, destinationPINBlockFormat);
            evt.addMessage(new SimpleMsg("result", "PIN under Data Key 2", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN exportPIN (EncryptedPIN pinUnderLmk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk), new SimpleMsg("parameter",
                    "Data Key 2", kd2), new SimpleMsg("parameter", "Destination PIN Block Format",
                    destinationPINBlockFormat)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Export PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = exportPINImpl(pinUnderLmk, kd2, destinationPINBlockFormat);
            evt.addMessage(new SimpleMsg("result", "PIN under Data Key 2", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public byte[] generateCBC_MAC (byte[] data, SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "data", data), new SimpleMsg("parameter", "data key",
                    kd),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate CBC-MAC", cmdParameters));
        byte[] result = null;
        try {
            result = generateCBC_MACImpl(data, kd);
            evt.addMessage(new SimpleMsg("result", "CBC-MAC", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param keyLength
     * @param keyType
     * @return generated key
     * @throws SMException
     */
    protected SecureDESKey generateKeyImpl (short keyLength, String keyType) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param keyLength
     * @param keyType
     * @param encryptedKey
     * @param kek
     * @return imported key
     * @throws SMException
     */
    protected SecureDESKey importKeyImpl (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param key
     * @param kek
     * @return exported key
     * @throws SMException
     */
    protected byte[] exportKeyImpl (SecureDESKey key, SecureDESKey kek) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pin
     * @param accountNumber
     * @return encrypted PIN under LMK
     * @throws SMException
     */
    protected EncryptedPIN encryptPINImpl (String pin, String accountNumber) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLmk
     * @return clear pin as entered by card holder
     * @throws SMException
     */
    protected String decryptPINImpl (EncryptedPIN pinUnderLmk) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd1
     * @param kd1
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd1
     * @param kd1
     * @param kd2
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @param kd2
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLmk
     * @param kd2
     * @param destinationPINBlockFormat
     * @return exported pin
     * @throws SMException
     */
    protected EncryptedPIN exportPINImpl (EncryptedPIN pinUnderLmk, SecureDESKey kd2,
            byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param data
     * @param kd
     * @return generated CBC-MAC
     * @throws SMException
     */
    protected byte[] generateCBC_MACImpl (byte[] data, SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }
}



