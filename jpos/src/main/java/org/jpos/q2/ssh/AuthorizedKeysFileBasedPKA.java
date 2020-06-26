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

package org.jpos.q2.ssh;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import org.apache.sshd.common.cipher.ECCurves;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.buffer.BufferUtils;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.util.encoders.Base64;

import static org.apache.sshd.common.config.keys.impl.ECDSAPublicKeyEntryDecoder.MAX_ALLOWED_POINT_SIZE;
import static org.apache.sshd.common.config.keys.impl.ECDSAPublicKeyEntryDecoder.MAX_CURVE_NAME_LENGTH;
import static org.apache.sshd.common.util.security.eddsa.Ed25519PublicKeyDecoder.MAX_ALLOWED_SEED_LEN;
import static org.apache.sshd.common.util.security.eddsa.EdDSASecurityProviderUtils.CURVE_ED25519_SHA512;

public class AuthorizedKeysFileBasedPKA extends AbstractPKA
{
    String username;
    String filename;

    public AuthorizedKeysFileBasedPKA(String username,String filename)
    {
        this.filename = filename;
        this.username = username;
    }

    @Override
    protected String getUsername()
    {
        return username;
    }

    protected List<PublicKey> parseAuthorizedKeys() throws Exception
    {
        List<PublicKey> authorizedKeys = new ArrayList<>();
        AuthorizedKeysDecoder decoder = new AuthorizedKeysDecoder();
        File file = new File(filename);
        Scanner scanner = null;
        try
        {
            scanner = new Scanner(file).useDelimiter("\n");
            while (scanner.hasNext())
            {
                final PublicKey publicKey = decoder.decodePublicKey(scanner.next());
                authorizedKeys.add(publicKey);
            }
        }
        finally
        {
            if (scanner != null)
            {
                scanner.close();
            }
        }
        return authorizedKeys;
    }

    class AuthorizedKeysDecoder
    {
        private byte[] bytes;
        private int pos;

        public PublicKey decodePublicKey(String keyLine) throws Exception
        {
            bytes = null;
            pos = 0;

            for (String part : keyLine.split(" "))
            {
                if (part.startsWith("AAAA"))
                {
                    byte[] bytePart = part.getBytes();
                    bytes = Base64.decode(bytePart);
                    break;
                }
            }
            if (bytes == null)
            {
                throw new IllegalArgumentException("no Base64 part to decode");
            }

            String type = decodeType();
            if (KeyPairProvider.SSH_RSA.equals(type)) {
                BigInteger e = decodeBigInt();
                BigInteger m = decodeBigInt();
                KeyFactory keyFactory = SecurityUtils.getKeyFactory(KeyUtils.RSA_ALGORITHM);
                return keyFactory.generatePublic(new RSAPublicKeySpec(m, e));
            } else if (KeyPairProvider.SSH_DSS.equals(type)) {
                BigInteger p = decodeBigInt();
                BigInteger q = decodeBigInt();
                BigInteger g = decodeBigInt();
                BigInteger y = decodeBigInt();
                KeyFactory keyFactory = SecurityUtils.getKeyFactory(KeyUtils.DSS_ALGORITHM);
                return keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
            } else if (KeyPairProvider.SSH_ED25519.equals(type)) {
                byte[] seed = readRLEBytes(MAX_ALLOWED_SEED_LEN);
                EdDSAParameterSpec params = EdDSANamedCurveTable.getByName(CURVE_ED25519_SHA512);
                KeyFactory keyFactory = SecurityUtils.getKeyFactory(SecurityUtils.EDDSA);
                return keyFactory.generatePublic(new EdDSAPublicKeySpec(seed, params));
            } else {
                ECCurves curve = ECCurves.fromKeyType(type);
                if (curve == null) {
                    throw new IllegalArgumentException("unknown type " + type);
                }
                String keyCurveName = curve.getName();
                String encCurveName = decodeString(MAX_CURVE_NAME_LENGTH);
                if (!keyCurveName.equals(encCurveName)) {
                    throw new IllegalArgumentException(
                            "Mismatched key curve name (" + keyCurveName + ") vs. encoded one (" + encCurveName + ")");
                }
                byte[] octets = readRLEBytes(MAX_ALLOWED_POINT_SIZE);
                ECPoint w;
                try {
                    w = ECCurves.octetStringToEcPoint(octets);
                    if (w == null) {
                        throw new IllegalArgumentException(
                                "No ECPoint generated for curve=" + curve.getName()
                                        + " from octets=" + BufferUtils.toHex(':', octets));
                    }
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException(
                            "Failed (" + e.getClass().getSimpleName() + ")"
                                    + " to generate ECPoint for curve=" + curve.getName()
                                    + " from octets=" + BufferUtils.toHex(':', octets)
                                    + ": " + e.getMessage());
                }
                ECParameterSpec params = curve.getParameters();
                KeyFactory keyFactory = SecurityUtils.getKeyFactory(KeyUtils.EC_ALGORITHM);
                return keyFactory.generatePublic(new ECPublicKeySpec(w, params));
            }
        }

        private String decodeType()
        {
            int len = decodeInt();
            String type = new String(bytes, pos, len);
            pos += len;
            return type;
        }

        private int decodeInt()
        {
            return ((bytes[pos++] & 0xFF) << 24) | ((bytes[pos++] & 0xFF) << 16)
                   | ((bytes[pos++] & 0xFF) << 8) | (bytes[pos++] & 0xFF);
        }

        private BigInteger decodeBigInt()
        {
            int len = decodeInt();
            byte[] bigIntBytes = new byte[len];
            System.arraycopy(bytes, pos, bigIntBytes, 0, len);
            pos += len;
            return new BigInteger(bigIntBytes);
        }

        private byte[] readRLEBytes(int maxAllowed)
        {
            int len = decodeInt();
            if (len > maxAllowed) {
                throw new IllegalArgumentException(
                        "Requested block length (" + len + ") exceeds max. allowed (" + maxAllowed + ")");
            }
            if (len < 0) {
                throw new IllegalArgumentException("Negative block length requested: " + len);
            }
            byte[] RLEBytes = new byte[len];
            System.arraycopy(bytes, pos, RLEBytes, 0, len);
            pos += len;
            return RLEBytes;
        }

        private String decodeString(int maxChars)
        {
            byte[] result = readRLEBytes(maxChars * 4);
            return new String(result, StandardCharsets.UTF_8);
        }
    }
}
