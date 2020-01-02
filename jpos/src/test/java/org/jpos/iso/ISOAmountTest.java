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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ISOAmountTest {
    ISOAmount iSOAmount;

    @BeforeEach
    public void setUp() throws Exception {
        iSOAmount = new ISOAmount(28, 840, BigDecimal.TEN.setScale(2));
    }

    @Test
    public void testDump() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        iSOAmount.dump(new PrintStream(baos), ":-o");
        // then
        String lineSep     = System.getProperty ("line.separator");
        String expected = ":-o<field id=\"28\" currency=\"840\" type=\"amount\" value=\"10.00\"/>" + lineSep;
        assertThat(baos.toString(), is(expected));
    }

    @Test
    public void testGetAmountAsString() throws ISOException {
        assertThat(iSOAmount.getAmountAsString(), is("8402000000001000"));
    }

    @Test
    public void testGetAmountAsLegacyString() throws ISOException {
        assertThat(iSOAmount.getAmountAsLegacyString(), is("000000001000"));
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
        assertThat(iSOAmount.getAmount(), is(BigDecimal.TEN.setScale(2)));
    }

    @Test
    public void testGetScale() {
        assertThat(iSOAmount.getScale(), is(2));
    }

    @Test
    public void testGetScaleAsString() {
        assertThat(iSOAmount.getScaleAsString(), is("2"));
    }

    @Test
    public void testGetCurrencyCode() {
        assertThat(iSOAmount.getCurrencyCode(), is(840));
    }

    @Test
    public void testGetCurrencyCodeAsString() throws ISOException {
        assertThat(iSOAmount.getCurrencyCodeAsString(), is("840"));
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

    @Test
    public void testScaleConverstionArithmeticException() {
        try {
            ISOAmount amnt = new ISOAmount (4, 600, new BigDecimal ("12.34"));
            fail("should raise ISOException");
        } catch (ISOException ignored) { }
    }
}
