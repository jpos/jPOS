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

package org.jpos.security.jceadapter;

import static org.mockito.Mockito.mock;

import java.security.Key;
import java.security.Provider;

import org.jpos.testhelpers.EqualsHashCodeTestCase;
import org.junit.jupiter.api.BeforeEach;

public class JCEHandlerMacEngineKeyTest extends EqualsHashCodeTestCase {
    private Provider provider;
    private Key macKey;

    @BeforeEach
    public void onSetup() {
        provider = mock(Provider.class);
        macKey = mock(Key.class);
    }

    @Override
    protected Object createInstance() throws Exception {
        String macAlgorithm = "AAA";
        return new JCEHandler.MacEngineKey(macAlgorithm, macKey);
    }

    @Override
    protected Object createNotEqualInstance() throws Exception {
        String macAlgorithm = "ZZZ";
        return new JCEHandler.MacEngineKey(macAlgorithm, macKey);
    }
}
