package org.jpos.tlv.packager.bertlv;


import org.jpos.iso.BinaryInterpreter;
import org.jpos.iso.ISOException;
import org.jpos.iso.LiteralBinaryInterpreter;


/**
 * Packager for BER TLV values. This packager does not require sub-field packagers
 *
 * @author Vishnu Pillai
 */

public class BERTLVBinaryPackager extends DefaultICCBERTLVPackager {

    public BERTLVBinaryPackager() throws ISOException {
        super();
    }

    @Override
    protected BinaryInterpreter getTagInterpreter() {
        return LiteralBinaryInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getLengthInterpreter() {
        return LiteralBinaryInterpreter.INSTANCE;
    }

    @Override
    protected BinaryInterpreter getValueInterpreter() {
        return LiteralBinaryInterpreter.INSTANCE;
    }
}
