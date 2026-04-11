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

package org.jpos.space;

import org.jpos.util.ConcurrentUtil;
import org.jpos.util.NameRegistrar;

import java.util.StringTokenizer;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Creates a space based on a space URI.
 *
 * <p>A space URI has three parts:
 *  <ul>
 *   <li>scheme
 *   <li>name
 *   <li>optional parameter
 *  </ul>
 * <p>
 * Examples:
 *
 * <pre>
 *   // default unnamed space (tspace:default)
 *   Space sp = SpaceFactory.getSpace ();
 *
 *   // transient space named "test"
 *   Space sp = SpaceFactory.getSpace ("transient:test");
 *
 *   // lspace (Loom-optimized) named "test"
 *   Space sp = SpaceFactory.getSpace ("lspace:test");
 *
 *   // persistent space named "test"
 *   Space sp = SpaceFactory.getSpace ("persistent:test");
 *
 *   // jdbm space named test
 *   Space sp = SpaceFactory.getSpace ("jdbm:test");
 *
 *   // jdbm space named test, storage located in /tmp/test
 *   Space sp = SpaceFactory.getSpace ("jdbm:test:/tmp/test");
 * </pre>
 *
 */
public class SpaceFactory {
    /** Default constructor. */
    public SpaceFactory() {}
    /** Scheme token for transient (in-memory) TSpace. */
    public static final String TSPACE     = "tspace";
    /** Scheme token for LSpace. */
    public static final String LSPACE     = "lspace";
    /** Alias scheme token for transient space. */
    public static final String TRANSIENT  = "transient";
    /** Alias scheme token for persistent (JDBM) space. */
    public static final String PERSISTENT = "persistent";
    /** Scheme token for remote spacelet. */
    public static final String SPACELET   = "spacelet";
    /** Scheme token for JDBM-backed persistent space. */
    public static final String JDBM       = "jdbm";
    /** Scheme token for Berkeley DB JE-backed persistent space. */
    public static final String JE         = "je";
    /** Default space name when no name is specified. */
    public static final String DEFAULT    = "default";
    private static ScheduledThreadPoolExecutor gcExecutor = ConcurrentUtil.newScheduledThreadPoolExecutor();

    /**
     * Returns the default transient space.
     * @return the default TransientSpace
     */
    public static Space getSpace () {
        return getSpace (TSPACE, DEFAULT, null);
    }

    /**
     * Returns the Space identified by the given URI (e.g. {@code tspace:myname}).
     * @param spaceUri the space URI ({@code scheme:name} or {@code scheme:name:param})
     * @return Space for given URI or null
     */
    public static Space getSpace (String spaceUri) {
        if (spaceUri == null)
            return getSpace ();

        String scheme = null;
        String name   = null;
        String param  = null;

        StringTokenizer st = new StringTokenizer (spaceUri, ":");
        int count = st.countTokens();
        if (count == 0) {
            scheme = TSPACE;
            name   = DEFAULT;
        }
        else if (count == 1) {
            scheme = TSPACE;
            name   = st.nextToken ();
        }
        else {
            scheme = st.nextToken ();
            name   = st.nextToken ();
        }
        if (st.hasMoreTokens()) {
            param  = st.nextToken ();
        }
        return getSpace (scheme, name, param);
    }
    /** Returns (or creates) the space for the given scheme, name, and optional parameter.
     * @param scheme the space scheme (e.g. tspace, jdbm)
     * @param name the space name
     * @param param optional parameter (e.g. file path for jdbm)
     * @return the matching Space instance
     */
    public static Space getSpace (String scheme, String name, String param) {
        Space sp = null;
        String uri = normalize (scheme, name, param);
        synchronized (SpaceFactory.class) {
            try {
                sp = (Space) NameRegistrar.get (uri);
            } catch (NameRegistrar.NotFoundException e) {
                if (SPACELET.equals (scheme) || "rspace".equals(scheme))
                    throw new SpaceError (uri + " not found.");

                sp = createSpace (scheme, name, param);
                NameRegistrar.register (uri, sp);
            }
        }
        if (sp == null) {
            throw new SpaceError ("Invalid space: " + uri);
        }
        return sp;
    }
    /** Returns the shared scheduled executor used for space garbage collection.
     * @return the GC executor
     */
    public static ScheduledThreadPoolExecutor getGCExecutor() {
        return gcExecutor;
    }
    private static Space createSpace (String scheme, String name, String param)
    {
        Space sp = null;
        if (TSPACE.equals (scheme) || TRANSIENT.equals (scheme)) {
            sp = new TSpace();
        } else if (LSPACE.equals (scheme)) {
            sp = new LSpace();
        } else if (JDBM.equals (scheme) || PERSISTENT.equals (scheme)) {
            if (param != null)
                sp = JDBMSpace.getSpace (name, param);
            else
                sp = JDBMSpace.getSpace (name);
        } else if (JE.equals (scheme)) {
            if (param != null)
                sp = JESpace.getSpace (name, param);
            else
                sp = JESpace.getSpace (name);
        }
        return sp;
    }
    private static String normalize (String scheme, String name, String param) {
        StringBuilder sb = new StringBuilder (scheme);
        sb.append (':');
        sb.append (name);
        if (param != null) {
            sb.append (':');
            sb.append (param);
        }
        return sb.toString();
    }
}

