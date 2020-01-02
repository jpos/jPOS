/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package  org.jpos.security;

import org.javatuples.Pair;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.jpos.util.SimpleMsg;

import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


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
public class BaseSMAdapter<T>
        implements SMAdapter<T>, Configurable, LogSource {
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

    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    @Override
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    @Override
    public Logger getLogger () {
        return  logger;
    }

    @Override
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

    @Override
    public SecureDESKey generateKey (short keyLength, String keyType) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key Length", keyLength));
        cmdParameters.add(new SimpleMsg("parameter", "Key Type", keyType));
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

    @Override
    public SecureKey generateKey(SecureKeySpec keySpec) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key Specification", keySpec));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate Key", cmdParameters));
        SecureKey result = null;
        try {
            result = generateKeyImpl(keySpec);
            evt.addMessage(new SimpleMsg("result", "Generated Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public byte[] generateKeyCheckValue(T kd) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key with untrusted check value", kd));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate Key Check Value", cmdParameters));
        byte[] result = null;
        try {
            result = generateKeyCheckValueImpl(kd);
            evt.addMessage(new SimpleMsg("result", "Generated Key Check Value", ISOUtil.hexString(result)));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public SecureDESKey translateKeyScheme(SecureDESKey key, KeyScheme destKeyScheme)
            throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key", key));
        cmdParameters.add(new SimpleMsg("parameter", "Destination Key Scheme", destKeyScheme));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate Key Scheme", cmdParameters));
        SecureDESKey result = null;
        try {
            result = translateKeySchemeImpl(key, destKeyScheme);
            evt.addMessage(new SimpleMsg("result", "Translate Key Scheme", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public SecureDESKey importKey (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key Length", keyLength));
        cmdParameters.add(new SimpleMsg("parameter", "Key Type", keyType));
        cmdParameters.add(new SimpleMsg("parameter", "Encrypted Key", encryptedKey));
        cmdParameters.add(new SimpleMsg("parameter", "Key-Encrypting Key", kek));
        cmdParameters.add(new SimpleMsg("parameter", "Check Parity", checkParity));
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

    @Override
    public SecureKey importKey(SecureKey kek, SecureKey key, SecureKeySpec keySpec
            , boolean checkParity) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key-Encrypting Key", kek));
        cmdParameters.add(new SimpleMsg("parameter", "Encrypted Key", key));
        cmdParameters.add(new SimpleMsg("parameter", "Key Specification", keySpec));
        cmdParameters.add(new SimpleMsg("parameter", "Check Parity", checkParity));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import Key", cmdParameters));
        SecureKey result = null;
        try {
            result = importKeyImpl(kek, key, keySpec, checkParity);
            evt.addMessage(new SimpleMsg("result", "Imported Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public byte[] exportKey (SecureDESKey key, SecureDESKey kek) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key", key));
        cmdParameters.add(new SimpleMsg("parameter", "Key-Encrypting Key", kek));
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

    @Override
    public SecureKey exportKey(SecureKey kek, SecureKey key, SecureKeySpec keySpec)
            throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key-Encrypting Key", kek));
        cmdParameters.add(new SimpleMsg("parameter", "Key", key));
        cmdParameters.add(new SimpleMsg("parameter", "Key Specification", keySpec));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Export Key", cmdParameters));
        SecureKey result = null;
        try {
            result = exportKeyImpl(kek, key, keySpec);
            evt.addMessage(new SimpleMsg("result", "Exported Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public EncryptedPIN encryptPIN (String pin, String accountNumber, boolean extract) throws SMException {
        accountNumber = extract ? EncryptedPIN.extractAccountNumberPart(accountNumber) : accountNumber;
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "clear pin", pin));
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNumber));
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
    @Override
    public EncryptedPIN encryptPIN (String pin, String accountNumber) throws SMException {
        return encryptPIN(pin, accountNumber, true);
    }

    @Override
    public String decryptPIN (EncryptedPIN pinUnderLmk) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk));
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

    @Override
    public EncryptedPIN importPIN(EncryptedPIN pinUnderKd1, T kd1) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1));
        cmdParameters.add(new SimpleMsg("parameter", "Data Key 1", kd1));
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

    @Override
    public EncryptedPIN translatePIN(EncryptedPIN pinUnderKd1, T kd1,
            T kd2, byte destinationPINBlockFormat) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1));
        cmdParameters.add(new SimpleMsg("parameter", "Data Key 1", kd1));
        cmdParameters.add(new SimpleMsg("parameter", "Data Key 2", kd2));
        cmdParameters.add(new SimpleMsg("parameter", "Destination PIN Block Format", destinationPINBlockFormat));
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

    @Override
    public EncryptedPIN importPIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk) throws SMException {
        return importPIN(pinUnderDuk,ksn,bdk,false);
    }

    @Override
    public EncryptedPIN importPIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk, boolean tdes) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "PIN under Derived Unique Key", pinUnderDuk));
        cmdParameters.add(new SimpleMsg("parameter", "Key Serial Number", ksn));
        cmdParameters.add(new SimpleMsg("parameter", "Base Derivation Key", bdk));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = importPINImpl(pinUnderDuk, ksn, bdk, tdes);
            evt.addMessage(new SimpleMsg("result", "PIN under LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public EncryptedPIN translatePIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk, T kd2, byte destinationPINBlockFormat) throws SMException {
        return translatePIN(pinUnderDuk,ksn,bdk,kd2,destinationPINBlockFormat,false);
    }

    @Override
    public EncryptedPIN translatePIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk, T kd2, byte destinationPINBlockFormat, boolean tdes) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "PIN under Derived Unique Key", pinUnderDuk));
        cmdParameters.add(new SimpleMsg("parameter", "Key Serial Number", ksn));
        cmdParameters.add(new SimpleMsg("parameter", "Base Derivation Key", bdk));
        cmdParameters.add(new SimpleMsg("parameter", "Data Key 2", kd2));
        cmdParameters.add(new SimpleMsg("parameter", "Destination PIN Block Format", destinationPINBlockFormat));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderDuk, ksn, bdk, kd2, destinationPINBlockFormat,tdes);
            evt.addMessage(new SimpleMsg("result", "PIN under Data Key 2", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public EncryptedPIN exportPIN(EncryptedPIN pinUnderLmk, T kd2, byte destinationPINBlockFormat) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk));
        cmdParameters.add(new SimpleMsg("parameter", "Data Key 2", kd2));
        cmdParameters.add(new SimpleMsg("parameter", "Destination PIN Block Format", destinationPINBlockFormat));
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

    @Override
    public EncryptedPIN generatePIN(String accountNumber, int pinLen)
            throws SMException {
      return generatePIN(accountNumber, pinLen, null);
    }

    @Override
    public EncryptedPIN generatePIN(String accountNumber, int pinLen, List<String> excludes)
            throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", accountNumber));
      cmdParameters.add(new SimpleMsg("parameter", "PIN length", pinLen));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));

      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Generate PIN", cmdParameters));
      EncryptedPIN result = null;
      try {
        result = generatePINImpl(accountNumber, pinLen, excludes);
        evt.addMessage(new SimpleMsg("result", "Generated PIN", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    @Override
    public void printPIN(String accountNo, EncryptedPIN pinUnderKd1, T kd1
                         ,String template, Map<String, String> fields) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo == null ? "" : accountNo));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under Key data 1", pinUnderKd1 == null ? "" : pinUnderKd1));
      if (kd1!=null)
        cmdParameters.add(new SimpleMsg("parameter", "Key data 1", kd1));
      cmdParameters.add(new SimpleMsg("parameter", "Template", template == null ? "" : template));
      if (fields!=null)
        cmdParameters.add(new SimpleMsg("parameter", "Fields", fields));

      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Print PIN", cmdParameters));
      try {
          printPINImpl(accountNo, pinUnderKd1, kd1, template, fields);
      } catch (Exception e) {
          evt.addMessage(e);
          throw  e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
          Logger.log(evt);
      }
    }

    @Override
    public String calculatePVV(EncryptedPIN pinUnderLMK, T pvkA,
                               T pvkB, int pvkIdx) throws SMException {
      return calculatePVV(pinUnderLMK, pvkA, pvkB, pvkIdx, null);
    }

    @Override
    public String calculatePVV(EncryptedPIN pinUnderLMK, T pvkA,
                               T pvkB, int pvkIdx, List<String> excludes)
            throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderLMK.getAccountNumber()));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under LMK", pinUnderLMK));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB));
      cmdParameters.add(new SimpleMsg("parameter", "PVK index", pvkIdx));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PVV", cmdParameters));
      String result = null;
      try {
        result = calculatePVVImpl(pinUnderLMK, pvkA, pvkB, pvkIdx, excludes);
        evt.addMessage(new SimpleMsg("result", "Calculated PVV", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    @Override
    public String calculatePVV(EncryptedPIN pinUnderKd1, T kd1,
                               T pvkA, T pvkB, int pvkIdx)
            throws SMException {
      return calculatePVV(pinUnderKd1, kd1, pvkA, pvkB, pvkIdx, null);
    }

    @Override
    public String calculatePVV(EncryptedPIN pinUnderKd1, T kd1,
                               T pvkA, T pvkB, int pvkIdx,
                               List<String> excludes) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1));
      cmdParameters.add(new SimpleMsg("parameter", "Data Key 1", kd1));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB));
      cmdParameters.add(new SimpleMsg("parameter", "PVK index", pvkIdx));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PVV", cmdParameters));
      String result = null;
      try {
        result = calculatePVVImpl(pinUnderKd1, kd1, pvkA, pvkB, pvkIdx, excludes);
        evt.addMessage(new SimpleMsg("result", "Calculated PVV", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    @Override
    public boolean verifyPVV(EncryptedPIN pinUnderKd1, T kd1, T pvkA,
                          T pvkB, int pvki, String pvv) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()));
        cmdParameters.add(new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1));
        cmdParameters.add(new SimpleMsg("parameter", "Data Key 1", kd1));
        cmdParameters.add(new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA));
        cmdParameters.add(new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB));
        cmdParameters.add(new SimpleMsg("parameter", "pvki", pvki));
        cmdParameters.add(new SimpleMsg("parameter", "pvv", pvv));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify a PIN Using the VISA Method", cmdParameters));

      try {
        boolean r = verifyPVVImpl(pinUnderKd1, kd1, pvkA, pvkB, pvki, pvv);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public String calculateIBMPINOffset(EncryptedPIN pinUnderLmk, T pvk,
                           String decTab, String pinValData, int minPinLen)
            throws SMException {
      return calculateIBMPINOffset(pinUnderLmk, pvk, decTab, pinValData, minPinLen, null);
    }

    @Override
    public String calculateIBMPINOffset(EncryptedPIN pinUnderLmk, T pvk,
                           String decTab, String pinValData, int minPinLen,
                           List<String> excludes) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderLmk.getAccountNumber()));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk));
      cmdParameters.add(new SimpleMsg("parameter", "PVK", pvk));
      cmdParameters.add(new SimpleMsg("parameter", "decimalisation table", decTab));
      cmdParameters.add(new SimpleMsg("parameter", "PIN validation data", pinValData));
      cmdParameters.add(new SimpleMsg("parameter", "minimum PIN length", minPinLen));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PIN offset", cmdParameters));
      String result = null;
      try {
        result = calculateIBMPINOffsetImpl(pinUnderLmk, pvk,
                decTab, pinValData, minPinLen, excludes);
        evt.addMessage(new SimpleMsg("result", "Calculated PIN offset", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    @Override
    public String calculateIBMPINOffset(EncryptedPIN pinUnderKd1, T kd1,
                           T pvk, String decTab, String pinValData, int minPinLen)
            throws SMException {
      return calculateIBMPINOffset(pinUnderKd1, kd1, pvk, decTab,
              pinValData, minPinLen, null);
    }

    @Override
    public String calculateIBMPINOffset(EncryptedPIN pinUnderKd1, T kd1,
                           T pvk, String decTab, String pinValData, int minPinLen,
                           List<String> excludes) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1));
      cmdParameters.add(new SimpleMsg("parameter", "Data Key 1", kd1));
      cmdParameters.add(new SimpleMsg("parameter", "PVK", pvk));
      cmdParameters.add(new SimpleMsg("parameter", "decimalisation table", decTab));
      cmdParameters.add(new SimpleMsg("parameter", "PIN validation data", pinValData));
      cmdParameters.add(new SimpleMsg("parameter", "minimum PIN length", minPinLen));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PIN offset", cmdParameters));
      String result = null;
      try {
        result = calculateIBMPINOffsetImpl(pinUnderKd1, kd1, pvk,
                decTab, pinValData, minPinLen, excludes);
        evt.addMessage(new SimpleMsg("result", "Calculated PIN offset", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    @Override
    public boolean verifyIBMPINOffset(EncryptedPIN pinUnderKd1, T kd1, T pvk,
                                      String offset, String decTab, String pinValData,
                                      int minPinLen) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()));
        cmdParameters.add(new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1));
        cmdParameters.add(new SimpleMsg("parameter", "Data Key 1", kd1));
        cmdParameters.add(new SimpleMsg("parameter", "PVK", pvk));
        cmdParameters.add(new SimpleMsg("parameter", "Pin block format", pinUnderKd1.getPINBlockFormat()));
        cmdParameters.add(new SimpleMsg("parameter", "decimalisation table", decTab));
        cmdParameters.add(new SimpleMsg("parameter", "PIN validation data", pinValData));
        cmdParameters.add(new SimpleMsg("parameter", "minimum PIN length", minPinLen));
        cmdParameters.add(new SimpleMsg("parameter", "offset", offset));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify PIN offset", cmdParameters));

      try {
        boolean r = verifyIBMPINOffsetImpl(pinUnderKd1, kd1, pvk, offset, decTab,
                 pinValData, minPinLen);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public EncryptedPIN deriveIBMPIN(String accountNo, T pvk,
                                     String decTab, String pinValData,
                                     int minPinLen, String offset) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "Offset", offset));
        cmdParameters.add(new SimpleMsg("parameter", "PVK", pvk));
        cmdParameters.add(new SimpleMsg("parameter", "Decimalisation table", decTab));
        cmdParameters.add(new SimpleMsg("parameter", "PIN validation data", pinValData));
        cmdParameters.add(new SimpleMsg("parameter", "Minimum PIN length", minPinLen));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Derive a PIN Using the IBM Method", cmdParameters));
      EncryptedPIN result = null;
      try {
        result = deriveIBMPINImpl(accountNo, pvk, decTab,  pinValData, minPinLen,  offset);
        evt.addMessage(new SimpleMsg("result", "Derived PIN", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    @Override
    public String calculateCVV(String accountNo, T cvkA, T cvkB,
                               Date expDate, String serviceCode) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB));
        cmdParameters.add(new SimpleMsg("parameter", "Exp date", expDate));
        cmdParameters.add(new SimpleMsg("parameter", "Service code", serviceCode));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate CVV/CVC", cmdParameters));
      String result = null;
      try {
        result = calculateCVVImpl(accountNo, cvkA, cvkB, expDate, serviceCode);
        evt.addMessage(new SimpleMsg("result", "Calculated CVV/CVC", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    @Override
    public String calculateCVD(String accountNo, T cvkA, T cvkB,
                               String expDate, String serviceCode) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB));
        cmdParameters.add(new SimpleMsg("parameter", "Exp date", expDate));
        cmdParameters.add(new SimpleMsg("parameter", "Service code", serviceCode));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Calculate CVV/CVC", cmdParameters));
        String result = null;
        try {
            result = calculateCVDImpl(accountNo, cvkA, cvkB, expDate, serviceCode);
            evt.addMessage(new SimpleMsg("result", "Calculated CVV/CVC", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return result;
    }

    @Override
    public String calculateCAVV(String accountNo, T cvk, String upn,
                                String authrc, String sfarc) throws SMException {

      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
      cmdParameters.add(new SimpleMsg("parameter", "cvk", cvk == null ? "" : cvk));
      cmdParameters.add(new SimpleMsg("parameter", "unpredictable number", upn));
      cmdParameters.add(new SimpleMsg("parameter", "auth rc", authrc));
      cmdParameters.add(new SimpleMsg("parameter", "second factor auth rc", sfarc));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate CAVV/AAV", cmdParameters));
      String result = null;
      try {
          result = calculateCAVVImpl(accountNo, cvk, upn, authrc, sfarc);
          evt.addMessage(new SimpleMsg("result", "Calculated CAVV/AAV", result));
      } catch (Exception e) {
          evt.addMessage(e);
          throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
          Logger.log(evt);
      }
      return result;
    }

    @Override
    public boolean verifyCVV(String accountNo , T cvkA, T cvkB,
                            String cvv, Date expDate, String serviceCode) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB));
        cmdParameters.add(new SimpleMsg("parameter", "CVV/CVC", cvv));
        cmdParameters.add(new SimpleMsg("parameter", "Exp date", expDate));
        cmdParameters.add(new SimpleMsg("parameter", "Service code", serviceCode));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify CVV/CVC", cmdParameters));
      try {
        boolean r = verifyCVVImpl(accountNo, cvkA, cvkB, cvv, expDate, serviceCode);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public boolean verifyCVD(String accountNo, T cvkA, T cvkB,
                            String cvv, String expDate, String serviceCode) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA));
        cmdParameters.add(new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB));
        cmdParameters.add(new SimpleMsg("parameter", "CVV/CVC", cvv));
        cmdParameters.add(new SimpleMsg("parameter", "Exp date", expDate));
        cmdParameters.add(new SimpleMsg("parameter", "Service code", serviceCode));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Verify CVV/CVC", cmdParameters));
        try {
            boolean r = verifyCVVImpl(accountNo, cvkA, cvkB, cvv, expDate, serviceCode);
            evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
            return r;
        } catch (Exception e) {
            evt.addMessage(e);
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
    }

    @Override
    public boolean verifyCAVV(String accountNo, T cvk, String cavv,
                              String upn, String authrc, String sfarc) throws SMException {

      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
      cmdParameters.add(new SimpleMsg("parameter", "cvk", cvk == null ? "" : cvk));
      cmdParameters.add(new SimpleMsg("parameter", "cavv", cavv == null ? "" : cavv));
      cmdParameters.add(new SimpleMsg("parameter", "unpredictable number", upn));
      cmdParameters.add(new SimpleMsg("parameter", "auth rc", authrc));
      cmdParameters.add(new SimpleMsg("parameter", "second factor auth rc", sfarc));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify CAVV/AAV", cmdParameters));
      boolean r = false;
      try {
          r = verifyCAVVImpl(accountNo, cvk, cavv, upn, authrc, sfarc);
          evt.addMessage(new SimpleMsg("result", "Verification status", r));
      } catch (Exception e) {
          evt.addMessage(e);
          throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
          Logger.log(evt);
      }
      return r;
    }

    @Override
    public boolean verifydCVV(String accountNo, T imkac, String dcvv,
                     Date expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "imk-ac", imkac == null ? "" : imkac));
        cmdParameters.add(new SimpleMsg("parameter", "dCVV", dcvv));
        cmdParameters.add(new SimpleMsg("parameter", "Exp date", expDate));
        cmdParameters.add(new SimpleMsg("parameter", "Service code", serviceCode));
        cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
        cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify dCVV", cmdParameters));
      try {
        boolean r = verifydCVVImpl(accountNo, imkac, dcvv, expDate, serviceCode, atc, mkdm);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public boolean verifydCVV(String accountNo, T imkac, String dcvv,
                     String expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "imk-ac", imkac == null ? "" : imkac));
        cmdParameters.add(new SimpleMsg("parameter", "dCVV", dcvv));
        cmdParameters.add(new SimpleMsg("parameter", "Exp date", expDate));
        cmdParameters.add(new SimpleMsg("parameter", "Service code", serviceCode));
        cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
        cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Verify dCVV", cmdParameters));
        try {
            boolean r = verifydCVVImpl(accountNo, imkac, dcvv, expDate, serviceCode, atc, mkdm);
            evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
            return r;
        } catch (Exception e) {
            evt.addMessage(e);
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
    }

    /**
     * @param imkcvc3 the issuer master key for generating and verifying CVC3
     * @param accountNo The account number including BIN and the check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param atc application transactin counter. This is used for ICC Master
     *        Key derivation. A 2 byte value must be supplied.
     * @param upn  unpredictable number. This is used for Session Key Generation
     *        A 4 byte value must be supplied.
     * @param data track data
     * @param mkdm ICC Master Key Derivation Method. If {@code null} specified
     *        is assumed.
     * @param cvc3 dynamic Card Verification Code 3
     * @return true if cvc3 is valid false if not
     * @throws SMException
     */
    @Override
    public boolean verifyCVC3(T imkcvc3, String accountNo, String acctSeqNo,
                     byte[] atc, byte[] upn, byte[] data, MKDMethod mkdm, String cvc3)
                     throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "imk-cvc3", imkcvc3 == null ? "" : imkcvc3));
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "accnt seq no", acctSeqNo));
        cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
        cmdParameters.add(new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)));
        cmdParameters.add(new SimpleMsg("parameter", "data", data == null ? "" : ISOUtil.hexString(data)));
        cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
        cmdParameters.add(new SimpleMsg("parameter", "cvc3", cvc3));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify CVC3", cmdParameters));
      try {
        boolean r = verifyCVC3Impl(imkcvc3, accountNo, acctSeqNo, atc, upn, data, mkdm, cvc3);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public boolean verifyARQC(MKDMethod mkdm, SKDMethod skdm, T imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, byte[] txnData) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
        cmdParameters.add(new SimpleMsg("parameter", "skd method", skdm));
        cmdParameters.add(new SimpleMsg("parameter", "imk-ac", imkac));
        cmdParameters.add(new SimpleMsg("parameter", "account number", accoutNo));
        cmdParameters.add(new SimpleMsg("parameter", "accnt seq no", acctSeqNo));
        cmdParameters.add(new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)));
        cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
        cmdParameters.add(new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)));
        cmdParameters.add(new SimpleMsg("parameter", "txn data", txnData == null ? "" : ISOUtil.hexString(txnData)));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify ARQC/TC/AAC", cmdParameters));
      try {
        boolean r = verifyARQCImpl(mkdm, skdm, imkac, accoutNo, acctSeqNo, arqc, atc, upn, txnData);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public byte[] generateARPC(MKDMethod mkdm, SKDMethod skdm, T imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
        cmdParameters.add(new SimpleMsg("parameter", "skd method", skdm));
        cmdParameters.add(new SimpleMsg("parameter", "imk-ac", imkac));
        cmdParameters.add(new SimpleMsg("parameter", "account number", accoutNo));
        cmdParameters.add(new SimpleMsg("parameter", "accnt seq no", acctSeqNo));
        cmdParameters.add(new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)));
        cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
        cmdParameters.add(new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)));
        cmdParameters.add(new SimpleMsg("parameter", "arpc gen. method", arpcMethod));
        cmdParameters.add(new SimpleMsg("parameter", "auth. rc", arc == null ? "" : ISOUtil.hexString(arc)));
        cmdParameters.add(new SimpleMsg("parameter", "prop auth. data"
                , propAuthData == null ? "" : ISOUtil.hexString(propAuthData))
        );
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Genarate ARPC", cmdParameters));
      try {
        byte[] result = generateARPCImpl(mkdm, skdm, imkac, accoutNo, acctSeqNo
            , arqc, atc, upn, arpcMethod, arc, propAuthData);
        evt.addMessage(new SimpleMsg("result", "Generated ARPC", result));
        return result;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public byte[] verifyARQCGenerateARPC(MKDMethod mkdm, SKDMethod skdm, T imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,byte[] txnData, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
        cmdParameters.add(new SimpleMsg("parameter", "skd method", skdm));
        cmdParameters.add(new SimpleMsg("parameter", "imk-ac", imkac));
        cmdParameters.add(new SimpleMsg("parameter", "account number", accoutNo));
        cmdParameters.add(new SimpleMsg("parameter", "accnt seq no", acctSeqNo));
        cmdParameters.add(new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)));
        cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
        cmdParameters.add(new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)));
        cmdParameters.add(new SimpleMsg("parameter", "txn data", txnData == null ? "" : ISOUtil.hexString(txnData)));
        cmdParameters.add(new SimpleMsg("parameter", "arpc gen. method", arpcMethod));
        cmdParameters.add(new SimpleMsg("parameter", "auth. rc", arc == null ? "" : ISOUtil.hexString(arc)));
        cmdParameters.add(new SimpleMsg("parameter", "prop auth. data",
                propAuthData == null ? "" : ISOUtil.hexString(propAuthData))
        );
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Genarate ARPC", cmdParameters));
      try {
            byte[] result = verifyARQCGenerateARPCImpl(
                      mkdm, skdm, imkac, accoutNo, acctSeqNo, arqc, atc, upn
                    , txnData, arpcMethod, arc, propAuthData
            );
        evt.addMessage(new SimpleMsg("result", "ARPC", result == null ? "" : ISOUtil.hexString(result)));
        return result;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public byte[] generateSM_MAC(MKDMethod mkdm, SKDMethod skdm
            ,T imksmi, String accountNo, String acctSeqNo
            ,byte[] atc, byte[] arqc, byte[] data) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
        cmdParameters.add(new SimpleMsg("parameter", "skd method", skdm));
        cmdParameters.add(new SimpleMsg("parameter", "imk-smi", imksmi));
        cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
        cmdParameters.add(new SimpleMsg("parameter", "accnt seq no", acctSeqNo));
        cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
        cmdParameters.add(new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)));
        cmdParameters.add(new SimpleMsg("parameter", "data", data == null ? "" : ISOUtil.hexString(data)));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Generate Secure Messaging MAC", cmdParameters));
      try {
        byte[] mac = generateSM_MACImpl(mkdm, skdm, imksmi, accountNo, acctSeqNo, atc, arqc, data);
        evt.addMessage(new SimpleMsg("result", "Generated MAC", mac!=null ? ISOUtil.hexString(mac) : ""));
        return mac;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    @Override
    public Pair<EncryptedPIN,byte[]> translatePINGenerateSM_MAC(MKDMethod mkdm
           ,SKDMethod skdm, PaddingMethod padm, T imksmi
           ,String accountNo, String acctSeqNo, byte[] atc, byte[] arqc
           ,byte[] data, EncryptedPIN currentPIN, EncryptedPIN newPIN
           ,T kd1, T imksmc, T imkac
           ,byte destinationPINBlockFormat) throws SMException {

      List<Loggeable> cmdParameters = new ArrayList<>();
      cmdParameters.add(new SimpleMsg("parameter", "mkd method", mkdm));
      cmdParameters.add(new SimpleMsg("parameter", "skd method", skdm));
      if (padm!=null)
        cmdParameters.add(new SimpleMsg("parameter", "padding method", padm));
      cmdParameters.add(new SimpleMsg("parameter", "imk-smi", imksmi));
      cmdParameters.add(new SimpleMsg("parameter", "account number", accountNo));
      cmdParameters.add(new SimpleMsg("parameter", "accnt seq no", acctSeqNo));
      cmdParameters.add(new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)));
      cmdParameters.add(new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)));
      cmdParameters.add(new SimpleMsg("parameter", "data", data == null ? "" : ISOUtil.hexString(data)));
      cmdParameters.add(new SimpleMsg("parameter", "Current Encrypted PIN", currentPIN));
      cmdParameters.add(new SimpleMsg("parameter", "New Encrypted PIN", newPIN));
      cmdParameters.add(new SimpleMsg("parameter", "Source PIN Encryption Key", kd1));
      cmdParameters.add(new SimpleMsg("parameter", "imk-smc", imksmc));
      if (imkac!=null)
        cmdParameters.add(new SimpleMsg("parameter", "imk-ac", imkac));
      cmdParameters.add(new SimpleMsg("parameter", "Destination PIN Block Format", destinationPINBlockFormat));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Translate PIN block format and Generate Secure Messaging MAC"
                    ,cmdParameters)
      );
      try {
            Pair<EncryptedPIN,byte[]> r = translatePINGenerateSM_MACImpl(mkdm, skdm
                ,padm, imksmi, accountNo, acctSeqNo, atc, arqc, data, currentPIN
                ,newPIN, kd1, imksmc, imkac, destinationPINBlockFormat
            );
            List<Loggeable> cmdResults = new ArrayList<>();
            cmdResults.add(new SimpleMsg("result", "Translated PIN block", r.getValue0()));
            cmdResults.add(new SimpleMsg("result", "Generated MAC", r.getValue1() == null ? "" : ISOUtil.hexString(r.getValue1())));
            evt.addMessage(new SimpleMsg("results", "Complex results", cmdResults));
            return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    /**
     * Encrypt Data Block.
     *
     * @param cipherMode block cipher mode
     * @param kd DEK or ZEK key used to encrypt data
     * @param data data to be encrypted
     * @param iv initial vector
     * @return encrypted data
     * @throws SMException
     */
    @Override
    public byte[] encryptData(CipherMode cipherMode, SecureDESKey kd
            ,byte[] data, byte[] iv) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Block Cipher Mode", cipherMode));
        if(kd != null)
            cmdParameters.add(new SimpleMsg("parameter", "Data key", kd));
        if(data != null)
            cmdParameters.add(new SimpleMsg("parameter", "Data", ISOUtil.hexString(data)));
        if(iv != null)
            cmdParameters.add(new SimpleMsg("parameter", "Initialization Vector", ISOUtil.hexString(iv)));

        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Encrypt Data", cmdParameters));
        byte[] encData = null;
        try {
            encData = encryptDataImpl(cipherMode, kd, data, iv);
            List<Loggeable> r = new ArrayList<>();
            r.add(new SimpleMsg("result", "Encrypted Data", encData));
            if(iv != null)
                r.add(new SimpleMsg("result", "Initialization Vector", iv));
            evt.addMessage(new SimpleMsg("results", r));
        } catch (Exception e) {
            evt.addMessage(e);
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return encData;
    }

    /**
     * Decrypt Data Block.
     *
     * @param cipherMode block cipher mode
     * @param kd DEK or ZEK key used to decrypt data
     * @param data data to be decrypted
     * @param iv initial vector
     * @return decrypted data
     * @throws SMException
     */
    @Override
    public byte[] decryptData(CipherMode cipherMode, SecureDESKey kd
            ,byte[] data, byte[] iv) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Block Cipher Mode", cipherMode));
        if(kd != null)
            cmdParameters.add(new SimpleMsg("parameter", "Data key", kd));
        if(data != null)
            cmdParameters.add(new SimpleMsg("parameter", "Data", ISOUtil.hexString(data)));
        if(iv != null)
            cmdParameters.add(new SimpleMsg("parameter", "Initialization Vector", ISOUtil.hexString(iv)));

        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Decrypt Data", cmdParameters));
        byte[] decData = null;
        try {
            decData = decryptDataImpl(cipherMode, kd, data, iv);
            List<Loggeable> r = new ArrayList<>();
            r.add(new SimpleMsg("result", "Decrypted Data", decData));
            if(iv != null)
                r.add(new SimpleMsg("result", "Initialization Vector", iv));
            evt.addMessage(new SimpleMsg("results", r));
        } catch (Exception e) {
            evt.addMessage(e);
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return decData;
    }

    @Override
    public byte[] generateCBC_MAC(byte[] data, T kd) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "data", data));
        cmdParameters.add(new SimpleMsg("parameter", "data key", kd));
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

    @Override
    public byte[] generateEDE_MAC(byte[] data, T kd) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "data", data));
        cmdParameters.add(new SimpleMsg("parameter", "data key", kd));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate EDE-MAC", cmdParameters));
        byte[] result = null;
        try {
            result = generateEDE_MACImpl(data, kd);
            evt.addMessage(new SimpleMsg("result", "EDE-MAC", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public SecureDESKey translateKeyFromOldLMK(SecureDESKey kd) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key under old LMK", kd));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate Key from old to new LMK", cmdParameters));
        SecureDESKey result = null;
        try {
            result = translateKeyFromOldLMKImpl(kd);
            evt.addMessage(new SimpleMsg("result", "Translated Key under new LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public SecureKey translateKeyFromOldLMK(SecureKey key, SecureKeySpec keySpec) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key under old LMK", key));
        cmdParameters.add(new SimpleMsg("parameter", "Key Specification", keySpec));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate Key from old to new LMK", cmdParameters));
        SecureKey result = null;
        try {
            result = translateKeyFromOldLMKImpl(key, keySpec);
            evt.addMessage(new SimpleMsg("result", "Translated Key under new LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    @Override
    public Pair<PublicKey, SecurePrivateKey> generateKeyPair(AlgorithmParameterSpec spec)
            throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Algorithm Parameter Spec", spec.getClass().getName()));

        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate public/private key pair", cmdParameters));
        Pair<PublicKey, SecurePrivateKey> result = null;
        try {
            result = generateKeyPairImpl(spec);
            List<Loggeable> cmdResults = new ArrayList<>();
            cmdResults.add(new SimpleMsg("result", "Public Key", result.getValue0().getEncoded()));
            cmdResults.add(new SimpleMsg("result", "Private Key", result.getValue1().getKeyBytes()));
            evt.addMessage(new SimpleMsg("results", "Complex results", cmdResults));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return result;
    }

    @Override
    public Pair<PublicKey, SecureKey> generateKeyPair(SecureKeySpec keySpec)
            throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Key Pair Specification", keySpec));

        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate public/private key pair", cmdParameters));
        Pair<PublicKey, SecureKey> result = null;
        try {
            result = generateKeyPairImpl(keySpec);
            List<Loggeable> cmdResults = new ArrayList<>();
            cmdResults.add(new SimpleMsg("result", "Public Key", result.getValue0().getEncoded()));
            cmdResults.add(new SimpleMsg("result", "Private Key", result.getValue1().getKeyBytes()));
            evt.addMessage(new SimpleMsg("results", "Complex results", cmdResults));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return result;
    }

    @Override
    public byte[] calculateSignature(MessageDigest hash, SecureKey privateKey
            ,byte[] data) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Hash Identifier", hash));
        cmdParameters.add(new SimpleMsg("parameter", "Private Key", privateKey));
        cmdParameters.add(new SimpleMsg("parameter", "data", data));

        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate data signature", cmdParameters));
        byte[] result = null;
        try {
            result = calculateSignatureImpl(hash, privateKey, data);
            evt.addMessage(new SimpleMsg("result", "Data Signature", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return result;
    }

    @Override
    public byte[] encryptData(SecureKey encKey, byte[] data
            , AlgorithmParameterSpec algspec, byte[] iv) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Encription Key", encKey));
        cmdParameters.add(new SimpleMsg("parameter", "Data", ISOUtil.hexString(data)));
        if (algspec != null)
            cmdParameters.add(new SimpleMsg("parameter", "Algorithm Spec", algspec));
        if (iv != null)
            cmdParameters.add(new SimpleMsg("parameter", "Initialization Vector", ISOUtil.hexString(iv)));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Encrypt Data", cmdParameters));
        byte[] result = null;
        try {
            result = encryptDataImpl(encKey, data, algspec, iv);
            List<Loggeable> r = new ArrayList<>();
            r.add(new SimpleMsg("result", "Encrypted Data", result));
            if (iv != null)
                r.add(new SimpleMsg("result", "Initialization Vector", iv));
            evt.addMessage(new SimpleMsg("results", r));
        } catch (SMException ex) {
            evt.addMessage(ex);
            throw ex;
        } catch (RuntimeException ex) {
            evt.addMessage(ex);
            throw new SMException(ex);
        } finally {
            Logger.log(evt);
        }
        return result;
    }

    @Override
    public byte[] decryptData(SecureKey privKey, byte[] data
            , AlgorithmParameterSpec algspec, byte[] iv) throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        cmdParameters.add(new SimpleMsg("parameter", "Decription Key", privKey));
        cmdParameters.add(new SimpleMsg("parameter", "Encrypted Data", ISOUtil.hexString(data)));
        if (algspec != null)
            cmdParameters.add(new SimpleMsg("parameter", "Algorithm Spec", algspec));
        if (iv != null)
            cmdParameters.add(new SimpleMsg("parameter", "Initialization Vector", ISOUtil.hexString(iv)));
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Decrypt Data", cmdParameters));
        byte[] result = null;
        try {
            result = decryptDataImpl(privKey, data, algspec, iv);
            List<Loggeable> r = new ArrayList<>();
            r.add(new SimpleMsg("result", "Decrypted Data", result));
            if (iv != null)
                r.add(new SimpleMsg("result", "Initialization Vector", iv));
            evt.addMessage(new SimpleMsg("results", r));
        } catch (SMException ex) {
            evt.addMessage(ex);
            throw ex;
        } catch (RuntimeException ex) {
            evt.addMessage(ex);
            throw new SMException(ex);
        } finally {
            Logger.log(evt);
        }
        return result;
    }

    @Override
    public void eraseOldLMK () throws SMException {
        List<Loggeable> cmdParameters = new ArrayList<>();
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Erase the key change storage", cmdParameters));
        try {
            eraseOldLMKImpl();
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
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
     * Your SMAdapter should override this method if it has this functionality.
     *
     * @param keySpec
     * @return generated key
     * @throws SMException
     */
    protected SecureKey generateKeyImpl(SecureKeySpec keySpec) throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param kd
     * @return generated Key Check Value
     * @throws SMException
     */
    protected byte[] generateKeyCheckValueImpl(T kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param key
     * @param destKeyScheme
     * @return translated key with {@code destKeyScheme} scheme
     * @throws SMException
     */
    protected SecureDESKey translateKeySchemeImpl(SecureDESKey key, KeyScheme destKeyScheme)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param keyLength
     * @param keyType
     * @param encryptedKey
     * @param kek
     * @param checkParity
     * @return imported key
     * @throws SMException
     */
    protected SecureDESKey importKeyImpl(short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality.
     *
     * @param kek
     * @param key
     * @param keySpec
     * @param checkParity
     * @return imported key
     * @throws SMException
     */
    protected SecureKey importKeyImpl(SecureKey kek, SecureKey key,
            SecureKeySpec keySpec, boolean checkParity) throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param key
     * @param kek
     * @return exported key
     * @throws SMException
     */
    protected byte[] exportKeyImpl(SecureDESKey key, SecureDESKey kek) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality.
     *
     * @param kek
     * @param key
     * @param keySpec
     * @return exported key
     * @throws SMException
     */
    protected SecureKey exportKeyImpl(SecureKey kek, SecureKey key
            , SecureKeySpec keySpec) throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
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
    protected EncryptedPIN importPINImpl(EncryptedPIN pinUnderKd1, T kd1) throws SMException {
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
    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderKd1, T kd1,
            T kd2, byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @deprecated
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk) throws SMException {
        return importPINImpl(pinUnderDuk,ksn,bdk,false);
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @param tdes
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk, boolean tdes) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @deprecated
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @param kd2
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk, T kd2, byte destinationPINBlockFormat) throws SMException {
        return translatePINImpl(pinUnderDuk,ksn,bdk,kd2,destinationPINBlockFormat,false);
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @param kd2
     * @param tdes
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            T bdk, T kd2, byte destinationPINBlockFormat,
            boolean tdes) throws SMException {
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
    protected EncryptedPIN exportPINImpl(EncryptedPIN pinUnderLmk, T kd2,
            byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNumber
     * @param pinLen
     * @param excludes
     * @return generated PIN under LMK
     * @throws SMException
     */
    protected EncryptedPIN generatePINImpl(String accountNumber, int pinLen, List<String> excludes) throws
            SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param pinUnderKd1
     * @param kd1
     * @param template
     * @param fields
     * @throws SMException
     */
    protected void printPINImpl(String accountNo, EncryptedPIN pinUnderKd1, T kd1
                             ,String template, Map<String, String> fields) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLMK
     * @param pvkA
     * @param pvkB
     * @param pvkIdx
     * @param excludes
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException
     */
    protected String calculatePVVImpl(EncryptedPIN pinUnderLMK,
                       T pvkA, T pvkB, int pvkIdx, List<String> excludes)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd1
     * @param kd1
     * @param pvkA
     * @param pvkB
     * @param pvkIdx
     * @param excludes
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException
     */
    protected String calculatePVVImpl(EncryptedPIN pinUnderKd1, T kd1,
                       T pvkA, T pvkB, int pvkIdx,
                       List<String> excludes) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd
     * @param kd
     * @param pvkA
     * @param pvkB
     * @param pvki
     * @param pvv
     * @return true if pin is valid false if not
     * @throws SMException
     */
    protected boolean verifyPVVImpl(EncryptedPIN pinUnderKd, T kd, T pvkA,
                        T pvkB, int pvki, String pvv) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLmk
     * @param pvk
     * @param decTab
     * @param pinValData
     * @param minPinLen
     * @param excludes
     * @return IBM PIN Offset
     * @throws SMException
     */
    protected String calculateIBMPINOffsetImpl(EncryptedPIN pinUnderLmk, T pvk,
                              String decTab, String pinValData, int minPinLen,
                              List<String> excludes) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd1
     * @param kd1
     * @param pvk
     * @param decTab
     * @param pinValData
     * @param minPinLen
     * @param excludes
     * @return IBM PIN Offset
     * @throws SMException
     */
    protected String calculateIBMPINOffsetImpl(EncryptedPIN pinUnderKd1, T kd1,
                              T pvk, String decTab, String pinValData,
                              int minPinLen, List<String> excludes)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd
     * @param kd
     * @param pvk
     * @param offset
     * @param decTab
     * @param pinValData
     * @param minPinLen
     * @return true if pin is valid false if not
     * @throws SMException
     */
    protected boolean verifyIBMPINOffsetImpl(EncryptedPIN pinUnderKd, T kd
                            ,T pvk, String offset, String decTab
                            ,String pinValData, int minPinLen) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param pvk
     * @param decTab
     * @param pinValData
     * @param minPinLen
     * @param offset
     * @return derived PIN under LMK
     * @throws SMException
     */
    protected EncryptedPIN deriveIBMPINImpl(String accountNo, T pvk, String decTab
                                , String pinValData, int minPinLen, String offset)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvkA
     * @param cvkB
     * @param expDate
     * @param serviceCode
     * @return Card Verification Code/Value
     * @throws SMException
     */
    protected String calculateCVVImpl(String accountNo, T cvkA, T cvkB,
                                   Date expDate, String serviceCode) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }


    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvkA
     * @param cvkB
     * @param expDate
     * @param serviceCode
     * @return Card Verification Digit (Code/Value)
     * @throws SMException
     */
    protected String calculateCVDImpl(String accountNo, T cvkA, T cvkB,
                                   String expDate, String serviceCode) throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }


    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvk
     * @param upn
     * @param authrc
     * @param sfarc
     * @return Cardholder Authentication Verification Value
     * @throws SMException
     */
    protected String calculateCAVVImpl(String accountNo, T cvk, String upn,
                                    String authrc, String sfarc) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvkA
     * @param cvkB
     * @param cvv
     * @param expDate
     * @param serviceCode
     * @return true if CVV/CVC is falid or false if not
     * @throws SMException
     */
    protected boolean verifyCVVImpl(String accountNo, T cvkA, T cvkB,
                        String cvv, Date expDate, String serviceCode) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvkA
     * @param cvkB
     * @param cvv
     * @param expDate
     * @param serviceCode
     * @return {@code true} if CVV/CVC is valid or {@code false} otherwise
     * @throws SMException
     */
    protected boolean verifyCVVImpl(String accountNo, T cvkA, T cvkB,
                        String cvv, String expDate, String serviceCode) throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvk
     * @param cavv
     * @param upn
     * @param authrc
     * @param sfarc
     * @return Cardholder Authentication Verification Value
     * @throws SMException
     */
    protected boolean verifyCAVVImpl(String accountNo, T cvk, String cavv,
                                     String upn, String authrc, String sfarc) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param imkac
     * @param dcvv
     * @param expDate
     * @param serviceCode
     * @param atc
     * @param mkdm
     * @return true if dcvv is valid false if not
     * @throws SMException
     */
    protected boolean verifydCVVImpl(String accountNo, T imkac, String dcvv,
                     Date expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param imkac
     * @param dcvv
     * @param expDate
     * @param serviceCode
     * @param atc
     * @param mkdm
     * @return true if dcvv is valid false if not
     * @throws SMException
     */
    protected boolean verifydCVVImpl(String accountNo, T imkac, String dcvv,
                     String expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param imkcvc3
     * @param accountNo
     * @param acctSeqNo
     * @param atc
     * @param upn
     * @param data
     * @param mkdm
     * @param cvc3
     * @return true if cvc3 is valid false if not
     * @throws SMException
     */
    protected boolean verifyCVC3Impl(T imkcvc3, String accountNo, String acctSeqNo,
                     byte[] atc, byte[] upn, byte[] data, MKDMethod mkdm, String cvc3)
                     throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param imkac
     * @param accountNo
     * @param acctSeqNo
     * @param arqc
     * @param atc
     * @param upn
     * @param txnData
     * @return true if ARQC/TC/AAC is falid or false if not
     * @throws SMException
     */
    protected boolean verifyARQCImpl(MKDMethod mkdm, SKDMethod skdm, T imkac
            ,String accountNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, byte[] txnData) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param imkac
     * @param accountNo
     * @param acctSeqNo
     * @param arqc
     * @param atc
     * @param upn
     * @param arpcMethod
     * @param arc
     * @param propAuthData
     * @return calculated ARPC
     * @throws SMException
     */
    protected byte[] generateARPCImpl(MKDMethod mkdm, SKDMethod skdm, T imkac
            ,String accountNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param imkac
     * @param accountNo
     * @param acctSeqNo
     * @param arqc
     * @param atc
     * @param upn
     * @param transData
     * @param arpcMethod
     * @param arc
     * @param propAuthData
     * @return calculated ARPC
     * @throws SMException
     */
    protected byte[] verifyARQCGenerateARPCImpl(MKDMethod mkdm, SKDMethod skdm, T imkac
            ,String accountNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,byte[] transData, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }



    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param imksmi
     * @param accountNo
     * @param acctSeqNo
     * @param atc
     * @param arqc
     * @param data
     * @return generated 8 bytes MAC
     * @throws SMException
     */
    protected byte[] generateSM_MACImpl(MKDMethod mkdm, SKDMethod skdm
            ,T imksmi, String accountNo, String acctSeqNo
            ,byte[] atc, byte[] arqc, byte[] data) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param padm
     * @param imksmi
     * @param accountNo
     * @param acctSeqNo
     * @param atc
     * @param arqc
     * @param data
     * @param currentPIN
     * @param newPIN
     * @param kd1
     * @param imksmc
     * @param imkac
     * @param destinationPINBlockFormat
     * @return Pair of values, encrypted PIN and 8 bytes MAC
     * @throws SMException
     */
    protected Pair<EncryptedPIN,byte[]> translatePINGenerateSM_MACImpl(MKDMethod mkdm
           ,SKDMethod skdm, PaddingMethod padm, T imksmi
           ,String accountNo, String acctSeqNo, byte[] atc, byte[] arqc
           ,byte[] data, EncryptedPIN currentPIN, EncryptedPIN newPIN
           ,T kd1, T imksmc, T imkac
           ,byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param cipherMode
     * @param kd
     * @param data
     * @param iv
     * @return encrypted data
     * @throws SMException
     */
    protected byte[] encryptDataImpl(CipherMode cipherMode, SecureDESKey kd
            ,byte[] data, byte[] iv) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param cipherMode
     * @param kd
     * @param data
     * @param iv
     * @return decrypted data
     * @throws SMException
     */
    protected byte[] decryptDataImpl(CipherMode cipherMode, SecureDESKey kd
            ,byte[] data, byte[] iv) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param data
     * @param kd
     * @return generated CBC-MAC
     * @throws SMException
     */
    protected byte[] generateCBC_MACImpl(byte[] data, T kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param data
     * @param kd
     * @return generated EDE-MAC
     * @throws SMException
     */
    protected byte[] generateEDE_MACImpl(byte[] data, T kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Translate key from encryption under the LMK held in key change storage
     * to encryption under a new LMK.
     *
     * @param kd the key encrypted under old LMK
     * @return key encrypted under the new LMK
     * @throws SMException if the parity of the imported key is not adjusted AND checkParity = true
     */
    protected SecureDESKey translateKeyFromOldLMKImpl(SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality.
     *
     * @param key
     * @param keySpec
     * @return key encrypted under the new LMK
     * @throws SMException if the parity of the imported key is not adjusted AND checkParity = true
     */
    protected SecureKey translateKeyFromOldLMKImpl(SecureKey key, SecureKeySpec keySpec) throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param spec algorithm specific parameters (contains e.g. key size)
     * @return key pair generated according to passed parameters
     * @throws SMException
     */
    protected Pair<PublicKey, SecurePrivateKey> generateKeyPairImpl(AlgorithmParameterSpec spec)
            throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality.
     *
     * @param keySpec
     * @return key pair generated according to passed parameters
     * @throws SMException
     */
    protected Pair<PublicKey, SecureKey> generateKeyPairImpl(SecureKeySpec keySpec)
            throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param hash identifier of the hash algorithm used to hash passed data.
     * @param privateKey private key used to compute data signature.
     * @param data data to be sifned.
     * @return signature of passed data.
     * @throws SMException
     */
    protected byte[] calculateSignatureImpl(MessageDigest hash, SecureKey privateKey
            ,byte[] data) throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Encrypts clear Data Block with specified cipher.
     *
     * @param encKey the data encryption key
     * @param data data block to encrypt
     * @param algspec algorithm specification
     * @param iv the inital vector
     * @return encrypted data block
     * @throws SMException
     */
    protected byte[] encryptDataImpl(SecureKey encKey, byte[] data
            , AlgorithmParameterSpec algspec, byte[] iv)
            throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Decrypts Data Block encrypted with assymetric cipher.
     *
     * @param decKey the data decryption key
     * @param data data block to decrypt
     * @param algspec algorithm specification
     * @param iv the inital vector
     * @return decrypted data block
     * @throws SMException
     */
    protected byte[] decryptDataImpl(SecureKey decKey, byte[] data
            , AlgorithmParameterSpec algspec, byte[] iv)
            throws SMException {
        throw new UnsupportedOperationException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Erase the key change storage area of memory
     *
     * It is recommended that this command is used after keys stored
     * by the Host have been translated from old to new LMKs.
     *
     * @throws SMException
     */
    protected void eraseOldLMKImpl () throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    @Override
    public byte[] dataEncrypt(T bdk, byte[] clearText) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    @Override
    public byte[] dataDecrypt(T bdk, byte[] clearText) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    @Override
    public SecureDESKey formKEYfromClearComponents(short keyLength, String keyType, String... clearComponents) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

}
