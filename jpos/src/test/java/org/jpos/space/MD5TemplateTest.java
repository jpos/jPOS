package org.jpos.space;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class MD5TemplateTest {

    @Test
    public void testConstructor() throws Throwable {
        byte[] digest = new byte[3];
        MD5Template mD5Template = new MD5Template("1", digest);
        assertSame("mD5Template.digest", digest, mD5Template.digest);
        assertEquals("mD5Template.key", "1", mD5Template.key);
    }

    @Test
    public void testGetKey() throws Throwable {
        byte[] digest = new byte[1];
        Integer key = Integer.valueOf(0);
        Integer result = (Integer) new MD5Template(key, digest).getKey();
        assertSame("result", key, result);
    }

    @Test
    public void testGetKey1() throws Throwable {
        byte[] key = new byte[1];
        byte[] result = (byte[]) new MD5Template(key, "testString".getBytes()).getKey();
        assertSame("result", key, result);
    }

    @Test
    public void testGetKey2() throws Throwable {
        String result = (String) new MD5Template("testString", "\n".getBytes()).getKey();
        assertEquals("result", "testString", result);
    }

}
