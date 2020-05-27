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

package org.jpos.core;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author apr@cs.com.uy
 * @since jPOS 1.1
 *
 * This class is called 'CardHolder', but a better name could have been 'Card'
 * At some point we'll deprecate this one and create a new 'Card' class.
 */
public class CardHolder implements Cloneable, Serializable, Loggeable {
    private static final long serialVersionUID = 7449770625551878435L;
    private static final String TRACK1_SEPARATOR = "^";
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
    protected String trailer;
    /**
     * Optional security code (CVC, CVV, Locale ID, wse)
     * @serial
     */
    protected String securityCode;
    /**
     * Track1 Data
     * @serial
     */
    protected String track1;

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
     * Construct a CardHolder based on content received on
     * field 35 (track2) or field 2 (PAN) + field 14 (EXP)
     * @param m an ISOMsg
     * @throws InvalidCardException
     */
    public CardHolder (ISOMsg m)
        throws InvalidCardException
    {
        super();
        if (m.hasField(35))
            parseTrack2((String) m.getValue(35));
        else if (m.hasField(2)) {
            setPAN((String) m.getValue(2));
            if (m.hasField(14))
                setEXP((String) m.getValue(14));
        } else {
            throw new InvalidCardException("required fields not present");
        }
        if (m.hasField(45)) {
            setTrack1((String) m.getValue(45));
        }
        if (m.hasField(55)) {
            setSecurityCode(m.getString(55));
        }
    }

    /**
     * extract pan/exp/trailler from track2
     * @param s a valid track2
     * @exception InvalidCardException
     */
    public void parseTrack2 (String s) 
        throws InvalidCardException
    {
        if (s == null)
            throw new InvalidCardException ("null track2 data");
        int separatorIndex = s.replace ('D','=').indexOf(TRACK2_SEPARATOR);
        if (separatorIndex > 0 && s.length() > separatorIndex+4) {
            pan = s.substring(0, separatorIndex);
            exp = s.substring(separatorIndex+1, separatorIndex+1+4);
            trailer = s.substring(separatorIndex+1+4);
        } else 
            throw new InvalidCardException ("Invalid track2 format");
    }

    /**
     * @param track1 card's track1
     */
    public void setTrack1(String track1) {
        this.track1 = track1;
    }

    /**
     * @return the track1
     */
    public String getTrack1() {
        return track1;
    }

    /**
     * @return true if we have a track1
     */
    public boolean hasTrack1() {
        return track1!=null;
    }

    /**
     * @return the Name written on the card (from track1)
     */
    public String getNameOnCard() {
        String name = null;
        if (track1!=null) {
            StringTokenizer st = 
                    new StringTokenizer(track1, TRACK1_SEPARATOR);
            if (st.countTokens()<2)
                return null;
            st.nextToken(); // Skips the first token
            name = st.nextToken(); // This is the name
        }
        return name;
    }

    /**
     * @return reconstructed track2 or null
     */
    public String getTrack2() {
        if (hasTrack2())
            return pan + TRACK2_SEPARATOR + exp + trailer;
        else
            return null;
    }
    /**
     * @return true if we have a (may be valid) track2
     */
    public boolean hasTrack2() {
        return pan != null && exp != null && trailer != null;
    }

    /**
     * assigns securityCode to this CardHolder object
     * @param securityCode Card's security code
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
     * @deprecated use getTrailer()
     * @return trailer (may be null)
     */
    @SuppressWarnings("unused")
    public String getTrailler() {
        return trailer;
    }
    /**
     * Set Card's trailer
     * @deprecated use setTrailer
     * @param trailer Card's trailer
     */
    @SuppressWarnings("unused")
    public void setTrailler (String trailer) {
        this.trailer = trailer;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    /**
     * Sets Primary Account Number
     * @param pan Primary Account NUmber
     * @exception InvalidCardException
     */
    public void setPAN (String pan) 
        throws InvalidCardException
    { 
        if (pan.length() < MINPANLEN)
            throw new InvalidCardException ("PAN length smaller than min required");
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
            throw new InvalidCardException ("Invalid Exp length, must be 4");
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
     * @return true if card is expired (or expiration is invalid)
     */
    public boolean isExpired () {
        return isExpired(new Date());
    }

    /**
     * Y2K compliant expiration check
     * @param currentDate current system's date
     * @return true if card is expired (or expiration is invalid)
     */
    public boolean isExpired(Date currentDate) {
        if (exp == null || exp.length() != 4)
            return true;
        String now = ISODate.formatDate(currentDate, "yyyyMM");
        try {
            int mm = Integer.parseInt(exp.substring(2));
            int aa = Integer.parseInt(exp.substring(0,2));
            if (aa < 100 && mm > 0 && mm <= 12) {
                String expDate = (aa < 70 ? "20" : "19") + exp;
                if (expDate.compareTo(now) >= 0)
                    return false;
            }
        } catch (NumberFormatException ignored) {
            // NOPMD
        }
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
                crc+= c*2 >= 10 ? c*2 -9 : c*2;
            else
                crc+=c;
        }
        return crc % 10 == 0;
    }

    /**
     * dumps CardHolder basic information<br>
     * by default we do not dump neither track1/2 nor securityCode
     * for security reasons.
     * @param p a PrintStream usually suplied by Logger
     * @param indent ditto
     * @see org.jpos.util.Loggeable
     */
    public void dump (PrintStream p, String indent) {
        p.print (indent + "<CardHolder");
        if (hasTrack1())
            p.print (" trk1=\"true\"");

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
        return trailer != null && trailer.length() >= 3 ?
            trailer.substring (0, 3) :
            "   ";
    }
    public boolean seemsManualEntry() {
        return trailer == null || trailer.trim().length() == 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (exp == null ? 0 : exp.hashCode());
        result = prime * result + (pan == null ? 0 : pan.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CardHolder other = (CardHolder) obj;
        if (exp == null) {
            if (other.exp != null)
                return false;
        } else if (!exp.equals(other.exp))
            return false;
        if (pan == null) {
            if (other.pan != null)
                return false;
        } else if (!pan.equals(other.pan))
            return false;
        return true;
    }
}
