/*
 * $Log$
 * Revision 1.3  2000/06/22 12:17:42  apr
 * Added equals method
 *
 * Revision 1.2  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  1999/10/08 12:53:57  apr
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
 * represents a CardBrand and SubBrand
 */
public class CardBrand {
    String name, productName;

    /**
     * @param name Card brand's name
     */
    public CardBrand (String name) {
	this.name = name;
	this.productName = null;
    }
    /**
     * @param name Card brand's name
     * @param productName Card's product name (i.e. vanity name)
     */
    public CardBrand (String name, String productName) {
	this.name = name;
	this.productName = productName;
    }
    public String toString() {
	return name + (productName != null ? "/" + productName : "");
    }
    public String getName() {
	return name;
    }
    public String getProduct() {
	return productName != null ? productName : "";
    }
    public boolean equals (Object obj) {
	return obj != null ? toString().equals(obj.toString()) : false;
    }
}
