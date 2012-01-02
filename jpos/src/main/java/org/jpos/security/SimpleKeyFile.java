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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;


/**
 * Implements SecureKeyStore using a properties file.
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 * @see java.util.Properties
 */
public class SimpleKeyFile
        implements SecureKeyStore, Configurable, LogSource  {
    Properties props = new Properties();
    File file;
    String header = "Key File";
    protected Logger logger = null;
    protected String realm = null;

    public SimpleKeyFile () {
    }

    public SimpleKeyFile (String keyFileName) throws SecureKeyStoreException
    {
        init(keyFileName);
    }

    public void init (String keyFileName) throws SecureKeyStoreException {
        file = new File(keyFileName);
        try {
            if (!file.exists())
                file.createNewFile();
            load();
        } catch (Exception e) {
            throw  new SecureKeyStoreException(e);
        }
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
     *
     * @param cfg configuration object
     * @throws ConfigurationException
     */
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        try {
            init(cfg.get("key-file"));
            header = cfg.get("file-header", header);
        } catch (Exception e) {
            throw  new ConfigurationException(e);
        }
    }


    public synchronized SecureKey getKey (String alias) throws SecureKeyStoreException {
        SecureKey secureKey = null;
        LogEvent evt = new LogEvent(this, "get-key");
        evt.addMessage("alias", alias);
        try {
            load();
            String keyClassName = getProperty(alias, "class");
            Class c = Class.forName(keyClassName);
            secureKey = (SecureKey)c.newInstance();
            if (!(secureKey instanceof SecureDESKey))
                throw  new SecureKeyStoreException("Unsupported SecureKey class: " +
                        secureKey.getClass().getName());
            byte[] keyBytes = ISOUtil.hex2byte(getProperty(alias, "key"));
            short keyLength = Short.parseShort(getProperty(alias, "length"));
            String keyType = getProperty(alias, "type");
            byte[] KeyCheckValue = ISOUtil.hex2byte(getProperty(alias, "checkvalue"));
            secureKey = new SecureDESKey(keyLength, keyType, keyBytes, KeyCheckValue);
            evt.addMessage(secureKey);
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SecureKeyStoreException ? (SecureKeyStoreException) e : new SecureKeyStoreException(e);
        } finally {
            Logger.log(evt);
        }
        return  secureKey;
    }

    public synchronized void setKey (String alias, SecureKey secureKey) throws SecureKeyStoreException {
        LogEvent evt = new LogEvent(this, "set-key");
        evt.addMessage("alias", alias);
        evt.addMessage(secureKey);
        try {
            if (!(secureKey instanceof SecureDESKey))
                throw  new SecureKeyStoreException("Unsupported SecureKey class: " +
                        secureKey.getClass().getName());
            load();                 // load new changes (possibly made manually on the file)
            setProperty(alias, "class", secureKey.getClass().getName());
            setProperty(alias, "key", ISOUtil.hexString(secureKey.getKeyBytes()));
            setProperty(alias, "length", new Short(secureKey.getKeyLength()).toString());
            setProperty(alias, "type", secureKey.getKeyType());
            String keyCheckValueHexString = ISOUtil.hexString(((SecureDESKey)secureKey).getKeyCheckValue());
            setProperty(alias, "checkvalue", keyCheckValueHexString);
            store();
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SecureKeyStoreException ? (SecureKeyStoreException) e : new SecureKeyStoreException(e);
        } finally {
            Logger.log(evt);
        }
    }

    void load () throws SecureKeyStoreException {
        FileInputStream in;
        try {
            if (!file.canRead())
                throw  new SecureKeyStoreException("Can't read from file: " + file.getCanonicalPath());
            in = new FileInputStream(file);
            props.load(in);
            in.close();
        } catch (Exception e) {
            throw  new SecureKeyStoreException(e);
        }
    }

    void store () throws SecureKeyStoreException {
        FileOutputStream out;
        try {
            if (!file.canWrite())
                throw  new SecureKeyStoreException("Can't write to file: " + file.getCanonicalPath());
            out = new FileOutputStream(file);
            props.store(out, header);
            out.flush();
            out.close();
        } catch (Exception e) {
            throw  new SecureKeyStoreException(e);
        }
    }

    public String getProperty (String alias, String subName) throws SecureKeyStoreException {
        // key here has nothing to do with cryptographic keys
        String key = alias + "." + subName;
        String value = props.getProperty(key);
        if (value == null)
            throw  new SecureKeyStoreException("Key can't be retrieved. Can't get property: " + key);
        return value.trim();
    }

    public void setProperty (String alias, String subName, String value) {
        // key here has nothing to do with cryptographic keys
        String key = alias + "." + subName;
        props.setProperty(key, value);
    }

    public Map<String,SecureKey> getKeys() throws SecureKeyStoreException {
      Map keys    = new Hashtable();
      for ( Object k :props.keySet() ){
        String alias = ((String)k).split("\\.")[0];
        if ( !keys.containsKey(alias) ){
          keys.put(alias,getKey(alias));
        }
      }
      return keys;
    }
}



