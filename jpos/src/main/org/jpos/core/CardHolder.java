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

/*
 * $Log$
 * Revision 1.12  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.11  2000/10/23 10:43:17  apr
 * Added seemsManualEntry() method
 *
 * Revision 1.10  2000/07/22 20:29:17  apr
 * Added equals method
 *
 * Revision 1.9  2000/06/20 11:05:17  apr
 * Added set/get Trailler
 *
 * Revision 1.8  2000/04/16 23:53:06  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.7  2000/03/02 12:31:01  apr
 * Get rid of javadoc warnings - done
 *
 * Revision 1.6  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.5  2000/01/11 01:24:40  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.4  1999/12/11 18:06:45  apr
 * Added getServiceCode() method
 *
 * Revision 1.3  1999/11/26 12:16:46  apr
 * CVS devel snapshot
 *
 * Revision 1.2  1999/09/26 22:31:57  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:05  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package org.jpos.core;
import java.io.*;
import org.jpos.iso.ISODate;
import org.jpos.util.Loggeable;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * represents a CardHolder
 */
public class CardHolder implements Cloneable, Serializable, Loggeable {
    private static final char TRACK2_SEPARATOR = '=';
    private static final int  BINLEN           =  6;
    private static final int  MINPANLEN        = 10;
    /**
     * Primary Account Number
     * @serial
     */
    protected String pan;
    /**
     * Expiration date (YYMM)
     * @serial
     */
    protected String exp;
    /**
     * Track2 trailler
     * @serial
     */
    protected String trailler;
    /**
     * Optional security code (CVC, CVV, Locale ID, wse)
     * @serial
     */
    protected String securityCode;

    /**
     * creates an empty CardHolder
     */
    public CardHolder() {
	super();
    }

    /**
     * creates a new CardHolder based on track2
     * @param track2 cards track2
     * @exception InvalidCardException
     */
    public CardHolder (String track2) 
	throws InvalidCardException
    {
	super();
	parseTrack2 (track2);
    }

    /**
     * creates a new CardHolder based on pan and exp
     * @param track2 cards track2
     * @exception InvalidCardException
     */
    public CardHolder (String pan, String exp) 
	throws InvalidCardException
    {
	super();
	setPAN (pan);
	setEXP (exp);
    }

    /**
     * extract pan/exp/trailler from track2
     * @param s a valid track2
     * @exception InvalidCardException
     */
    public void parseTrack2 (String s) 
	throws InvalidCardException
    {
        int separatorIndex = s.indexOf(TRACK2_SEPARATOR);
        if ((separatorIndex > 0) && (s.length() > separatorIndex+4)) {
            pan = s.substring(0, separatorIndex);
            exp = s.substring(separatorIndex+1, separatorIndex+1+4);
	    trailler = s.substring(separatorIndex+1+4);
        } else 
	    throw new InvalidCardException (s);
    }

    /**
     * @return reconstructed track2 or null
     */
    public String getTrack2() {
	if (hasTrack2())
	    return pan + TRACK2_SEPARATOR + exp + trailler;
	else
	    return null;
    }
    /**
     * @return true if we have a (may be valid) track2
     */
    public boolean hasTrack2() {
	return (pan != null && exp != null && trailler != null);
    }

    /**
     * assigns securityCode to this CardHolder object
     * @param securityCode
     */
    public void setSecurityCode(String securityCode) {
	this.securityCode = securityCode;
    }
    /**
     * @return securityCode (or null)
     */
    public String getSecurityCode() {
	return securityCode;
    }
    /**
     * @return true if we have a security code
     */
    public boolean hasSecurityCode() {
	return securityCode != null;
    }
    /**
     * @return trailler (may be null)
     */
    public String getTrailler() {
	return trailler;
    }
    /**
     * Set Trailler (used by OR-mapping stuff)
     * @param trailler
     */
    public void setTrailler (String trailler) {
	this.trailler = trailler;
    }

