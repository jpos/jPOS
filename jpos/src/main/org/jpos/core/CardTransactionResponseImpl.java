/*
 * $Log$
 * Revision 1.3  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.2  2000/01/30 23:33:50  apr
 * CVS sync/backup - intermediate version
 *
 * Revision 1.1  1999/11/26 12:16:48  apr
 * CVS devel snapshot
 *
 */

package org.jpos.core;

import java.io.*;
import java.util.*;
import org.jpos.iso.*;
import org.jpos.core.*;

/**
 * CardTransactionResponse reference Implementation
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * @see CardTransactionResponse
 */
public class CardTransactionResponseImpl implements CardTransactionResponse
{
    CardAgent agent;
    byte[] image;
    ISOMsg[] msg;
    int flags;

    public static final int AUTHORIZATION = 0x00000001;
    public static final int FINANCIAL     = 0x00000002;
    public static final int OFFLINE       = 0x00000080;
    public static final int HOST_NOTIFIED = 0x00000100;
    public static final int AUTHORITATIVE = 0x20000000;
    public static final int REVERSED      = 0x40000000;
    public static final int INVALID       = 0x80000000;

    public CardTransactionResponseImpl (CardAgent agent) {
	super();
	this.agent = agent;
	image      = null;
	msg        = new ISOMsg[0];
	flags      = 0;
    }
    public CardTransactionResponseImpl (CardAgent agent, int flags) {
	super();
	this.agent = agent;
	image = null;
	msg   = new ISOMsg[0];
	this.flags = flags;
    }

    public CardTransactionResponseImpl (CardAgent agent, byte[] image) 
	throws CardAgentException
    {
	this.agent = agent;
	this.image = image;
	readImage(image);
    }
    public int getFlags() {
	return flags;
    }
    public void setFlags (int flags) {
	this.flags = flags;
    }
    public void or (int mask) {
	flags |= mask;
    }
    public void and (int mask) {
	flags &= mask;
    }
    public synchronized byte[] getImage() throws CardAgentException {
	if (image == null)
	    image = createImage();
	return image;
    }
    public synchronized void addMsg (ISOMsg m) {
	ISOMsg[] a = new ISOMsg[msg.length + 1];
	for (int i=0; i<msg.length; i++)
	    a[i] = msg[i];
	a[msg.length] = m;
	msg = a;
    }

    private ISOPackager getImagePackager() {
	if (agent instanceof CardAgentBase)
	    return  ((CardAgentBase) agent).getImagePackager();
	else
	    return new ISO87BPackager();
    }

    protected byte[] createImage() throws CardAgentException
    {
	if (msg == null)
	    throw new CardAgentException ("Invalid transaction state");

	try {
	    ByteArrayOutputStream b = new ByteArrayOutputStream();
	    ObjectOutputStream o = new ObjectOutputStream (b);
	    o.writeInt(agent.getID());
	    o.writeInt(0);		// version
	    o.writeInt(flags);
	    o.writeInt(msg.length);
	    for (int i=0; i<msg.length; i++) {
		ISOMsg m = msg[i];
		m.setPackager (getImagePackager());
		byte[] packed = m.pack();
		o.writeInt (packed.length);
		o.write (packed);
	    }
	    o.flush();
	    return b.toByteArray();
	} catch (ISOException e) {
	    throw new CardAgentException (e);
	} catch (IOException e) {
	    throw new CardAgentException (e);
	}
    }

    protected synchronized void readImage(byte[] image)
	throws CardAgentException
    {
	try {
	    ByteArrayInputStream b = new ByteArrayInputStream(image);
	    ObjectInputStream o = new ObjectInputStream (b);
	    int agentId = o.readInt();
	    if (agentId != agent.getID())
		throw new CardAgentException ("unknogn agent "+agentId);
	    o.readInt();	// version
	    flags = o.readInt();
	    int msgCount = o.readInt();
	    msg = new ISOMsg[msgCount];

	    ISOPackager packager = getImagePackager();
	    for (int i=0; i<msgCount; i++) {
		byte[] packed = new byte[o.readInt()];
		o.read (packed, 0, packed.length);
		packager.unpack ((msg[i] = new ISOMsg()), packed);
	    }
	} catch (ISOException e) {
	    throw new CardAgentException (e);
	} catch (IOException e) {
	    throw new CardAgentException (e);
	}
    }

    public boolean isValid() {
	return (flags & INVALID) == 0;
    }
    public boolean isHostNotified() {
	return (flags & HOST_NOTIFIED) == 0;
    }

    public String getAutCode() {
	String autCode = "99";
	try {
	    if (isValid() && msg[0].hasField(39))
		autCode = (String) msg[0].getValue(39);
	} catch (ISOException e) { }
	return autCode;
    }
    public String getAutNumber() {
	String autNumber = null;
	try {
	    if (getAutCode().equals("00") && msg[0].hasField(38))
		autNumber = (String) msg[0].getValue(38);
	} catch (ISOException e) { }
	return autNumber;
    }
    private String getMessage0() {
	String autCode = getAutCode();
	Configuration cfg = agent.getConfiguration();
	String msg = cfg.get (agent.getPropertyPrefix() + ".rc." + autCode);
	if (msg == null)
	    msg = "-";
	return msg;
    }
    public String getMessage() {
	return getMessage0().substring(1);
    }
    public boolean isApproved() {
	return getMessage0().startsWith ("#");
    }
    public boolean canContinue() {
	return isApproved() || getMessage0().startsWith ("+");
    }
    public boolean isOnline() {
	return (flags & HOST_NOTIFIED ) != 0;
    }
    public boolean isAuthoritative() {
	return (flags & AUTHORITATIVE) != 0;
    }
    public boolean isFinancial() {
        return (flags & FINANCIAL ) != 0;
    }
    public String getBatchName () {
        try {
            if (isApproved() && isFinancial() && msg[0].hasField(41))
                return (String) msg[0].getValue(41) + ".current";
        } catch (ISOException e) { }
        return null;
    }

}
