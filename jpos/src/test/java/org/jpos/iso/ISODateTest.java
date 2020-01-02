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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ISODateTest {
    TimeZone aus;

    @BeforeEach
    public void setUp() throws Exception {
        aus = TimeZone.getTimeZone("GMT+10:00");
    }

    @Test
    public void testONEYEAR() {
        assertThat(ISODate.ONE_YEAR, is(31536000000L));
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
        GregorianCalendar cal = new GregorianCalendar(aus);
        cal.set(2011, 02, 25);        
        String result = ISODate.getJulianDate(cal.getTime(), aus);
        assertThat(result, is("1084"));
    }

    @Test
    public void testGetExpirationDate() {
        GregorianCalendar cal = new GregorianCalendar(aus);
        cal.set(2011, 02, 25);
        String result = ISODate.getExpirationDate(cal.getTime(), aus);
        assertThat(result, is("1103"));
    }

    @Test
    public void testGetEuropeanDate() {
        GregorianCalendar cal = new GregorianCalendar(aus);
        cal.set(2011, 02, 25);
        String result = ISODate.getEuropeanDate(cal.getTime(), aus);
        assertThat(result, is("250311"));
    }

    @Test
    public void testGetANSIDate() {
        GregorianCalendar cal = new GregorianCalendar(aus);
        cal.set(2011, 02, 25);
        String result = ISODate.getANSIDate(cal.getTime(), aus);
        assertThat(result, is("110325"));
    }

    @Test
    public void testGetDate() {
        GregorianCalendar cal = new GregorianCalendar(aus);
        cal.set(2011, 02, 25);
        String result = ISODate.getDate(cal.getTime(), aus);
        assertThat(result, is("0325"));
    }

    @Test
    public void testGetTime() {
        // given
        Calendar cal = new GregorianCalendar(aus);
        cal.set(2011, Calendar.FEBRUARY, 25, 23, 55, 56);
        cal.set(Calendar.MILLISECOND, 23);
        Date date = cal.getTime();
        // when
        String result = ISODate.getTime(date, aus);
        // then
        assertThat(result, is("235556"));
        
        Calendar cal2 = new GregorianCalendar(TimeZone.getDefault());
        cal2.set(2011, Calendar.FEBRUARY, 25, 23, 55, 56);
        cal2.set(Calendar.MILLISECOND, 23);
        Date date2 = cal2.getTime();
        // when
        String result2 = ISODate.getTime(date2, TimeZone.getDefault());
        // then
        assertThat(result2, is("235556"));
    }

    @Test
    public void testGetDateTime() {
        // given
        Calendar cal = new GregorianCalendar(aus);
        cal.set(2011, Calendar.FEBRUARY, 25, 23, 55, 56);
        cal.set(Calendar.MILLISECOND, 23);
        Date date = cal.getTime();
        // when
        String result = ISODate.getDateTime(date, aus);
        // then
        assertThat(result, is("0225235556"));
        
        Calendar cal2 = new GregorianCalendar(TimeZone.getDefault());
        cal2.set(2011, Calendar.FEBRUARY, 25, 23, 55, 56);
        cal2.set(Calendar.MILLISECOND, 23);
        Date date2 = cal2.getTime();
        // when
        String result2 = ISODate.getDateTime(date2, TimeZone.getDefault());
        // then
        assertThat(result2, is("0225235556"));
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
