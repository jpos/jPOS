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

package org.jpos.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Defines the primary usage of the key contained in the key block.
 * <p>
 * Each value repesents bytes 5-6 of the Keyblok Header.
 * <p>
 * This class defines proprietary specific key usages. In the great majority of
 * cases, the ones defined by TR-31 {@link KeyUsage} will be sufficient. There
 * are no strong reasons for separating e.g. KEK keys to ZMK and TMK, or PINENC
 * keys to ZPK and TPK. KEK and PINENC should be enough.
 * <p>
 * However, when it is necessary to use, for example: private/public RSA keys
 * or HMAC keys, the only option is to use the proprietary key usages.
 * <p>
 * The proprietary key usages for {@code ExtKeyUsage} are optional and can be
 * defined in your resources at {@value ExtKeyUsage#EXTERNAL_KEY_USAGES}. A file
 * with the same name in the jPOS test resources can be used as an example.
 */
public class ExtKeyUsage extends KeyUsage {

    /**
     * Key usages by code key.
     * <p>
     * This field has to be first
     */
    private static final Map<String, KeyUsage> MAP = new LinkedHashMap<>();

    private static final String EXTERNAL_KEY_USAGES = "META-INF/org/jpos/security/proprietary-hsm.properties";

    private static final String KEY_USAGE_PREFIX    = "ku.";

    /**
     * Key usages by entry key.
     * <p>
     * This field has to be after {@link MAP} and before key usages;
     */
    private static final Map<String, KeyUsage> EXT_DEF = loadKeyUsagesFromClasspath(EXTERNAL_KEY_USAGES);

    /**
     * DEK - Data Encryption Key.
     *
     * @apiNote It is proprietary specific version of {@link KeyUsage#ENC}
     */
    public final static KeyUsage DEK        = getKeyUsage("DEK");

    /**
     * ZEK - Zone Encryption Key.
     *
     * @apiNote It is proprietary specific version of {@link KeyUsage#ENC}
     */
    public final static KeyUsage ZEK        = getKeyUsage("ZEK");

    /**
     * TEK - Terminal Encryption Key.
     *
     * @apiNote It is proprietary specific version of {@link KeyUsage#ENC}
     */
    public final static KeyUsage TEK        = getKeyUsage("TEK");

    /**
     * RSA Public Key.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage RSAPK      = getKeyUsage("RSAPK");

    /**
     * RSA Private Key for signing or key management.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage RSASK      = getKeyUsage("RSASK");

    /**
     * RSA Private Key for ICC personalization.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage RSASKICC   = getKeyUsage("RSASKICC");

    /**
     * RSA Private Key for PIN translation.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage RSASKPIN   = getKeyUsage("RSASKPIN");

    /**
     * RSA Private Key for TLS.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage RSASKTLS   = getKeyUsage("RSASKTLS");

    /**
     * TMK - Terminal Master Key.
     *
     * @apiNote It is proprietary specific version of {@link KeyUsage#KEK}
     */
    public final static KeyUsage TMK        = getKeyUsage("TMK");

    /**
     * ZMK - Zone Master Key.
     *
     * @apiNote It is proprietary specific version of {@link KeyUsage#KEK}
     */
    public final static KeyUsage ZMK        = getKeyUsage("ZMK");

    /**
     * HMAC key using SHA-1.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage HMACSHA1   = getKeyUsage("HMACSHA1");

    /**
     * HMAC key using SHA-224.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage HMACSHA224 = getKeyUsage("HMACSHA224");

    /**
     * HMAC key using SHA-256.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage HMACSHA256 = getKeyUsage("HMACSHA256");

    /**
     * HMAC key using SHA-384.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage HMACSHA384 = getKeyUsage("HMACSHA384");

    /**
     * HMAC key using SHA-512.
     *
     * @apiNote It is proprietary specific, there is no equivalent in TR-31.
     */
    public final static KeyUsage HMACSHA512 = getKeyUsage("HMACSHA512");

    /**
     * TPK - Terminal PIN Encryption Key.
     *
     * @apiNote It is proprietary specific version of {@link KeyUsage#PINENC}
     */
    public final static KeyUsage TPK        = getKeyUsage("TPK");

    /**
     * ZPK - Zone PIN Encryption Key.
     *
     * @apiNote It is proprietary specific version of {@link KeyUsage#PINENC}
     */
    public final static KeyUsage ZPK        = getKeyUsage("ZPK");


    /**
     * Internal constructor.
     * <p>
     * The constructor is protected to guarantee only one instance of the key
     * usage in the entire JVM. This makes it possible to use the operator
     * {@code ==} or {@code !=} as it does for enums.
     *
     * @param code the key usage code
     * @param name the usage name
     */
    protected ExtKeyUsage(String code, String name) {
        super(code, name);
    }

    private static KeyUsage getKeyUsage(String entry) {
        if (EXT_DEF == null)
            return null;

        return EXT_DEF.get(entry);
    }

    /**
     * Returns the enum constant of this type with the specified {@code code}.
     *
     * @param code
     * @return the enum constant with the specified processing code or
     *         {@code null} if unknown.
     */
    public static KeyUsage valueOfByCode(String code) {
        KeyUsage ku = MAP.get(code);
        if (ku != null)
            return ku;

        return TR31MAP.get(code);
    }

    public static Map<String, KeyUsage> entries() {
        Map ret = new LinkedHashMap(TR31MAP);
        ret.putAll(MAP);
        return Collections.unmodifiableMap(ret);
    }

    private static InputStream loadResourceAsStream(String name) {
        InputStream in = null;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null)
            in = contextClassLoader.getResourceAsStream(name);

        if (in == null)
            in = ExtKeyUsage.class.getClassLoader().getResourceAsStream(name);

        return in;
    }

    static Map<String, KeyUsage> loadKeyUsagesFromClasspath(String resource) {
        Properties props = new Properties();
        try (InputStream in = loadResourceAsStream(resource)) {
            props.load(in);
            return registerKeyUsages(props);
        } catch (IOException | NullPointerException ex) {
            return null;
        }
    }

    private static Map<String, KeyUsage> registerKeyUsages(Properties props) {
        Map<String, KeyUsage> ret = new LinkedHashMap<>();
        for (String key : props.stringPropertyNames()) {
            if (!key.startsWith(KEY_USAGE_PREFIX))
                continue;

            String value = props.getProperty(key);
            if (value == null || value.isEmpty())
                continue;

            String k = key.substring(KEY_USAGE_PREFIX.length());
            String[] entry = value.split(",");
            KeyUsage ku = new KeyUsage(entry[0], entry[1]);
            // Override is disabled
            if (MAP.containsKey(ku.getCode()))
                continue;

            ret.put(k, ku);
            MAP.put(ku.getCode(), ku);
        }

        return ret;
    }

}
