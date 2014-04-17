package org.jpos.tlv.packager;

import org.jpos.iso.BinaryPrefixer;
import org.jpos.iso.ISOBinaryFieldPackager;
import org.jpos.iso.LiteralBinaryInterpreter;

/**
 * @author Vishnu Pillai
 */
public class IFB_LHBINARY extends ISOBinaryFieldPackager {
	public IFB_LHBINARY() {
		super(LiteralBinaryInterpreter.INSTANCE, BinaryPrefixer.B);
	}

	/**
	 * @param len
	 *            - field len
	 * @param description
	 *            symbolic descrption
	 */
	public IFB_LHBINARY(int len, String description) {
		super(len, description, LiteralBinaryInterpreter.INSTANCE,
				BinaryPrefixer.B);
		checkLength(len, 255);
	}

	public void setLength(int len) {
		checkLength(len, 255);
		super.setLength(len);
	}
}
