/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.packager;

import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Packager for fields containing TLV sub-fields without a bitmap
 *
 * The Tag is alphanumeric so a mapping between fieldNumber and tag are required. A TagMapper
 * implementation should provide this mapping
 *
 */

public class GenericTaggedFieldsPackager extends GenericPackager {

    private TagMapper tagMapper = null;
    private Integer fieldId = 0;

    public GenericTaggedFieldsPackager() throws ISOException {
        super();
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
            int maxField = fld.length;
            for (int i = getFirstField(); i < maxField && consumed < b.length; i++) {
                if (fld[i] != null) {
                    ISOComponent c = fld[i].createComponent(i);
                    int unpacked = fld[i].unpack(c, b, consumed);
                    consumed = consumed + unpacked;
                    if (unpacked > 0) {
                        m.set(c);
                    }
                }
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
        LogEvent evt = new LogEvent(this, "unpack");
        try {
            if (m.getComposite() != m)
                throw new ISOException("Can't call packager on non Composite");

            // if ISOMsg and headerLength defined
            if (m instanceof ISOMsg && ((ISOMsg) m).getHeader() == null && headerLength > 0) {
                byte[] h = new byte[headerLength];
                in.read(h, 0, headerLength);
                ((ISOMsg) m).setHeader(h);
            }

            if (!(fld[0] instanceof ISOMsgFieldPackager) &&
                    !(fld[0] instanceof ISOBitMapPackager)) {
                ISOComponent mti = fld[0].createComponent(0);
                fld[0].unpack(mti, in);
                m.set(mti);
            }

            int maxField = fld.length;
            for (int i = getFirstField(); i < maxField; i++) {
                if (fld[i] == null)
                    continue;

                ISOComponent c = fld[i].createComponent(i);
                fld[i].unpack(c, in);
                if (logger != null) {
                    evt.addMessage("<unpack fld=\"" + i
                            + "\" packager=\""
                            + fld[i].getClass().getName() + "\">");
                    if (c.getValue() instanceof ISOMsg)
                        evt.addMessage(c.getValue());
                    else
                        evt.addMessage("  <value>"
                                + c.getValue().toString()
                                + "</value>");
                    evt.addMessage("</unpack>");
                }
                m.set(c);

            }
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (EOFException e) {
            throw e;
        } catch (Exception e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            Logger.log(evt);
        }
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
            int len = 0;

            for (int i = getFirstField(); i <= m.getMaxField(); i++) {
                c = (ISOComponent) fields.get(i);
                if (c != null) {
                    try {
                        byte[] b = fld[i].pack(c);
                        len += b.length;
                        l.add(b);
                    } catch (Exception e) {
                        evt.addMessage("error packing subfield " + i);
                        evt.addMessage(c);
                        evt.addMessage(e);
                        throw e;
                    }
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

    public void setFieldPackager(ISOFieldPackager[] fld) {
        super.setFieldPackager(fld);
        for (int i = 0; i < fld.length; i++) {
            if (fld[i] instanceof TaggedFieldPackagerBase) {
                ((TaggedFieldPackagerBase) fld[i]).setParentFieldNumber(fieldId);
                ((TaggedFieldPackagerBase) fld[i]).setTagMapper(tagMapper);
                ((TaggedFieldPackagerBase) fld[i]).setPackingLenient(isPackingLenient());
                ((TaggedFieldPackagerBase) fld[i]).setUnpackingLenient(isUnpackingLenient());
            }
        }
    }

    @Override
    protected void setGenericPackagerParams(Attributes atts) {
        super.setGenericPackagerParams(atts);
        try {
            Class<? extends TagMapper> clazz = Class.forName(atts.getValue("tagMapper")).asSubclass(TagMapper.class);
            tagMapper = clazz.newInstance();
            fieldId = Integer.parseInt(atts.getValue("id"));
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Subclasses may override this method if a lenient packing is required when a
     * field-to-tag mapping cannot be found.
     *
     * @return A boolean value for or against lenient packing
     */
    protected boolean isPackingLenient() {
        return false;
    }

    /**
     * Subclasses may override this method if a lenient unpacking is required when a
     * tag-to-field mapping cannot be found.
     *
     * @return A boolean value for or against lenient unpacking
     */
    protected boolean isUnpackingLenient() {
        return false;
    }

}


