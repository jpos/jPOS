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

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * represents a CardBrand and SubBrand
 */
public class CardBrand {
    String name, productName;
    Object handBack;

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
    public void setHandBack (Object handBack) {
        this.handBack = handBack;
    }
    public Object getHandBack () {
        return handBack;
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

