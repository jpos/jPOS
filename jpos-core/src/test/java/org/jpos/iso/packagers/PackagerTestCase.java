/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

import junit.framework.TestCase;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.TestUtils;
import org.jpos.iso.packager.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * Simple 'HelloWorld' like TestCase
 * used to play with jUnit and to verify
 * build/junit system is running OK.
 */
public class PackagerTestCase extends TestCase {
    private XMLPackager xmlPackager;
    public static final String PREFIX = "../../modules/jpos/test/org/jpos/iso/packagers/";

    private ISOMsg getMsg (String message) throws Exception {
        FileInputStream fis = new FileInputStream (PREFIX + message + ".xml");
        byte[] b = new byte[fis.available()];
        fis.read (b);
        ISOMsg m = new ISOMsg ();
        m.setPackager (xmlPackager);
        m.unpack (b);
        fis.close();
        return m;
    }
    private byte[] getImage (String message) throws Exception {
        FileInputStream fis = new FileInputStream (PREFIX + message + ".bin");
        byte[] b = new byte[fis.available()];
        fis.read (b);
        fis.close();
        return b;
    }

    private void writeImage (String message, byte[] b) throws Exception {
        FileOutputStream fos = new FileOutputStream (PREFIX + message + ".run");
        fos.write (b);
        fos.close();
    }

    public void setUp () throws Exception {
        xmlPackager = new XMLPackager();
    }
    public PackagerTestCase (String name) {
        super (name);
    }
    public void testPostPackager () throws Exception {
        doTest (new PostPackager(), "post", "post");
    }
    public void testISO87APackager() throws Exception {
        doTest (new ISO87APackager(), "ISO87", "ISO87APackager");
    }
    public void testISO87BPackager() throws Exception {
        doTest (new ISO87BPackager(), "ISO87", "ISO87BPackager");
    }
    public void testGeneric87ascii() throws Exception {
        doTest (new GenericPackager ("../cfg/packager/iso87ascii.xml"),
            "ISO87", "ISO87APackager");
    }
    public void testGeneric87binary() throws Exception {
        doTest (new GenericPackager ("../cfg/packager/iso87binary.xml"),
            "ISO87", "ISO87BPackager");
    }
    public void testISO93APackager() throws Exception {
        doTest (new ISO93APackager(), "ISO93", "ISO93APackager");
    }
    public void testISO93BPackager() throws Exception {
        doTest (new ISO93BPackager(), "ISO93", "ISO93BPackager");
    }
    public void testGeneric93ascii() throws Exception {
        doTest (new GenericPackager ("../cfg/packager/iso93ascii.xml"),
            "ISO93", "ISO93APackager");
    }
    public void testGeneric93binary() throws Exception {
        doTest (new GenericPackager ("../cfg/packager/iso93binary.xml"),
            "ISO93", "ISO93BPackager");
    }        
    public void testF64Binary() throws Exception {
        doTest (new GenericPackager ("../cfg/packager/iso87binary.xml"),
            "ISO87-Field64", "ISO87B-Field64");
    }
    public void testF64ascii() throws Exception {
        doTest (new GenericPackager ("../cfg/packager/iso87ascii.xml"),
            "ISO87-Field64", "ISO87A-Field64");
    }
    public void testXMLPackager () throws Exception {
        doTest (xmlPackager, "XMLPackager", "XMLPackager");
    }
    
    public void testGeneric93ebcdic() throws Exception {
        doTest (new GenericPackager ("../cfg/packager/iso93ebcdic-custom.xml"),
            "ISO93ebcdic-Custom-XmlMsg", "ISO93ebcdic-Custom-Img");        
}    
    private void doTest (ISOPackager packager, String msg, String img)
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
}

