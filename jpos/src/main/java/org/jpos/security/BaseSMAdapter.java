/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.javatuples.Pair;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.*;
import org.jpos.util.NameRegistrar.NotFoundException;


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
        implements SMAdapter, Configurable, LogSource {
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


    public byte[] generateKeyCheckValue (SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key with untrusted check value", kd)
        };
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

    public EncryptedPIN encryptPIN (String pin, String accountNumber, boolean extract) throws SMException {
        accountNumber = extract ? EncryptedPIN.extractAccountNumberPart(accountNumber) : accountNumber;
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
    public EncryptedPIN encryptPIN (String pin, String accountNumber) throws SMException {
        return encryptPIN(pin, accountNumber, true);
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

    public EncryptedPIN generatePIN(String accountNumber, int pinLen)
            throws SMException {
      return generatePIN(accountNumber, pinLen, null);
    }

    public EncryptedPIN generatePIN(String accountNumber, int pinLen, List<String> excludes)
            throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<Loggeable>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", accountNumber));
      cmdParameters.add(new SimpleMsg("parameter", "PIN length", pinLen));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));

      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Generate PIN", cmdParameters.toArray(new Loggeable[0])));
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

    public String calculatePVV(EncryptedPIN pinUnderLMK, SecureDESKey pvkA,
                               SecureDESKey pvkB, int pvkIdx) throws SMException {
      return calculatePVV(pinUnderLMK, pvkA, pvkB, pvkIdx, null);
    }

    public String calculatePVV(EncryptedPIN pinUnderLMK, SecureDESKey pvkA,
                               SecureDESKey pvkB, int pvkIdx,
                               List<String> excludes) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<Loggeable>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderLMK.getAccountNumber()));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under LMK", pinUnderLMK));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB));
      cmdParameters.add(new SimpleMsg("parameter", "PVK index", pvkIdx));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PVV", cmdParameters.toArray(new Loggeable[0])));
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

    public String calculatePVV(EncryptedPIN pinUnderKd1, SecureDESKey kd1,
                               SecureDESKey pvkA, SecureDESKey pvkB, int pvkIdx)
            throws SMException {
      return calculatePVV(pinUnderKd1, kd1, pvkA, pvkB, pvkIdx, null);
    }

    public String calculatePVV(EncryptedPIN pinUnderKd1, SecureDESKey kd1,
                               SecureDESKey pvkA, SecureDESKey pvkB, int pvkIdx,
                               List<String> excludes) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<Loggeable>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1));
      cmdParameters.add(new SimpleMsg("parameter", "Data Key 1", kd1));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA));
      cmdParameters.add(new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB));
      cmdParameters.add(new SimpleMsg("parameter", "PVK index", pvkIdx));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PVV", cmdParameters.toArray(new Loggeable[0])));
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

    public boolean verifyPVV(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey pvkA,
                          SecureDESKey pvkB, int pvki, String pvv) throws SMException {

      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()),
        new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1),
        new SimpleMsg("parameter", "Data Key 1", kd1),
        new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA),
        new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB),
        new SimpleMsg("parameter", "pvki", pvki),
        new SimpleMsg("parameter", "pvv", pvv)
      };
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

    public String calculateIBMPINOffset(EncryptedPIN pinUnderLmk, SecureDESKey pvk,
                           String decTab, String pinValData, int minPinLen)
            throws SMException {
      return calculateIBMPINOffset(pinUnderLmk, pvk, decTab, pinValData, minPinLen, null);
    }

    public String calculateIBMPINOffset(EncryptedPIN pinUnderLmk, SecureDESKey pvk,
                           String decTab, String pinValData, int minPinLen,
                           List<String> excludes) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<Loggeable>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", pinUnderLmk.getAccountNumber()));
      cmdParameters.add(new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk));
      cmdParameters.add(new SimpleMsg("parameter", "PVK", pvk));
      cmdParameters.add(new SimpleMsg("parameter", "decimalisation table", decTab));
      cmdParameters.add(new SimpleMsg("parameter", "PIN validation data", pinValData));
      cmdParameters.add(new SimpleMsg("parameter", "minimum PIN length", minPinLen));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINs list", excludes));
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PIN offset", cmdParameters.toArray(new Loggeable[0])));
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

    public String calculateIBMPINOffset(EncryptedPIN pinUnderKd1, SecureDESKey kd1,
                           SecureDESKey pvk, String decTab, String pinValData, int minPinLen)
            throws SMException {
      return calculateIBMPINOffset(pinUnderKd1, kd1, pvk, decTab,
              pinValData, minPinLen, null);
    }

    public String calculateIBMPINOffset(EncryptedPIN pinUnderKd1, SecureDESKey kd1,
                           SecureDESKey pvk, String decTab, String pinValData, int minPinLen,
                           List<String> excludes) throws SMException {
      List<Loggeable> cmdParameters = new ArrayList<Loggeable>();
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
      evt.addMessage(new SimpleMsg("command", "Calculate PIN offset", cmdParameters.toArray(new Loggeable[0])));
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

    public boolean verifyIBMPINOffset(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey pvk,
                                      String offset, String decTab, String pinValData,
                                      int minPinLen) throws SMException {
      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()),
        new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1),
        new SimpleMsg("parameter", "Data Key 1", kd1),
        new SimpleMsg("parameter", "PVK", pvk),
        new SimpleMsg("parameter", "Pin block format", pinUnderKd1.getPINBlockFormat()),
        new SimpleMsg("parameter", "decimalisation table", decTab),
        new SimpleMsg("parameter", "PIN validation data", pinValData),
        new SimpleMsg("parameter", "minimum PIN length", minPinLen),
        new SimpleMsg("parameter", "offset", offset)
      };
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

    public EncryptedPIN deriveIBMPIN(String accountNo, SecureDESKey pvk,
                                     String decTab, String pinValData,
                                     int minPinLen, String offset) throws SMException {
      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", accountNo),
        new SimpleMsg("parameter", "Offset", offset),
        new SimpleMsg("parameter", "PVK", pvk), 
        new SimpleMsg("parameter", "Decimalisation table", decTab),
        new SimpleMsg("parameter", "PIN validation data", pinValData),
        new SimpleMsg("parameter", "Minimum PIN length", minPinLen)
      };
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

    public String calculateCVV(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                               Date expDate, String serviceCode) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA),
            new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB),
            new SimpleMsg("parameter", "Exp date", expDate),
            new SimpleMsg("parameter", "Service code", serviceCode)
      };
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

    public boolean verifyCVV(String accountNo , SecureDESKey cvkA, SecureDESKey cvkB,
                            String cvv, Date expDate, String serviceCode) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA),
            new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB),
            new SimpleMsg("parameter", "CVV/CVC", cvv),
            new SimpleMsg("parameter", "Exp date", expDate),
            new SimpleMsg("parameter", "Service code", serviceCode)
      };
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

    public boolean verifydCVV(String accountNo, SecureDESKey imkac, String dcvv,
                     Date expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "imk-ac", imkac == null ? "" : imkac),
            new SimpleMsg("parameter", "dCVV", dcvv),
            new SimpleMsg("parameter", "Exp date", expDate),
            new SimpleMsg("parameter", "Service code", serviceCode),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "mkd method", mkdm)
      };
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
     *        is assumed {@see MKDMethod#OPTION_A}
     * @param cvc3 dynamic Card Verification Code 3
     * @return
     * @throws SMException
     */
    public boolean verifyCVC3(SecureDESKey imkcvc3, String accountNo, String acctSeqNo,
                     byte[] atc, byte[] upn, byte[] data, MKDMethod mkdm, String cvc3)
                     throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "imk-cvc3", imkcvc3 == null ? "" : imkcvc3),
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)),
            new SimpleMsg("parameter", "data", data == null ? "" : ISOUtil.hexString(data)),
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "cvc3", cvc3)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify CVC3", cmdParameters));
      try {
        boolean r = verifyCVC3Impl( imkcvc3, accountNo, acctSeqNo, atc, upn, data, mkdm, cvc3);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public boolean verifyARQC(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, byte[] transData) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "imk-ac", imkac),
            new SimpleMsg("parameter", "account number", accoutNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)),
            new SimpleMsg("parameter", "trans. data", transData == null ? "" : ISOUtil.hexString(transData))
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify ARQC/TC/AAC", cmdParameters));
      try {
        boolean r = verifyARQCImpl( mkdm, skdm, imkac, accoutNo, acctSeqNo, arqc, atc, upn, transData);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public byte[] generateARPC(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "imk-ac", imkac),
            new SimpleMsg("parameter", "account number", accoutNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)),
            new SimpleMsg("parameter", "arpc gen. method", arpcMethod),
            new SimpleMsg("parameter", "auth. rc", arc == null ? "" : ISOUtil.hexString(arc)),
            new SimpleMsg("parameter", "prop auth. data", propAuthData == null
                                       ? "" : ISOUtil.hexString(propAuthData))
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Genarate ARPC", cmdParameters));
      try {
        byte[] result = generateARPCImpl( mkdm, skdm, imkac, accoutNo, acctSeqNo
                           ,arqc, atc, upn, arpcMethod, arc, propAuthData );
        evt.addMessage(new SimpleMsg("result", "Generated ARPC", result));
        return result;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public byte[] verifyARQCGenerateARPC(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,byte[] transData, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "imk-ac", imkac),
            new SimpleMsg("parameter", "account number", accoutNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)),
            new SimpleMsg("parameter", "trans. data", transData == null ? "" : ISOUtil.hexString(transData)),
            new SimpleMsg("parameter", "arpc gen. method", arpcMethod),
            new SimpleMsg("parameter", "auth. rc", arc == null ? "" : ISOUtil.hexString(arc)),
            new SimpleMsg("parameter", "prop auth. data", propAuthData == null
                                       ? "" : ISOUtil.hexString(propAuthData))
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Genarate ARPC", cmdParameters));
      try {
        byte[] result = verifyARQCGenerateARPCImpl( mkdm, skdm, imkac, accoutNo,
                acctSeqNo, arqc, atc, upn, transData, arpcMethod, arc, propAuthData );
        evt.addMessage(new SimpleMsg("result", "ARPC", result == null ? "" : ISOUtil.hexString(result)));
        return result;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public byte[] generateSM_MAC(MKDMethod mkdm, SKDMethod skdm
            ,SecureDESKey imksmi, String accountNo, String acctSeqNo
            ,byte[] atc, byte[] arqc, byte[] data) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "imk-smi", imksmi),
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "data", data == null ? "" : ISOUtil.hexString(data))
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Generate Secure Messaging MAC", cmdParameters));
      try {
        byte[] mac = generateSM_MACImpl( mkdm, skdm, imksmi, accountNo, acctSeqNo, atc, arqc, data);
        evt.addMessage(new SimpleMsg("result", "Generated MAC", mac!=null ? ISOUtil.hexString(mac) : ""));
        return mac;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public Pair<EncryptedPIN,byte[]> translatePINGenerateSM_MAC(MKDMethod mkdm
           ,SKDMethod skdm, PaddingMethod padm, SecureDESKey imksmi
           ,String accountNo, String acctSeqNo, byte[] atc, byte[] arqc
           ,byte[] data, EncryptedPIN currentPIN, EncryptedPIN newPIN
           ,SecureDESKey kd1, SecureDESKey imksmc, SecureDESKey imkac
           ,byte destinationPINBlockFormat) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "padding method", padm),
            new SimpleMsg("parameter", "imk-smi", imksmi),
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "data", data == null ? "" : ISOUtil.hexString(data)),
            new SimpleMsg("parameter", "Current Encrypted PIN", currentPIN),
            new SimpleMsg("parameter", "New Encrypted PIN", newPIN),
            new SimpleMsg("parameter", "Source PIN Encryption Key", kd1),
            new SimpleMsg("parameter", "imk-smc", imksmc),
            new SimpleMsg("parameter", "imk-ac", imkac),
            new SimpleMsg("parameter", "Destination PIN Block Format", destinationPINBlockFormat)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Translate PIN block format and Generate Secure Messaging MAC", cmdParameters));
      try {
        Pair<EncryptedPIN,byte[]> r = translatePINGenerateSM_MACImpl( mkdm, skdm
                ,padm, imksmi, accountNo, acctSeqNo, atc, arqc, data, currentPIN
                ,newPIN, kd1, imksmc, imkac, destinationPINBlockFormat);
        SimpleMsg[] cmdResults = {
              new SimpleMsg("result", "Translated PIN block", r.getValue0()),
              new SimpleMsg("result", "Generated MAC", r.getValue1() == null ? "" : ISOUtil.hexString(r.getValue1()))
        };
        evt.addMessage(new SimpleMsg("results", "Complex results", cmdResults));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
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

    public byte[] generateEDE_MAC (byte[] data, SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "data", data), new SimpleMsg("parameter", "data key",
                    kd),
        };
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

    public SecureDESKey translateKeyFromOldLMK (SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key under old LMK", kd)
        };
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

    public void eraseOldLMK () throws SMException {
        SimpleMsg[] cmdParameters =  {
        };
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
     * Your SMAdapter should override this method if it has this functionality
     * @param kd
     * @return generated Key Check Value
     * @throws SMException
     */
    protected byte[] generateKeyCheckValueImpl (SecureDESKey kd) throws SMException {
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
     * @param pinUnderLMK
     * @param pvkA
     * @param pvkB
     * @param pvkIdx
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException 
     */
    protected String calculatePVVImpl(EncryptedPIN pinUnderLMK,
                       SecureDESKey pvkA, SecureDESKey pvkB, int pvkIdx,
                       List<String> excludes) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd1
     * @param kd1
     * @param pvkA
     * @param pvkB
     * @param pvkIdx
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException
     */
    protected String calculatePVVImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1,
                       SecureDESKey pvkA, SecureDESKey pvkB, int pvkIdx,
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
     * @return
     * @throws SMException 
     */
    protected boolean verifyPVVImpl(EncryptedPIN pinUnderKd, SecureDESKey kd, SecureDESKey pvkA,
                        SecureDESKey pvkB, int pvki, String pvv) throws SMException {
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
    protected String calculateIBMPINOffsetImpl(EncryptedPIN pinUnderLmk, SecureDESKey pvk,
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
    protected String calculateIBMPINOffsetImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1,
                              SecureDESKey pvk, String decTab, String pinValData,
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
     * @return
     * @throws SMException
     */
    protected boolean verifyIBMPINOffsetImpl(EncryptedPIN pinUnderKd, SecureDESKey kd
                            ,SecureDESKey pvk, String offset, String decTab
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
    protected EncryptedPIN deriveIBMPINImpl(String accountNo, SecureDESKey pvk
                              ,String decTab, String pinValData, int minPinLen
                              ,String offset) throws SMException {
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
    protected String calculateCVVImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                                   Date expDate, String serviceCode) throws SMException {
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
    protected boolean verifyCVVImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                        String cvv, Date expDate, String serviceCode) throws SMException {
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
     * @return
     * @throws SMException
     */
    protected boolean verifydCVVImpl(String accountNo, SecureDESKey imkac, String dcvv,
                     Date expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
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
     * @return
     * @throws SMException
     */
    protected boolean verifyCVC3Impl(SecureDESKey imkcvc3, String accountNo, String acctSeqNo,
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
     * @param transData
     * @return true if ARQC/TC/AAC is falid or false if not
     * @throws SMException
     */
    protected boolean verifyARQCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accountNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, byte[] transData) throws SMException {
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
    protected byte[] generateARPCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
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
     * @param arpcMethod
     * @param arc
     * @param propAuthData
     * @return calculated ARPC
     * @throws SMException
     */
    protected byte[] verifyARQCGenerateARPCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
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
            ,SecureDESKey imksmi, String accountNo, String acctSeqNo
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
           ,SKDMethod skdm, PaddingMethod padm, SecureDESKey imksmi
           ,String accountNo, String acctSeqNo, byte[] atc, byte[] arqc
           ,byte[] data, EncryptedPIN currentPIN, EncryptedPIN newPIN
           ,SecureDESKey kd1, SecureDESKey imksmc, SecureDESKey imkac
           ,byte destinationPINBlockFormat) throws SMException {
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

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param data
     * @param kd
     * @return generated EDE-MAC
     * @throws SMException
     */
    protected byte[] generateEDE_MACImpl (byte[] data, SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Translate key from encryption under the LMK held in “key change storage”
     * to encryption under a new LMK.
     *
     * @param kd the key encrypted under old LMK
     * @return key encrypted under the new LMK
     * @throws SMException if the parity of the imported key is not adjusted AND checkParity = true
     */
    protected SecureDESKey translateKeyFromOldLMKImpl (SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
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
}



