/*
 * $Log$
 * Revision 1.5  2000/01/11 01:24:40  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
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

package uy.com.cs.jpos.core;
import java.io.*;
import uy.com.cs.jpos.iso.ISODate;
import uy.com.cs.jpos.util.Loggeable;

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
     */
    protected String pan;
    /**
     * Expiration date (YYMM)
     */
    protected String exp;
    /**
     * Track2 trailler
     */
    protected String trailler;
    /**
     * Optional security code (CVC, CVV, Locale ID, wse)
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
     * @see uy.com.cs.jpos.iso.Loggeable
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
}
