package uy.com.cs.jpos.iso;

import java.util.*;
import java.text.*;

/**
 * provides various parsing and format functions used
 * by the ISO 8583 specs.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOUtil
 */
public class ISODate {
	public static String formatDate (Date d, String pattern) {
		SimpleDateFormat df =
			(SimpleDateFormat) DateFormat.getDateTimeInstance();

		// ##FIXME##
		// Our country Uruguay seems to be unknown for Java and
		// for various JRE implementations/OSs...
		// The following line shoulb be commented out on any supported
		// country (i.e. the US)
		//
		df.setTimeZone (new SimpleTimeZone(-3 * 60*60*1000, "AGT"));

		df.applyPattern (pattern);
		return df.format (d);
	}
	/**
	 * converts a string in DD/MM/YY format to a Date object
	 * Warning: return null on invalid dates (prints Exception to console)
	 *
	 * @return parsed Date
	 */
	public static Date parse(String s) {
		Date d = null;
		SimpleDateFormat df =
			(SimpleDateFormat) DateFormat.getDateInstance(
				DateFormat.SHORT, Locale.UK);

		// ##FIXME##
		df.setTimeZone (new SimpleTimeZone(-3 * 60*60*1000, "AGT"));
		try {
			d = df.parse (s);
		} catch (java.text.ParseException e) {
			System.out.println (e);
			System.out.println ("invalid date "+s);
		}
		return d;
	}
	/**
	 * converts a string in DD/MM/YY HH:MM:SS format to a Date object
	 * Warning: return null on invalid dates (prints Exception to console)
	 *
	 * @return parsed Date
	 */
	public static Date parseDateTime(String s) {
		Date d = null;
		SimpleDateFormat df =
			(SimpleDateFormat) DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.MEDIUM, Locale.UK);

		// ##FIXME##
		df.setTimeZone (new SimpleTimeZone(-3 * 60*60*1000, "AGT"));
		try {
			d = df.parse (s);
		} catch (java.text.ParseException e) {
			System.out.println (e);
			System.out.println ("invalid date/time "+s);
		}
		return d;
	}
	/**
	 * @return date in MMddHHmmss format suitable for FIeld 7
	 */
	public static String getDateTime (Date d) {
		return formatDate (d, "MMddHHmmss");
	}
	/**
	 * @return date in HHmmss format - suitable for field 12
	 */
	public static String getTime (Date d) {
		return formatDate (d, "HHmmss");
	}
	/**
	 * @return date in MMdd format - suitable for field 13
	 */
	public static String getDate(Date d) {
		return formatDate (d, "MMdd");
	}
}

