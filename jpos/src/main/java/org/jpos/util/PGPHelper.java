/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

package org.jpos.util;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.jpos.q2.Q2;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PGPHelper {
    private static KeyFingerPrintCalculator fingerPrintCalculater = new BcKeyFingerprintCalculator();
    private static final String PUBRING = "META-INF/.pgp/pubring.asc";
    private static final String SIGNER = "license@jpos.org";
    static {
        Security.addProvider(new BouncyCastleProvider());
//        PGPUtil.setDefaultProvider("BC");
    }

    public static boolean verifySignature (InputStream in, PGPPublicKey pk) throws IOException, NoSuchProviderException, PGPException, SignatureException {
        boolean verify = false;
        boolean newl = false;
        int ch;
        ArmoredInputStream ain = new ArmoredInputStream(in, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while ((ch = ain.read()) >= 0 && ain.isClearText()) {
            if (newl) {
                out.write((byte) '\n');
                newl = false;
            }
            if (ch == '\n') {
                newl = true;
                continue;
            }
            out.write((byte) ch);
        }
        PGPObjectFactory pgpf = new PGPObjectFactory(ain, fingerPrintCalculater);
        Object o = pgpf.nextObject();
        if (o instanceof PGPSignatureList) {
            PGPSignatureList list = (PGPSignatureList)o;
            if (list.size() > 0) {
                PGPSignature sig = list.get(0);
                sig.init (new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), pk);
                while ((ch = ain.read()) >= 0 && ain.isClearText()) {
                    if (newl) {
                        out.write((byte) '\n');
                        newl = false;
                    }
                    if (ch == '\n') {
                        newl = true;
                        continue;
                    }
                    out.write((byte) ch);
                }
                sig.update(out.toByteArray());
                verify = sig.verify();
            }
        }
        return verify;
    }

    public static PGPPublicKey readPublicKey(InputStream in, String id)
            throws IOException, PGPException
    {
        in = PGPUtil.getDecoderStream(in);
        id = id.toLowerCase();

        PGPPublicKeyRingCollection pubRings = new PGPPublicKeyRingCollection(in, fingerPrintCalculater);
        Iterator rIt = pubRings.getKeyRings();
        while (rIt.hasNext()) {
            PGPPublicKeyRing pgpPub = (PGPPublicKeyRing) rIt.next();
            try {
                pgpPub.getPublicKey();
            }
            catch (Exception ignored) {
                continue;
            }
            Iterator kIt = pgpPub.getPublicKeys();
            boolean isId = false;
            while (kIt.hasNext()) {
                PGPPublicKey pgpKey = (PGPPublicKey) kIt.next();

                Iterator iter = pgpKey.getUserIDs();
                while (iter.hasNext()) {
                    String uid = (String) iter.next();
                    if (uid.toLowerCase().contains(id)) {
                        isId = true;
                        break;
                    }
                }
                if (pgpKey.isEncryptionKey() && isId) {
                    return pgpKey;
                }
            }
        }
        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }
    public static boolean checkSignature() {
        boolean ok = false;
        try {
            InputStream is = Q2.class.getClassLoader().getResourceAsStream(Q2.LICENSEE);
            InputStream ks = Q2.class.getClassLoader().getResourceAsStream(PUBRING);
            PGPPublicKey pk = PGPHelper.readPublicKey(ks, SIGNER);
            ok = verifySignature(is, pk);
        } catch (Exception ignored) {
            // NOPMD: signature isn't good
        }
        return ok;
    }

    public static int checkLicense() {
        int rc = 0x80000;
        boolean newl = false;
        int ch;

        try {
            InputStream in = Q2.class.getClassLoader().getResourceAsStream(Q2.LICENSEE);
            InputStream ks = Q2.class.getClassLoader().getResourceAsStream(PUBRING);
            PGPPublicKey pk = readPublicKey(ks, SIGNER);
            ArmoredInputStream ain = new ArmoredInputStream(in, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pk.getFingerprint(), "HmacSHA256"));

            while ((ch = ain.read()) >= 0 && ain.isClearText()) {
                if (newl) {
                    out.write((byte) '\n');
                    newl = false;
                }
                if (ch == '\n') {
                    newl = true;
                    continue;
                }
                out.write((byte) ch);
            }
            PGPObjectFactory pgpf = new PGPObjectFactory(ain, fingerPrintCalculater);
            Object o = pgpf.nextObject();
            if (o instanceof PGPSignatureList) {
                PGPSignatureList list = (PGPSignatureList) o;
                if (list.size() > 0) {
                    PGPSignature sig = list.get(0);
                    sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), pk);
                    while ((ch = ain.read()) >= 0 && ain.isClearText()) {
                        if (newl) {
                            out.write((byte) '\n');
                            newl = false;
                        }
                        if (ch == '\n') {
                            newl = true;
                            continue;
                        }
                        out.write((byte) ch);
                    }
                    sig.update(out.toByteArray());
                    if (sig.verify()) {
                        rc &= 0x7FFFF;
                        ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
                        String s;
                        Pattern p1 = Pattern.compile("\\s(valid through:)\\s(\\d\\d\\d\\d-\\d\\d-\\d\\d)?", Pattern.CASE_INSENSITIVE);
                        Pattern p2 = Pattern.compile("\\s(instances:)\\s([\\d]{0,4})?", Pattern.CASE_INSENSITIVE);
                        while ((s = reader.readLine()) != null) {
                            Matcher matcher = p1.matcher(s);
                            if (matcher.find() && matcher.groupCount() == 2) {
                                String lDate = matcher.group(2);
                                if (lDate.compareTo(Q2.getBuildTimestamp().substring(0, 10)) < 0) {
                                    rc |= 0x40000;
                                }
                            }
                            matcher = p2.matcher(s);
                            if (matcher.find() && matcher.groupCount() == 2) {
                                rc |= Integer.parseInt(matcher.group(2));
                            }
                        }
                    }
                }
                if (!Arrays.equals(Q2.PUBKEYHASH, mac.doFinal(pk.getEncoded())))
                    rc |= 0x20000;
            }
        } catch (Exception ignored) {
            // NOPMD: signature isn't good
        }
        return rc;
    }
}