    /**
     * Sets Primary Account Number
     * @param pan
     * @exception InvalidCardException
     */
    public void setPAN (String pan) 
	throws InvalidCardException
    { 
	if (pan.length() < MINPANLEN)
	    throw new InvalidCardException (pan);
	this.pan = pan;
    }

    /**
     * @return Primary Account Number
     */
    public String getPAN () { 
	return pan;
    }

    /**
     * Get Bank Issuer Number
     * @return bank issuer number
     */
    public String getBIN () { 
	return pan.substring(0, BINLEN);
    }

    /**
     * Set Expiration Date
     * @param exp card expiration date
     * @exception InvalidCardException
     */
    public void setEXP (String exp) 
	throws InvalidCardException
    { 
	if (exp.length() != 4)
	    throw new InvalidCardException (pan+"/"+exp);
	this.exp = exp;
    }

    /**
     * Get Expiration Date
     * @return card expiration date
     */
    public String getEXP () { 
	return exp;
    }

    /**
     * Y2K compliant expiration check
     * @return true if card is expired (or invalid exp)
     */
    public boolean isExpired () {
	if (exp == null || exp.length() != 4)
	    return true;
        String now = ISODate.formatDate(new java.util.Date(), "yyyyMM");
        try {
            int mm = Integer.parseInt(exp.substring(2));
            int aa = Integer.parseInt(exp.substring(0,2));
            if ((aa < 100) && (mm > 0) && (mm <= 12)) {
                String expDate = ((aa < 70) ? "20" : "19") + exp;
                if (expDate.compareTo(now) >= 0)
                    return false;
            }
        } catch (NumberFormatException e) { }
        return true;
    }
    public boolean isValidCRC () {
        return isValidCRC(this.pan);
    }
    public static boolean isValidCRC (String p) {
        int i, crc;

        int odd = p.length() % 2;
        
        for (i=crc=0; i<p.length(); i++) {
            char c = p.charAt(i);
            if (!Character.isDigit (c))
                return false;
            c = (char) (c - '0');
            if (i % 2 == odd)
                crc+=(c*2) >= 10 ? ((c*2)-9) : (c*2);        
            else
                crc+=c;
        }
        return crc % 10 == 0;
    }

    /**
     * dumps CardHolder basic information<br>
     * by default we do not dump neither track2 nor securityCode
     * for security reasons.
     * @param p a PrintStream usually suplied by Logger
     * @param indent ditto
     * @see org.jpos.util.Loggeable
     */
    public void dump (PrintStream p, String indent) {
        p.print (indent + "<CardHolder");
	if (hasTrack2())
	    p.print (" trk2=\"true\"");

	if (hasSecurityCode())
	    p.print (" sec=\"true\"");

	if (isExpired())
	    p.print (" expired=\"true\"");

        p.println (">");
	p.println (indent + "  " + "<pan>" +pan +"</pan>");
	p.println (indent + "  " + "<exp>" +exp +"</exp>");
        p.println (indent + "</CardHolder>");
    }

    /**
     * @return ServiceCode (if available) or a String with three blanks
     */
    public String getServiceCode () {
	return (trailler != null && trailler.length() >= 3) ?
	    trailler.substring (0, 3) :
	    "   ";
    }
    public boolean seemsManualEntry() {
        return trailler == null ? true : (trailler.trim().length() == 0);
    }

    /**
     * compares two cardholder object<br>
     * based on PAN and EXP
     * @param obj a CardHolder instance
     * @return true if pan and exp matches
     */
    public boolean equals(Object obj) {
	if ((obj != null) && (obj instanceof CardHolder)) {
	    CardHolder ch = (CardHolder) obj;
	    if ( (pan != null) && (ch.pan != null) &&
	         (exp != null) && (ch.exp != null) &&
		 pan.equals (ch.pan) && exp.equals (ch.exp))
		return true;
	}
	return false;
    }
}
