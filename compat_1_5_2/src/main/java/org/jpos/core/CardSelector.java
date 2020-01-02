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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


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
@SuppressWarnings("unchecked")
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
                if (b >= e.low && b <= e.high)
                    return e.card;
            }
        } catch (NumberFormatException x) { }
        throw new UnknownCardException (bin);

    }
    public void remove (CardBrand card) {
        Iterator i = list.iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (e.card == card)
                i.remove ();
        }
    }
}
