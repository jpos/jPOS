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

package org.jpos.iso;


/**
 * @author joconnor
 */
public class PerformanceTesting
{
    public static void main(String[] args) throws Exception
    {
        if (args.length == 0) {
            performPackAll();
            performUnpackAll();
        } else if (args.length < 3)
        {
            System.out.println("Usage: PackagerClass Length fieldData");
        } else
        {
            runPack(args[0], Integer.parseInt(args[1]), args[2]);
        }
    }
    
    private static void performPackAll() throws Exception
    {
        runPack("org.jpos.iso.IF_CHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IF_TCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFA_AMOUNT", 20, "12345678");
        runPack("org.jpos.iso.IFA_BINARY", 5, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFA_FLLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFA_FLLNUM", 20, "1234");
        runPack("org.jpos.iso.IFA_LCHAR", 8, "ABCD");
        runPack("org.jpos.iso.IFA_LLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFA_LLBNUM", 20, "12345");
        runPack("org.jpos.iso.IFA_LLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFA_LLLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFA_LLLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFA_LLLLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFA_LLLLLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFA_LLLNUM", 20, "123456");
        runPack("org.jpos.iso.IFA_LLNUM", 20, "123456");
        runPack("org.jpos.iso.IFA_NUMERIC", 20, "123456");
        runPack("org.jpos.iso.IFB_AMOUNT", 20, "12345678");
        runPack("org.jpos.iso.IFB_BINARY", 5, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFB_LLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFB_LLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFB_LLHBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFB_LLHCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFB_LLHECHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFB_LLHFBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFB_LLHNUM", 20, "1234");
        runPack("org.jpos.iso.IFB_LLLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFB_LLLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFB_LLLHBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFB_LLLHECHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFB_LLLNUM", 20, "1234");
        runPack("org.jpos.iso.IFB_LLNUM", 20, "1234");
        runPack("org.jpos.iso.IFB_NUMERIC", 20, "123456");
        runPack("org.jpos.iso.IFE_CHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFE_LLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFE_LLLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runPack("org.jpos.iso.IFE_LLLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFE_LLNUM", 20, "123456");
        runPack("org.jpos.iso.IFE_NUMERIC", 20, "123456");
        runPack("org.jpos.iso.IFEB_LLLNUM", 20, "123456");
        runPack("org.jpos.iso.IFEB_LLNUM", 20, "123456");
        runPack("org.jpos.iso.IFEP_LLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFIPM_LLLCHAR", 20, "ABCDefgh1234");
        runPack("org.jpos.iso.IFMC_LLCHAR", 20, "ABCDefgh1234");
    }

    private static void performUnpackAll() throws Exception
    {
        runUnpack("org.jpos.iso.IF_CHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFA_AMOUNT", 20, "12345678");
        runUnpack("org.jpos.iso.IFA_BINARY", 5, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFA_FLLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFA_FLLNUM", 20, "1234");
        runUnpack("org.jpos.iso.IFA_LCHAR", 8, "ABCD");
        runUnpack("org.jpos.iso.IFA_LLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFA_LLBNUM", 20, "12345");
        runUnpack("org.jpos.iso.IFA_LLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFA_LLLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFA_LLLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFA_LLLLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFA_LLLLLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFA_LLLNUM", 20, "123456");
        runUnpack("org.jpos.iso.IFA_LLNUM", 20, "123456");
        runUnpack("org.jpos.iso.IFA_NUMERIC", 20, "123456");
        runUnpack("org.jpos.iso.IFB_AMOUNT", 20, "12345678");
        runUnpack("org.jpos.iso.IFB_BINARY", 5, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFB_LLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFB_LLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFB_LLHBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFB_LLHCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFB_LLHECHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFB_LLHFBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFB_LLHNUM", 20, "1234");
        runUnpack("org.jpos.iso.IFB_LLLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFB_LLLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFB_LLLHBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFB_LLLHECHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFB_LLLNUM", 20, "1234");
        runUnpack("org.jpos.iso.IFB_LLNUM", 20, "1234");
        runUnpack("org.jpos.iso.IFB_NUMERIC", 20, "123456");
        runUnpack("org.jpos.iso.IFE_CHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFE_LLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFE_LLLBINARY", 57, new byte[] {1, 17, 33, 49, 65});
        runUnpack("org.jpos.iso.IFE_LLLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFE_LLNUM", 20, "123456");
        runUnpack("org.jpos.iso.IFE_NUMERIC", 20, "123456");
        runUnpack("org.jpos.iso.IFEB_LLLNUM", 20, "123456");
        runUnpack("org.jpos.iso.IFEB_LLNUM", 20, "123456");
        runUnpack("org.jpos.iso.IFEP_LLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFIPM_LLLCHAR", 20, "ABCDefgh1234");
        runUnpack("org.jpos.iso.IFMC_LLCHAR", 20, "ABCDefgh1234");
    }

    public static void runPack(String packagerName, int len, String data) throws Exception
    {
        Class packagerClass = Class.forName(packagerName);
        ISOFieldPackager packager = (ISOFieldPackager)packagerClass.newInstance();
        packager.setLength(len);
        ISOField f = new ISOField(12, data);
        long start = System.currentTimeMillis();
        runPackTest(packager, f);
        long end = System.currentTimeMillis();
        boolean isOldImplementation = packagerClass.getSuperclass().getName().endsWith("ISOFieldPackager")
            || packagerClass.getSuperclass().getName().endsWith("IF_TBASE");
        System.out.println("Pack: " + (isOldImplementation ? "old " : "new ") + packager.getClass().getName() + " = " + (end - start));
        
    }

    public static void runPack(String packagerName, int len, byte[] data) throws Exception
    {
        Class packagerClass = Class.forName(packagerName);
        ISOFieldPackager packager = (ISOFieldPackager)packagerClass.newInstance();
        packager.setLength(len);
        ISOBinaryField f = new ISOBinaryField(12, data);
        long start = System.currentTimeMillis();
        runPackTest(packager, f);
        long end = System.currentTimeMillis();
        boolean isOldImplementation = packagerClass.getSuperclass().getName().endsWith("ISOFieldPackager")
            || packagerClass.getSuperclass().getName().endsWith("IF_TBASE");
        System.out.println("Pack: " + (isOldImplementation ? "old " : "new ") + packager.getClass().getName() + " = " + (end - start));
    }

    private static void runPackTest(ISOFieldPackager p, ISOComponent c) throws ISOException
    {
        for (int i = 0; i < 1000000; i++)
            p.pack(c);
    }

    public static void runUnpack(String packagerName, int len, String data) throws Exception
    {
        Class packagerClass = Class.forName(packagerName);
        ISOFieldPackager packager = (ISOFieldPackager)packagerClass.newInstance();
        packager.setLength(len);
        ISOField f = new ISOField(12, data);
        byte[] raw = packager.pack(f);
        long start = System.currentTimeMillis();
        runUnpackTest(packager, f, raw);
        long end = System.currentTimeMillis();
        boolean isOldImplementation = packagerClass.getSuperclass().getName().endsWith("ISOFieldPackager")
            || packagerClass.getSuperclass().getName().endsWith("IF_TBASE");
        System.out.println("Unpack: " + (isOldImplementation ? "old " : "new ") + packager.getClass().getName() + " = " + (end - start));
        
    }

    public static void runUnpack(String packagerName, int len, byte[] data) throws Exception
    {
        Class packagerClass = Class.forName(packagerName);
        ISOFieldPackager packager = (ISOFieldPackager)packagerClass.newInstance();
        packager.setLength(len);
        ISOBinaryField f = new ISOBinaryField(12, data);
        byte[] raw = packager.pack(f);
        long start = System.currentTimeMillis();
        runUnpackTest(packager, f, raw);
        long end = System.currentTimeMillis();
        boolean isOldImplementation = packagerClass.getSuperclass().getName().endsWith("ISOFieldPackager")
            || packagerClass.getSuperclass().getName().endsWith("IF_TBASE");
        System.out.println("Unpack: " + (isOldImplementation ? "old " : "new ") + packager.getClass().getName() + " = " + (end - start));
        
    }

    private static void runUnpackTest(ISOFieldPackager p, ISOComponent c, byte[] raw) throws ISOException
    {
        for (int i = 0; i < 1000000; i++)
            p.unpack(c, raw, 0);
    }
}
