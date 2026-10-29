/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CardTest  {

    @Test
    public void testTrack2() throws Throwable {
        Track2 t2 = Track2.builder()
          .track("4111111111111111=20111234561234567890").build();

        assertEquals("4111111111111111", t2.getPan(), "pan");
        assertEquals("2011", t2.getExp(), "exp");
        assertEquals("123", t2.getServiceCode(), "serviceCode");
        assertEquals("4561", t2.getCvv(), "cvv");
        assertEquals("234567890", t2.getDiscretionaryData(), "discretionaryData");
    }

    @Test
    public void testShortTrack2() throws Throwable {
        Track2 t2 = Track2.builder()
                .track("4111111111111111=").build();

        assertEquals("4111111111111111", t2.getPan(), "pan");
    }

    @Test
    public void testTrack1() throws Throwable {
        Track1 t1 = Track1.builder()
          .track("%B4111111111111111^FAT ALBERT                ^201112345671234567890?").build();

        assertEquals("4111111111111111", t1.getPan(), "pan");
        assertEquals("2011", t1.getExp(), "exp");
        assertEquals("123", t1.getServiceCode(), "serviceCode");
        assertEquals("4567", t1.getCvv(), "cvv");
        assertEquals("1234567890", t1.getDiscretionaryData(), "discretionaryData");
    }

    @Test
    public void testCard() throws Throwable {
        Track1 t1 = Track1.builder()
          .track("%B4111111111111111^FAT ALBERT                ^401112345671234567890?").build();
        Track2 t2 = Track2.builder()
          .track("4111111111111111=40111234561234567890").build();
        Card c = Card.builder()
          .pan("4111111111111111")
          .exp("4011")
          .cvv("123")
          .cvv2("4567")
          .serviceCode("123")
          .track1(t1)
          .track2(t2)
          .build();

        assertEquals(false, c.isExpired(new Date()), "not expired");
    }

    @Test
    public void testInvalidPAN() throws Throwable {
        try {
            Track1 t1 = Track1.builder()
              .track("%B4111111111111112^FAT ALBERT                ^201112345671234567890?").build();
            Track2 t2 = Track2.builder()
              .track("4111111111111112=201112345612345678901").build();
            Card c = Card.builder()
              .pan("4111111111111112")
              .exp("2011")
              .cvv("123")
              .cvv2("4567")
              .serviceCode("201")
              .track1(t1)
              .track2(t2)
              .build();

            fail ("InvalidCardException was not raised");
        } catch (InvalidCardException ignored) { }
    }

    @Test
    public void testISOMsg() throws Throwable {
        ISOMsg m = new ISOMsg("0800");
        m.set(45, "%B4111111111111111^FAT ALBERT                ^401112345671234567890?");
        m.set(35, "4111111111111111=40111234561234567890");
        m.set(2, "4111111111111111");
        m.set(14, "4011");
        Card c = Card.builder()
          .isomsg(m)
          .cvv2("123")
          .serviceCode("123")
          .build();

        assertEquals(false, c.isExpired(new Date()), "not expired");
        assertEquals("4111111111111111", c.getPan(), "pan");
        assertEquals("4011", c.getExp(), "exp");
        assertEquals("123", c.getServiceCode(), "serviceCode");
        assertEquals("123", c.getTrack1().getServiceCode(), "t1.serviceCode");
        assertEquals("123", c.getTrack2().getServiceCode(), "t2.serviceCode");
        assertEquals("123", c.getCvv2(), "cvv2");
        assertEquals("4567", c.getTrack1().getCvv(), "t1.cvv");
        assertEquals("4561", c.getTrack2().getCvv(), "t2.cvv");
        assertEquals("1234567890", c.getTrack1().getDiscretionaryData(), "discretionaryData");
        assertEquals("234567890", c.getTrack2().getDiscretionaryData(), "discretionaryData");
    }

    @Test
    public void testOverrideTrack1Pattern() throws Throwable {
        Track1 t1 = Track1.builder()
          .pattern(Pattern.compile("^[%]?[A-Z]+([0-9]{1,19})\\^([^\\^]{2,28})\\^([0-9]{4})([0-9]{3})([0-9]{4})?([0-9]{1,10})?"))
          .track("B4111111111111111^ALPHAMERDADO PRUEBA         ^401110100026.000.003-6    000").build();
        Track2 t2 = Track2.builder()
          .track("4111111111111111=40111014561234567890").build();
        Card c = Card.builder()
          .pan("4111111111111111")
          .exp("4011")
          .cvv("123")
          .cvv2("4567")
          .serviceCode("101")
          .track1(t1)
          .track2(t2)
          .build();

        assertEquals(false, c.isExpired(new Date()), "not expired");
    }
    @Test
    public void testShortPan() throws Throwable {
        Track2 t2 = Track2.builder()
          .track("41111111111111=2011123456123456789012").build();

        assertEquals("41111111111111", t2.getPan(), "pan");
        assertEquals("2011", t2.getExp(), "exp");
        assertEquals("123", t2.getServiceCode(), "serviceCode");
        assertEquals("4561", t2.getCvv(), "cvv");
        assertEquals("23456789012", t2.getDiscretionaryData(), "discretionaryData");
    }

    @Test
    public void test6DigitBIN() throws Throwable {
        Card c = Card.builder()
          .pan("4111111111111111")
          .exp("4011")
          .cvv("123")
          .cvv2("4567")
          .serviceCode("101")
          .build();

        assertEquals("411111", c.getBin(), "pan");
    }

    @Test
    public void test8DigitBIN() throws Throwable {
        Card c = Card.builder()
          .pan("4111111111111111")
          .exp("4011")
          .cvv("123")
          .cvv2("4567")
          .serviceCode("101")
          .build();

        assertEquals("41111111", c.getBin(8), "pan");
    }

    @Test
    public void testBuildTrack1Data() throws Throwable {
        // Load card data
        Track1.Builder track1Builder = Track1.builder();
        track1Builder.pan("4111111111111111");
        track1Builder.nameOnCard("NAME ON CARD TEST");
        track1Builder.exp("2011");
        track1Builder.serviceCode("123");
        track1Builder.cvv("4567");
        track1Builder.discretionaryData("1234567890");

        // Construct the Track1 data based on the card data provided
        Track1 track1 = track1Builder.build();

        // Loads the card values from the previously generated Track1
        // to validate if those values match the original ones.
        Track1 track1Bis = Track1.builder().track(track1.getTrack()).build();

        assertEquals(track1, track1Bis, "Card data values don't match original ones.");
    }

    @Test
    public void testBuildTrack1DataPatternDoesNotMatch() throws Throwable {

        String originalTrack1 = "%B4111111111111111^NAME ON CARD TEST^201112345671234567890";

        // Load card data from track data input
        Track1.Builder builder = Track1.builder().track(originalTrack1);

        // Override card data with empty PAN
        builder.pan("");

        // Construct the Track1 data based on the card data provided
        Track1 track1 = builder.buildTrackData().build();

        // Generated track1 doesn't match the pattern. The track attribute keeps the original value
        assertEquals(originalTrack1, track1.getTrack());
    }

    @Test
    public void testBuildTrack2Data() throws Throwable {
        // Load card data
        Track2.Builder track2Builder = Track2.builder();
        track2Builder.pan("4111111111111111");
        track2Builder.exp("2011");
        track2Builder.serviceCode("123");
        track2Builder.cvv("4567");
        track2Builder.discretionaryData("123456789");

        // Construct the Track2 data based on the card data provided
        Track2 track2 = track2Builder.build();

        // Loads the card values from the previously generated Track2
        // to validate if those values match the original ones.
        Track2 track2Bis = Track2.builder().track(track2.getTrack()).build();

        assertEquals(track2, track2Bis, "Card data values don't match original ones.");
    }

    @Test
    public void testBuildTrack2DataPatternDoesNotMatch() throws Throwable {

        String originalTrack2 = "4111111111111111=20111234567123456789";

        // Load card data from track data input
        Track2.Builder builder = Track2.builder().track(originalTrack2);

        // Override card data with empty PAN
        builder.pan("");

        // Construct the Track2 data based on the card data provided
        Track2 track2 = builder.buildTrackData().build();

        // Generated track2 doesn't match the pattern. The track attribute keeps the original value
        assertEquals(originalTrack2, track2.getTrack());
    }
}
