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

package org.jpos.emv.cryptogram;

import org.jpos.emv.IssuerApplicationData;
import org.jpos.tlv.TLVList;
import static org.jpos.emv.cryptogram.CryptogramDataBuilder.minimumSetOfDataElement;

public class CVNCPADataBuilder implements CryptogramDataBuilder {

    @Override
    public String getDefaultARPCRequest(boolean approved) {
        return approved ? "3030" : "3031";
    }

    @Override
    public String buildARQCRequest(TLVList data, IssuerApplicationData iad) {
        StringBuilder sb = new StringBuilder();
        minimumSetOfDataElement(data).stream().forEach(sb::append);
        sb.append(iad.toString());
        return sb.toString();
    }

    @Override
    public PaddingMethod getPaddingMethod() {
        return PaddingMethod.ISO9797Method2;
    }
}
