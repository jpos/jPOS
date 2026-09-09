/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void testPivotYear() {
        Date future = ISODate.parseISODate("20990101000000");
        assertEquals ("202306", ISODate.formatDate(ISODate.parseISODate("230601000000"), "yyyyMM"));
        assertEquals ("210106", ISODate.formatDate(ISODate.parseISODate("010601000000", future.getTime()), "yyyyMM"));
    }

    @Test
    void testParseISODateFeb29PrefersPreviousLeapYear() {
        Calendar now = new GregorianCalendar();
        now.clear();
        now.set(2025, Calendar.JANUARY, 10, 9, 0, 0);
        Date result = ISODate.parseISODate("0229090000", now.getTimeInMillis());
        assertThat(ISODate.formatDate(result, "yyyy-MM-dd"), is("2024-02-29"));
    }

    @Test
    void testParseISODateFeb29InLeapYearDoesNotRollToNextYear() {
        Calendar now = new GregorianCalendar();
        now.clear();
        now.set(2024, Calendar.OCTOBER, 15, 12, 0, 0);
        Date result = ISODate.parseISODate("0229120000", now.getTimeInMillis());
        assertThat(ISODate.formatDate(result, "yyyy-MM-dd"), is("2024-02-29"));
    }

    @Test
    void testParseISODateFeb29WithNoLeapYearNearbyRollsToMarch1st() {
        // 2025/2026/2027 are all non-leap, so none of the three candidate
        // years has a genuine Feb 29th; for backward compatibility this
        // keeps returning a Date the old way (rollover to March 1st in the
        // nearest candidate year) instead of returning null.
        Calendar now = new GregorianCalendar();
        now.clear();
        now.set(2026, Calendar.JUNE, 15, 12, 0, 0);
        Date result = ISODate.parseISODate("0229120000", now.getTimeInMillis());
        assertThat(ISODate.formatDate(result, "yyyy-MM-dd"), is("2026-03-01"));
    }

    @Test
    void testParseISODateFeb29PrefersNextLeapYear() {
        Calendar now = new GregorianCalendar();
        now.clear();
        now.set(2027, Calendar.DECEMBER, 20, 12, 0, 0);
        Date result = ISODate.parseISODate("0229120000", now.getTimeInMillis());
        assertThat(ISODate.formatDate(result, "yyyy-MM-dd"), is("2028-02-29"));
    }

    @Test
    void testParseISODateDoesNotLeakDstGapIntoOtherCandidates() {
        TimeZone newYork = TimeZone.getTimeZone("America/New_York");
        Calendar now = new GregorianCalendar(newYork);
        now.clear();
        // 2026-03-09 12:00 local; the previous-year candidate (2025-03-09) falls
        // in the US DST spring-forward gap, where 02:30 local time doesn't exist.
        now.set(2026, Calendar.MARCH, 9, 12, 0, 0);
        Date result = ISODate.parseISODate("0309023000", now.getTimeInMillis(), newYork);

        Calendar expected = new GregorianCalendar(newYork);
        expected.clear();
        expected.set(2026, Calendar.MARCH, 9, 2, 30, 0);
        assertThat(result, is(expected.getTime()));
    }

    @Test
    void testParseISODateWithMonthTooSmallDoesNotThrow() {
        // Month "00" is not a real month in any candidate year, so -- same
        // reasoning as the no-leap-year-nearby case above -- this falls back
        // to the old lenient-rollover behavior (month index -1 rolls back
        // into December of the previous year) for backward compatibility,
        // rather than throwing or returning null.
        Calendar now = new GregorianCalendar();
        now.clear();
        now.set(2025, Calendar.JUNE, 15, 12, 0, 0);
        Date result = ISODate.parseISODate("0001010000", now.getTimeInMillis());
        assertThat(ISODate.formatDate(result, "yyyy-MM-dd"), is("2024-12-01"));
    }

    @Test
    void testParseISODateWithMonthTooLargeDoesNotThrow() {
        // Month "13" is not a real month in any candidate year either, so
        // this falls back to the same old lenient-rollover behavior (month
        // index 12 rolls forward into January of the next year) instead of
        // throwing -- isValidDay() must reject out-of-range months rather
        // than index DAYS_IN_MONTH with them directly.
        Calendar now = new GregorianCalendar();
        now.clear();
        now.set(2025, Calendar.JUNE, 15, 12, 0, 0);
        Date result = ISODate.parseISODate("1301010000", now.getTimeInMillis());
        assertThat(ISODate.formatDate(result, "yyyy-MM-dd"), is("2026-01-01"));
    }
}
