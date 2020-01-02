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

package org.jpos.util;

import org.jpos.iso.ISOUtil;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Tags implements Serializable {
    private static final long serialVersionUID = -7749305134294641955L;
    private transient Set<String> ts;

    public Tags() {
        ts = Collections.synchronizedSet(new TreeSet<String>());
    }
    public Tags(String tags) {
        this();
        if (tags != null)
            setTags(tags);
    }
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
    public boolean add (String t) {
        return t != null && ts.add(t.trim());
    }
    public boolean remove (String t) {
        return t != null && ts.remove(t.trim());
    }
    public boolean contains (String t) {
        return t != null && ts.contains(t.trim());
    }
    public Iterator<String> iterator() {
        return ts.iterator();
    }
    public int size() {
        return ts.size();
    }
    public boolean containsAll (Tags tags) {
        return ts.containsAll(tags.ts);
    }
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

    private void writeObject(ObjectOutputStream os)
            throws java.io.IOException {
        os.defaultWriteObject();
        os.writeObject(toString());
    }
    private void readObject(ObjectInputStream is)
            throws java.io.IOException, ClassNotFoundException {
        ts = new TreeSet<>();
        is.defaultReadObject();
        setTags((String) is.readObject());
    }
}
