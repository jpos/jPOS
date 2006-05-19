/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.transaction;

import java.io.Serializable;

/**
 * ContextRecovery can be implemented by a TransactionParticipant in
 * order to customize the activation of a persisted context.
 *
 * @author apr
 * @since 1.4.7
 * @see TransactionParticipant
 */
public interface ContextRecovery {
    /**
     * Give participant the chance to "activate" a previously 
     * persisted context.
     *
     * @param id the Transaction identifier
     * @param context transaction context (as persisted by TransactionManager)
     * @param commit true if transaction is committing
     * @return activated context
     */
    public Serializable recover (long id, Serializable context, boolean commit);
}

