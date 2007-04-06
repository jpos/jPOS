/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction;

import java.io.PrintStream;
import java.util.List;
import java.util.Iterator;
import org.jpos.util.Loggeable;

public class PausedTransaction implements Loggeable {
    private long id;
    private List members;
    private Iterator iter;
    private boolean aborting;
    public PausedTransaction (
            long id, List members, Iterator iter, boolean aborting) 
    {
        super();
        this.id = id;
        this.members = members;
        this.iter = iter;
        this.aborting = aborting;
    }
    public long id() {
        return id;
    }
    public List members() {
        return members;
    }
    public Iterator iterator() {
        return iter;
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + "id: " + id
                + (isAborting() ? " (aborting)" : ""));

    }
    public boolean isAborting() {
        return aborting;
    }
}

