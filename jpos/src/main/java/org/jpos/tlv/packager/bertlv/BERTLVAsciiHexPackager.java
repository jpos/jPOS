package org.jpos.tlv.packager.bertlv;


import org.jpos.iso.AsciiHexInterpreter;
import org.jpos.iso.BinaryInterpreter;
import org.jpos.iso.ISOException;


/**
 * Packager for BER TLV values. This packager does not require sub-field packagers
 *
 * @author Vishnu Pillai
 */

public class BERTLVAsciiHexPackager extends DefaultICCBERTLVPackager {

    public BERTLVAsciiHexPackager() throws ISOException {
        super();
    }

    @Override
    protected BinaryInterpreter getTagInterpreter() {
        return AsciiHexInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getLengthInterpreter() {
        return AsciiHexInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getValueInterpreter() {
        return AsciiHexInterpreter.INSTANCE;
    }

}
