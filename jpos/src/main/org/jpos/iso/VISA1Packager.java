package uy.com.cs.jpos.iso;

import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
public class VISA1Packager 
    extends SimpleLogProducer implements ISOPackager, VISA1ResponseFilter
{
    public static final byte[] FS = { (byte)'\034' };
    int[] sequence;
    int respField;
    String badResultCode;
    String okPattern;
    VISA1ResponseFilter filter;

    /**
     * @param sequence array of fields that go to VISA1 request
     * @param respField where to put response
     * @param badResultCode (i.e. "05")
     * @param okPattern (i.e. "AUT. ")
     */
    public VISA1Packager 
	(int[] sequence, int respField, String badResultCode, String okPattern)
    { 
        super();
	this.sequence      = sequence;
	this.respField     = respField;
	this.badResultCode = badResultCode;
	this.okPattern     = okPattern;
	setVISA1ResponseFilter (this);
    }
    public void setVISA1ResponseFilter (VISA1ResponseFilter filter) {
	this.filter = filter;
    }
    protected int handleSpecialField35 (ISOMsg m, Vector v) 
	throws ISOException
    {
	int len = 0;
	byte[] entryMode = new byte[1];
	if (m.hasField (35)) {
	    entryMode[0] = (byte) '\001';
	    byte[] value = ((String)m.getValue(35)).getBytes();
	    v.addElement (entryMode);
	    v.addElement (value);
	    v.addElement (FS);
	    len += value.length+2;
	} else if (m.hasField (2) && m.hasField (14)) {
	    entryMode[0] = (byte) '\000';
	    String simulatedTrack2 = 
		(String) m.getValue(2) + "=" + (String) m.getValue(14);
	    v.addElement (entryMode);
	    v.addElement (simulatedTrack2.getBytes());
	    v.addElement (FS);
	    len += simulatedTrack2.length()+2;
	}
	return len;
    }
    public byte[] pack (ISOComponent c) throws ISOException
    {
	LogEvent evt = new LogEvent (this, "pack");
	try {
	    if (!(c instanceof ISOMsg))
		throw new ISOException
		    ("Can't call VISA1 packager on non ISOMsg");
	
	    ISOMsg m = (ISOMsg) c;

	    int len  = 0;
	    Vector v = new Vector();
	    for (int i=0; i<sequence.length; i++) {
		int fld = sequence[i];
		if (fld == 35) 
		    len += handleSpecialField35 (m, v);
		else if (m.hasField(fld)) {
		    byte[] value;
		    if (fld == 4) {
			long l = Long.parseLong (((String)m.getValue(4)));
			value = ISOUtil.formatAmount (l,12).trim().getBytes();
		    }
		    else
			value = ((String)m.getValue(fld)).getBytes();
		    v.addElement (value);
		    len += value.length;
		    if (i < (sequence.length-1)) {
			v.addElement (FS);
			len++;
		    }
		}
	    }

	    int k = 0;
	    byte[] d = new byte[len];
	    for (int i=0; i<v.size(); i++) {
		byte[] b = (byte[]) v.elementAt(i);
		for (int j=0; j<b.length; j++)
		    d[k++] = b[j];
	    }
	    if (logger != null)	 // save a few CPU cycle if no logger available
		evt.addMessage (ISOUtil.dumpString (d));
	    return d;
	} catch (ISOException e) {
	    evt.addMessage (e);
	    throw e;
	} finally {
	    Logger.log(evt);
	}
    }

    public String guessAutNumber (String s) {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<s.length(); i++)
            if (Character.isDigit(s.charAt(i))) 
                buf.append (s.charAt(i));
	if (buf.length() == 0)
	    return null;

        while (buf.length() > 6)
            buf.deleteCharAt(0);
        while (buf.length() < 6)
            buf.insert(0, "0");
	
        return buf.toString();
    }

    public int unpack (ISOComponent m, byte[] b) throws ISOException
    {
	String response = new String (b);
	m.set (new ISOField (respField, response));
	m.set (new ISOField (39, badResultCode));
	if (response.startsWith (okPattern)) {
	    String autNumber = filter.guessAutNumber (response);
	    if (autNumber != null) {
		m.set (new ISOField (39, "00"));
		m.set (new ISOField (38, autNumber));
	    }
	}
	return b.length;
    }
    public String getFieldDescription(ISOComponent m, int fldNumber)
    {
	return "VISA 1 fld "+fldNumber;
    }
}
