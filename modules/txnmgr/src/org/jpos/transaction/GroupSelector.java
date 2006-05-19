/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.transaction;

import java.io.Serializable;

/**
 * GroupSelector can be implemented by a TransactionParticipant in
 * order to switch the transaction to a new group of participants.
 *
 * @author apr
 * @since 1.4.7
 * @see TransactionParticipant
 */
public interface GroupSelector extends TransactionParticipant {
    /**
     * @param id transaction id
     * @param context transaction context 
     * @return group name or null for no-action
     */
    public String select (long id, Serializable context);
}

