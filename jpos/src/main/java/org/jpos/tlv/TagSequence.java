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

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.util.List;
import java.util.Map;

/**
 * @author Vishnu Pillai
 *         Date: 4/11/14
 */
public interface TagSequence<T> extends TagValue<T> {

    Map<String, List<TagValue<T>>> getChildren();

    void add(TagValue<T> tagValue);

    boolean hasTag(String tag);

    TagValue<T> getFirst(String tag);

    List<TagValue<T>> get(String tag);

    Map<String, List<TagValue<T>>> getAll();

    List<TagValue<T>> getOrderedList();

    void writeTo(ISOMsg isoMsg) throws ISOException;

    void readFrom(ISOMsg isoMsg) throws ISOException;
}
