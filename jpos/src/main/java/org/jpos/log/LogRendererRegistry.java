/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

package org.jpos.log;

import java.io.PrintStream;
import java.util.*;

/**
 * A registry for managing {@link LogRenderer} instances associated with specific class types and renderer types.
 * This class allows for the registration, retrieval, and management of {@link LogRenderer} instances dynamically,
 * using a thread-safe approach to ensure proper operation in multi-threaded environments.
 */
public class LogRendererRegistry {
    private static final Map<LogRendererRegistry.Key, LogRenderer<?>> renderers = Collections.synchronizedMap(
      new LinkedHashMap<>()
    );
    static {
        for (LogRenderer<?> r : ServiceLoader.load(LogRenderer.class)) {
            register (r);
        }
    }

    /**
     * Registers a {@link LogRenderer} in the registry with a key generated from the renderer's class and type.
     * @param renderer The renderer to register. Must not be null.
     * @throws NullPointerException if the renderer is null.
     */
    public static <T> void register (LogRenderer<?> renderer) {
        Objects.requireNonNull(renderer);
        renderers.put(new LogRendererRegistry.Key(renderer.clazz(), renderer.type()), renderer);
    }

    /**
     * Dumps the current state of the registry to the specified {@link PrintStream}.
     * @param ps The {@link PrintStream} to which the dump will be written, e.g.: System.out
     */
    public static void dump (PrintStream ps) {
        ps.println (LogRendererRegistry.class);
        for (Map.Entry<LogRendererRegistry.Key, LogRenderer<?>> entry : renderers.entrySet()) {
            ps.println("  " + entry.getKey() + ": " + entry.getValue().getClass());
        }
        ps.println ();
    }

    /**
     * Retrieves a {@link LogRenderer} that matches the specified class and type. If no direct match is found,
     * it attempts to find a renderer for any superclass or implemented interfaces. If no specific renderer is found,
     * it defaults to a renderer for {@link Object}, if present for the given type.
     *
     * @param clazz The class for which a renderer is required.
     * @param type The type of the renderer.
     * @return The matching {@link LogRenderer}, or a default renderer if no specific match is found.
     */
    @SuppressWarnings("unchecked")
    public static <T> LogRenderer<T> getRenderer(Class<?> clazz, LogRenderer.Type type) {
        LogRenderer<T> renderer = getRendererForClass(clazz, type);
        boolean needsCache = false;
        if (renderer == null) {
            needsCache = true;
            renderer = getRendererForInterface(clazz, type);
        }
        if (renderer == null) {
            renderer = (LogRenderer<T>) renderers.get(new LogRendererRegistry.Key(Object.class, type));
        }
        if (renderer != null && needsCache)
            renderers.put(new LogRendererRegistry.Key(clazz, renderer.type(), true), renderer);
        return renderer;
    }

    /**
     * Recursively searches for a renderer for the given class and type, considering superclasses.
     *
     * @param clazz The class for which a renderer is needed.
     * @param type The type of the renderer.
     * @return A matching renderer, or null if none is found.
     */
    @SuppressWarnings("unchecked")
    private static <T> LogRenderer<T> getRendererForClass (Class<?> clazz, LogRenderer.Type type) {
        LogRenderer<T> renderer = (LogRenderer<T>) renderers.get(new LogRendererRegistry.Key(clazz, type));
        if (renderer == null && clazz.getSuperclass() != Object.class) {
            renderer = getRendererForClass(clazz.getSuperclass(), type);
        }
        return renderer;
    }

    /**
     * Searches for a renderer among the interfaces implemented by the specified class.
     *
     * @param clazz The class whose interfaces will be checked for a matching renderer.
     * @param type The type of the renderer.
     * @return A matching renderer, or null if none is found.
     */
    @SuppressWarnings("unchecked")
    private static <T> LogRenderer<T> getRendererForInterface (Class<?> clazz, LogRenderer.Type type) {
        for (Class<?> i : clazz.getInterfaces()) {
            LogRenderer<T> renderer = (LogRenderer<T>) renderers.get(new LogRendererRegistry.Key(i, type));
            if (renderer != null)
                return renderer;
        }
        return null;
    }

    /**
     * A private key class to encapsulate the combination of class type and renderer type.
     * This key is used to uniquely identify renderers within the registry.
     */
    private static class Key {
        private final Class<?> clazz;
        private final LogRenderer.Type type;
        private final boolean cache;

        public Key(Class<?> clazz, LogRenderer.Type type) {
            this (clazz, type, false);
        }
        public Key(Class<?> clazz, LogRenderer.Type type, boolean cache) {
            this.clazz = clazz;
            this.type = type;
            this.cache = cache;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LogRendererRegistry.Key key = (LogRendererRegistry.Key) o;
            return Objects.equals(clazz, key.clazz) && type == key.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, type);
        }

        @Override
        public String toString() {
            return "Key{" + clazz + ", type=" + type + (cache ? ", cached" : "") + '}';
        }
    }
}
