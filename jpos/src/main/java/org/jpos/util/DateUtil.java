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

package org.jpos.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Convenience helpers for parsing and formatting dates and times in a small
 * set of jPOS-specific patterns. Methods that take {@code null} return
 * {@code null} so call sites do not need to guard themselves.
 */
public class DateUtil {
    /** Utility class; instances carry no state. */
    public DateUtil() {}
    static SimpleDateFormat dfDate = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    static SimpleDateFormat dfDateTime = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.US);

    static SimpleDateFormat dfDate_mmddyyyy = new SimpleDateFormat("MM/dd/yyyy");
    static SimpleDateFormat dfDate_yyyymmdd = new SimpleDateFormat("yyyyMMdd");
    static SimpleDateFormat dfDateTime_mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    static SimpleDateFormat dfDate_mmddyy = new SimpleDateFormat("MM/dd/yy");
    /**
     * Parses {@code s} using the US short date format.
     *
     * @param s date string, or {@code null}
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseDate (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate.parse (s);
    }
    /**
     * Parses {@code s} using the {@code MM/dd/yyyy} pattern.
     *
     * @param s date string, or {@code null}
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseDate_mmddyyyy (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate_mmddyyyy.parse (s);
    }
    /**
     * Parses {@code s} using the {@code yyyyMMdd} pattern.
     *
     * @param s date string, or {@code null}
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseDate_yyyymmdd (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate_yyyymmdd.parse (s);
    }
    /**
     * Parses {@code s} using the {@code MM/dd/yy} pattern.
     *
     * @param s date string, or {@code null}
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseDate_mmddyy (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate_mmddyy.parse (s);
    }

    /**
     * Parses {@code s} using the US short date / medium time format.
     *
     * @param s date-time string, or {@code null}
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseDateTime (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDateTime.parse (s);
    }
    /**
     * Parses {@code s} using the {@code MM/dd/yyyy HH:mm:ss} pattern.
     *
     * @param s date-time string, or {@code null}
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseDateTime_mmddyyyy (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDateTime_mmddyyyy.parse (s);
    }
    /**
     * Parses {@code s} using the {@code MM/dd/yyyy HH:mm:ss} pattern.
     *
     * @param s timestamp string, or {@code null}
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseTimestamp (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDateTime_mmddyyyy.parse (s);
    }

    /**
     * Parses {@code s} using the {@code MM/dd/yyyy HH:mm:ss} pattern in the
     * given time zone.
     *
     * @param s        date-time string, or {@code null}
     * @param tzString time-zone identifier accepted by {@link TimeZone#getTimeZone(String)};
     *                 {@code null} keeps the JVM default zone
     * @return parsed date, or {@code null} when {@code s} is {@code null}
     * @throws ParseException if the string cannot be parsed
     */
    public static Date parseDateTime_mmddyyyy (String s, String tzString)
        throws ParseException
    {
        if (s == null)
            return null;
        DateFormat df = (DateFormat) dfDateTime_mmddyyyy.clone();
        if (tzString != null)
            df.setTimeZone (TimeZone.getTimeZone (tzString));
        return df.parse (s);
    }

    /**
     * Formats {@code d} using the US short date format.
     *
     * @param d date, or {@code null}
     * @return formatted string, or {@code null} when {@code d} is {@code null}
     */
    public static String dateToString (Date d) {
        if (d == null)
            return null;
        return dfDate.format (d);
    }
    /**
     * Formats {@code d} using the {@code MM/dd/yyyy} pattern.
     *
     * @param d date, or {@code null}
     * @return formatted string, or {@code null} when {@code d} is {@code null}
     */
    public static String dateToString_mmddyyyy (Date d) {
        if (d == null)
            return null;
        return dfDate_mmddyyyy.format (d);
    }

    /**
     * Formats {@code d} using the US short date / medium time format.
     *
     * @param d date, or {@code null}
     * @return formatted string, or {@code null} when {@code d} is {@code null}
     */
    public static String dateTimeToString (Date d) {
        if (d == null)
            return null;
        return dfDateTime.format (d);
    }
    /**
     * Formats {@code d} using the US short date / medium time format in the
     * given time zone.
     *
     * @param d        date, or {@code null}
     * @param tzString time-zone identifier; {@code null} keeps the JVM default zone
     * @return formatted string, or {@code null} when {@code d} is {@code null}
     */
    public static String dateTimeToString (Date d, String tzString) {
        if (d == null)
            return null;
        DateFormat df = (DateFormat) dfDateTime.clone();
        if (tzString != null)
            df.setTimeZone (TimeZone.getTimeZone (tzString));
        return df.format (d);
    }
    /**
     * Formats {@code d} using the {@code MM/dd/yyyy HH:mm:ss} pattern.
     *
     * @param d date, or {@code null}
     * @return formatted string, or {@code null} when {@code d} is {@code null}
     */
    public static String dateTimeToString_mmddyyyy (Date d) {
        if (d == null)
            return null;
        return dfDateTime_mmddyyyy.format (d);
    }
    /**
     * Formats {@code d} using the {@code MM/dd/yyyy HH:mm:ss} pattern.
     *
     * @param d date, or {@code null}
     * @return formatted string, or {@code null} when {@code d} is {@code null}
     */
    public static String timestamp (Date d) {
        if (d == null)
            return null;
        return dfDateTime_mmddyyyy.format (d);
    }
    /**
     * Formats {@code d} using the {@code MM/dd/yyyy} pattern.
     *
     * @param d date, or {@code null}
     * @return formatted string, or {@code null} when {@code d} is {@code null}
     */
    public static String postdate (Date d) {
        if (d == null)
            return null;
        return dfDate_mmddyyyy.format (d);
    }

   /**
    * Parses an {@code MMDDYY} date and {@code HHMMSS} time, choosing the
    * century closest to "now" so two-digit years near a century boundary
    * round to the correct year.
    *
    * @param d date in {@code MMDDYY}
    * @param t time in {@code HHMMSS}
    * @return parsed date
    */
    public static Date parseDateTime (String d, String t) {
        Calendar cal = new GregorianCalendar();
        Date now = new Date ();
        cal.setTime (now);

        int YY = Integer.parseInt (d.substring (4));
        int MM = Integer.parseInt (d.substring (0, 2))-1;
        int DD = Integer.parseInt (d.substring (2, 4));
        int hh = Integer.parseInt (t.substring (0, 2));
        int mm = Integer.parseInt (t.substring (2, 4));
        int ss = Integer.parseInt (t.substring (4));
        int century = cal.get (Calendar.YEAR) / 100;

        cal.set (Calendar.YEAR, (century * 100) + YY);
        cal.set (Calendar.MONTH, MM);
        cal.set (Calendar.DATE, DD);
        cal.set (Calendar.HOUR_OF_DAY, hh);
        cal.set (Calendar.MINUTE, mm);
        cal.set (Calendar.SECOND, ss);

        //
        // I expect this program to continue running by 2099 ... --apr@jpos.org
        //
	Date thisCentury = cal.getTime();
	cal.set (Calendar.YEAR, (--century * 100) + YY);
	Date previousCentury = cal.getTime();

	if (Math.abs (now.getTime() - previousCentury.getTime()) <
	    Math.abs (now.getTime() - thisCentury.getTime()) )
	    thisCentury = previousCentury;
	return thisCentury;
    }
   /**
    * Parses an {@code HHMM} or {@code HHMMSS} time using today as the date.
    *
    * @param t time string
    * @return parsed date with today's year/month/day
    */
    public static Date parseTime (String t) {
        return parseTime (t, new Date());
    }
    /**
     * Parses an {@code HHMM} or {@code HHMMSS} time using {@code now} as the
     * date portion.
     *
     * @param t   time string
     * @param now date supplying the year/month/day
     * @return parsed date with the supplied date and the parsed time
     */
    public static Date parseTime (String t, Date now) {
        Calendar cal = new GregorianCalendar();
        cal.setTime (now);

        int hh = Integer.parseInt (t.substring (0, 2));
        int mm = Integer.parseInt (t.substring (2, 4));
        int ss = t.length() > 4 ? Integer.parseInt (t.substring (4)) : 0;

        cal.set (Calendar.HOUR_OF_DAY, hh);
        cal.set (Calendar.MINUTE, mm);
        cal.set (Calendar.SECOND, ss);

	return cal.getTime();
    }

    /**
     * Formats {@code d} using the JVM default date format in the given time zone.
     *
     * @param d        date
     * @param tzString time-zone identifier; {@code null} keeps the JVM default zone
     * @return formatted date string
     */
    public static String dateToString (Date d, String tzString) {
        DateFormat df = (DateFormat) DateFormat.getDateInstance().clone();
        if (tzString != null)
            df.setTimeZone (TimeZone.getTimeZone (tzString));
        return df.format (d);
    }
    /**
     * Formats the time portion of {@code d} using the JVM short time format
     * in the given time zone.
     *
     * @param d        date
     * @param tzString time-zone identifier; {@code null} keeps the JVM default zone
     * @return formatted time string
     */
    public static String timeToString (Date d, String tzString) {
        DateFormat df = (DateFormat)
            DateFormat.getTimeInstance(DateFormat.SHORT).clone();
        if (tzString != null)
            df.setTimeZone (TimeZone.getTimeZone (tzString));
        return df.format (d);
    }
    /**
     * Formats the time portion of {@code d} using the JVM short time format.
     *
     * @param d date
     * @return formatted time string
     */
    public static String timeToString (Date d) {
        return timeToString (d, null);
    }
    /**
     * Renders a duration in milliseconds as a compact {@code 1h2m3s}-style string.
     *
     * @param period duration in milliseconds
     * @return human-readable duration; always includes a seconds component,
     *         and includes hours/minutes only when non-zero
     */
    public static String toDays (long period) {
        StringBuffer sb = new StringBuffer();
        long hours = period / 3600000L;
        if (hours > 0) {
            sb.append (hours);
            sb.append ("h");
            period -= (hours * 3600000L);
        }
        long mins = period / 60000L;
        if (mins > 0) {
            sb.append (mins);
            sb.append ("m");
            period -= (mins * 60000L);
        }
        long secs = period / 1000L;
        sb.append (secs);
        sb.append ("s");
        return sb.toString();
    }
}

