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
 * <p>
 *
 * Examples:
 *
 * <pre>
 *   // default unnamed space (tspace:default)
 *   Space sp = SpaceFactory.getSpace (); 
 *
 *   // transient space named "test"
 *   Space sp = SpaceFactory.getSpace ("transient:test");  
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
    public static final String TSPACE     = "tspace";
    public static final String TRANSIENT  = "transient";
    public static final String PERSISTENT = "persistent";
    public static final String SPACELET   = "spacelet";
    public static final String JDBM       = "jdbm";
    public static final String JE         = "je";
    public static final String DEFAULT    = "default";
    private static ScheduledThreadPoolExecutor gcExecutor = ConcurrentUtil.newScheduledThreadPoolExecutor();

    /**
     * @return the default TransientSpace
     */
    public static Space getSpace () {
        return getSpace (TSPACE, DEFAULT, null);
    }

    /**
     * @param spaceUri 
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
    public static ScheduledThreadPoolExecutor getGCExecutor() {
        return gcExecutor;
    }
    private static Space createSpace (String scheme, String name, String param)
    {
        Space sp = null;
        if (TSPACE.equals (scheme) || TRANSIENT.equals (scheme)) {
            sp = new TSpace();
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

