package org.jpos.iso;

import java.util.*;
import org.jpos.iso.ISOUtil;

/**
 * ISO Currency Conversion package 
 * @author salaman@teknos.com
 * @version $Id$
 */
public class Currency {
    String alphacode;
    int isocode;
    int numdecimals;
        
    public Currency(String alphacode, int isocode, int numdecimals) {
        this.alphacode=alphacode;
        this.isocode=isocode;
        this.numdecimals=numdecimals;
    }
    public int getDecimals() {
        return numdecimals;
    }
    public int getIsoCode() {
        return isocode;
    }
    public String getAlphaCode() {
        return alphacode;
    }
}
