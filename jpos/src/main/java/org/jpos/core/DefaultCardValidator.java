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

/** Default {@link CardValidator} that checks PAN consistency across tracks and validates the LUHN check digit. */
public class DefaultCardValidator implements CardValidator {
    private static LUHNCalculator DEFAULT_LUHN_CALCULATOR = new DefaultLUHNCalculator();
    private LUHNCalculator luhnCalculator = DEFAULT_LUHN_CALCULATOR;

    /** Default constructor. */
    public DefaultCardValidator() {
        super();
    }

    /**
     * Validates the given card, checking PAN/track consistency and LUHN checksum.
     * @param card the card to validate
     * @throws InvalidCardException if the card data is invalid
     */
    public void validate (Card card) throws InvalidCardException {
        if (card != null) {
            String pan = card.getPan();
            if (pan != null) {
                if (card.getTrack1() != null && !pan.equals(card.getTrack1().getPan()))
                    throw new InvalidCardException ("track1 PAN mismatch");
                if (card.getTrack2() != null && !pan.equals(card.getTrack2().getPan()))
                    throw new InvalidCardException ("track2 PAN mismatch");
            }
            String exp = card.getExp();
            if (exp != null) {
                if (card.getTrack1() != null && !exp.equals(card.getTrack1().getExp()))
                    throw new InvalidCardException ("track1 EXP mismatch");
                if (card.getTrack2() != null && !exp.equals(card.getTrack2().getExp()))
                    throw new InvalidCardException ("track2 EXP mismatch");
            }
            if (card.getServiceCode() != null) {
                int mismatch = 0;
                if (card.hasBothTracks()) {
                    if (card.getTrack2().getServiceCode() != null) {
                        if (!card.getTrack2().getServiceCode().equals(card.getServiceCode()))
                            mismatch++;
                        if (!card.getTrack2().getServiceCode().equals(card.getTrack1().getServiceCode()))
                            mismatch++;
                    }
                } else if (card.hasTrack2()) {
                    if (card.getTrack2().getServiceCode() != null) {
                        if (!card.getTrack2().getServiceCode().equals(card.getServiceCode()))
                            mismatch++;
                    }
                } else if (card.hasTrack1()) {
                    if (card.getTrack1().getServiceCode() != null) {
                        if (!card.getTrack1().getServiceCode().equals(card.getServiceCode()))
                            mismatch++;
                    }
                }
                if (mismatch > 0) {
                    throw new InvalidCardException(String.format("service code mismatch (%d)", mismatch));
                }
            }
            if (luhnCalculator != null && !luhnCalculator.verify(pan))
                throw new InvalidCardException ("invalid LUHN");
        }
    }

    /**
     * Sets the LUHN calculator to use for checksum validation.
     * @param luhnCalculator the LUHN calculator implementation
     */
    public void setLuhnCalculator(LUHNCalculator luhnCalculator) {
        this.luhnCalculator = luhnCalculator;
    }
}
