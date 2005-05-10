/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.iso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

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
        /**
         * Formats a date object, using the default time zone for this host
         * @param d date object to be formatted
         * @param pattern to be used for formatting
         */
    public static String formatDate (Date d, String pattern) {
        return formatDate(d, pattern, TimeZone.getDefault());
    }
    /**
     * You should use this version of formatDate() if you want a specific 
     * timeZone to calculate the date on.
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
            (SimpleDateFormat) DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.MEDIUM, Locale.UK);

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
        int YY = 0;
        if (d.length() == 14) {
            YY = Integer.parseInt(d.substring (0, 4));
            d = d.substring (4);
        }
        else if (d.length() == 12) {
            YY = 2000 + Integer.parseInt(d.substring (0, 2));
            d = d.substring (2);
        } 
        int MM = Integer.parseInt(d.substring (0, 2))-1;
        int DD = Integer.parseInt(d.substring (2, 4));
        int hh = Integer.parseInt(d.substring (4, 6));
        int mm = Integer.parseInt(d.substring (6, 8));
        int ss = Integer.parseInt(d.substring (8,10));

        Calendar cal = new GregorianCalendar();
        Date now = new Date();

        cal.setTime (now);
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

            if (Math.abs (now.getTime() - previousYear.getTime()) <
                Math.abs (now.getTime() - thisYear.getTime()) )
                thisYear = previousYear;
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
}

