/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

public class DefaultCardValidator implements CardValidator {
    public static LUHNCalculator DEFAULT_LUHN_CALCULATOR = new DefaultLUHNCalculator();

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
            if (!DEFAULT_LUHN_CALCULATOR.verify(pan))
                throw new InvalidCardException ("Invalid LUHN");
        }
    }
}
