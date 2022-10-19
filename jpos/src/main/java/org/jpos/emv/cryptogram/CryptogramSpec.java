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
