/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
 * A card validator implementation that performs no validation.
 *
 * <p>This validator is useful in scenarios where card validation should be
 * completely bypassed, such as testing environments or when using external
 * validation services.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SelectDestination participant = new SelectDestination();
 * Configuration cfg = new SimpleConfiguration();
 * cfg.put("ignore-card-validations", "true");
 * participant.setConfiguration(cfg);
 * }</pre>
 *
 * @see CardValidator
 * @see org.jpos.transaction.participant.SelectDestination
 * @since 3.0.1
 */
public class NoCardValidator implements CardValidator {

    @Override
    public void validate(Card card) throws InvalidCardException {
        return;
    }
}
