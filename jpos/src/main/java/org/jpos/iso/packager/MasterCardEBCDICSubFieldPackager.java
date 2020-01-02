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

package org.jpos.iso.packager;

import org.jpos.iso.EbcdicPrefixer;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOPackager;


/**
 * MasterCard EBCDIC SubField packager
 * @author Mark Salter
 * @author Robert Demski
 * @version $Revision: 2706 $ $Date: 2009-03-05 11:24:43 +0000 (Thu, 05 Mar 2009) $
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 *
 * This packager can be used is to handle EBCDIC subfields
 * such as field 48 for MasterCard.
 */
public class MasterCardEBCDICSubFieldPackager extends EuroSubFieldPackager
{ 
    /**
     * Default constructor
     */
    public MasterCardEBCDICSubFieldPackager() {
        super();
        tagPrefixer = EbcdicPrefixer.LL;
    }

}
