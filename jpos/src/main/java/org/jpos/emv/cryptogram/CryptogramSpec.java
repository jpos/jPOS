/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;

/**
 * 
 * Interface that represents the parameters and data used by the cryptogram generation algorithm
 * 
 * 
 * 
 * @author Rainer Reyes
 */
public interface CryptogramSpec {

    /**
     * Return method of the derivation of ICC Master Key used for Application Cryptogram generation
     * 
     * @return Master Key Derivation Method
     */
    MKDMethod getMKDMethod();

    /**
     * 
     * Return method of the derivation of Unique DEA Key UDK (Session Key) used for Application Cryptogram generation
     * @return Session Key Derivation Method
     */
    SKDMethod getSKDMethod();

    /**
     * Returns method of generation of the response cryptogram
     * 
     * @return ARPC Generation Method
     */
    ARPCMethod getARPCMethod();

    /**
     * 
     * Returns class in charge of selecting the data elements and building the string 
     * that will be used for the generation of the cryptogram
     * 
     * @return Cryptogram Data Builder 
     */
    CryptogramDataBuilder getDataBuilder();


}
