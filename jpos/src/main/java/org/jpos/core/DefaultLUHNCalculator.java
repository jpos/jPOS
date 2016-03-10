package org.jpos.core;

public class DefaultLUHNCalculator implements LUHNCalculator {
    /**
     * Compute card's check digit (LUHN)
     * @param p PAN (without checkdigit)
     * @return the checkdigit
     */
    public char calculate (String p)
        throws InvalidCardException
    {
        int i, crc;
        int odd = p.length() % 2;

        for (i=crc=0; i<p.length(); i++) {
            char c = p.charAt(i);
            if (!Character.isDigit (c)) {
                throw new IllegalArgumentException("Invalid PAN " + p);
            }
            c = (char) (c - '0');
            if (i % 2 != odd)
                crc+= c*2 >= 10 ? c*2 -9 : c*2;
            else
                crc+=c;
        }

        return (char) ((crc % 10 == 0 ? 0 : 10 - crc % 10) + '0');
    }

    /**
     * Verify Card's PAN
     * @param p full card PAN
     * @return true if pan LUHN's matches
     */
    public boolean verify (String p) throws InvalidCardException {
        if (p == null || p.length() < 5)
            throw new InvalidCardException ("Invalid PAN " + p);

        return p.charAt(p.length()-1) == calculate (p.substring(0, p.length()-1));
    }
}
