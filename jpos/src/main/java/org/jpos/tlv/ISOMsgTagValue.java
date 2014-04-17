package org.jpos.tlv;

import org.jpos.iso.ISOMsg;

/**
 * @author Vishnu Pillai
 *
 */
public class ISOMsgTagValue extends TagValueBase<ISOMsg> {

    public ISOMsgTagValue(String tag, ISOMsg value) {
        super(tag, value);
    }

}
