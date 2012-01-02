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

package org.jpos.iso;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ISODateTest {
    TimeZone aus;

    @Before
    public void setUp() throws Exception {
        aus = TimeZone.getTimeZone("GMT+10:00");
    }

    @Test
    public void testONEYEAR() {
        assertThat(ISODate.ONE_YEAR, is(1471228928L));
    }

    @Test
    public void testParseZoneAus() {
        java.util.Date result = ISODate.parse("27/12/2010", aus);
        assertThat(result.getTime(), is(1293372000000L));
    }

    @Test
    public void testParseDateZoneAusNonsenseDateReturnsValue() {
        java.util.Date result = ISODate.parse("31/02/2011", aus);
        assertThat(result.getTime(), is(1299074400000L));
    }

    @Test
    public void testParseBadInputSwallowsExceptionAndReturnsNull() {
        java.util.Date result = ISODate.parse("31/02/WIBBLE", aus);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void testParseDateZoneWithTimeSentReturnsOKButIgnoresTime() {
        long expectedDateWithoutTime = 1299074400000L;
        long unwantedDateIncludingTimeValue = 1299123895000L;
        java.util.Date result = ISODate.parse("31/02/2011 13:45:55", aus);
        assertThat(result.getTime(), allOf(is(expectedDateWithoutTime), is(not(unwantedDateIncludingTimeValue))));
    }

    @Test
    public void testParseStringTimeZoneAus() {
        java.util.Date result = ISODate.parseDateTime("27/12/2010 13:44:55", aus);
        assertThat(result.getTime(), is(1293421495000L));
    }

    @Test
    public void testParseStringTimeZoneAusNonsenseDateReturnsValue() {
        java.util.Date result = ISODate.parseDateTime("31/02/2011 13:44:55", aus);
        assertThat(result.getTime(), is(1299123895000L));
    }

    @Test
    public void testParseStringTimeZoneNoTimeSentReturnsNull() {
        java.util.Date result = ISODate.parseDateTime("31/02/2011", aus);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void testGetJulianDate() {
        Date date = new Date(2011, 02, 25);
        String result = ISODate.getJulianDate(date, aus);
        assertThat(result, is("1084"));
    }

    @Test
    public void testGetExpirationDate() {
        Date date = new Date(2011, 02, 25);
        String result = ISODate.getExpirationDate(date, aus);
        assertThat(result, is("1103"));
    }

    @Test
    public void testGetEuropeanDate() {
        Date date = new Date(2011, 02, 25);
        String result = ISODate.getEuropeanDate(date, aus);
        assertThat(result, is("250311"));
    }

    @Test
    public void testGetANSIDate() {
        Date date = new Date(2011, 02, 25);
        String result = ISODate.getANSIDate(date, aus);
        assertThat(result, is("110325"));
    }

    @Test
    public void testGetDate() {
        Date date = new Date(2011, 02, 25);
        String result = ISODate.getDate(date, aus);
        assertThat(result, is("0325"));
    }

    @Ignore("test failing on TZs other than aus, expected: is '235556' got: '115556'")
    @Test
    public void testGetTime() {
        // given
        Calendar cal = new GregorianCalendar(2011, Calendar.FEBRUARY, 25, 23, 55, 56);
        cal.set(Calendar.MILLISECOND, 23);
        Date date = cal.getTime();
        // when
        String result = ISODate.getTime(date, aus);
        // then
        assertThat(result, is("235556"));
    }

    @Ignore("test failing on TZs other than aus - Expected: is '0225235556' got: '0226115556'")
    @Test
    public void testGetDateTime() {
        // given
        Calendar cal = new GregorianCalendar(2011, Calendar.FEBRUARY, 25, 23, 55, 56);
        cal.set(Calendar.MILLISECOND, 23);
        Date date = cal.getTime();
        // when
        String result = ISODate.getDateTime(date, aus);
        // then
        assertThat(result, is("0225235556"));
    }

    @Test
    public void testParseISODate4DigitYear() {
        java.util.Date result = ISODate.parseISODate("20101227235657", aus);
        assertThat(result.getTime(), is(1293458217000L));
    }

    @Test
    public void testParseISODate2DigitYear() {
        java.util.Date result = ISODate.parseISODate("101227235657", aus);
        assertThat(result.getTime(), is(1293458217000L));
    }
}
