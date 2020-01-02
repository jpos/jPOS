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
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.bouncycastle.util.encoders.Base64;

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
            if (type.equals("ssh-rsa"))
            {
                BigInteger e = decodeBigInt();
                BigInteger m = decodeBigInt();
                RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
                return KeyFactory.getInstance("RSA").generatePublic(spec);
            }
            else if (type.equals("ssh-dss"))
            {
                BigInteger p = decodeBigInt();
                BigInteger q = decodeBigInt();
                BigInteger g = decodeBigInt();
                BigInteger y = decodeBigInt();
                DSAPublicKeySpec spec = new DSAPublicKeySpec(y, p, q, g);
                return KeyFactory.getInstance("DSA").generatePublic(spec);
            }
            else
            {
                throw new IllegalArgumentException("unknown type " + type);
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
    }
}
