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
public interface AbortParticipant extends TransactionParticipant {
    /**
     * Called by TransactionManager in preparation for a transaction
     * that is known to abort.
     *
     * @param id the Transaction identifier
     * @param context transaction context
     * @return 0 [| NO_JOIN | READONLY)
     */
    public int  prepareForAbort (long id, Serializable context);
}

