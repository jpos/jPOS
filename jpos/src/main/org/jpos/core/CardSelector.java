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
 * Revision 1.5  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.4  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.3  1999/11/26 12:16:47  apr
 * CVS devel snapshot
 *
 * Revision 1.2  1999/11/24 20:27:30  apr
 * Added overloaded add(card,bin) method
 *
 * Revision 1.1  1999/10/08 12:53:58  apr
 * Devel intermediate version - CVS sync
 *
 */

package org.jpos.core;

import java.util.*;


/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * helper class maps BIN (Bank Issuer Number) to CardNames<br>
 * We use a custom <strong>not-so-singleton</strong> pattern
 * here. Although <strong>getDefault()</strong> returns a unique
 * instance of CardSelector we have the chance to use multiple
 * instances of CardSelector within the same JVM if ever needed.
 */
public class CardSelector {
    List list;
    static CardSelector defaultInstance;

    public class Entry {
	CardBrand card;
	int low;
	int high;
	public Entry (CardBrand card, int low, int high) {
	    this.card = card;
	    this.low  = low;
	    this.high = high;
	}
    }
    public CardSelector() {
	list = new Vector();
    }
    public CardSelector (List list) {
	this.list=list;
    }
    /**
     * @param card CardBrand
     * @param low BIN
     * @param high BIN
     */
    public void add (CardBrand card, int low, int high) {
	list.add (new Entry (card, low, high));
    }
    /**
     * @param card CardBrand
     * @param bin
     */
    public void add (CardBrand card, int bin) {
	list.add (new Entry (card, bin, bin));
    }
    public static void setDefault (CardSelector def) {
	defaultInstance = def;
    }
    public static CardSelector getDefault () {
	if (defaultInstance == null)
	    defaultInstance = new CardSelector();
	return defaultInstance;
    }
    public CardBrand getCardBrand(String bin) 
	throws UnknownCardException
    {
	try {
	    int b = Integer.parseInt (bin);
	    Iterator i = list.iterator();
	    while (i.hasNext()) {
		Entry e = (Entry) i.next();
		if ((b >= e.low) && (b <= e.high)) 
		    return e.card;
	    }
	} catch (NumberFormatException x) { }
	throw new UnknownCardException (bin);

    }
}
