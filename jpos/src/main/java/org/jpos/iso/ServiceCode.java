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

import java.util.Objects;

/**
 * Implements a parser for card's service code as per ISO/IEC 7813:2006(E).
 * 
 * @version $Revision$ $Date$
 */
public class ServiceCode {

    final private char[] value;
   
    /**
     * Creates a ServiceCode instance.
     * 
     * @param value Three-digit service code value.
     */
    public ServiceCode(String value) {

        Objects.requireNonNull(value);

        if (!value.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("Invalid service code.");
        }

        this.value = value.toCharArray();
    }

    public boolean isPrivate() {
        return value[0] == '7';
    }

    public boolean isTest() {
        return value[0] == '9';
    }

    public boolean isICC() {
        return value[0] == '2' || value[0] == '6';
    }

    public boolean isInternational() {
        return value[0] == '1' || value[0] == '2';
    }

    public boolean isNational() {
        return value[0] == '5' || value[0] == '6';
    }

    public boolean hasNoRestrictions() {
        return value[2] == '0' || value[2] == '1' || value[2] == '6';
    }

    public boolean isGoodsAndServicesOnly() {
        return value[2] == '2' || value[2] == '5' || value[2] == '7';
    }

    public boolean isATMOnly() {
        return value[2] == '3';
    }

    public boolean isCashOnly() {
        return value[2] == '4';
    }
    
    public boolean isPINRequired() {
        return value[2] == '0' || value[2] == '3' || value[2] == '5';
    }
    
    public boolean mustPromptForPINIfPEDPresent() {
        return value[2] == '6' || value[2] == '7';
    }
    
    public boolean isNormalAuthorization() {
        return value[1] == '0';
    }

    public boolean isIssuerAuthorization() {
        return value[1] == '2';
    }
}