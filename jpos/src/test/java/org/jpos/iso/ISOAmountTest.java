package org.jpos.iso;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

public class ISOAmountTest {
    ISOAmount iSOAmount;

    @Before
    public void setUp() throws Exception {
        iSOAmount = new ISOAmount(28, 831, BigDecimal.TEN);
    }

    @Test
    public void testDump() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        iSOAmount.dump(new PrintStream(baos), ":-o");
        // then
        String lineSep     = System.getProperty ("line.separator");
        String expected = ":-o<field id=\"28\" currency=\"831\" type=\"amount\" value=\"10\"/>" + lineSep;
        assertThat(baos.toString(), is(expected));
    }

    @Test
    public void testGetAmountAsString() throws ISOException {
        assertThat(iSOAmount.getAmountAsString(), is("8310000000000010"));
    }

    @Test
    public void testGetAmountAsLegacyString() throws ISOException {
        assertThat(iSOAmount.getAmountAsLegacyString(), is("000000000010"));
    }

    @Test
    public void testPack() throws IOException {
        try {
            iSOAmount.pack(null);
            fail("ISOException Expected");
        } catch (ISOException isoe) {
            assertThat(isoe.getMessage(), is("Not available"));
        }
    }

    @Test
    public void testUnPack2() throws IOException {
        try {
            iSOAmount.unpack((InputStream) null);
            fail("ISOException Expected");
        } catch (ISOException isoe) {
            assertThat(isoe.getMessage(), is("Not available"));
        }
    }

    @Test
    public void testUnpack() {
        try {
            iSOAmount.unpack(new byte[1]);
            fail("ISOException Expected");
        } catch (ISOException isoe) {
            assertThat(isoe.getMessage(), is("Not available"));
        }
    }

    @Test
    public void testGetAmount() {
        assertThat(iSOAmount.getAmount(), is(BigDecimal.TEN));
    }

    @Test
    public void testGetScale() {
        assertThat(iSOAmount.getScale(), is(0));
    }

    @Test
    public void testGetScaleAsString() {
        assertThat(iSOAmount.getScaleAsString(), is("0"));
    }

    @Test
    public void testGetCurrencyCode() {
        assertThat(iSOAmount.getCurrencyCode(), is(831));
    }

    @Test
    public void testGetCurrencyCodeAsString() throws ISOException {
        assertThat(iSOAmount.getCurrencyCodeAsString(), is("831"));
    }

    @Test
    public void testReadWriteExternal() throws IOException, ClassNotFoundException, ISOException {
        // given
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        ObjectOutputStream ostr = new ObjectOutputStream(data);
        // when
        iSOAmount.writeExternal(ostr);
        ostr.close();
        ObjectInputStream istr = new ObjectInputStream(new ByteArrayInputStream(data.toByteArray()));
        // then
        ISOAmount pickled = new ISOAmount();
        pickled.readExternal(istr);
        istr.close();
        assertThat(pickled.getAmountAsString(), is(iSOAmount.getAmountAsString()));
    }

    @Test
    public void testBadWriteAttemptISOException() throws IOException {
        iSOAmount = new ISOAmount() {
            @Override
            public Object getValue() throws ISOException {
                throw new ISOException("boo!");
            }
        };
        ObjectOutput oo = mock(ObjectOutput.class);
        try {
            iSOAmount.writeExternal(oo);
        } catch (IOException ioe) {
            assertThat(ioe.getMessage(), is("org.jpos.iso.ISOException: boo!"));
        }
    }

    @Test
    public void testBadReadAttemptISOException() throws IOException, ClassNotFoundException {
        iSOAmount = new ISOAmount() {
            @Override
            public void setValue(Object obj) throws ISOException {
                throw new ISOException("yikes!");
            }
        };
        ObjectInput in = mock(ObjectInput.class);
        try {
            iSOAmount.readExternal(in);
        } catch (IOException ioe) {
            assertThat(ioe.getMessage(), is("yikes!"));
        }
    }

    @Test
    public void testISOAmount() {
        iSOAmount = new ISOAmount();
        assertThat(iSOAmount.getKey(), is((Object) Integer.valueOf(-1)));
    }

    @Test
    public void testISOAmountInt() {
        iSOAmount = new ISOAmount(75);
        assertThat(iSOAmount.getKey(), is((Object) Integer.valueOf(75)));
    }

    @Test
    public void testSetValueBadLength() {
        try {
            iSOAmount.setValue("1234");
            fail("expected invalid length ISOException");
        } catch (ISOException e) {
            assertThat(e.getMessage(), is("ISOAmount invalid length 4"));
        }
    }

    @Test
    public void testSetValueBadCurrencyCode() {
        try {
            iSOAmount.setValue("X23456789012");
            fail("expected exception");
        } catch (ISOException e) {
            assertThat(e.getMessage(), is("For input string: \"X23\""));
        }
    }
}
