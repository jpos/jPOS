/**
 * $Id$
 *
 * $Log$
 * Revision 1.1  1999/12/20 20:14:16  apr
 * Added VISA1ResponseFilter support
 *
 */
package uy.com.cs.jpos.iso;

public interface VISA1ResponseFilter {
    /**
     * @param VISA1 response message
     * @return authorization number or null
     */
    public String guessAutNumber (String response);
}
