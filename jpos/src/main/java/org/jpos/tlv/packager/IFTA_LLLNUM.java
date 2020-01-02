/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.tlv.packager;

import org.jpos.iso.IFA_LLLNUM;
import org.jpos.iso.IF_CHAR;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.LeftPadder;
import org.jpos.iso.TaggedFieldPackager;
import org.jpos.tlv.ISOTaggedField;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;

/**
 * @author Vishnu Pillai
 */
public class IFTA_LLLNUM extends IFA_LLLNUM implements TaggedFieldPackager {
    private String token;

    private IF_CHAR tagPackager;

    public IFTA_LLLNUM() {
        super();
    }

    public IFTA_LLLNUM(int len, String description) {
        super(len, description);
    }

    @Override
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOTaggedField(getToken(), super.createComponent(fieldNumber));
    }

    @Override
    public void setToken(String token) {
        this.token = token;
        tagPackager = new IF_CHAR(token.length(), "Tag");
        tagPackager.setPadder(LeftPadder.ZERO_PADDER);
    }

    @Override
    public String getToken() {
        return token;
    }

    protected ISOFieldPackager getTagPackager() {
        return tagPackager;
    }

    protected byte[] packTag(ISOComponent c) throws ISOException {
        return getTagPackager().pack(new ISOField((Integer) c.getKey(), ((ISOTaggedField) c).getTag()));
    }

    protected int unpackTag(ISOComponent c, byte[] tagBytes, int offset) throws ISOException {
        ISOField tagField = new ISOField((Integer) c.getKey());
        int consumed = getTagPackager().unpack(tagField, tagBytes, offset);
        ((ISOTaggedField) c).setTag(tagField.getValue().toString());
        return consumed;
    }

    protected void unpackTag(ISOComponent c, InputStream in) throws ISOException, IOException {
        ISOField tagField = new ISOField((Integer) c.getKey());
        getTagPackager().unpack(tagField, in);
        ((ISOTaggedField) c).setTag(tagField.getValue().toString());
    }

    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        byte[] tagBytes = packTag(c);
        byte[] message = super.pack(c);
        byte[] b = new byte[tagBytes.length + message.length];
        System.arraycopy(tagBytes, 0, b, 0, tagBytes.length);
        System.arraycopy(message, 0, b, tagBytes.length, message.length);
        return b;
    }

    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        int consumed = unpackTag(c, b, offset);
        return consumed + super.unpack(c, b, offset + consumed);
    }

    @Override
    public void unpack(ISOComponent c, InputStream in) throws IOException, ISOException {
        unpackTag(c, in);
        super.unpack(c, in);
    }

    @Override
    public void pack(ISOComponent c, ObjectOutput out) throws IOException, ISOException {
        byte[] tagBytes = packTag(c);
        out.write(tagBytes);
        super.pack(c, out);
    }

}
