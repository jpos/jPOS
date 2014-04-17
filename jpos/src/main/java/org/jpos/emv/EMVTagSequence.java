package org.jpos.emv;

import org.jpos.iso.ISOException;
import org.jpos.tlv.TagSequenceBase;

/**
 * @author Vishnu Pillai
 */
public class EMVTagSequence extends TagSequenceBase {

    public EMVTagSequence() {
        super();
    }

    protected EMVTagSequence(String tag) {
        super(tag);
    }

    @Override
    protected EMVTagSequence createTagValueSequence(String tag) {
        return new EMVTagSequence(tag);
    }

    @Override
    protected EMVTag createLiteralTagValuePair(String tag, String value) throws ISOException {
        try {
            return new LiteralEMVTag(EMVStandardTagType.forHexCode(tag), value);
        } catch (NoTagNumberForProprietaryTagException e) {
            throw new ISOException(e);
        } catch (UnknownTagNumberException e) {
            throw new ISOException(e);
        }
    }

    @Override
    protected EMVTag createBinaryTagValuePair(String tag, byte[] value) throws ISOException {
        try {
            return new BinaryEMVTag(EMVStandardTagType.forHexCode(tag), value);
        } catch (NoTagNumberForProprietaryTagException e) {
            throw new ISOException(e);
        } catch (UnknownTagNumberException e) {
            throw new ISOException(e);
        }
    }
}
