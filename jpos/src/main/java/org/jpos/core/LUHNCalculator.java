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

package org.jpos.core;

/** Strategy interface for LUHN check-digit calculation and verification. */
public interface LUHNCalculator {
    /**
     * Verifies the LUHN check digit of a full PAN.
     * @param pan the full card PAN including check digit
     * @return {@code true} if the check digit is valid
     * @throws InvalidCardException if the PAN is null or too short
     */
    boolean verify (String pan) throws InvalidCardException;
    /**
     * Computes the LUHN check digit for a PAN body (without check digit).
     * @param pan the PAN without the check digit
     * @return the computed check digit character
     * @throws InvalidCardException if the PAN contains non-digit characters
     */
    char calculate (String pan) throws InvalidCardException;
}
