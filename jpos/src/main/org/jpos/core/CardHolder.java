/**
 * @author apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1999/09/26 19:54:05  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;
import java.io.*;
import uy.com.cs.jpos.iso.ISODate;
import uy.com.cs.jpos.iso.Loggeable;

public class CardHolder implements Cloneable, Serializable, Loggeable {
    public static final char TRACK2_SEPARATOR = '=';
    public static final int  BINLEN           =  6;
    public static final int  MINPANLEN        = 10;
    protected String pan;
    protected String exp;
    protected String trailler;
    protected String securityCode;	// CVC, CVV, Locale ID, wse

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
     * @return bank issuer number
     */
    public String getBIN () { 
	return pan.substring(0, BINLEN);
    }

    /**
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
}
