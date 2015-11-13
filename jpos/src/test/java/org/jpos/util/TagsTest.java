package org.jpos.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TagsTest {
    @Test
    public void testEmpty () {
        Tags ts = new Tags();
        assertEquals("", ts.toString());
    }

    @Test
    public void testOrder () {
        Tags ts = new Tags("def,abc");
        assertEquals("abc,def", ts.toString());
    }

    @Test
    public void testEscape () {
        Tags ts = new Tags("d\\,ef,abc\\\\");
        assertEquals("abc\\\\,d\\,ef", ts.toString());
    }

    @Test
    public void testEqualsAndHashCode () {
        Tags ts1 = new Tags("xyz,abc");
        Tags ts2 = new Tags(ts1.toString());
        Tags ts3 = new Tags("abc", "xyz");
        assertTrue(ts1.equals(ts2));
        assertTrue(ts1.hashCode() == ts2.hashCode());
        assertTrue(ts1.equals(ts3));
        assertTrue(ts1.hashCode() == ts3.hashCode());
        ts2.add("xyz"); // dupe entry
        assertTrue(ts1.equals(ts2));
        assertTrue(ts1.hashCode() == ts2.hashCode());
        ts2.add("zyx");
        assertFalse(ts1.equals(ts2));
        assertFalse(ts1.hashCode() == ts2.hashCode());
    }

    @Test
    public void testSerialization() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Tags ts1 = new Tags("xyz,abc");
        new ObjectOutputStream(baos).writeObject(ts1);
        byte[] buf = baos.toByteArray();
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(buf));
        Tags ts2 = (Tags) is.readObject();
        assertTrue(ts1.equals(ts2));
        assertTrue(ts1.hashCode() == ts2.hashCode());
        assertEquals(ts1.toString(), ts2.toString());
    }
}
