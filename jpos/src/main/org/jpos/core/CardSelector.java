/*
 * $Log$
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

package uy.com.cs.jpos.core;

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
