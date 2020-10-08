package org.jpos.tlv.packager.bertlv;

import org.jpos.iso.IFB_BINARY;
import org.jpos.iso.IFB_BITMAP;
import org.jpos.iso.IFB_NUMERIC;
import org.jpos.iso.IFE_CHAR;
import org.jpos.iso.IFE_LLLBINARY;
import org.jpos.iso.IFE_LLLCHAR;
import org.jpos.iso.IF_NOP;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsgFieldPackager;

public class Bug349BinaryPackager extends ISOBasePackager {
    private ISOFieldPackager[] fld = {
            /*000*/ new IFB_NUMERIC(4, "Message Type Indicator", true),
            /*001*/ new IFB_BITMAP(8, "Bitmap"),
            /*002*/ new IF_NOP(),
            /*003*/ new IF_NOP(),
            /*004*/ new IF_NOP(),
            /*005*/ new IF_NOP(),
            /*006*/ new IF_NOP(),
            /*007*/ new IF_NOP(),
            /*008*/ new IF_NOP(),
            /*009*/ new IF_NOP(),
            /*010*/ new IF_NOP(),
            /*011*/ new IFB_NUMERIC(6, "Systems trace audit number", true),
            /*012*/ new IFB_NUMERIC(6, "Date and time, Local transaction", true),
            /*013*/ new IFB_NUMERIC(4, "Date, Effective", true),
            /*014*/ new IF_NOP(),
            /*015*/ new IF_NOP(),
            /*016*/ new IF_NOP(),
            /*017*/ new IF_NOP(),
            /*018*/ new IF_NOP(),
            /*019*/ new IF_NOP(),
            /*020*/ new IF_NOP(),
            /*021*/ new IF_NOP(),
            /*022*/ new IF_NOP(),
            /*023*/ new IF_NOP(),
            /*024*/ new IF_NOP(),
            /*025*/ new IF_NOP(),
            /*026*/ new IF_NOP(),
            /*027*/ new IF_NOP(),
            /*028*/ new IF_NOP(),
            /*029*/ new IF_NOP(),
            /*030*/ new IF_NOP(),
            /*031*/ new IF_NOP(),
            /*032*/ new IF_NOP(),
            /*033*/ new IF_NOP(),
            /*034*/ new IF_NOP(),
            /*035*/ new IF_NOP(),
            /*036*/ new IF_NOP(),
            /*037*/ new IF_NOP(),
            /*038*/ new IF_NOP(),
            /*039*/ new IFE_CHAR(2, "Response Code"),
            /*040*/ new IF_NOP(),
            /*041*/ new IFE_CHAR(8, "Card acceptor terminal identification"),
            /*042*/ new IF_NOP(),
            /*043*/ new IF_NOP(),
            /*044*/ new IF_NOP(),
            /*045*/ new IF_NOP(),
            /*046*/ new IFE_LLLCHAR(999, "CCT ID"),
            /*047*/ new IF_NOP(),
            /*048*/ new IF_NOP(),
            /*049*/ new IF_NOP(),
            /*050*/ new IF_NOP(),
            /*051*/ new IF_NOP(),
            /*052*/ new IF_NOP(),
            /*053*/ new IFB_NUMERIC(16, "Security related control information", true),
            /*054*/ new IF_NOP(),
            /*055*/ new ISOMsgFieldPackager(new IFE_LLLBINARY(999, "IC card system related data"), new Bug349BERTLVBinaryHexPackager()),
            /*056*/ new IF_NOP(),
            /*057*/ new IF_NOP(),
            /*058*/ new IF_NOP(),
            /*059*/ new IF_NOP(),
            /*060*/ new IF_NOP(),
            /*061*/ new IF_NOP(),
            /*062*/ new IF_NOP(),
            /*063*/ new IF_NOP(),
            /*064*/ new IFB_BINARY(8, "Message authentication code field"),
    };

    private static class Bug349BERTLVBinaryHexPackager extends BERTLVBinaryPackager {

        public Bug349BERTLVBinaryHexPackager() throws ISOException {
            super();

            /* Generic packager does the similar activity (creating an empty fld field) for a BERTLV
             * packager. Looks like a hack
             */
            ISOFieldPackager[] fld = new ISOFieldPackager[1];
            setFieldPackager(fld);
        }
    }

    public Bug349BinaryPackager() throws ISOException {
        super();
        setFieldPackager(fld);
        BERTLVBinaryPackager.setTagFormatMapper(Bug349TLVFormatMapper.INSTANCE);
    }
}
