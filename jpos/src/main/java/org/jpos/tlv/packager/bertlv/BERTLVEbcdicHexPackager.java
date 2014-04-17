package org.jpos.tlv.packager.bertlv;


import org.jpos.iso.BinaryInterpreter;
import org.jpos.iso.EbcdicHexInterpreter;
import org.jpos.iso.ISOException;


/**
 * Packager for BER TLV values. This packager does not require sub-field packagers
 *
 * @author Vishnu Pillai
 */

public class BERTLVEbcdicHexPackager extends DefaultICCBERTLVPackager {

    public BERTLVEbcdicHexPackager() throws ISOException {
        super();
    }

    @Override
    protected BinaryInterpreter getTagInterpreter() {
        return EbcdicHexInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getLengthInterpreter() {
        return EbcdicHexInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getValueInterpreter() {
        return EbcdicHexInterpreter.INSTANCE;
    }
}
