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

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Java-serialization helpers with deserialization filters that reject
 * known gadget-chain classes and enforce a depth limit.
 */
public class Serializer {
    /** Utility class; instances carry no state. */
    public Serializer() {}
    private static final int MAX_DEPTH = 32;

    private static final Set<String> REJECTED_CLASSES = Set.of(
        "org.apache.commons.collections.functors.InvokerTransformer",
        "org.apache.commons.collections.functors.InstantiateTransformer",
        "org.apache.commons.collections4.functors.InvokerTransformer",
        "org.apache.commons.collections4.functors.InstantiateTransformer",
        "org.apache.xalan.xsltc.trax.TemplatesImpl",
        "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
        "org.codehaus.groovy.runtime.ConvertedClosure",
        "org.codehaus.groovy.runtime.MethodClosure",
        "org.springframework.beans.factory.ObjectFactory",
        "com.sun.org.apache.bcel.internal.util.ClassLoader",
        "org.mozilla.javascript.NativeJavaObject",
        "com.mchange.v2.c3p0.WrapperConnectionPoolDataSource",
        "com.mchange.v2.c3p0.JndiRefForwardingDataSource",
        "bsh.XThis",
        "bsh.Interpreter",
        "com.sun.rowset.JdbcRowSetImpl"
    );

    private static final Set<String> REJECTED_PACKAGES = Set.of(
        "org.apache.commons.collections.functors.",
        "org.apache.commons.collections4.functors.",
        "javassist.",
        "net.bytebuddy.",
        "org.hibernate.jmx.",
        "javax.management."
    );

    private static final ObjectInputFilter SERIAL_FILTER = filterInfo -> {
        if (filterInfo.depth() > MAX_DEPTH)
            return ObjectInputFilter.Status.REJECTED;

        Class<?> clazz = filterInfo.serialClass();
        if (clazz != null) {
            String name = clazz.getName();
            if (REJECTED_CLASSES.contains(name))
                return ObjectInputFilter.Status.REJECTED;
            for (String pkg : REJECTED_PACKAGES) {
                if (name.startsWith(pkg))
                    return ObjectInputFilter.Status.REJECTED;
            }
        }
        return ObjectInputFilter.Status.UNDECIDED;
    };

    /**
     * Creates an ObjectInputStream with a deserialization filter that rejects
     * known gadget-chain classes and enforces resource limits.
     *
     * @param in the underlying input stream
     * @return a filtered ObjectInputStream
     * @throws IOException if an I/O error occurs
     */
    public static ObjectInputStream createSafeObjectInputStream(InputStream in) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(in);
        ois.setObjectInputFilter(SERIAL_FILTER);
        return ois;
    }

    /**
     * Creates an ObjectInputStream with an allow-list filter that only permits
     * classes matching the specified packages or exact class names.
     *
     * @param in the underlying input stream
     * @param allowedPackages package prefixes to allow (e.g. "org.jpos.iso.")
     * @return a filtered ObjectInputStream
     * @throws IOException if an I/O error occurs
     */
    public static ObjectInputStream createAllowListObjectInputStream(InputStream in, String... allowedPackages) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(in);
        ois.setObjectInputFilter(filterInfo -> {
            if (filterInfo.depth() > MAX_DEPTH)
                return ObjectInputFilter.Status.REJECTED;

            Class<?> clazz = filterInfo.serialClass();
            if (clazz == null)
                return ObjectInputFilter.Status.UNDECIDED;

            if (clazz.isPrimitive() || clazz.isArray())
                return ObjectInputFilter.Status.ALLOWED;

            String name = clazz.getName();
            if (name.startsWith("java.lang.") || name.startsWith("java.util.") || name.startsWith("java.math."))
                return ObjectInputFilter.Status.ALLOWED;

            for (String pkg : allowedPackages) {
                if (name.startsWith(pkg))
                    return ObjectInputFilter.Status.ALLOWED;
            }
            return ObjectInputFilter.Status.REJECTED;
        });
        return ois;
    }

    /**
     * Serializes {@code obj} into a byte array using standard Java serialization.
     *
     * @param obj object to serialize
     * @return the serialized byte array
     * @throws IOException if writing fails
     */
    public static byte[] serialize (Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(obj);
        return baos.toByteArray();
    }
    /**
     * Deserializes the byte array using {@link #createSafeObjectInputStream(InputStream)}.
     *
     * @param b serialized bytes
     * @return the deserialized object
     * @throws IOException if reading fails
     * @throws ClassNotFoundException if a referenced class cannot be loaded
     */
    public static Object deserialize (byte[] b) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ObjectInputStream is = createSafeObjectInputStream(bais);
        return is.readObject();
    }
    /**
     * Deserializes the byte array and casts the result to {@code T}.
     *
     * @param <T> expected concrete type
     * @param b serialized bytes
     * @param clazz expected class (used for the unchecked cast)
     * @return the deserialized object
     * @throws IOException if reading fails
     * @throws ClassNotFoundException if a referenced class cannot be loaded
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize (byte[] b, Class<T> clazz) throws IOException, ClassNotFoundException {
        return (T) deserialize(b);
    }
    /**
     * Round-trips an object through serialization and back, useful for deep-cloning.
     *
     * @param <T> object type
     * @param obj object to clone
     * @return a fresh deserialized copy of {@code obj}
     * @throws IOException if serialization fails
     * @throws ClassNotFoundException if a referenced class cannot be loaded
     */
    @SuppressWarnings("unchecked")
    public static <T> T serializeDeserialize (T obj) throws IOException, ClassNotFoundException {
        return (T) deserialize (serialize(obj));
    }

    /**
     * Serializes a {@code Map<String,String>} using a compact entry-by-entry format.
     *
     * @param m the map to serialize
     * @return the serialized byte array
     * @throws IOException if writing fails
     */
    public static byte[] serializeStringMap (Map<String,String> m)
      throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream     oos = new ObjectOutputStream (baos);
        Set s = m.entrySet();
        oos.writeInt (s.size());
        for (Object value : s) {
            Map.Entry entry = (Map.Entry) value;
            oos.writeObject(entry.getKey());
            oos.writeObject(entry.getValue());
        }
        oos.close();
        return baos.toByteArray();
    }
    /**
     * Inverse of {@link #serializeStringMap(Map)}; only allows JDK collection/string classes.
     *
     * @param buf the serialized bytes
     * @return the deserialized map
     * @throws ClassNotFoundException if a referenced class cannot be loaded
     * @throws IOException if reading fails
     */
    public static Map<String,String> deserializeStringMap (byte[] buf)
      throws ClassNotFoundException, IOException
    {
        ByteArrayInputStream  bais = new ByteArrayInputStream (buf);
        ObjectInputStream     ois  = createAllowListObjectInputStream(bais);
        Map<String,String> m = new HashMap<>();
        int size = ois.readInt();
        for (int i=0; i<size; i++) {
            m.put (
              (String) ois.readObject(),
              (String) ois.readObject()
            );
        }
        return m;
    }
}
