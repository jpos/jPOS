package org.jpos.tlv;


import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;


/**
 * @author Vishnu Pillai
 *
 */
public class ISOTaggedField extends ISOComponent {

    private final ISOComponent delegate;
    private String tag;

    public ISOTaggedField(String tag, ISOComponent delegate) {
        if (tag == null) {
            throw new IllegalArgumentException("tag cannot be null");
        }
        if (delegate == null) {
            throw new IllegalArgumentException("delegate cannot be null");
        }
        this.tag = tag;
        this.delegate = delegate;
    }


    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public void setFieldNumber(final int fieldNumber) {
        delegate.setFieldNumber(fieldNumber);
    }


    @Override
    public void setValue(final Object obj) throws ISOException {
        delegate.setValue(obj);
    }


    @Override
    public void set(final ISOComponent c) throws ISOException {
        delegate.set(c);
    }


    @Override
    public void unset(final int fldno) throws ISOException {
        delegate.unset(fldno);
    }


    @Override
    public ISOComponent getComposite() {
        if (delegate.getComposite() == delegate) {
            return this;
        } else {
            return null;
        }
    }


    @Override
    public Object getKey() throws ISOException {
        return delegate.getKey();
    }


    @Override
    public Object getValue() throws ISOException {
        return delegate.getValue();
    }


    @Override
    public byte[] getBytes() throws ISOException {
        return delegate.getBytes();
    }


    @Override
    public int getMaxField() {
        return delegate.getMaxField();
    }


    @Override
    public Map getChildren() {
        return delegate.getChildren();
    }


    @Override
    public void pack(final OutputStream out) throws IOException, ISOException {
        delegate.pack(out);
    }


    @Override
    public void dump(final PrintStream p, final String indent) {
        if (tag != null) {
            p.print(indent + "<" + tag + ">");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            delegate.dump(ps, "");

            String s = new String(baos.toByteArray());
            if (s.endsWith("\r") || s.endsWith("\n") || s.endsWith("\r\n")) {
                s = s.substring(0, s.length() - 1);
            }
            p.print(s);
            p.print("</" + tag + ">\n");
        } else {
            delegate.dump(p, indent);
        }
    }


    @Override
    public byte[] pack() throws ISOException {
        return delegate.pack();
    }


    @Override
    public int unpack(final byte[] b) throws ISOException {
        return delegate.unpack(b);
    }


    @Override
    public void unpack(final InputStream in) throws IOException, ISOException {
        delegate.unpack(in);
    }


    public ISOComponent getDelegate() {
        return delegate;
    }
}
