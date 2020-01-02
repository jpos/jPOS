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

package org.jpos.security;

import java.io.Serializable;

/**
 * The {@code SecureVariantKey} class wraps any keys that are secured by
 * the security module with variant methods.
 *
 * @author Robert Demski
 */
public abstract class SecureVariantKey extends SecureKey implements Serializable {

    private static final long serialVersionUID = -3165785988271048707L;

    /**
     * Indicates key protection variant metchod appiled to this key by a security module.
     */
    protected Byte variant;

    /**
     * Sets key protection variant metchod appiled to this key by the security module.
     *
     * @param variant key variant method used to protect this key.
     */
    public void setVariant(byte variant) {
        this.variant = variant;
    }

    /**
     * Gets the key variant method used to protect this key.
     *
     * @return key variant method used to protect this key.
     */
    public abstract byte getVariant();

}
