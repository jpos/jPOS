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

package org.jpos.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jpos.iso.ISOMsg;
import org.jpos.testhelpers.EqualsHashCodeTestCase;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.*;

public class CardHolderTest extends EqualsHashCodeTestCase {
    @Test
    public void testConstructor() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        assertFalse(cardHolder.hasTrack2(), "cardHolder.hasTrack2()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        CardHolder cardHolder = new CardHolder("10Characte", "4Cha");
        assertEquals("4Cha", cardHolder.exp, "cardHolder.exp");
        assertEquals("10Characte", cardHolder.pan, "cardHolder.pan");
    }

    @Test
    public void testConstructor2() throws Throwable {
        CardHolder cardHolder = new CardHolder("11Character", "4Cha");
        assertEquals("4Cha", cardHolder.exp, "cardHolder.exp");
        assertEquals("11Character", cardHolder.pan, "cardHolder.pan");
    }

    @Test
    public void testConstructor3() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        assertEquals("H:!;", cardHolder.exp, "cardHolder.exp");
        assertEquals("k'X9|", cardHolder.pan, "cardHolder.pan");
        assertEquals("uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,", cardHolder.trailer, "cardHolder.trailler");
    }

    @Test
    public void testConstructorThrowsInvalidCardException() {
        assertThrows(InvalidCardException.class, () -> new CardHolder("11Character", "3Ch"));
    }

    @Test
    public void testConstructorThrowsInvalidCardException1() {
        assertThrows(InvalidCardException.class, () -> new CardHolder("11Character", "5Char"));
    }

    @Test
    public void testConstructorThrowsInvalidCardException2() {
        assertThrows(InvalidCardException.class, () -> new CardHolder("9Characte", "testCardHolderExp"));
    }

    @Test
    public void testConstructorThrowsInvalidCardException3() {
        assertThrows(InvalidCardException.class, () -> new CardHolder("testCardHolderTrack2"));
    }

    @Test
    public void testConstructorThrowsInvalidCardException4() {
        ISOMsg m = new ISOMsg(100);
        assertThrows(InvalidCardException.class, () -> new CardHolder(m));
        assertEquals(0, m.getDirection(), "m.getDirection()");
    }

    @Test
    public void testDump() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrack1(null);
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        cardHolder.setTrailer("testCardHolderTrailler");
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump2() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setTrack1("testCardHolderTrack1");
        cardHolder.setSecurityCode(null);
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump3() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1("testCardHolderTrack1");
        cardHolder.setEXP("99-8");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump4() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("9912");
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump5() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1("testCardHolderTrack1");
        cardHolder.setEXP("9913");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump6() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        new CardHolder("testCardHolderPan", "4Cha").dump(p, "testCardHolderIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").dump(null, "testCardHolderIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.PrintStream.print(String)\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetBIN() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        String result = cardHolder.getBIN();
        assertEquals("testCa", result, "result");
    }

    @Test
    public void testGetBINThrowsNullPointerException() throws Throwable {
        try {
            new CardHolder().getBIN();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.substring(int, int)\" because \"this.pan\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetEXP() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("99-8");
        String result = cardHolder.getEXP();
        assertEquals("99-8", result, "result");
    }

    @Test
    public void testGetEXP1() throws Throwable {
        String result = new CardHolder().getEXP();
        assertNull(result, "result");
    }

    @Test
    public void testGetNameOnCard() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1(" `^o;t~Dfv._uUa7agT,\tQ2lt @0@5BT0O)a");
        String result = cardHolder.getNameOnCard();
        assertEquals("o;t~Dfv._uUa7agT,\tQ2lt @0@5BT0O)a", result, "result");
    }

    @Test
    public void testGetNameOnCard1() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setTrack1("testCardHolderTrack1");
        String result = cardHolder.getNameOnCard();
        assertNull(result, "result");
    }

    @Test
    public void testGetNameOnCard2() throws Throwable {
        String result = new CardHolder().getNameOnCard();
        assertNull(result, "result");
    }

    @Test
    public void testGetPAN() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        String result = cardHolder.getPAN();
        assertEquals("testCardHolderPan", result, "result");
    }

    @Test
    public void testGetPAN1() throws Throwable {
        String result = new CardHolder().getPAN();
        assertNull(result, "result");
    }

    @Test
    public void testGetSecurityCode() throws Throwable {
        String result = new CardHolder().getSecurityCode();
        assertNull(result, "result");
    }

    @Test
    public void testGetSecurityCode1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        String result = cardHolder.getSecurityCode();
        assertEquals("testCardHolderSecurityCode", result, "result");
    }

    @Test
    public void testGetServiceCode() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.parseTrack2(" trk1=\"true\"");
        String result = cardHolder.getServiceCode();
        assertEquals("   ", result, "result");
    }

    @Test
    public void testGetServiceCode1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrailer("w:4");
        String result = cardHolder.getServiceCode();
        assertEquals("w:4", result, "result");
    }

    @Test
    public void testGetServiceCode2() throws Throwable {
        String result = new CardHolder("testCardHolderPan", "4Cha").getServiceCode();
        assertEquals("   ", result, "result");
    }

    @Test
    public void testGetTrack1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrack1("testCardHolderTrack1");
        String result = cardHolder.getTrack1();
        assertEquals("testCardHolderTrack1", result, "result");
    }

    @Test
    public void testGetTrack11() throws Throwable {
        String result = new CardHolder("testCardHolderPan", "4Cha").getTrack1();
        assertNull(result, "result");
    }

    @Test
    public void testGetTrack2() throws Throwable {
        String result = new CardHolder().getTrack2();
        assertNull(result, "result");
    }

    @Test
    public void testGetTrack21() throws Throwable {
        String result = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").getTrack2();
        assertEquals("k'X9|=H:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,", result, "result");
    }

    @Test
    public void testGetTrailer() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrailer("testCardHolderTrailler");
        String result = cardHolder.getTrailer();
        assertEquals("testCardHolderTrailler", result, "result");
    }

    @Test
    public void testGetTrailer1() throws Throwable {
        String result = new CardHolder().getTrailer();
        assertNull(result, "result");
    }

    @Test
    public void testHasSecurityCode() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        boolean result = cardHolder.hasSecurityCode();
        assertTrue(result, "result");
    }

    @Test
    public void testHasSecurityCode1() throws Throwable {
        boolean result = new CardHolder("testCardHolderPan", "4Cha").hasSecurityCode();
        assertFalse(result, "result");
    }

    @Test
    public void testHasTrack1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1("testCardHolderTrack1");
        boolean result = cardHolder.hasTrack1();
        assertTrue(result, "result");
    }

    @Test
    public void testHasTrack11() throws Throwable {
        boolean result = new CardHolder("testCardHolderPan", "4Cha").hasTrack1();
        assertFalse(result, "result");
    }

    @Test
    public void testHasTrack2() throws Throwable {
        boolean result = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").hasTrack2();
        assertTrue(result, "result");
    }

    @Test
    public void testHasTrack21() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        cardHolder.setEXP("9913");
        boolean result = cardHolder.hasTrack2();
        assertFalse(result, "result");
    }

    @Test
    public void testHasTrack22() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        boolean result = cardHolder.hasTrack2();
        assertFalse(result, "result");
    }

    @Test
    public void testHasTrack23() throws Throwable {
        boolean result = new CardHolder().hasTrack2();
        assertFalse(result, "result");
    }

    @Test
    public void testIsExpired() throws Throwable {
        boolean result = new CardHolder("testCardHolderPan", "4Cha").isExpired();
        assertTrue(result, "result");
    }

    @Test
    public void testIsExpired1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setEXP("9913");
        boolean result = cardHolder.isExpired();
        assertTrue(result, "result");
    }

    @Test
    public void testIsExpired2() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("99-8");
        boolean result = cardHolder.isExpired();
        assertTrue(result, "result");
    }

    @Test
    public void testIsExpired3() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setEXP("9912");
        boolean result = cardHolder.isExpired();
        assertTrue(result, "result");
    }

    @Test
    public void testIsExpired4() throws Throwable {
        boolean result = new CardHolder().isExpired();
        assertTrue(result, "result");
    }

    @Test
    public void testIsValidCRC() throws Throwable {
        boolean result = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").isValidCRC();
        assertFalse(result, "result");
    }

    @Test
    public void testIsValidCRC1() throws Throwable {
        boolean result = CardHolder.isValidCRC("42");
        assertTrue(result, "result");
    }

    @Test
    public void testIsValidCRC2() throws Throwable {
        boolean result = CardHolder.isValidCRC("0");
        assertTrue(result, "result");
    }

    @Test
    public void testIsValidCRC3() throws Throwable {
        boolean result = CardHolder.isValidCRC("");
        assertTrue(result, "result");
    }

    @Test
    public void testIsValidCRC4() throws Throwable {
        boolean result = CardHolder.isValidCRC("1");
        assertFalse(result, "result");
    }

    @Test
    public void testIsValidCRC5() throws Throwable {
        boolean result = CardHolder.isValidCRC("testCardHolderp");
        assertFalse(result, "result");
    }

    @Test
    public void testIsValidCRC6() throws Throwable {
        boolean result = CardHolder.isValidCRC("41CharactersXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        assertFalse(result, "result");
    }

    @Test
    public void testIsValidCRC7() throws Throwable {
        boolean result = CardHolder.isValidCRC("2C");
        assertFalse(result, "result");
    }

    @Test
    public void testIsValidCRCThrowsNullPointerException() throws Throwable {
        try {
            new CardHolder().isValidCRC();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testIsValidCRCThrowsNullPointerException1() throws Throwable {
        try {
            CardHolder.isValidCRC(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testParseTrack2() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.parseTrack2("uD|kOAf(Ls@RaT\f4_3[l\f#mF*'7-yeK^S?qBK ~G");
        assertEquals("u", cardHolder.pan, "cardHolder.pan");
        assertEquals("|kOA", cardHolder.exp, "cardHolder.exp");
        assertEquals("f(Ls@RaT\f4_3[l\f#mF*'7-yeK^S?qBK ~G", cardHolder.trailer, "cardHolder.trailler");
    }

    @Test
    public void testParseTrack2ThrowsInvalidCardException() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        assertThrows(InvalidCardException.class, () -> cardHolder.parseTrack2("D0"));
        assertEquals("4Cha", cardHolder.exp, "cardHolder.exp");
        assertEquals("testCardHolderPan", cardHolder.pan, "cardHolder.pan");
        assertNull(cardHolder.trailer, "cardHolder.trailler");
    }

    @Test
    public void testParseTrack2ThrowsInvalidCardException1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        assertThrows(InvalidCardException.class, () -> cardHolder.parseTrack2("testCardHolders"));
        assertNull(cardHolder.exp, "cardHolder.exp");
        assertNull(cardHolder.pan, "cardHolder.pan");
        assertNull(cardHolder.trailer, "cardHolder.trailler");
    }

    @Test
    public void testParseTrack2ThrowsInvalidCardException2() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        try {
            cardHolder.parseTrack2(null);
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("null track2 data", ex.getMessage(), "ex.getMessage()");
            assertEquals("H:!;", cardHolder.exp, "cardHolder.exp");
            assertEquals("k'X9|", cardHolder.pan, "cardHolder.pan");
            assertEquals("uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,", cardHolder.trailer, "cardHolder.trailler");
        }
    }

    @Test
    public void testSeemsManualEntry() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setTrailer("");
        boolean result = cardHolder.seemsManualEntry();
        assertTrue(result, "result");
    }

    @Test
    public void testSeemsManualEntry1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrailer("1");
        boolean result = cardHolder.seemsManualEntry();
        assertFalse(result, "result");
    }

    @Test
    public void testSeemsManualEntry2() throws Throwable {
        boolean result = new CardHolder().seemsManualEntry();
        assertTrue(result, "result");
    }

    @Test
    public void testSetEXP() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("9913");
        assertEquals("9913", cardHolder.exp, "cardHolder.exp");
    }

    @Test
    public void testSetEXPThrowsInvalidCardException() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        assertThrows(InvalidCardException.class, () -> cardHolder.setEXP("5Char"));
        assertEquals("4Cha", cardHolder.exp, "cardHolder.exp");
    }

    @Test
    public void testSetEXPThrowsInvalidCardException1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        assertThrows(InvalidCardException.class, () -> cardHolder.setEXP("3Ch"));
        assertEquals("4Cha", cardHolder.exp, "cardHolder.exp");
    }

    @Test
    public void testSetEXPThrowsNullPointerException() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        try {
            cardHolder.setEXP(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"exp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals("4Cha", cardHolder.exp, "cardHolder.exp");
        }
    }

    @Test
    public void testSetPAN() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("10Characte");
        assertEquals("10Characte", cardHolder.pan, "cardHolder.pan");
    }

    @Test
    public void testSetPAN1() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setPAN("11Character");
        assertEquals("11Character", cardHolder.pan, "cardHolder.pan");
    }

    @Test
    public void testSetPANThrowsInvalidCardException() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        assertThrows(InvalidCardException.class, () -> cardHolder.setPAN("9Characte"));
        assertEquals("k'X9|", cardHolder.pan, "cardHolder.pan");
    }

    @Test
    public void testSetPANThrowsNullPointerException() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        try {
            cardHolder.setPAN(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"pan\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals("k'X9|", cardHolder.pan, "cardHolder.pan");
        }
    }

    @Test
    public void testSetSecurityCode() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        assertEquals("testCardHolderSecurityCode", cardHolder.securityCode, "cardHolder.securityCode");
    }

    @Test
    public void testSetTrack1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrack1("testCardHolderTrack1");
        assertEquals("testCardHolderTrack1", cardHolder.track1, "cardHolder.track1");
    }

    @Test
    public void testSetTrailler() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrailer("testCardHolderTrailler");
        assertEquals("testCardHolderTrailler", cardHolder.trailer, "cardHolder.trailler");
    }

    @Override
    protected Object createInstance() throws Exception {
        return new CardHolder("AACardHolderPan", "9AAA");
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        return new CardHolder("ZZCardHolderPan", "5ZZZ");
    }
}
