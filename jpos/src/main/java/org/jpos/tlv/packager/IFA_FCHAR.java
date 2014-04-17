package org.jpos.tlv.packager;

import org.jpos.iso.AsciiInterpreter;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOStringFieldPackager;
import org.jpos.iso.NullPadder;
import org.jpos.iso.Prefixer;

/**
 *
 * Fully consuming packager
 *
 * @author Vishnu Pillai
 */
public class IFA_FCHAR extends ISOStringFieldPackager {

    public IFA_FCHAR() {
        super(NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, FullyConsumingPrefixer.INSTANCE);
    }

    public static class FullyConsumingPrefixer implements Prefixer {

        private static final FullyConsumingPrefixer INSTANCE = new FullyConsumingPrefixer();

        private FullyConsumingPrefixer() {
        }

        @Override
        public void encodeLength(int length, byte[] bytes) throws ISOException {

        }

        @Override
        public int decodeLength(byte[] bytes, int offset) throws ISOException {
            return bytes.length - offset;
        }

        @Override
        public int getPackedLength() {
            return 0;
        }
    }
}
