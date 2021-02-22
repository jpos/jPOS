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

import org.jpos.iso.ISOUtil;
import org.jpos.security.SystemSeed;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class SensitiveString implements Supplier<String> {
    private SecretKey key;
    private byte[] encoded;
    private static Random rnd;
    private static final String AES = "AES/CBC/PKCS5Padding";

    static {
        rnd = new SecureRandom();
    }

    public SensitiveString(String s) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        key = generateKey();
        encoded = encrypt(s.getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensitiveString that = (SensitiveString) o;
        return this.get().equals(that.get());
    }

    private SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        int maxKeyLength =  Cipher.getMaxAllowedKeyLength(keyGen.getAlgorithm());
        keyGen.init(maxKeyLength == Integer.MAX_VALUE ? 256 : maxKeyLength);
        return keyGen.generateKey();
    }

    private byte[] encrypt(byte[] b) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final Cipher cipher = Cipher.getInstance(AES);
        final byte[] iv = randomIV();
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] enc = cipher.doFinal(b);
        ByteBuffer buf = ByteBuffer.allocate(iv.length + enc.length);
        buf.put(ISOUtil.xor(iv, SystemSeed.getSeed(iv.length, iv.length)));
        buf.put(enc);
        return buf.array();
    }
    private byte[] decrypt(byte[] encoded)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
      IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException
    {
        byte[] iv = new byte[16];
        byte[] cryptogram = new byte[encoded.length - iv.length];
        System.arraycopy(encoded, 0, iv, 0, iv.length);
        System.arraycopy(encoded, iv.length, cryptogram, 0, cryptogram.length);
        final Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ISOUtil.xor(iv, SystemSeed.getSeed(iv.length, iv.length))));
        return cipher.doFinal(cryptogram);
    }

    private byte[] randomIV() {
        final byte[] b = new byte[16];
        rnd.nextBytes(b);
        return b;
    }

    @Override
    public String get() {
        try {
            return new String(decrypt(encoded));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | NoSuchProviderException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new AssertionError(e.getMessage());
        }
    }
}
