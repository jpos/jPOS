/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.iso;

import java.io.*;
import java.util.*;
import org.jpos.util.Loggeable;
import org.jpos.util.LogSource;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.iso.packager.ISO93BPackager;

/*
 * $Log$
 * Revision 1.29  2000/11/21 14:55:13  apr
 * preliminary Externalizable support
 *
 * Revision 1.28  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.27  2000/09/09 10:58:39  apr
 * unset now silently ignores if field does not exist
 *
 * Revision 1.26  2000/05/03 16:18:56  apr
 * commented out bitmaps in logs
 *
 * Revision 1.25  2000/04/16 23:53:08  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.24  2000/04/16 22:12:01  apr
 * Moved packager implementations to org.jpos.iso.packager
 *
 * Revision 1.23  2000/03/29 12:58:03  apr
 * change Victor's tabs to 8 spaces - no other change
 *
 * Revision 1.22  2000/03/29 08:28:39  victor
 * Added support for tertiary bitmap
 *
 * Revision 1.21  2000/03/20 21:56:39  apr
 * DocBugFix: broken links to API_users_guide
 *
 * Revision 1.20  2000/03/09 02:34:32  apr
 * New methods isRequest, isResponse, isRetransmission, setMTI, getMTI and
 * setResponseMTI
 *
 * Revision 1.19  2000/03/05 02:16:37  apr
 * Added XMLPackager
 *
 * Revision 1.18  2000/03/05 01:23:42  apr
 * Changed XMLdump to lowercase
 * Fixed bug in merge (we were not merging last field in an ISOMsg)
 *
 * Revision 1.17  2000/03/04 00:37:53  apr
 * Show fieldNumber on inner message dumps
 *
 * Revision 1.16  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.15  2000/01/11 01:24:47  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.14  1999/10/01 10:54:08  apr
 * Added merge method
 *
 */

/**
 * implements <b>Composite</b>
 * whithin a <b>Composite pattern</b>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOField
 */
