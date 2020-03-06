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

package org.jpos.iso.packagers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.TestUtils;
import org.jpos.iso.packager.*;
import org.jpos.util.Profiler;
import org.jpos.util.TPS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;

public class PackagerTestCase {
    private XMLPackager xmlPackager;
    public static final String PREFIX = "build/resources/test/org/jpos/iso/packagers/";

    private ISOMsg getMsg (String message) throws Exception {
        FileInputStream fis = new FileInputStream (PREFIX + message + ".xml");
        ISOMsg m = null;
        try {
            byte[] b = new byte[fis.available()];
            fis.read (b);
            m = new ISOMsg ();
            m.setPackager (xmlPackager);
            m.unpack (b);
        } finally {
            fis.close();
        }
        return m;
    }
    private byte[] getImage (String message) throws Exception {
        byte[] b = null;
        FileInputStream fis = new FileInputStream (PREFIX + message + ".bin");
        try {
            b = new byte[fis.available()];
            fis.read (b);
        } finally {
            fis.close();
        }
        return b;
    }

    private void writeImage (String message, byte[] b) throws Exception {
        FileOutputStream fos = new FileOutputStream (PREFIX + message + ".run");
        try{ 
           fos.write(b);
        } finally {
            fos.close();
        }
    }

    @BeforeEach
    public void setUp () throws Exception {
        xmlPackager = new XMLPackager();
    }
    @Test
    public void testPostPackager () throws Exception {
        doTest (new PostPackager(), "post", "post");
    }
    @Test
    public void testISO87APackager() throws Exception {
        doTest (new ISO87APackager(), "ISO87", "ISO87APackager");
    }
    @Test
    public void testISO87BPackager() throws Exception {
        doTest (new ISO87BPackager(), "ISO87", "ISO87BPackager");
    }
    @Test
    public void testGeneric87ascii() throws Exception {
        doTest (new GenericPackager ("src/main/resources/packager/iso87ascii.xml"),
            "ISO87", "ISO87APackager");
    }
    @Test
    public void testGeneric87asciiAsResource() throws Exception {
        doTest (new GenericPackager ("jar:packager/iso87ascii.xml"),
                "ISO87", "ISO87APackager");
    }

    @Test
    public void testGeneric87binary() throws Exception {
        doTest (new GenericPackager ("src/main/resources/packager/iso87binary.xml"),
            "ISO87", "ISO87BPackager");
    }
    @Test
    public void testISO93APackager() throws Exception {
        doTest (new ISO93APackager(), "ISO93", "ISO93APackager");
    }
    @Test
    public void testISO93BPackager() throws Exception {
        doTest (new ISO93BPackager(), "ISO93", "ISO93BPackager");
    }
    @Test
    public void testGeneric93ascii() throws Exception {
        doTest (new GenericPackager ("src/main/resources/packager/iso93ascii.xml"),
            "ISO93", "ISO93APackager");
    }
    @Test
    public void testGeneric93binary() throws Exception {
        doTest (new GenericPackager ("src/main/resources/packager/iso93binary.xml"), "ISO93", "ISO93BPackager");
    }        
    @Test
    public void testF64Binary() throws Exception {
        doTest (new GenericPackager ("src/main/resources/packager/iso87binary.xml"), "ISO87-Field64", "ISO87B-Field64");
    }
    @Test
    public void testF64ascii() throws Exception {
        doTest (new GenericPackager ("src/main/resources/packager/iso87ascii.xml"),
            "ISO87-Field64", "ISO87A-Field64");
    }
    @Test
    public void testXMLPackager () throws Exception {
        doTest (xmlPackager, "XMLPackager", "XMLPackager", true);
    }

    @Test
    public void testGeneric93ebcdic() throws Exception {
        doTest (new GenericPackager ("src/dist/cfg/packager/iso93ebcdic-custom.xml"), "ISO93ebcdic-Custom-XmlMsg", "ISO93ebcdic-Custom-Img");        
    }
    @Test
    public void testPerformance() throws Exception {
        final int COUNT = 100000;
        ISOPackager p = new GenericPackager ("src/main/resources/packager/iso87binary.xml");
        ISOMsg baseMsg = getMsg("ISO87");
        System.out.println ("\n--- pack/unpack performance test ---\n");
        Profiler prof = new Profiler();
        TPS tps = new TPS(true);
        for (int i=0; i<COUNT; i++) {
            pack (baseMsg, p);
            tps.tick();
        }
        prof.checkPoint ("PACK " + tps.toString());

        byte[] buf = baseMsg.pack();
        tps = new TPS(true);
        for (int i=0; i<COUNT; i++) {
            unpack (buf, p);
            tps.tick();
        }
        prof.checkPoint ("UNPACK " + tps.toString());

        tps = new TPS(true);
        for (int i=0; i<COUNT; i++) {
            pack (baseMsg, p);
            unpack (buf, p);
            tps.tick();
        }
        prof.checkPoint ("PACK/UNPACK " + tps.toString());
        tps = new TPS(true);
        for (int i=0; i<COUNT; i++) {
            updatePackAndUnpack(baseMsg, p);
            tps.tick();
        }
        prof.checkPoint ("UPDATE/PACK/UNPACK " + tps.toString());

        prof.dump(System.out, "");
        System.out.println ("");
    }
    private void pack (ISOMsg m, ISOPackager p) throws Exception {
        m.setPackager (p);
        m.pack();
    }
    private void unpack (byte[] buf, ISOPackager p) throws Exception {
        ISOMsg m = new ISOMsg();
        m.setPackager (p);
        m.unpack (buf);
    }

