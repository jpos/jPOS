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

package org.jpos.iso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

/**
 * provides various parsing and format functions used
 * by the ISO 8583 specs.
 *
 * @author apr@cs.com.uy
 * @author Hani S. Kirollos
 * @version $Id$
 * @see ISOUtil
 */
public class ISODate {
    private ISODate() {
        throw new AssertionError();
    }

    public static final long ONE_YEAR = 365L*86400L*1000L;
   /**
    * Formats a date object, using the default time zone for this host
    *
    * WARNING: See <a href="https://jpos.org/faq/isodate_pattern.html">important issue</a> related to date pattern.
    *
    * @param d date object to be formatted
    * @param pattern to be used for formatting
    */
    public static String formatDate (Date d, String pattern) {
        return formatDate(d, pattern, TimeZone.getDefault());
    }
    /**
     * You should use this version of formatDate() if you want a specific 
     * timeZone to calculate the date on.
     *
     * WARNING: See <a href="https://jpos.org/faq/isodate_pattern.html">important issue</a> related to date pattern.
     *
     * @param d date object to be formatted
     * @param pattern to be used for formatting
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     */
    public static String formatDate (Date d, String pattern, TimeZone timeZone) {
        SimpleDateFormat df =
            (SimpleDateFormat) DateFormat.getDateTimeInstance();
        df.setTimeZone(timeZone);
        df.applyPattern(pattern);
        return df.format(d);
    }
    /**
     * converts a string in DD/MM/YY format to a Date object
     * Warning: return null on invalid dates (prints Exception to console)
     * Uses default time zone for this host
     * @return parsed Date (or null)
     */
    public static Date parse(String s) {
        return parse(s, TimeZone.getDefault());
    }
    /**
     * converts a string in DD/MM/YY format to a Date object
     * Warning: return null on invalid dates (prints Exception to console)
     * @param s String in DD/MM/YY recorded in timeZone
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return parsed Date (or null)
     */
    public static Date parse(String s, TimeZone timeZone) {
        Date d = null;
        SimpleDateFormat df =
            (SimpleDateFormat) DateFormat.getDateInstance(
                DateFormat.SHORT, Locale.UK);
        df.setTimeZone (timeZone);
        try {
            d = df.parse (s);
        } catch (java.text.ParseException e) {
        }
        return d;
    }
    /**
     * converts a string in DD/MM/YY HH:MM:SS format to a Date object
     * Warning: return null on invalid dates (prints Exception to console)
     * Uses default time zone for this host
     * @return parsed Date (or null)
     */
    public static Date parseDateTime(String s) {
        return parseDateTime(s, TimeZone.getDefault());
    }
    /**
     * converts a string in DD/MM/YY HH:MM:SS format to a Date object
     * Warning: return null on invalid dates (prints Exception to console)
     * @param s string in DD/MM/YY HH:MM:SS format recorded in timeZone
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return parsed Date (or null)
     */
    public static Date parseDateTime(String s, TimeZone timeZone) {
        Date d = null;
        SimpleDateFormat df =
            new SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.UK);

