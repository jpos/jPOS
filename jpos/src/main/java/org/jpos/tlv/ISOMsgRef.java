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

package org.jpos.tlv;


import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;


/**
 * @author Vishnu Pillai
 */
public class ISOMsgRef {

    private final ISOMsg delegate;
    private int offset = 0;

    public ISOMsgRef(ISOMsg delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate cannot be null");
        }
        this.delegate = delegate;
    }

    public ISOMsg reference(int fieldNumber) {
        return new Ref(fieldNumber);
    }


    public class Ref extends ISOMsg implements OffsetIndexedComposite {

        private Integer fieldNumber;

        private Ref(int fieldNumber) {
            this.fieldNumber = fieldNumber;
        }

        @Override
        public void incOffset() {
            if (offset < delegate.getMaxField()) {
                offset++;
            }
        }

        @Override
        public void setOffset(int offset) {
            if (offset <= delegate.getMaxField()) {
                ISOMsgRef.this.offset = offset;
            }
        }

        @Override
        public void resetOffset() {
            offset = 0;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public Object getKey() throws ISOException {
            return fieldNumber;
        }


        @Override
        public void setFieldNumber(final int fieldNumber) {
            this.fieldNumber = fieldNumber;
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
        public void unset(final int fldno) {
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
        public Object getValue() {
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
            delegate.dump(p, indent);
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

        @Override
        public void setDirection(int direction) {
            delegate.setDirection(direction);
        }

        @Override
        public void setHeader(byte[] b) {
            delegate.setHeader(b);
        }

        @Override
        public void setHeader(ISOHeader header) {
            delegate.setHeader(header);
        }

        @Override
        public byte[] getHeader() {
            return delegate.getHeader();
        }

        @Override
        public ISOHeader getISOHeader() {
            return delegate.getISOHeader();
        }

        @Override
        public int getDirection() {
            return delegate.getDirection();
        }

        @Override
        public boolean isIncoming() {
            return delegate.isIncoming();
        }

        @Override
        public boolean isOutgoing() {
            return delegate.isOutgoing();
        }

        @Override
        public void setPackager(ISOPackager p) {
            delegate.setPackager(p);
        }

        @Override
        public ISOPackager getPackager() {
            return delegate.getPackager();
        }

        @Override
        public void set(int fldno, String value) {
            delegate.set(fldno, value);
        }

        @Override
        public void set(String fpath, String value) {
            delegate.set(fpath, value);
        }

        @Override
        public void set(String fpath, ISOComponent c) throws ISOException {
            delegate.set(fpath, c);
        }

        @Override
        public void set(String fpath, byte[] value) {
            delegate.set(fpath, value);
        }

        @Override
        public void set(int fldno, byte[] value) {
            delegate.set(fldno, value);
        }

        @Override
        public void unset(int[] flds) {
            delegate.unset(flds);
        }

        @Override
        public void unset(String fpath) {
            delegate.unset(fpath);
        }

        @Override
        public void recalcBitMap() throws ISOException {
            delegate.recalcBitMap();
        }

        @Override
        public ISOComponent getComponent(int fldno) {
            return delegate.getComponent(fldno);
        }

        @Override
        public Object getValue(int fldno) {
            return delegate.getValue(fldno);
        }

        @Override
        public Object getValue(String fpath) throws ISOException {
            return delegate.getValue(fpath);
        }

        @Override
        public ISOComponent getComponent(String fpath) throws ISOException {
            return delegate.getComponent(fpath);
        }

        @Override
        public String getString(int fldno) {
            return delegate.getString(fldno);
        }

        @Override
        public String getString(String fpath) {
            return delegate.getString(fpath);
        }

        @Override
        public byte[] getBytes(int fldno) {
            return delegate.getBytes(fldno);
        }

        @Override
        public byte[] getBytes(String fpath) {
            return delegate.getBytes(fpath);
        }

        @Override
        public boolean hasField(int fldno) {
            return delegate.hasField(fldno);
        }

        @Override
        public boolean hasFields(int[] fields) {
            return delegate.hasFields(fields);
        }

        @Override
        public boolean hasField(String fpath) {
            return delegate.hasField(fpath);
        }

        @Override
        public boolean hasFields() {
            return delegate.hasFields();
        }

        @Override
        public Object clone() {
            return delegate.clone();
        }

        @Override
        public Object clone(int[] fields) {
            return delegate.clone(fields);
        }

        @Override
        public void merge(ISOMsg m) {
            delegate.merge(m);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public boolean isInner() {
            return delegate.isInner();
        }

        @Override
        public void setMTI(String mti) throws ISOException {
            delegate.setMTI(mti);
        }

        @Override
        public void move(int oldFieldNumber, int newFieldNumber) throws ISOException {
            delegate.move(oldFieldNumber, newFieldNumber);
        }

        @Override
        public String getMTI() throws ISOException {
            return delegate.getMTI();
        }

        @Override
        public boolean isRequest() throws ISOException {
            return delegate.isRequest();
        }
    }

}