/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.util;

import org.jpos.iso.ISOUtil;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/** Serializable, mutable set of string tags with helpers for parsing comma-separated values. */
public class Tags implements Serializable {
    private static final long serialVersionUID = -7749305134294641955L;
    private transient Set<String> ts;

    /** Default constructor. */
    public Tags() {
        ts = Collections.synchronizedSet(new TreeSet<String>());
    }

    /**
     * Creates a tag set from a comma-encoded tag string.
     *
     * @param tags comma-encoded tag string, or {@code null}
     */
    public Tags(String tags) {
        this();
        if (tags != null)
            setTags(tags);
    }
    /**
     * Creates a tag set from the provided tag values.
     *
     * @param tags tag values to add, ignoring blank entries
     */
    public Tags(String... tags) {
        this();
        if (tags != null) {
            for (String t : tags) {
                t = t.trim();
                if (t.length() > 0)
                    ts.add(t);
            }
        }
    }

    /**
     * Replaces the current tag set with the tags contained in the supplied string.
     *
     * @param tags comma-encoded tag string, or {@code null}
     */
    public void setTags(String tags) {
        ts.clear();
        if (tags != null) {
            for (String t : ISOUtil.commaDecode(tags)) {
                t = t.trim();
                if (t.length() > 0)
                    ts.add(t);
            }
        }
    }
    /**
     * Adds a tag to the set.
     *
     * @param t tag to add
     * @return {@code true} if the set changed
     */
    public boolean add (String t) {
        return t != null && ts.add(t.trim());
    }

    /**
     * Removes a tag from the set.
     *
     * @param t tag to remove
     * @return {@code true} if the set changed
     */
    public boolean remove (String t) {
        return t != null && ts.remove(t.trim());
    }

    /**
     * Checks whether the set contains the supplied tag.
     *
     * @param t tag to look up
     * @return {@code true} if the tag is present
     */
    public boolean contains (String t) {
        return t != null && ts.contains(t.trim());
    }

    /**
     * Returns an iterator over the tags.
     *
     * @return iterator over the current tags
     */
    public Iterator<String> iterator() {
        return ts.iterator();
    }

    /**
     * Returns the number of tags in the set.
     *
     * @return tag count
     */
    public int size() {
        return ts.size();
    }

    /**
     * Checks whether all tags from the supplied set are present.
     *
     * @param tags tag set to compare against
     * @return {@code true} if all supplied tags are present
     */
    public boolean containsAll (Tags tags) {
        return ts.containsAll(tags.ts);
    }

    /**
     * Checks whether any tag from the supplied set is present.
     *
     * @param tags tag set to compare against
     * @return {@code true} if any supplied tag is present, or if the supplied set is empty
     */
    public boolean containsAny (Tags tags) {
        for (String s : tags.ts) {
            if (ts.contains(s))
                return true;
        }
        return tags.size() == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : ts) {
            if (sb.length() > 0)
                sb.append(',');
            for (int i = 0; i<s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '\\':
                    case ',' :
                        sb.append('\\');
                        break;
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tags tagSet = (Tags) o;
        return ts.equals(tagSet.ts);
    }

    @Override
    public int hashCode() {
        return 19*ts.hashCode();
    }

    /**
     * Serializes the tag set as its comma-encoded string representation.
     *
     * @param os object output stream receiving the serialized form
     * @throws IOException if the stream cannot be written
     */
    private void writeObject(ObjectOutputStream os)
            throws java.io.IOException {
        os.defaultWriteObject();
        os.writeObject(toString());
    }
    /**
     * Recreates the transient tag set from its serialized string form.
     *
     * @param is object input stream providing the serialized form
     * @throws IOException if the stream cannot be read
     * @throws ClassNotFoundException if the serialized payload is invalid
     */
    private void readObject(ObjectInputStream is)
            throws java.io.IOException, ClassNotFoundException {
        ts = new TreeSet<>();
        is.defaultReadObject();
        setTags((String) is.readObject());
    }
}