    private ISOMsg updatePackAndUnpack (ISOMsg m, ISOPackager p) throws Exception {
        Date now = new Date();
        m.setPackager (p);
        m.set(7, ISODate.getDateTime(now));
        m.set (12, ISODate.formatDate(now, "HHmmss"));
        m.set (13, ISODate.formatDate (now, "MMdd"));

        int stan = Integer.parseInt(m.getString(11)) % 1000000;
        if (++stan == 0L) stan++;
        m.set (11, Integer.toString(stan));
        m.set (4, Integer.toString (stan));

        ISOMsg m1 = new ISOMsg();
        m1.setPackager(p);
        m1.unpack (m.pack());
        return m1;
    }
    private void doTest (ISOPackager packager, String msg, String img)
        throws Exception
    {
        doTest(packager, msg, img, false);
    }
    private void doTest (ISOPackager packager, String msg, String img, boolean removeCRLF)
        throws Exception
    {
        // Logger logger = new Logger();
        // logger.addListener (new SimpleLogListener (System.out));
        // packager.setLogger (logger, msg + "-m");

        ISOMsg m = getMsg (msg);
        m.setPackager (packager);
        byte[] p = m.pack();
        ByteArrayOutputStream out = new ByteArrayOutputStream ();
        m.pack (out);

        assertTrue (Arrays.equals (out.toByteArray(), p));

        writeImage (img, p);

        byte[] b = getImage (img);
        if (removeCRLF) {
            TestUtils.assertEquals(removeAllCRLF(b), removeAllCRLF(p));
        } else {
            TestUtils.assertEquals(b, p);
        }

        ISOMsg m1 = new ISOMsg ();
        // packager.setLogger (logger, msg + "-m1");
        m1.setPackager (packager);
        m1.unpack (b);
        if (removeCRLF) {
            TestUtils.assertEquals(removeAllCRLF(b), removeAllCRLF(m1.pack()));
        } else {
            TestUtils.assertEquals(b, m1.pack());
        }

        ISOMsg m2 = new ISOMsg ();
        m2.setPackager (packager);
        // packager.setLogger (logger, msg + "-m2");
        m2.unpack (new ByteArrayInputStream (out.toByteArray()));
        if (removeCRLF) {
            TestUtils.assertEquals(removeAllCRLF(b), removeAllCRLF(m2.pack()));
        } else {
            TestUtils.assertEquals(b, m2.pack());
        }
    }

    /**
     * Used specifically when testing XmlPackager.  When messages are generated
     * on Windows they include a carriage return (CR) and is then compared against
     * a version that was generated on Unix.
     *
     * This function is used to remove CR and LF before comparison.
     *
     * @param in Byte array to modify.
     * @return Modified byte array.
     */
    private byte[] removeAllCRLF(byte[] in) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(in);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (byteArrayInputStream.available() > 0) {
            int b = byteArrayInputStream.read();
            if (b != 10 && b != 13) {
                byteArrayOutputStream.write(b);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    /*
    private void doPack (ISOMsg ISOPackager packager, String msg, String img)
        throws Exception
    {
        // Logger logger = new Logger();
        // logger.addListener (new SimpleLogListener (System.out));
        // packager.setLogger (logger, msg + "-m");

        ISOMsg m = getMsg (msg);
        m.setPackager (packager);
        byte[] p = m.pack();

        byte[] b = getImage (img);
        TestUtils.assertEquals(b, p);

        ISOMsg m1 = new ISOMsg ();
        // packager.setLogger (logger, msg + "-m1");
        m1.setPackager (packager);
        m1.unpack (b);
        TestUtils.assertEquals(b, m1.pack());

        ISOMsg m2 = new ISOMsg ();
        m2.setPackager (packager);
        // packager.setLogger (logger, msg + "-m2");
        m2.unpack (new ByteArrayInputStream (out.toByteArray()));
        TestUtils.assertEquals(b, m2.pack());
    }
    */

}

