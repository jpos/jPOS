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

package org.jpos.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.q2.install.ModuleUtils;
import org.jpos.security.SystemSeed;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PGPHelper {
    private static KeyFingerPrintCalculator fingerPrintCalculator = new BcKeyFingerprintCalculator();
    private static final String PUBRING = "META-INF/.pgp/pubring.asc";
    private static final String SIGNER = "license@jpos.org";
    static {
        if(Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
    }

    private static boolean verifySignature(InputStream in, PGPPublicKey pk) throws IOException, PGPException {
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
        PGPObjectFactory pgpf = new PGPObjectFactory(ain, fingerPrintCalculator);
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

    private static PGPPublicKey readPublicKey(InputStream in, String id)
            throws IOException, PGPException
    {
        in = PGPUtil.getDecoderStream(in);
        id = id.toLowerCase();

        PGPPublicKeyRingCollection pubRings = new PGPPublicKeyRingCollection(in, fingerPrintCalculator);
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
                if (pgpKey.isEncryptionKey() && isId && Arrays.equals(new byte[] {
                  (byte) 0x59, (byte) 0xA9, (byte) 0x23, (byte) 0x24, (byte) 0xE9, (byte) 0x3B, (byte) 0x28, (byte) 0xE8,
                  (byte) 0xA3, (byte) 0x82, (byte) 0xA0, (byte) 0x51, (byte) 0xE4, (byte) 0x32, (byte) 0x78, (byte) 0xEE,
                  (byte) 0xF5, (byte) 0x9D, (byte) 0x8B, (byte) 0x45}, pgpKey.getFingerprint())) {
                    return pgpKey;
                }
            }
        }
        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }
    public static boolean checkSignature() {
        boolean ok = false;
        try (InputStream is = getLicenseeStream()) {
            InputStream ks = Q2.class.getClassLoader().getResourceAsStream(PUBRING);
            PGPPublicKey pk = PGPHelper.readPublicKey(ks, SIGNER);
            ok = verifySignature(is, pk);
        } catch (Exception ignored) {
            // NOPMD: signature isn't good
        }
        return ok;
    }

    public static int checkLicense() {
        int rc = 0x90000;
        boolean newl = false;
        int ch;

        try (InputStream in = getLicenseeStream()){
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
            PGPObjectFactory pgpf = new PGPObjectFactory(ain, fingerPrintCalculator);
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
                        String h = ModuleUtils.getSystemHash();
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
                            if (s.contains(h)) {
                                rc &= 0xEFFFF;
                            }
                        }
                    }
                }
                if (!Arrays.equals(Q2.PUBKEYHASH, mac.doFinal(pk.getEncoded())))
                    rc |= 0x20000;
                if (ModuleUtils.getRKeys().contains(PGPHelper.getLicenseeHash()))
                    rc |= 0x80000;
            }
        } catch (Exception ignored) {
            // NOPMD: signature isn't good
        }
        return rc;
    }

    static InputStream getLicenseeStream() throws FileNotFoundException {
        String lf = System.getProperty("LICENSEE");
        File l = new File (lf != null ? lf : Q2.LICENSEE);
        return l.canRead() && l.length() < 8192 ? new FileInputStream(l) : Q2.class.getClassLoader().getResourceAsStream(Q2.LICENSEE);
    }
    public static String getLicensee() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = getLicenseeStream()) {
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                PrintStream p = new PrintStream(baos);
                p.println();
                p.println();
                while (br.ready())
                    p.println(br.readLine());
            }
        }
        return baos.toString();
    }
    public static String getLicenseeHash() throws IOException, NoSuchAlgorithmException {
        return ISOUtil.hexString(hash(getLicensee()));
    }

    /**
     * Simple PGP encryptor between byte[].
     *
     * @param clearData The test to be encrypted
     * @param keyRing public key ring input stream
     * @param fileName  File name. This is used in the Literal Data Packet (tag 11)
     *                  which is really only important if the data is to be related to
     *                  a file to be recovered later. Because this routine does not
     *                  know the source of the information, the caller can set
     *                  something here for file name use that will be carried. If this
     *                  routine is being used to encrypt SOAP MIME bodies, for
     *                  example, use the file name from the MIME type, if applicable.
     *                  Or anything else appropriate.
     * @param withIntegrityCheck true if an integrity packet is to be included
     * @param armor true for ascii armor
     * @param ids destination ids
     * @return encrypted data.
     * @throws IOException
     * @throws PGPException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public static byte[] encrypt(byte[] clearData, InputStream keyRing,
                                 String fileName, boolean withIntegrityCheck,
                                 boolean armor, String... ids)
      throws IOException, PGPException, NoSuchProviderException, NoSuchAlgorithmException {
        if (fileName == null) {
            fileName = PGPLiteralData.CONSOLE;
        }
        PGPPublicKey[] encKeys = readPublicKeys(keyRing, ids);
        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        OutputStream out = encOut;
        if (armor) {
            out = new ArmoredOutputStream(out);
        }
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(
          PGPCompressedDataGenerator.ZIP);
        OutputStream cos = comData.open(bOut); // compressed output stream
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(cos,
          PGPLiteralData.BINARY, fileName,
          clearData.length,
          new Date()
        );
        pOut.write(clearData);

        lData.close();
        comData.close();
        BcPGPDataEncryptorBuilder dataEncryptor =
          new BcPGPDataEncryptorBuilder(PGPEncryptedData.TRIPLE_DES);
        dataEncryptor.setWithIntegrityPacket(withIntegrityCheck);
        dataEncryptor.setSecureRandom(new SecureRandom());

        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(dataEncryptor);
        for (PGPPublicKey pk : encKeys)
            cPk.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(pk));

        byte[] bytes = bOut.toByteArray();
        OutputStream cOut = cPk.open(out, bytes.length);
        cOut.write(bytes);
        cOut.close();
        out.close();
        return encOut.toByteArray();
    }


    /**
     * Simple PGP encryptor between byte[].
     *
     * @param clearData The test to be encrypted
     * @param keyRing public key ring input stream
     * @param fileName  File name. This is used in the Literal Data Packet (tag 11)
     *                  which is really only important if the data is to be related to
     *                  a file to be recovered later. Because this routine does not
     *                  know the source of the information, the caller can set
     *                  something here for file name use that will be carried. If this
     *                  routine is being used to encrypt SOAP MIME bodies, for
     *                  example, use the file name from the MIME type, if applicable.
     *                  Or anything else appropriate.
     * @param withIntegrityCheck true if an integrity packet is to be included
     * @param armor true for ascii armor
     * @param ids destination ids
     * @return encrypted data.
     * @throws IOException
     * @throws PGPException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     */
    public static byte[] encrypt(byte[] clearData, String keyRing,
                                 String fileName, boolean withIntegrityCheck,
                                 boolean armor, String... ids)
      throws IOException, PGPException, NoSuchProviderException, NoSuchAlgorithmException {
        return encrypt (clearData, new FileInputStream(keyRing), fileName, withIntegrityCheck, armor, ids);
    }

    /**
     * decrypt the passed in message stream
     *
     * @param encrypted The message to be decrypted.
     * @param password  Pass phrase (key)
     * @return Clear text as a byte array. I18N considerations are not handled
     *         by this routine
     * @throws IOException
     * @throws PGPException
     * @throws NoSuchProviderException
     */
    public static byte[] decrypt(byte[] encrypted, InputStream keyIn, char[] password)
      throws IOException, PGPException, NoSuchProviderException {
        InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(encrypted));
        PGPObjectFactory pgpF = new PGPObjectFactory(in, fingerPrintCalculator);
        PGPEncryptedDataList enc;
        Object o = pgpF.nextObject();

        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        //
        // find the secret key
        //
        Iterator it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
          PGPUtil.getDecoderStream(keyIn), fingerPrintCalculator);

        while (sKey == null && it.hasNext()) {
            pbe = (PGPPublicKeyEncryptedData) it.next();
            sKey = findSecretKey(pgpSec, pbe.getKeyID(), password);
        }

        if (sKey == null) {
            throw new IllegalArgumentException(
              "secret key for message not found.");
        }

        InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));
        PGPObjectFactory pgpFact = new PGPObjectFactory(clear, fingerPrintCalculator);
        PGPCompressedData cData = (PGPCompressedData) pgpFact.nextObject();
        pgpFact = new PGPObjectFactory(cData.getDataStream(), fingerPrintCalculator);
        PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();
        InputStream unc = ld.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch;

        while ((ch = unc.read()) >= 0) {
            out.write(ch);
        }
        byte[] returnBytes = out.toByteArray();
        out.close();
        return returnBytes;
    }

    /**
     * decrypt the passed in message stream
     *
     * @param encrypted The message to be decrypted.
     * @param password  Pass phrase (key)
     * @return Clear text as a byte array. I18N considerations are not handled
     *         by this routine
     * @throws IOException
     * @throws PGPException
     * @throws NoSuchProviderException
     */
    public static byte[] decrypt(byte[] encrypted, String keyIn, char[] password)
      throws IOException, PGPException, NoSuchProviderException {
        return decrypt (encrypted, new FileInputStream(keyIn), password);
    }


    private static PGPPublicKey[] readPublicKeys(InputStream in, String[] ids)
      throws IOException, PGPException
    {
        in = PGPUtil.getDecoderStream(in);
        List<PGPPublicKey> keys = new ArrayList<>();

        PGPPublicKeyRingCollection pubRings = new PGPPublicKeyRingCollection(in, fingerPrintCalculator);
        Iterator rIt = pubRings.getKeyRings();
        while (rIt.hasNext()) {
            PGPPublicKeyRing pgpPub = (PGPPublicKeyRing) rIt.next();
            try {
                pgpPub.getPublicKey();
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            Iterator kIt = pgpPub.getPublicKeys();
            boolean isId = false;
            while (kIt.hasNext()) {
                PGPPublicKey pgpKey = (PGPPublicKey) kIt.next();

                Iterator iter = pgpKey.getUserIDs();
                while (iter.hasNext()) {
                    String uid = (String) iter.next();
                    // System.out.println("    uid: " + uid + " isEncryption? "+ pgpKey.isEncryptionKey());
                    for (String id : ids) {
                        if (uid.toLowerCase().indexOf(id.toLowerCase()) >= 0) {
                            isId = true;
                        }
                    }
                }
                if (isId && pgpKey.isEncryptionKey()) {
                    keys.add(pgpKey);
                    isId = false;
                }
            }
        }
        if (keys.size() == 0)
            throw new IllegalArgumentException("Can't find encryption key in key ring.");

        return keys.toArray(new PGPPublicKey[keys.size()]);
    }

    private static PGPPrivateKey findSecretKey(
      PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
      throws PGPException, NoSuchProviderException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

        if (pgpSecKey == null) {
            return null;
        }
        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(
          new BcPGPDigestCalculatorProvider()
        ).build(pass);

        return pgpSecKey.extractPrivateKey(decryptor);
    }

    private static byte[] hash (String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(s.getBytes(StandardCharsets.UTF_8));
    }
}