public class ISOMsg extends ISOComponent 
    implements Cloneable, Loggeable, Externalizable
{
    protected Hashtable fields;
    protected int maxField;
    protected ISOPackager packager;
    protected boolean dirty, maxFieldDirty;
    protected int direction;
    protected byte[] header;
    protected int fieldNumber = -1;
    public static final int INCOMING = 1;
    public static final int OUTGOING = 2;
    protected static ISOPackager internalPackager = null;

    public ISOMsg () {
        fields = new Hashtable ();
        maxField = -1;
        dirty = true;
        maxFieldDirty=true;
        direction = 0;
        header = null;
    }
    public ISOMsg (int fieldNumber) {
        this();
        this.fieldNumber = fieldNumber;
    }
    /**
     * Sets the direction information related to this message
     * @param direction can be either ISOMsg.INCOMING or ISOMsg.OUTGOING
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }
    /**
     * Sets an optional message header image
     * @param b header image
     */
     public void setHeader(byte[] b) {
        header = b;
     }
    /**
     * get optional message header image
     * @return message header image (may be null)
     */
     public byte[] getHeader() {
        return header;
     }
    /**
     * @return the direction (ISOMsg.INCOMING or ISOMsg.OUTGOING)
     * @see ISOChannel
     */
    public int getDirection() {
        return direction;
    }
    /**
     * @return true if this message is an incoming message
     * @see ISOChannel
     */
    public boolean isIncoming() {
        return direction == INCOMING;
    }
    /**
     * @return true if this message is an outgoing message
     * @see ISOChannel
     */
    public boolean isOutgoing() {
        return direction == OUTGOING;
    }
    /**
     * @return the max field number associated with this message
     */
    public int getMaxField() {
        if (maxFieldDirty)
            recalcMaxField();
        return maxField;
    }
    private void recalcMaxField() {
        ISOComponent c;
        maxField = 0;
        for (int i=1; i<=192; i++)
            if ((c = (ISOComponent) fields.get (new Integer (i))) != null)
                maxField = i;
        maxFieldDirty = false;
    }
    /**
     * @param p - a peer packager
     */
    public void setPackager (ISOPackager p) {
        packager = p;
    }
    /**
     * @return the peer packager
     */
    public ISOPackager getPackager () {
        return packager;
    }
    /**
     * Set a field within this message
     * @param c - a component
     * @exception ISOException
     */
    public void set (ISOComponent c) throws ISOException {
        Integer i = (Integer) c.getKey();
        fields.put (i, c);
        if (i.intValue() > maxField)
            maxField = i.intValue();
        dirty = true;
    }
    /**
     * Unset a field if it exists, otherwise ignore.
     * @param fldno - the field number
     */
    public void unset (int fldno) {
        if (fields.remove (new Integer (fldno)) != null)
	    dirty = maxFieldDirty = true;
    }
    /**
     * In order to interchange <b>Composites</b> and <b>Leafs</b> we use
     * getComposite(). A <b>Composite component</b> returns itself and
     * a Leaf returns null.
     *
     * @return ISOComponent
     */
    public ISOComponent getComposite() {
        return this;
    }
    /**
     * setup BitMap
     * @exception ISOException
     */
    public void recalcBitMap () throws ISOException {
        ISOComponent c;
        if (!dirty)
            return;

	if(maxField>128)
	{
	    BitSet bmap=new BitSet(64);
	    for (int i=1; i<=64; i++)
		if((c=(ISOComponent) fields.get(new Integer (i+128))) != null) 
		    bmap.set (i);
	    set (new ISOBitMap (65, bmap));
	}
		
        BitSet bmap = new BitSet (getMaxField() > 64 ? 128 : 64);
		int tmpMaxField=maxField > 128 ? 128 : maxField;

        for (int i=1; i<=tmpMaxField; i++)
            if ((c = (ISOComponent) fields.get (new Integer (i))) != null) 
                bmap.set (i);
        set (new ISOBitMap (-1, bmap));
		
        dirty = false;
    }
    /**
     * clone fields
     */
    public Hashtable getChildren() {
        return (Hashtable) fields.clone();
    }
    /**
     * pack the message with the current packager
     * @return the packed message
     * @exception ISOException
     */
    public byte[] pack() throws ISOException {
        synchronized (this) {
            recalcBitMap();
            return packager.pack(this);
        }
    }
    /**
     * unpack a message
     * @param b - raw message
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack(byte[] b) throws ISOException {
        synchronized (this) {
            return packager.unpack(this, b);
        }
    }
    /**
     * dump the message to a PrintStream. The output is sorta
     * XML, intended to be easily parsed.
     * <br>
     * Each component is responsible for its own dump function,
     * ISOMsg just calls dump on every valid field.
     * @param p - print stream
     * @param indent - optional indent string
     */
    public void dump (PrintStream p, String indent) {
        ISOComponent c;
	p.print (indent + "<" + XMLPackager.ISOMSG_TAG);
	switch (direction) {
	    case INCOMING:
		p.print (" direction=\"incoming\"");
		break;
	    case OUTGOING:
		p.print (" direction=\"outgoing\"");
		break;
	}
	if (fieldNumber != -1)
	    p.print (" "+XMLPackager.ID_ATTR +"=\""+fieldNumber +"\"");
	p.println (">");
	String newIndent = indent + "  ";
        for (int i=0; i<=maxField; i++) {
            if ((c = (ISOComponent) fields.get (new Integer (i))) != null)
                c.dump (p, newIndent);
	    //
	    // Uncomment to include bitmaps within logs
	    // 
	    // if (i == 0) {
	    //  if ((c = (ISOComponent) fields.get (new Integer (-1))) != null)
	    //    c.dump (p, newIndent);
	    // }
	    //
	}

        p.println (indent + "</" + XMLPackager.ISOMSG_TAG+">");
    }
    /**
     * get the component associated with the given field number
     * @param fldno the Field Number
     * @return the Component
     */
    public ISOComponent getComponent(int fldno) {
        return (ISOComponent) fields.get(new Integer(fldno));
    }
    /**
     * Return the object value associated with the given field number
     * @param fldno the Field Number
     * @return the field Object
     */
    public Object getValue(int fldno) throws ISOException {
        return getComponent(fldno).getValue();
    }
    /**
     * Check if a given field is valid
     * @param fldno the Field Number
     * @return boolean indicating the existence of the field
     */
    public boolean hasField(int fldno) {
        return fields.get(new Integer(fldno)) != null;
    }
    /**
     * Don't call setValue on an ISOMsg. You'll sure get
     * an ISOException. It's intended to be used on Leafs
     * @see ISOField
     * @see ISOException
     */
    public void setValue(Object obj) throws ISOException {
        throw new ISOException ("setValue N/A in ISOMsg");
    }
    
    public Object clone() {
        try {
            ISOMsg m = (ISOMsg) super.clone();
            m.fields = (Hashtable) fields.clone();
            return (Object) m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Partially clone an ISOMsg
     * @param fields int array of fields to go
     * @return new ISOMsg instance
     */
    public Object clone(int[] fields) {
        try {
            ISOMsg m = (ISOMsg) super.clone();
            m.fields = new Hashtable();
            for (int i=0; i<fields.length; i++) {
                if (hasField(fields[i])) {
                    try {
                        m.set (getComponent(fields[i]));
                    } catch (ISOException e) { 
                        // it should never happen
                    }
                }
            }
            return (Object) m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * add all fields present on received parameter to this ISOMsg<br>
     * please note that received fields take precedence over 
     * existing ones (simplifying card agent message creation 
     * and template handling)
     * @param m ISOMsg to merge
     */
    public void merge (ISOMsg m) {
	for (int i=0; i<=m.getMaxField(); i++) 
	    try {
		if (m.hasField(i))
		    set (m.getComponent(i));
	    } catch (ISOException e) {
		// should never happen 
	    }
    }

    /**
     * @return a string suitable for a log
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        if (isIncoming())
            s.append("<-- ");
        else if (isOutgoing())
            s.append("--> ");
        else
            s.append("    ");

        try {
            s.append((String) getValue(0));
            if (hasField(11)) {
                s.append(' ');
                s.append((String) getValue(11));
            }
            if (hasField(41)) {
                s.append(' ');
                s.append((String) getValue(41));
            }
        } catch (ISOException e) { }
        return s.toString();
    }
    public Object getKey() throws ISOException {
        if (fieldNumber != -1)
            return new Integer(fieldNumber);
        throw new ISOException ("This is not a subField");
    }
    public Object getValue() {
        return this;
    }
    /**
     * @return true on inner messages
     */
    public boolean isInner() {
	return fieldNumber > -1;
    }
    /**
     * @param mti new MTI
     * @exception ISOException if message is inner message
     */
    public void setMTI (String mti) throws ISOException {
	if (isInner())
	    throw new ISOException ("can't setMTI on inner message");
	set (new ISOField (0, mti));
    }
    /**
     * @return current MTI
     * @exception ISOException on inner message or MTI not set
     */
    public String getMTI() throws ISOException {
	if (isInner())
	    throw new ISOException ("can't getMTI on inner message");
	else if (!hasField(0))
	    throw new ISOException ("MTI not available");
	return (String) getValue(0);
    }
    /**
     * @return true if message "seems to be" a request
     * @exception ISOException on MTI not set
     */
    public boolean isRequest() throws ISOException {
	return Character.getNumericValue(getMTI().charAt (2))%2 == 0;
    }
    /**
     * @return true if message "seems not to be" a request
     * @exception ISOException on MTI not set
     */
    public boolean isResponse() throws ISOException {
	return !isRequest();
    }
    /**
     * @return true if message is Retransmission
     * @exception ISOException on MTI not set
     */
    public boolean isRetransmission() throws ISOException {
	return getMTI().charAt(3) == '1';
    }
    /**
     * sets an appropiate response MTI<br>
     * i.e. 0110 becomes 0120<br>
     * i.e. 0111 becomes 0120<br>
     * i.e. 1201 becomes 1210<br>
     * @exception ISOException on MTI not set or it is not a request
     */
    public void setResponseMTI() throws ISOException {
	if (!isRequest())
	    throw new ISOException ("not a request - can't set response MTI");

	String mti = getMTI();
	set (new ISOField (0,
	    mti.substring(0,2)
		+(Character.getNumericValue(getMTI().charAt (2))+1) + "0"
	    )
	);
    }

    public void writeExternal (ObjectOutput out) throws IOException {
        try {
            ISOPackager p = new ISO93BPackager();
            byte[] b;
            synchronized (this) {
                recalcBitMap();
                b = getInternalPackager().pack(this);
            }
            out.writeObject (b);
        } catch (ISOException e) {
            throw new IOException (e.getMessage());
        }
    }
    public void readExternal  (ObjectInput in) 
        throws IOException, ClassNotFoundException
    {
        byte[] b = (byte[]) in.readObject();
        try {
            synchronized (this) {
                getInternalPackager().unpack(this, b);
            }
        } catch (ISOException e) {
            throw new IOException (e.getMessage());
        }
    }
    protected ISOPackager getInternalPackager() {
        synchronized (ISOMsg.class) {
            if (internalPackager == null)
                internalPackager = new ISO93BPackager();
        }
        return internalPackager;
    }
}

