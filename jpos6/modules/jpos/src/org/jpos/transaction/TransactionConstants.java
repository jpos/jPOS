/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction;

import java.io.Serializable;

public interface TransactionConstants {
    public static final int ABORTED  = 0;
    public static final int PREPARED = 1;

    /**
     * This participant does not join the transaction
     */
    public static final int NO_JOIN  = 0x40;

    /**
     * Context has not been modified (no need to persist a snapshot)
     */
    public static final int READONLY = 0x80;
}

