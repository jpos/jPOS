/**
 * ISODate
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOUtil
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:25  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.util.*;
import java.text.*;

public class ISODate {
	public static String formatDate (Date d, String pattern) {
		SimpleDateFormat df =
			(SimpleDateFormat) DateFormat.getDateTimeInstance();

		df.setTimeZone (new SimpleTimeZone(-3 * 60*60*1000, "AGT"));
		df.applyPattern (pattern);
		return df.format (d);
	}
	public static Date parse(String s) {
		Date d = null;
		SimpleDateFormat df =
			(SimpleDateFormat) DateFormat.getDateInstance(
				DateFormat.SHORT, Locale.UK);

		df.setTimeZone (new SimpleTimeZone(-3 * 60*60*1000, "AGT"));
		try {
			d = df.parse (s);
		} catch (java.text.ParseException e) {
			System.out.println (e);
			System.out.println ("invalid date "+s);
		}
		return d;
	}
	public static String getDateTime (Date d) {
		return formatDate (d, "MMddHHmmss");
	}
	public static String getTime (Date d) {
		return formatDate (d, "HHmmss");
	}
	public static String getDate(Date d) {
		return formatDate (d, "MMdd");
	}
	public static void main (String args[]) {
		Date d = parse("28/05/1963");
		System.out.println ("getDate() = " + getDate (d));
		System.out.println ("getTime() = " + getTime (d));
		System.out.println ("Date: " + d);
	}
}

