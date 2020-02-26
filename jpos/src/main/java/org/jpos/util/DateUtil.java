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

package org.jpos.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    static SimpleDateFormat dfDate = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    static SimpleDateFormat dfDateTime = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.US);

    static SimpleDateFormat dfDate_mmddyyyy = new SimpleDateFormat("MM/dd/yyyy");
    static SimpleDateFormat dfDate_yyyymmdd = new SimpleDateFormat("yyyyMMdd");
    static SimpleDateFormat dfDateTime_mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    static SimpleDateFormat dfDate_mmddyy = new SimpleDateFormat("MM/dd/yy");
    public static Date parseDate (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate.parse (s);
    }
    public static Date parseDate_mmddyyyy (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate_mmddyyyy.parse (s);
    }
    public static Date parseDate_yyyymmdd (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate_yyyymmdd.parse (s);
    }
    public static Date parseDate_mmddyy (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDate_mmddyy.parse (s);
    }
    
    public static Date parseDateTime (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDateTime.parse (s);
    }
    public static Date parseDateTime_mmddyyyy (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDateTime_mmddyyyy.parse (s);
    }
    public static Date parseTimestamp (String s) throws ParseException {
        if (s == null)
            return null;
        return dfDateTime_mmddyyyy.parse (s);
    }

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
    
    public static String dateToString (Date d) {
        if (d == null)
            return null;
        return dfDate.format (d);
    }
    public static String dateToString_mmddyyyy (Date d) {
        if (d == null)
            return null;
        return dfDate_mmddyyyy.format (d);
    }
    
    public static String dateTimeToString (Date d) {
        if (d == null)
            return null;
        return dfDateTime.format (d);
    }
    public static String dateTimeToString (Date d, String tzString) {
        if (d == null)
            return null;
        DateFormat df = (DateFormat) dfDateTime.clone();
        if (tzString != null)
            df.setTimeZone (TimeZone.getTimeZone (tzString));
        return df.format (d);
    }
    public static String dateTimeToString_mmddyyyy (Date d) {
        if (d == null)
            return null;
        return dfDateTime_mmddyyyy.format (d);
    }
    public static String timestamp (Date d) {
        if (d == null)
            return null;
        return dfDateTime_mmddyyyy.format (d);
    }
    public static String postdate (Date d) {
        if (d == null)
            return null;
        return dfDate_mmddyyyy.format (d);
    }

   /**
    * @param d MMDDAA
    * @param t HHMMSS
    * @return Date Object
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
    * @param t HHMM[SS]
    * @return Date Object
    */
    public static Date parseTime (String t) {
        return parseTime (t, new Date());
    }
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

    public static String dateToString (Date d, String tzString) {
        DateFormat df = (DateFormat) DateFormat.getDateInstance().clone();
        if (tzString != null)
            df.setTimeZone (TimeZone.getTimeZone (tzString));
        return df.format (d);
    }
    public static String timeToString (Date d, String tzString) {
        DateFormat df = (DateFormat) 
            DateFormat.getTimeInstance(DateFormat.SHORT).clone();
        if (tzString != null)
            df.setTimeZone (TimeZone.getTimeZone (tzString));
        return df.format (d);
    }
    public static String timeToString (Date d) {
        return timeToString (d, null);
    }
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

