/*
 * Copyright (c) 2003 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.space;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Request adds to TinySpace a few helper methods suitable for
 * exchanging requests and responses over an outter [Transient|Tiny]Space.
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 1.4.7
 */
public class Request extends TinySpace {
    public static String REQUEST  = "$REQ";
    public static String RESPONSE = "$RESP";
    public static String ERROR    = "$ERR";
    
    public Request () {
        super();
    }
    public Request (Object value) {
        out (REQUEST, value);
    }

    public Object getResponse () {
        return rd (RESPONSE);
    }
    public Object getResponse (long timeout) {
        return rd (RESPONSE, timeout);
    }
    public void setResponse (Object o) {
        out (RESPONSE, o);
    }

    public Object getRequest () {
        return rd (REQUEST);
    }
    public Object getRequest (long timeout) {
        return rd (REQUEST, timeout);
    }

    public void addError (Object o) {
        out (ERROR, o);
    }
    public Object[] getErrors () {
        return (Object[]) SpaceUtil.inpAll(this, ERROR);
    }
    public Object getError () {
        return inp (ERROR);
    }
}

