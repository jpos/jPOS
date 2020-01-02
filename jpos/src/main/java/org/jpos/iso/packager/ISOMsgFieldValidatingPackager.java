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


import org.jpos.iso.*;

/**
 * THIS CLASS SHOWS THAT IsoMsgFieldValidator IS NOT NECESSARY. IF WE
 * MODIFY CURRENT PACKAGER ISOMsgFieldPackager. IF IT IMPLEMENTS ISOValidator
 * INTERFACE, AND ADD validate(ISOComponent c) METHOD LIKE HERE. IN
 * TesterValidatingPackager FILE WE COULD CHANGE FIELD 48 VALIDATOR TO
 * THAT NEW ISOMsgFieldValidator. SEE COMMENTED LINE.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class ISOMsgFieldValidatingPackager extends ISOMsgFieldPackager implements ISOValidator {

    public ISOMsgFieldValidatingPackager (
            ISOFieldPackager fieldPackager,
            ISOPackager msgPackager ){
        super( fieldPackager, msgPackager );
    }

    public ISOComponent validate(ISOComponent m) throws ISOException {
        return ((ISOBaseValidatingPackager)msgPackager).validate( m );
    }
}