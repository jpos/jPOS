/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import org.jpos.core.Environment;
import org.jpos.iso.ISOUtil;
import org.jpos.log.evt.License;
import org.jpos.q2.Q2;
import org.jpos.q2.install.ModuleUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.bouncycastle.bcpg.ArmoredOutputStream.VERSION_HDR;

/**
 * PGP utility helpers used by jPOS for license verification, public-key
 * loading, and simple encryption/decryption with Bouncy Castle.
 */
public class PGPHelper {
    /** Utility class; instances carry no state. */
    public PGPHelper() {}
    private static KeyFingerPrintCalculator fingerPrintCalculator = new BcKeyFingerprintCalculator();
    private static final String PUBRING = "META-INF/.pgp/pubring.asc";
    private static final String SIGNER = "license@jpos.org";
    private static int node;
    static {
        if(Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());

        String nodeString = Environment.get("${q2.node:1}");
        Pattern pattern = Pattern.compile("\\d+");

        try {
            Matcher matcher = pattern.matcher(nodeString);
            node = (nodeString == null || nodeString.isEmpty())
              ? 1 // Default value if nodeString is null or empty
              : (matcher.find()
              ? Integer.parseInt(matcher.group()) // Use matched digits if found
              : 1); // Default value if no match is found
        } catch (Throwable e) {
            node = 0; // Fallback to default value in case of any exception
        }
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

    private static String readClearText(InputStream in) throws IOException {
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
        return out.toString(StandardCharsets.UTF_8.name());
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
    /**
     * Verifies the signature on the bundled licensee file using the embedded jPOS public key.
     *
     * @return {@code true} if the signature verifies, {@code false} otherwise (including any error)
     */
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

    /**
     * Verifies the licensee file's signature, parses its metadata, and returns
     * a packed status code combining expiration, fingerprint match, instance
     * count, and revocation flags.
     *
     * @return packed status code; bits encode validity, expiration, fingerprint
     *         match, revocation, and the configured instance count
     */
    public static int checkLicense() {
        try (InputStream in = getLicenseeStream()){
            return checkLicense(in);
        } catch (Exception ignored) {
            // NOPMD: signature isn't good
        }
        return 0x90000;
    }

    private static int checkLicense(InputStream in) {
        int rc = 0x90000;
        boolean newl = false;
        int ch;

        try {
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
                        BufferedReader reader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8));
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
                                int n = Integer.parseInt(matcher.group(2));
                                node = n >= node ? node : 0;
                                rc |= n;
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

    /**
     * Returns the verified clear-text license payload.
     * <p>
     * The returned value is the text covered by the clear-text PGP signature, not
     * the armored license block. If the bundled or configured license cannot be
     * signature-verified, or if {@link #checkLicense()} reports an unacceptable
     * status, this method returns {@code null}.
     * <p>
     * Status bit {@code 0x10000} (license not bound to this system hash, used by
     * the Community Edition license) is considered acceptable. Critical status
     * bits {@code 0xE0000} are not.
     *
     * @return verified clear-text license payload, or {@code null}
     * @throws IOException if the license stream cannot be read
     */
    public static String getVerifiedLicenseText() throws IOException {
        byte[] license;
        try (InputStream is = getLicenseeStream()) {
            if (is == null)
                return null;
            license = is.readAllBytes();
        }
        try (InputStream ks = Q2.class.getClassLoader().getResourceAsStream(PUBRING)) {
            PGPPublicKey pk = readPublicKey(ks, SIGNER);
            if (!verifySignature(new ByteArrayInputStream(license), pk))
                return null;
        } catch (PGPException | RuntimeException e) {
            return null;
        }
        if ((checkLicense(new ByteArrayInputStream(license)) & 0xE0000) != 0)
            return null;
        return readClearText(new ByteArrayInputStream(license));
    }

    static InputStream getLicenseeStream() throws FileNotFoundException {
        String lf = System.getProperty("LICENSEE");
        File l = new File (lf != null ? lf : Q2.LICENSEE);
        return l.canRead() && l.length() < 8192 ? new FileInputStream(l) : Q2.class.getClassLoader().getResourceAsStream(Q2.LICENSEE);
    }
    /**
     * Returns the licensee file contents as a UTF-8 string with two leading blank lines.
     *
     * @return the licensee text, or empty if the licensee resource is unavailable
     * @throws IOException if reading the licensee stream fails
     */
    public static String getLicensee() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = getLicenseeStream()) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    PrintStream p = new PrintStream(baos, false, StandardCharsets.UTF_8.name());
                    p.println();
                    p.println();
                    while(br.ready())
                      p.println(br.readLine());
                }
            }
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }
    /**
     * Returns the SHA hex hash of the licensee text as produced by {@link #getLicensee()}.
     *
     * @return the hex-encoded hash
     * @throws IOException if the licensee stream cannot be read
     * @throws NoSuchAlgorithmException if the configured digest is not available
     */
    public static String getLicenseeHash() throws IOException, NoSuchAlgorithmException {
        return ISOUtil.hexString(hash(getLicensee()));
    }

    /**
     * Returns the resolved Q2 node number used during license validation.
     *
     * @return the Q2 node number, or 0 if it could not be resolved
     */
    public static int node () {
        return node;
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
     * @throws IOException if reading {@code keyRing} or writing the encrypted output fails
     * @throws PGPException if a PGP-level error occurs while building the message
     * @throws NoSuchProviderException if the {@code BC} provider is not registered
     * @throws NoSuchAlgorithmException if the requested cipher algorithm is unavailable
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
            out = ArmoredOutputStream.builder()
              .setVersion("BCPG/jPOS " + Q2.getVersion())
              .build(out);
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
     * @throws IOException if {@code keyRing} cannot be opened or the encrypted output cannot be written
     * @throws PGPException if a PGP-level error occurs while building the message
     * @throws NoSuchProviderException if the {@code BC} provider is not registered
     * @throws NoSuchAlgorithmException if the requested cipher algorithm is unavailable
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
     * @param keyIn secret key ring input stream
     * @param password  Pass phrase (key)
     * @return Clear text as a byte array. I18N considerations are not handled
     *         by this routine
     * @throws IOException if {@code keyIn} or the encrypted payload cannot be read
     * @throws PGPException if a PGP-level error occurs while decrypting
     * @throws NoSuchProviderException if the {@code BC} provider is not registered
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
     * @param keyIn path to the secret key ring file
     * @param password  Pass phrase (key)
     * @return Clear text as a byte array. I18N considerations are not handled
     *         by this routine
     * @throws IOException if the key file or encrypted payload cannot be read
     * @throws PGPException if a PGP-level error occurs while decrypting
     * @throws NoSuchProviderException if the {@code BC} provider is not registered
     */
    public static byte[] decrypt(byte[] encrypted, String keyIn, char[] password)
      throws IOException, PGPException, NoSuchProviderException {
        return decrypt (encrypted, new FileInputStream(keyIn), password);
    }

    /**
     * Returns the parsed jPOS {@link License} extracted from the licensee resource.
     *
     * @return the current license, including text and status flags
     * @throws IOException if the licensee stream cannot be read
     */
    public static License getLicense() throws IOException {
        return new License(getLicensee(), checkLicense());
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