        df.setTimeZone (timeZone);
        try {
            d = df.parse (s);
        } catch (java.text.ParseException e) { }
        return d;
    }

    /**
     * try to find out suitable date given [YY[YY]]MMDDhhmmss format<br>
     * (difficult thing being finding out appropiate year)
     * @param d date formated as [YY[YY]]MMDDhhmmss, typical field 13 + field 12
     * @return Date
     */
    public static Date parseISODate (String d) {
        return parseISODate (d, System.currentTimeMillis());
    }

    /**
     * try to find out suitable date given [YY[YY]]MMDDhhmmss format<br>
     * (difficult thing being finding out appropiate year)
     * @param d date formated as [YY[YY]]MMDDhhmmss, typical field 13 + field 12
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return Date
     */
    public static Date parseISODate (String d, TimeZone timeZone) {
        return parseISODate (d, System.currentTimeMillis(), timeZone);
    }

    /**
     * try to find out suitable date given [YY[YY]]MMDDhhmmss format<br>
     * (difficult thing being finding out appropiate year)
     * @param d date formated as [YY[YY]]MMDDhhmmss, typical field 13 + field 12
     * @param currentTime currentTime in millis
     * @return Date
     */
    public static Date parseISODate (String d, long currentTime) {
        return parseISODate (d, currentTime, TimeZone.getDefault() );
    }
    /**
     * try to find out suitable date given [YY[YY]]MMDDhhmmss format<br>
     * (difficult thing being finding out appropiate year)
     * @param d date formated as [YY[YY]]MMDDhhmmss, typical field 13 + field 12
     * @param currentTime currentTime in millis
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return Date
     */
    public static Date parseISODate (String d, long currentTime, TimeZone timeZone) {
        int YY = 0;

        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(timeZone);
        Date now = new Date(currentTime);
        cal.setTime (now);

        if (d.length() == 14) {
            YY = Integer.parseInt(d.substring (0, 4));
            d = d.substring (4);
        }
        else if (d.length() == 12) {
            YY = calculateNearestFullYear(Integer.parseInt(d.substring(0, 2)), cal);
            d = d.substring (2);
        } 
        int MM = Integer.parseInt(d.substring (0, 2))-1;
        int DD = Integer.parseInt(d.substring (2, 4));
        int hh = Integer.parseInt(d.substring (4, 6));
        int mm = Integer.parseInt(d.substring (6, 8));
        int ss = Integer.parseInt(d.substring (8,10));
        
        cal.set (Calendar.MONTH, MM);
        cal.set (Calendar.DATE, DD);
        cal.set (Calendar.HOUR_OF_DAY, hh);
        cal.set (Calendar.MINUTE, mm);
        cal.set (Calendar.SECOND, ss);
        cal.set (Calendar.MILLISECOND, 0);

        if (YY != 0) {
            cal.set (Calendar.YEAR, YY);
            return cal.getTime();
        } 
        else {
            Date thisYear = cal.getTime();
            cal.set (Calendar.YEAR, cal.get (Calendar.YEAR)-1);
            Date previousYear = cal.getTime();
            cal.set (Calendar.YEAR, cal.get (Calendar.YEAR)+2);
            Date nextYear = cal.getTime();
            if (Math.abs (now.getTime() - previousYear.getTime()) <
                Math.abs (now.getTime() - thisYear.getTime())) 
            {
                thisYear = previousYear;
            } else if (Math.abs (now.getTime() - thisYear.getTime()) >
                       Math.abs (now.getTime() - nextYear.getTime()))
            {
                thisYear = nextYear;
            }
            return thisYear;
        }
    }

    /**
     * @return date in MMddHHmmss format suitable for FIeld 7
     */
    public static String getDateTime (Date d) {
        return formatDate (d, "MMddHHmmss");
    }
        /**
         * @param d date object to be formatted
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return date in MMddHHmmss format suitable for FIeld 7
     */
    public static String getDateTime (Date d, TimeZone timeZone) {
        return formatDate (d, "MMddHHmmss", timeZone);
    }
    /**
     * @return date in HHmmss format - suitable for field 12
     */
    public static String getTime (Date d) {
        return formatDate (d, "HHmmss");
    }
    /**
         * @param d date object to be formatted
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return date in HHmmss format - suitable for field 12
     */
    public static String getTime (Date d, TimeZone timeZone) {
        return formatDate (d, "HHmmss", timeZone);
    }
    /**
     * @return date in MMdd format - suitable for field 13
     */
    public static String getDate(Date d) {
        return formatDate (d, "MMdd");
    }
    /**
         * @param d date object to be formatted
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return date in MMdd format - suitable for field 13
     */
    public static String getDate(Date d, TimeZone timeZone) {
        return formatDate (d, "MMdd", timeZone);
    }
    /**
     * @return date in yyMMdd format - suitable for ANSI field 8
     */
    public static String getANSIDate(Date d) {
        return formatDate (d, "yyMMdd");
    }
    /**
         * @param d date object to be formatted
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return date in yyMMdd format - suitable for ANSI field 8
     */
    public static String getANSIDate(Date d, TimeZone timeZone) {
        return formatDate (d, "yyMMdd", timeZone);
    }
    public static String getEuropeanDate(Date d) {
        return formatDate (d, "ddMMyy");
    }
    public static String getEuropeanDate(Date d, TimeZone timeZone) {
        return formatDate (d, "ddMMyy", timeZone);
    }
    /**
     * @return date in yyMM format - suitable for field 14
     */
    public static String getExpirationDate(Date d) {
        return formatDate (d, "yyMM");
    }
    /**
         * @param d date object to be formatted
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     *        and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return date in yyMM format - suitable for field 14
     */
    public static String getExpirationDate(Date d, TimeZone timeZone) {
        return formatDate (d, "yyMM", timeZone);
    }

    /**
     * @param d date object to be formatted
     * @return date in YDDD format suitable for bit 31 or 37
     * depending on interchange
     */
    public static String getJulianDate(Date d) {
      String day = formatDate(d, "DDD", TimeZone.getDefault());
      String year = formatDate(d, "yy", TimeZone.getDefault());
      year = year.substring(1);
      return year + day;
    }

    /**
     * @param d date object to be formatted
     * @param timeZone for GMT for example, use TimeZone.getTimeZone("GMT")
     * and for Uruguay use TimeZone.getTimeZone("GMT-03:00")
     * @return date in YDDD format suitable for bit 31 or 37
     * depending on interchange
     */
    public static String getJulianDate(Date d, TimeZone timeZone) {
      String day = formatDate(d, "DDD", timeZone);
      String year = formatDate(d, "yy", timeZone);
      year = year.substring(1);
      return year + day;
    }

    /**
     * Calculates the closest year in full YYYY format based on a two-digit year input.
     * The closest year is determined in relation to the current year provided by the Calendar instance.
     *
     * @param year The two-digit year to be converted (e.g., 23 for 2023 or 2123).
     * @param now  The current date provided as a Calendar instance used for reference.
     * @return The closest full year in YYYY format.
     * @throws IllegalArgumentException if the input year is not between 0 and 99.
     */
    private static int calculateNearestFullYear(int year, Calendar now) {
        if (year < 0 || year > 99) {
            throw new IllegalArgumentException("Year must be between 0 and 99");
        }

        int currentYear = now.get(Calendar.YEAR); // e.g., 2023
        int currentCentury = currentYear - currentYear % 100; // e.g., 2000 for 2023
        int possibleYear = currentCentury + year; // e.g., 2023 for year 23

        // Adjust to the closest century if needed
        if (Math.abs(year - currentYear % 100) > 50) {
            possibleYear += (year > currentYear % 100) ? -100 : 100;
        }
        return possibleYear;
    }

    public static String formatDuration(Duration d) {
        long days = d.toDays();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        StringJoiner sj = new StringJoiner(", ");
        if (days > 0)
            sj.add(days + "d");
        if (hours > 0)
            sj.add(hours + "h");
        if (minutes > 0)
            sj.add(minutes + "m");
        if (seconds > 0 || sj.length() == 0)
            sj.add(seconds + "s");
        return sj.toString();
    }
}
