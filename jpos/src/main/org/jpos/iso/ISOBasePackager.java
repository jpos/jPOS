package uy.com.cs.jpos.iso;

import java.util.*;
import uy.com.cs.jpos.util.Logger;
import uy.com.cs.jpos.util.LogProducer;
import uy.com.cs.jpos.util.LogEvent;

/*
 * $Log$
 * Revision 1.21  2000/02/28 10:46:40  apr
 * BugFix: changed (String)_--> toString() on unpack (logging) [Victor Salaman]
 *
 * Revision 1.20  2000/01/30 23:31:00  apr
 * Added debuging to unpack() method
 *
 * Revision 1.19  2000/01/23 16:07:30  apr
 * BugFix: BASE24Channel was not handling Headers
 * (reported by Mike Trank <mike@netcomsa.com>)
 *
 * Revision 1.18  2000/01/11 01:24:44  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.17  1999/11/24 18:16:43  apr
 * minor doc changes
 *
 * Revision 1.16  1999/09/30 12:01:13  apr
 * Added emitBitMap() and getFirstField() to fix broken X92Packager
 * after pack()/unpack() changes (reported by dflc@cs.com.uy)
 *
 * Revision 1.15  1999/09/25 13:35:07  apr
 * pack ignore field exceptions, log event and continue with next field
 *
 * Revision 1.14  1999/09/06 17:20:06  apr
 * Added Logger SubSystem
 *
 * Revision 1.13  1999/08/06 13:55:46  apr
 * Added support for Bitmap-less ISOMsgs (usually nested messages)
 *
 */

/**
 * provides base functionality for the actual packagers
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISO87APackager
 * @see ISO87BPackager
 */
public abstract class ISOBasePackager implements ISOPackager, LogProducer {
    protected ISOFieldPackager[] fld;

    protected Logger logger = null;
    protected String realm = null;

    protected void setFieldPackager (ISOFieldPackager[] fld) {
        this.fld = fld;
    }
    /**
     * @return true if BitMap have to be emited
     */
    protected boolean emitBitMap () {
	return (fld[1] instanceof ISOBitMapPackager);
    }
    /**
     * usually 2 for normal fields, 1 for bitmap-less
     * or ANSI X9.2 
     * @return first valid field
     */
    protected int getFirstField() {
	return (fld[1] instanceof ISOBitMapPackager) ? 2 : 1;
    }
    /**
     * @param   m   the Component to pack
     * @return      Message image
     * @exception ISOException
     */
    public byte[] pack (ISOComponent m) throws ISOException {
	LogEvent evt = new LogEvent (this, "pack");
	try {
	    if (m.getComposite() != m) 
		throw new ISOException ("Can't call packager on non Composite");

	    ISOComponent c;
	    Vector v = new Vector();
	    Hashtable fields = m.getChildren();
	    int len = 0;

	    // MTI (field 0)
	    c = (ISOComponent) fields.get (new Integer (0));
	    byte[] b = fld[0].pack(c);
	    len += b.length;
	    v.addElement (b);

	    if (emitBitMap()) {
		// BITMAP (-1 in HashTable)
		c = (ISOComponent) fields.get (new Integer (-1));
		b = getBitMapfieldPackager().pack(c);
		len += b.length;
		v.addElement (b);
	    }

	    // if Field 1 is a BitMap then we are packing an
	    // ISO-8583 message so next field is fld#2.
	    // else we are packing an ANSI X9.2 message, first field is 1
	    for (int i=getFirstField(); i<=m.getMaxField(); i++) {
		if ((c = (ISOComponent) fields.get (new Integer (i))) != null) {
		    try {
			b = fld[i].pack(c);
			len += b.length;
			v.addElement (b);
		    } catch (Exception e) {
			evt.addMessage ("error packing field "+i);
			evt.addMessage (c);
			evt.addMessage (e);
		    }
		}
	    }
	    int k = 0;
	    byte[] d = new byte[len];
	    for (int i=0; i<v.size(); i++) {
		b = (byte[]) v.elementAt(i);
		for (int j=0; j<b.length; j++)
		    d[k++] = b[j];
	    }
	    if (logger != null)	 // save a few CPU cycle if no logger available
		evt.addMessage (ISOUtil.hexString (d));
	    return d;
	} catch (ISOException e) {
	    evt.addMessage (e);
	    throw e;
	} finally {
	    Logger.log(evt);
	}
    }

    /**
     * @param   m   the Container of this message
     * @param   b   ISO message image
     * @return      consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent m, byte[] b) throws ISOException {
	LogEvent evt = new LogEvent (this, "unpack");
	try {
	    if (m.getComposite() != m) 
		throw new ISOException ("Can't call packager on non Composite");
	    if (logger != null)	 // save a few CPU cycle if no logger available
		evt.addMessage (ISOUtil.hexString (b));

	    int consumed;
	    ISOField mti     = new ISOField (0);
	    ISOBitMap bitmap = new ISOBitMap (-1);
	    consumed  = fld[0].unpack(mti, b, 0);
	    m.set (mti);

	    BitSet bmap = null;
	    int maxField = fld.length;
	    if (emitBitMap()) {
		consumed += getBitMapfieldPackager().unpack(bitmap,b,consumed);
		bmap = (BitSet) bitmap.getValue();
		if (logger != null)
		    evt.addMessage ("<bitmap>"+bmap.toString()+"</bitmap>");
		m.set (bitmap);
		maxField = bmap.size();
	    }
	    for (int i=getFirstField(); i<maxField; i++) {
		if (bmap == null || bmap.get(i)) {
		    ISOComponent c = fld[i].createComponent(i);
		    consumed += fld[i].unpack (c, b, consumed);
		    if (logger != null) {
			evt.addMessage ("<unpack fld=\"" + i 
					+"\" packager=\""
					+fld[i].getClass().getName()+ "\">");
			evt.addMessage ("  <value>" 
					+c.getValue().toString()
					+ "</value>");
			evt.addMessage ("</unpack>");
		    }
		    m.set(c);
		}
	    }
	    if (b.length != consumed) {
		evt.addMessage (
		    "WARNING: unpack len=" +b.length +" consumed=" +consumed
		);
	    }
	    return consumed;
	} catch (ISOException e) {
	    evt.addMessage (e);
	    throw e;
	} finally {
	    Logger.log (evt);
	}
    }
    /**
     * @param   m   the Container (i.e. an ISOMsg)
     * @param   fldNumber the Field Number
     * @return  Field Description
     */
    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return fld[fldNumber].getDescription();
    }
    /**
     * @return 128 for ISO-8583, should return 64 for ANSI X9.2
     */
    protected int getMaxValidField() {
        return 128;
    }
    /**
     * @return suitable ISOFieldPackager for Bitmap
     */
    protected ISOFieldPackager getBitMapfieldPackager() {
        return fld[1];
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
}
