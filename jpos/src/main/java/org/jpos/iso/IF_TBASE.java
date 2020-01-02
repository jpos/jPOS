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

package org.jpos.iso;

/**
 * IF_TBASE base field packager for IF_T* class
 * deal fields terminated by special token
 * @author Zhiyu Tang
 * @version $Id$
 * @see ISOComponent
 */
public abstract class IF_TBASE extends ISOFieldPackager implements TaggedFieldPackager {

    private String token;

    public IF_TBASE() {
          super();
          this.token = null;
    }

    public IF_TBASE(int len, String description) {
      super(len, description);
    }

    /**
      * @param len - field Len
      * @param description - details
      * @param token - the ending of the field
     */

    public IF_TBASE(int len, String description, String token) {
     super(len, description);
     this.token = token;
    }

    public void setToken(String token) {
                this.token = token;
    }

    public String getToken() {
        return token;
    }
}
