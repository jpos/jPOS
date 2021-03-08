package org.jpos.iso.packager;


import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GICCPackagerTest {
    byte[] expected_60_47_1 = ISOUtil.hex2byte("02000000000000000010F0F2F0F0F0F4 F4F0 F0F7 F0F1F0 F4F7 F0F0F5 F0F1 F1F0F1".replace(" ",""));
    @Test
    public void test_DE060_47_1_pack() throws Throwable {
        Logger logger = new Logger();
        logger.addListener (new SimpleLogListener(System.out));
        GenericPackager p = new GenericPackager("jar:packager/gicc_60.xml");
        p.setLogger(logger, "packager");

        ISOMsg m = new ISOMsg();
        m.setPackager(p);
        m.setMTI("200");
        m.set("60.40", ISOUtil.asciiToEbcdic("07"));
        m.set("60.47.1", ISOUtil.asciiToEbcdic("101"));
        Assertions.assertArrayEquals(expected_60_47_1, m.pack());
    }
    public void test_DE060_47_1_unpack() throws Throwable {
        Logger logger = new Logger();
        logger.addListener (new SimpleLogListener(System.out));
        GenericPackager p = new GenericPackager("jar:packager/gicc_60.xml");
        p.setLogger(logger, "packager");

        ISOMsg m = new ISOMsg();
        m.setPackager(p);
        m.unpack(expected_60_47_1);
        Assertions.assertArrayEquals(ISOUtil.asciiToEbcdic("07"), m.getBytes("60.40"));
        Assertions.assertArrayEquals(ISOUtil.asciiToEbcdic("101"), m.getBytes("60.47.1"));
        Assertions.assertArrayEquals(expected_60_47_1, m.pack());
    }
}
