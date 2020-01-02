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

package org.jpos.tlv;

/**
 * Builder to create TLV/LTV tag maps stored as sequence of characters.
 * <p>
 * Using {@code withTagLengthSwap(true)} while creating the builder causes
 * switchs {@code CharTagMap} in LTV mode.
 *
 * @author Robert Demski <drdemsey@gmail.com>
 */
public class CharTagMapBuilder {

    protected Integer tagSize;
    protected Integer lengthSize;

    protected boolean swapTagWithLength;

    /**
     * Constructs a new instance of the builder.
     */
    public CharTagMapBuilder() {
        super();
    }

    /**
     * Sets size of length element.
     *
     * @param size size of length elament
     * @return this, for chaining, not {@code null}
     */
    public CharTagMapBuilder withLengthSize(int size) {
        lengthSize = size;
        return this;
    }

    /**
     * Sets size of tag element.
     *
     * @param size size of length elament
     * @return this, for chaining, not {@code null}
     */
    public CharTagMapBuilder withTagSize(int size) {
        tagSize = size;
        return this;
    }

    /**
     * Swap Tag with Length.
     *
     * @param swap indicates if tag element will be swapped with length element
     * @return this, for chaining, not {@code null}
     */
    public CharTagMapBuilder withTagLengthSwap(boolean swap) {
        swapTagWithLength = swap;
        return this;
    }

    /**
     * Completes this builder by creating the {@code CharTagMap}.
     *
     * @return the created tag map, not {@code null}
     * @throws IllegalArgumentException if tag ma cannot be created
     */
    public CharTagMap build() throws IllegalArgumentException {
        CharTagMap tm = CharTagMap.getInstance();
        if (tagSize != null)
            tm.setTagSize(tagSize);

        if (lengthSize != null)
            tm.setLengthSize(lengthSize);

        tm.withTagLengthSwap(swapTagWithLength);

        return tm;
    }

}
