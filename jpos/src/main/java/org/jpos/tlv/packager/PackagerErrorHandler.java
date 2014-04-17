package org.jpos.tlv.packager;


import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;


/**
 * @author Vishnu Pillai
 */
public interface PackagerErrorHandler {

    public void handlePackError(ISOComponent m, ISOException e);

    public void handleUnpackError(ISOComponent isoComponent, byte[] msg, ISOException e);
}
