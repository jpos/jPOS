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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;


/**
 * Implements SecureKeyStore using a properties file.
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 * @see java.util.Properties
 */
public class SimpleKeyFile
        implements SecureKeyStore, ReConfigurable, LogSource  {
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
        return  value;
    }

    public void setProperty (String alias, String subName, String value) {
        // key here has nothing to do with cryptographic keys
        String key = alias + "." + subName;
        props.setProperty(key, value);
    }
}



