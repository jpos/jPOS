/**
 * $Id$
 *
 * $Log$
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  1999/12/20 20:14:16  apr
 * Added VISA1ResponseFilter support
 *
 */
package org.jpos.iso;

public interface VISA1ResponseFilter {
    /**
     * @param VISA1 response message
     * @return authorization number or null
     */
    public String guessAutNumber (String response);
}
