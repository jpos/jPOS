/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOMsg;
import org.jpos.testhelpers.EqualsHashCodeTestCase;
import org.junit.Test;

public class CardHolderTest extends EqualsHashCodeTestCase {
    @Test
    public void testConstructor() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        assertFalse("cardHolder.hasTrack2()", cardHolder.hasTrack2());
    }

    @Test
    public void testConstructor1() throws Throwable {
        CardHolder cardHolder = new CardHolder("10Characte", "4Cha");
        assertEquals("cardHolder.exp", "4Cha", cardHolder.exp);
        assertEquals("cardHolder.pan", "10Characte", cardHolder.pan);
    }

    @Test
    public void testConstructor2() throws Throwable {
        CardHolder cardHolder = new CardHolder("11Character", "4Cha");
        assertEquals("cardHolder.exp", "4Cha", cardHolder.exp);
        assertEquals("cardHolder.pan", "11Character", cardHolder.pan);
    }

    @Test
    public void testConstructor3() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        assertEquals("cardHolder.exp", "H:!;", cardHolder.exp);
        assertEquals("cardHolder.pan", "k'X9|", cardHolder.pan);
        assertEquals("cardHolder.trailler", "uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,", cardHolder.trailler);
    }

    @Test
    public void testConstructorThrowsInvalidCardException() throws Throwable {
        try {
            new CardHolder("11Character", "3Ch");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "11Character/3Ch", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsInvalidCardException1() throws Throwable {
        try {
            new CardHolder("11Character", "5Char");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "11Character/5Char", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsInvalidCardException2() throws Throwable {
        try {
            new CardHolder("9Characte", "testCardHolderExp");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "9Characte", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsInvalidCardException3() throws Throwable {
        try {
            new CardHolder("testCardHolderTrack2");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "testCardHolderTrack2", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsInvalidCardException4() throws Throwable {
        ISOMsg m = new ISOMsg(100);
        try {
            new CardHolder(m);
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "required fields not present", ex.getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testConstructorFieldsTwoBitmapThrowsNullPointerException() throws Throwable {
        ISOMsg m = new ISOMsg("testCardHolderMti");
        m.set(new ISOBitMap(2));
        try {
            new CardHolder(m);
            fail("Expected InvalidCardException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new CardHolder("11Character", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException1() throws Throwable {
        try {
            new CardHolder((ISOMsg) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDump() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrack1(null);
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        cardHolder.setTrailler("testCardHolderTrailler");
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump2() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setTrack1("testCardHolderTrack1");
        cardHolder.setSecurityCode(null);
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump3() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1("testCardHolderTrack1");
        cardHolder.setEXP("99-8");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump4() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("9912");
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump5() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1("testCardHolderTrack1");
        cardHolder.setEXP("9913");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        cardHolder.dump(p, "testCardHolderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump6() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        new CardHolder("testCardHolderPan", "4Cha").dump(p, "testCardHolderIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").dump(null, "testCardHolderIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetBIN() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        String result = cardHolder.getBIN();
        assertEquals("result", "testCa", result);
    }

    @Test
    public void testGetBINThrowsNullPointerException() throws Throwable {
        try {
            new CardHolder().getBIN();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetEXP() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("99-8");
        String result = cardHolder.getEXP();
        assertEquals("result", "99-8", result);
    }

    @Test
    public void testGetEXP1() throws Throwable {
        String result = new CardHolder().getEXP();
        assertNull("result", result);
    }

    @Test
    public void testGetNameOnCard() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1(" `^o;t~Dfv._uUa7agT,\tQ2lt @0@5BT0O)a");
        String result = cardHolder.getNameOnCard();
        assertEquals("result", "o;t~Dfv._uUa7agT,\tQ2lt @0@5BT0O)a", result);
    }

    @Test
    public void testGetNameOnCard1() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setTrack1("testCardHolderTrack1");
        String result = cardHolder.getNameOnCard();
        assertNull("result", result);
    }

    @Test
    public void testGetNameOnCard2() throws Throwable {
        String result = new CardHolder().getNameOnCard();
        assertNull("result", result);
    }

    @Test
    public void testGetPAN() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        String result = cardHolder.getPAN();
        assertEquals("result", "testCardHolderPan", result);
    }

    @Test
    public void testGetPAN1() throws Throwable {
        String result = new CardHolder().getPAN();
        assertNull("result", result);
    }

    @Test
    public void testGetSecurityCode() throws Throwable {
        String result = new CardHolder().getSecurityCode();
        assertNull("result", result);
    }

    @Test
    public void testGetSecurityCode1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        String result = cardHolder.getSecurityCode();
        assertEquals("result", "testCardHolderSecurityCode", result);
    }

    @Test
    public void testGetServiceCode() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.parseTrack2(" trk1=\"true\"");
        String result = cardHolder.getServiceCode();
        assertEquals("result", "   ", result);
    }

    @Test
    public void testGetServiceCode1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrailler("w:4");
        String result = cardHolder.getServiceCode();
        assertEquals("result", "w:4", result);
    }

    @Test
    public void testGetServiceCode2() throws Throwable {
        String result = new CardHolder("testCardHolderPan", "4Cha").getServiceCode();
        assertEquals("result", "   ", result);
    }

    @Test
    public void testGetTrack1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrack1("testCardHolderTrack1");
        String result = cardHolder.getTrack1();
        assertEquals("result", "testCardHolderTrack1", result);
    }

    @Test
    public void testGetTrack11() throws Throwable {
        String result = new CardHolder("testCardHolderPan", "4Cha").getTrack1();
        assertNull("result", result);
    }

    @Test
    public void testGetTrack2() throws Throwable {
        String result = new CardHolder().getTrack2();
        assertNull("result", result);
    }

    @Test
    public void testGetTrack21() throws Throwable {
        String result = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").getTrack2();
        assertEquals("result", "k'X9|=H:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,", result);
    }

    @Test
    public void testGetTrailler() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrailler("testCardHolderTrailler");
        String result = cardHolder.getTrailler();
        assertEquals("result", "testCardHolderTrailler", result);
    }

    @Test
    public void testGetTrailler1() throws Throwable {
        String result = new CardHolder().getTrailler();
        assertNull("result", result);
    }

    @Test
    public void testHasSecurityCode() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        boolean result = cardHolder.hasSecurityCode();
        assertTrue("result", result);
    }

    @Test
    public void testHasSecurityCode1() throws Throwable {
        boolean result = new CardHolder("testCardHolderPan", "4Cha").hasSecurityCode();
        assertFalse("result", result);
    }

    @Test
    public void testHasTrack1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrack1("testCardHolderTrack1");
        boolean result = cardHolder.hasTrack1();
        assertTrue("result", result);
    }

    @Test
    public void testHasTrack11() throws Throwable {
        boolean result = new CardHolder("testCardHolderPan", "4Cha").hasTrack1();
        assertFalse("result", result);
    }

    @Test
    public void testHasTrack2() throws Throwable {
        boolean result = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").hasTrack2();
        assertTrue("result", result);
    }

    @Test
    public void testHasTrack21() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        cardHolder.setEXP("9913");
        boolean result = cardHolder.hasTrack2();
        assertFalse("result", result);
    }

    @Test
    public void testHasTrack22() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("testCardHolderPan");
        boolean result = cardHolder.hasTrack2();
        assertFalse("result", result);
    }

    @Test
    public void testHasTrack23() throws Throwable {
        boolean result = new CardHolder().hasTrack2();
        assertFalse("result", result);
    }

    @Test
    public void testIsExpired() throws Throwable {
        boolean result = new CardHolder("testCardHolderPan", "4Cha").isExpired();
        assertTrue("result", result);
    }

    @Test
    public void testIsExpired1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setEXP("9913");
        boolean result = cardHolder.isExpired();
        assertTrue("result", result);
    }

    @Test
    public void testIsExpired2() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("99-8");
        boolean result = cardHolder.isExpired();
        assertTrue("result", result);
    }

    @Test
    public void testIsExpired3() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setEXP("9912");
        boolean result = cardHolder.isExpired();
        assertTrue("result", result);
    }

    @Test
    public void testIsExpired4() throws Throwable {
        boolean result = new CardHolder().isExpired();
        assertTrue("result", result);
    }

    @Test
    public void testIsValidCRC() throws Throwable {
        boolean result = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,").isValidCRC();
        assertFalse("result", result);
    }

    @Test
    public void testIsValidCRC1() throws Throwable {
        boolean result = CardHolder.isValidCRC("42");
        assertTrue("result", result);
    }

    @Test
    public void testIsValidCRC2() throws Throwable {
        boolean result = CardHolder.isValidCRC("0");
        assertTrue("result", result);
    }

    @Test
    public void testIsValidCRC3() throws Throwable {
        boolean result = CardHolder.isValidCRC("");
        assertTrue("result", result);
    }

    @Test
    public void testIsValidCRC4() throws Throwable {
        boolean result = CardHolder.isValidCRC("1");
        assertFalse("result", result);
    }

    @Test
    public void testIsValidCRC5() throws Throwable {
        boolean result = CardHolder.isValidCRC("testCardHolderp");
        assertFalse("result", result);
    }

    @Test
    public void testIsValidCRC6() throws Throwable {
        boolean result = CardHolder.isValidCRC("41CharactersXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        assertFalse("result", result);
    }

    @Test
    public void testIsValidCRC7() throws Throwable {
        boolean result = CardHolder.isValidCRC("2C");
        assertFalse("result", result);
    }

    @Test
    public void testIsValidCRCThrowsNullPointerException() throws Throwable {
        try {
            new CardHolder().isValidCRC();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testIsValidCRCThrowsNullPointerException1() throws Throwable {
        try {
            CardHolder.isValidCRC(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseTrack2() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.parseTrack2("uD|kOAf(Ls@RaT\f4_3[l\f#mF*'7-yeK^S?qBK ~G");
        assertEquals("cardHolder.pan", "u", cardHolder.pan);
        assertEquals("cardHolder.exp", "|kOA", cardHolder.exp);
        assertEquals("cardHolder.trailler", "f(Ls@RaT\f4_3[l\f#mF*'7-yeK^S?qBK ~G", cardHolder.trailler);
    }

    @Test
    public void testParseTrack2ThrowsInvalidCardException() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        try {
            cardHolder.parseTrack2("D0");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "D0", ex.getMessage());
            assertEquals("cardHolder.exp", "4Cha", cardHolder.exp);
            assertEquals("cardHolder.pan", "testCardHolderPan", cardHolder.pan);
            assertNull("cardHolder.trailler", cardHolder.trailler);
        }
    }

    @Test
    public void testParseTrack2ThrowsInvalidCardException1() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        try {
            cardHolder.parseTrack2("testCardHolders");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "testCardHolders", ex.getMessage());
            assertNull("cardHolder.exp", cardHolder.exp);
            assertNull("cardHolder.pan", cardHolder.pan);
            assertNull("cardHolder.trailler", cardHolder.trailler);
        }
    }

    @Test
    public void testParseTrack2ThrowsInvalidCardException2() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        try {
            cardHolder.parseTrack2(null);
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "null track2 data", ex.getMessage());
            assertEquals("cardHolder.exp", "H:!;", cardHolder.exp);
            assertEquals("cardHolder.pan", "k'X9|", cardHolder.pan);
            assertEquals("cardHolder.trailler", "uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,", cardHolder.trailler);
        }
    }

    @Test
    public void testSeemsManualEntry() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setTrailler("");
        boolean result = cardHolder.seemsManualEntry();
        assertTrue("result", result);
    }

    @Test
    public void testSeemsManualEntry1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrailler("1");
        boolean result = cardHolder.seemsManualEntry();
        assertFalse("result", result);
    }

    @Test
    public void testSeemsManualEntry2() throws Throwable {
        boolean result = new CardHolder().seemsManualEntry();
        assertTrue("result", result);
    }

    @Test
    public void testSetEXP() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setEXP("9913");
        assertEquals("cardHolder.exp", "9913", cardHolder.exp);
    }

    @Test
    public void testSetEXPThrowsInvalidCardException() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        try {
            cardHolder.setEXP("5Char");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "testCardHolderPan/5Char", ex.getMessage());
            assertEquals("cardHolder.exp", "4Cha", cardHolder.exp);
        }
    }

    @Test
    public void testSetEXPThrowsInvalidCardException1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        try {
            cardHolder.setEXP("3Ch");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "testCardHolderPan/3Ch", ex.getMessage());
            assertEquals("cardHolder.exp", "4Cha", cardHolder.exp);
        }
    }

    @Test
    public void testSetEXPThrowsNullPointerException() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        try {
            cardHolder.setEXP(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("cardHolder.exp", "4Cha", cardHolder.exp);
        }
    }

    @Test
    public void testSetPAN() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setPAN("10Characte");
        assertEquals("cardHolder.pan", "10Characte", cardHolder.pan);
    }

    @Test
    public void testSetPAN1() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        cardHolder.setPAN("11Character");
        assertEquals("cardHolder.pan", "11Character", cardHolder.pan);
    }

    @Test
    public void testSetPANThrowsInvalidCardException() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        try {
            cardHolder.setPAN("9Characte");
            fail("Expected InvalidCardException to be thrown");
        } catch (InvalidCardException ex) {
            assertEquals("ex.getMessage()", "9Characte", ex.getMessage());
            assertEquals("cardHolder.pan", "k'X9|", cardHolder.pan);
        }
    }

    @Test
    public void testSetPANThrowsNullPointerException() throws Throwable {
        CardHolder cardHolder = new CardHolder("k'X9|DH:!;uQ<kG8!P?- ,\"Y!u`r;jB^)>3AbS9,");
        try {
            cardHolder.setPAN(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("cardHolder.pan", "k'X9|", cardHolder.pan);
        }
    }

    @Test
    public void testSetSecurityCode() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setSecurityCode("testCardHolderSecurityCode");
        assertEquals("cardHolder.securityCode", "testCardHolderSecurityCode", cardHolder.securityCode);
    }

    @Test
    public void testSetTrack1() throws Throwable {
        CardHolder cardHolder = new CardHolder("testCardHolderPan", "4Cha");
        cardHolder.setTrack1("testCardHolderTrack1");
        assertEquals("cardHolder.track1", "testCardHolderTrack1", cardHolder.track1);
    }

    @Test
    public void testSetTrailler() throws Throwable {
        CardHolder cardHolder = new CardHolder();
        cardHolder.setTrailler("testCardHolderTrailler");
        assertEquals("cardHolder.trailler", "testCardHolderTrailler", cardHolder.trailler);
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
