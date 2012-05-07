package org.jpos.iso;

import org.jpos.iso.packager.XMLPackager;

import java.io.*;

/**
 * implements TLV Field where T may be a alpha numeric
 *
 * @author Vishnu Pillai
 *
 */
public class TLVField
    extends ISOField
    implements Cloneable, Externalizable {

    private static final long serialVersionUID = 2777917845404057273L;
    protected String tagName;

    /**
     * No args constructor
     * <font size="-1">(required by Externalizable support on ISOMsg)</font>
     */
    public TLVField() {
        super();
        this.tagName = null;
    }

    /**
     * @param n - the FieldNumber
     * @param t - the tagName
     */
    public TLVField(int n, String t) {
        super(n);
        this.tagName = t;
    }

    /**
     * @param n - fieldNumber
     * @param v - fieldValue
     * @param t - the tagName
     */
    public TLVField(int n, String t, String v) {
        super(n, v);
        this.tagName = t;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * dump this field to PrintStream. The output is sorta
     * XML, intended to be easily parsed.
     * @param p - print stream
     * @param indent - optional indent string
     */
    public void dump (PrintStream p, String indent) {
        if (value != null && value.indexOf('<') >= 0) {
            p.print (indent +"<"+XMLPackager.ISOFIELD_TAG + " " +
                XMLPackager.TAG_ATTR + "=\"" + tagName + "\" " +
                XMLPackager.ID_ATTR +"=\"" +fieldNumber +"\"><![CDATA[");
            p.print (value);
            p.println ("]]></" + XMLPackager.ISOFIELD_TAG + ">");                        
        } else {
            p.println (indent +"<"+XMLPackager.ISOFIELD_TAG + " " +
                XMLPackager.TAG_ATTR + "=\"" + tagName + "\" " +
                XMLPackager.ID_ATTR +"=\"" +fieldNumber +"\" "+
                XMLPackager.VALUE_ATTR
                +"=\"" +ISOUtil.normalize (value) +"\"/>");
        }
    }

    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeShort (fieldNumber);
        out.writeUTF(tagName);
        out.writeUTF (value);
    }

    public void readExternal  (ObjectInput in) 
        throws IOException, ClassNotFoundException {
        fieldNumber = in.readShort ();
        tagName     = in.readUTF();
        value       = in.readUTF();
    }
}
