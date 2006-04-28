/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.transaction;

import java.io.Serializable;

/**
 * 2 phase commit participant
 * @author apr
 * @since 1.4.7
 */
public interface TransactionParticipant extends TransactionConstants {
    /**
     * Called by TransactionManager in preparation for a transaction
     * @param id the Transaction identifier
     * @param context transaction context
     * @return PREPARED or ABORTED (| NO_JOIN | READONLY)
     */
    public int  prepare (long id, Serializable context);

    /**
     * Called by TransactionManager upon transaction commit.
     * Warning: implementation should be able to handle multiple calls
     * with the same transaction id (rare crash recovery)
     *
     * @param id the Transaction identifier
     * @param context transaction context
     */
    public void commit  (long id, Serializable context);

    /**
     * Called by TransactionManager upon transaction commit.
     * Warning: implementation should be able to handle multiple calls
     * with the same transaction id (rare crash recovery)
     *
     * @param id the Transaction identifier
     * @param context transaction context
     */
    public void abort   (long id, Serializable context);
}

