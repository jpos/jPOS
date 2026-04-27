/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

    /**
     * Indicates whether the first digit signals private interchange (digit 7).
     *
     * @return {@code true} if the first digit is {@code '7'}
     */
    public boolean isPrivate() {
        return value[0] == '7';
    }

    /**
     * Indicates whether the first digit signals test (digit 9).
     *
     * @return {@code true} if the first digit is {@code '9'}
     */
    public boolean isTest() {
        return value[0] == '9';
    }

    /**
     * Indicates whether the first digit signals an ICC-capable card (digits 2, 6).
     *
     * @return {@code true} if the first digit is {@code '2'} or {@code '6'}
     */
    public boolean isICC() {
        return value[0] == '2' || value[0] == '6';
    }

    /**
     * Indicates whether the first digit signals international interchange (digits 1, 2).
     *
     * @return {@code true} if the first digit is {@code '1'} or {@code '2'}
     */
    public boolean isInternational() {
        return value[0] == '1' || value[0] == '2';
    }

    /**
     * Indicates whether the first digit signals national interchange (digits 5, 6).
     *
     * @return {@code true} if the first digit is {@code '5'} or {@code '6'}
     */
    public boolean isNational() {
        return value[0] == '5' || value[0] == '6';
    }

    /**
     * Indicates whether the third digit allows transactions with no restrictions (digits 0, 1, 6).
     *
     * @return {@code true} when no service restrictions apply
     */
    public boolean hasNoRestrictions() {
        return value[2] == '0' || value[2] == '1' || value[2] == '6';
    }

    /**
     * Indicates whether the third digit restricts use to goods and services (digits 2, 5, 7).
     *
     * @return {@code true} for goods-and-services-only service codes
     */
    public boolean isGoodsAndServicesOnly() {
        return value[2] == '2' || value[2] == '5' || value[2] == '7';
    }

    /**
     * Indicates whether the third digit restricts use to ATMs (digit 3).
     *
     * @return {@code true} for ATM-only service codes
     */
    public boolean isATMOnly() {
        return value[2] == '3';
    }

    /**
     * Indicates whether the third digit restricts use to cash transactions (digit 4).
     *
     * @return {@code true} for cash-only service codes
     */
    public boolean isCashOnly() {
        return value[2] == '4';
    }

    /**
     * Indicates whether a PIN is required for transactions with this service code.
     *
     * @return {@code true} when the third digit is {@code '0'}, {@code '3'}, or {@code '5'}
     */
    public boolean isPINRequired() {
        return value[2] == '0' || value[2] == '3' || value[2] == '5';
    }

    /**
     * Indicates whether the cardholder must be prompted for a PIN when a PED is available.
     *
     * @return {@code true} when the third digit is {@code '6'} or {@code '7'}
     */
    public boolean mustPromptForPINIfPEDPresent() {
        return value[2] == '6' || value[2] == '7';
    }

    /**
     * Indicates whether the second digit signals normal authorization processing (digit 0).
     *
     * @return {@code true} for normal authorization
     */
    public boolean isNormalAuthorization() {
        return value[1] == '0';
    }

    /**
     * Indicates whether the second digit signals issuer authorization is required (digit 2).
     *
     * @return {@code true} when issuer authorization is required
     */
    public boolean isIssuerAuthorization() {
        return value[1] == '2';
    }
}