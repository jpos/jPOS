package org.jpos.tlv.packager;

import org.jpos.iso.IF_CHAR;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.LeftPadder;
import org.jpos.iso.TaggedFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.tlv.ISOTaggedField;
import org.jpos.tlv.OffsetIndexedComposite;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Vishnu Pillai
 */
public class TaggedSequencePackager extends GenericPackager {

    protected ISOFieldPackager tagPackager;
    protected Map<String, TaggedFieldPackager> packagerMap = new TreeMap<String, TaggedFieldPackager>();
    protected String tag;
    protected int length;
    protected boolean numericTag = false;

    public TaggedSequencePackager() throws ISOException {
        super();
    }

    public String getToken() {
        return tag;
    }

    /**
     * Specify a placeholder token so that we can determine the tag length.
     * <p/>
     * If the tags are all numeric and tags do not repeat, a numeric token may be a used like '00'.
     * Else, use a non numeric token e.g.: XX, ##, etc.
     *
     * @param token
     */
    public void setToken(String token) {
        this.tag = token;
        try {
            if (Integer.parseInt(tag) >= 0) {
                numericTag = true;
            }
        } catch (NumberFormatException e) {

        }
    }


    @Override
    public int unpack(ISOComponent m, byte[] b) throws ISOException {
        LogEvent evt = new LogEvent(this, "unpack");
        try {
            if (m.getComposite() != m)
                throw new ISOException("Can't call packager on non Composite");
            if (b.length == 0)
                return 0; // nothing to do
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage(ISOUtil.hexString(b));
            int consumed = 0;
            int subFieldId = 0;
            while (fld.length > subFieldId) {
                if (fld[subFieldId] instanceof TaggedFieldPackager) {
                    break;
                } else if (fld[subFieldId] != null) {
                    ISOComponent subField = fld[subFieldId].createComponent(subFieldId);
                    consumed += fld[subFieldId].unpack(subField, b, consumed);
                    m.set(subField);
                }
                subFieldId++;
            }
            if (subFieldId == 0 && !((fld[subFieldId] instanceof TaggedFieldPackager))) {
                subFieldId = 1;
            }

            while (consumed < b.length) {
                ISOField tagField = new ISOField(subFieldId);
                tagPackager.unpack(tagField, b, consumed);
                String tag = tagField.getValue().toString();
                ISOFieldPackager fieldPackager = (ISOFieldPackager) packagerMap.get(tag);
                if (fieldPackager == null) {
                    fieldPackager = (ISOFieldPackager) packagerMap.get("default");
                }
                if (fieldPackager == null) {
                    throw new ISOException("No default tag packager and no field packager configured for tag: " + tag);
                }
                //Numeric field numbering is helpful in accessing them - e.g.: using m.getComponent("path")
                ISOTaggedField taggedField = (ISOTaggedField) fieldPackager.createComponent(numericTag ? Integer.parseInt(tag) : subFieldId);
                consumed += fieldPackager.unpack(taggedField, b, consumed);
                //ISOTaggedField taggedField = new ISOTaggedField(tag, subField);
                m.set(taggedField);
                subFieldId++;
            }
            if (b.length != consumed) {
                evt.addMessage(
                        "WARNING: unpack len=" + b.length + " consumed=" + consumed);
            }
            return consumed;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (Exception e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            Logger.log(evt);
        }
    }

    @Override
    public void unpack(ISOComponent m, InputStream in) throws IOException, ISOException {
        throw new UnsupportedOperationException("Cannot unpack from input stream");
    }

    /**
     * Pack the subfield into a byte array
     */

    public byte[] pack(ISOComponent m) throws ISOException {
        LogEvent evt = new LogEvent(this, "pack");
        try {
            ISOComponent c;
            List<byte[]> l = new ArrayList<byte[]>();
            Map fields = m.getChildren();
            fields.remove(new Integer(-1));
            int len = 0;
            boolean tagsStarted = false;
            Iterator iterator = fields.values().iterator();
            if (m instanceof OffsetIndexedComposite) {
                int offset = ((OffsetIndexedComposite) m).getOffset();
                for (int i = 0; i < offset && iterator.hasNext(); i++) {
                    iterator.next();
                }
            }
            while (iterator.hasNext() && len < this.length) {

                Object obj = iterator.next();
                c = (ISOComponent) obj;

                byte[] b;
                if (c.getValue() != null) {
                    if (c instanceof ISOTaggedField) {
                        tagsStarted = true;
                        String tag = ((ISOTaggedField) c).getTag();
                        if (tag == null) {
                            evt.addMessage("error packing subfield " + c.getKey());
                            evt.addMessage(c);
                            throw new ISOException("Tag should not be null");
                        } else {
                            ISOFieldPackager fieldPackager = (ISOFieldPackager) packagerMap.get(tag);
                            if (fieldPackager == null) {
                                fieldPackager = (ISOFieldPackager) packagerMap.get("default");
                            }
                            if (fieldPackager == null) {
                                throw new ISOException("No default tag packager and no field packager configured for tag: " + tag);
                            }
                            b = fieldPackager.pack(c);
                            if ((len + b.length) > this.length) {
                                break;
                            }
                            len += b.length;
                            l.add(b);
                        }
                    } else if (numericTag) {
                        int tagNumber = (Integer) c.getKey();
                        String tag = ISOUtil.padleft(String.valueOf(tagNumber), this.tag.length(), '0');
                        ISOTaggedField isoTaggedField = new ISOTaggedField(tag, c);
                        if (fld.length > tagNumber) {
                            b = fld[(Integer) c.getKey()].pack(isoTaggedField);
                        } else {
                            ISOFieldPackager fieldPackager = (ISOFieldPackager) packagerMap.get(tag);
                            if (fieldPackager == null) {
                                fieldPackager = (ISOFieldPackager) packagerMap.get("default");
                            }
                            if (fieldPackager == null) {
                                throw new ISOException("No default tag packager and no field packager configured for tag: " + tag);
                            }
                            b = fieldPackager.pack(isoTaggedField);
                            if ((len + b.length) > this.length) {
                                break;
                            }
                        }
                        len += b.length;
                        l.add(b);
                    } else if (!tagsStarted) {
                        if (fld.length > (Integer) c.getKey()) {
                            b = fld[(Integer) c.getKey()].pack(c);
                        } else {
                            throw new ISOException("Non ISOTagField without packager definition. Cannot pack as tag is non-numeric");
                        }
                        len += b.length;
                        l.add(b);
                    } else {
                        evt.addMessage("error packing sub-field " + c.getKey() + ". Sub-field should be of type ISOTaggedField when tag is non-numeric");
                        evt.addMessage(c);
                        throw new ISOException("error packing sub-field " + c.getKey() + ". Sub-field should be of type ISOTaggedField when tag is non-numeric");
                    }
                }
                if (m instanceof OffsetIndexedComposite) {
                    ((OffsetIndexedComposite) m).incOffset();
                }
            }
            int k = 0;
            byte[] d = new byte[len];
            for (byte[] b : l) {
                System.arraycopy(b, 0, d, k, b.length);
                k += b.length;
            }
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage(ISOUtil.hexString(d));
            return d;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (Exception e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            Logger.log(evt);
        }
    }

    @Override
    protected void setGenericPackagerParams(Attributes atts) {
        super.setGenericPackagerParams(atts);
        this.setToken(atts.getValue("token"));
        this.length = Integer.parseInt(atts.getValue("length"));
    }

    public void setFieldPackager(ISOFieldPackager[] subFieldPackagers) {
        super.setFieldPackager(subFieldPackagers);
        for (ISOFieldPackager subFieldPackager : subFieldPackagers) {
            if (subFieldPackager instanceof TaggedFieldPackager) {
                String token = ((TaggedFieldPackager) subFieldPackager).getToken();
                if ("##########".startsWith(token)) {
                    token = "default";
                }
                packagerMap.put(token, (TaggedFieldPackager) subFieldPackager);
            }
        }
        this.tagPackager = getTagPackager();
    }

    protected ISOFieldPackager getTagPackager() {
        IF_CHAR tagPackager = new IF_CHAR(this.tag.length(), "Tag");
        tagPackager.setPadder(LeftPadder.ZERO_PADDER);
        return tagPackager;
    }
}