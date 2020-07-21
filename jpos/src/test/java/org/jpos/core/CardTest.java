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
          .track("4111111111111111=201112345612345678901").build();

        assertEquals("4111111111111111", t2.getPan(), "pan");
        assertEquals("2011", t2.getExp(), "exp");
        assertEquals("123", t2.getServiceCode(), "serviceCode");
        assertEquals("4561", t2.getCvv(), "cvv");
        assertEquals("2345678901", t2.getDiscretionaryData(), "discretionaryData");
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
          .track("%B4111111111111111^FAT ALBERT                ^201112345671234567890?").build();
        Track2 t2 = Track2.builder()
          .track("4111111111111111=201112345612345678901").build();
        Card c = Card.builder()
          .pan("4111111111111111")
          .exp("2011")
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
        m.set(45, "%B4111111111111111^FAT ALBERT                ^201112345671234567890?");
        m.set(35, "4111111111111111=201112345612345678901");
        m.set(2, "4111111111111111");
        m.set(14, "2011");
        Card c = Card.builder()
          .isomsg(m)
          .cvv2("123")
          .serviceCode("123")
          .build();

        assertEquals(false, c.isExpired(new Date()), "not expired");
        assertEquals("4111111111111111", c.getPan(), "pan");
        assertEquals("2011", c.getExp(), "exp");
        assertEquals("123", c.getServiceCode(), "serviceCode");
        assertEquals("123", c.getTrack1().getServiceCode(), "t1.serviceCode");
        assertEquals("123", c.getTrack2().getServiceCode(), "t2.serviceCode");
        assertEquals("123", c.getCvv2(), "cvv2");
        assertEquals("4567", c.getTrack1().getCvv(), "t1.cvv");
        assertEquals("4561", c.getTrack2().getCvv(), "t2.cvv");
        assertEquals("1234567890", c.getTrack1().getDiscretionaryData(), "discretionaryData");
        assertEquals("2345678901", c.getTrack2().getDiscretionaryData(), "discretionaryData");
    }

    @Test
    public void testOverrideTrack1Pattern() throws Throwable {
        Track1 t1 = Track1.builder()
          .pattern(Pattern.compile("^[%]?[A-Z]+([0-9]{1,19})\\^([^\\^]{2,28})\\^([0-9]{4})([0-9]{3})([0-9]{4})?([0-9]{1,10})?"))
          .track("B4111111111111111^ALPHAMERDADO PRUEBA         ^201110100026.000.003-6    000").build();
        Track2 t2 = Track2.builder()
          .track("4111111111111111=201110145612345678901").build();
        Card c = Card.builder()
          .pan("4111111111111111")
          .exp("2011")
          .cvv("123")
          .cvv2("4567")
          .serviceCode("101")
          .track1(t1)
          .track2(t2)
          .build();

        assertEquals(false, c.isExpired(new Date()), "not expired");
    }
}
