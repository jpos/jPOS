/*
 * $Log$
 * Revision 1.4  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.3  1999/11/26 12:16:45  apr
 * CVS devel snapshot
 *
 * Revision 1.2  1999/09/26 22:31:56  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:04  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package org.jpos.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;


/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * Singleton in charge of registering and further locating Agents
 * @see CardAgent
 * @see CardTransaction
 */
public class CardAgentLookup {
    private static CardAgentLookup instance = new CardAgentLookup();
    private ArrayList agents;

    /**
     * no external instantiation - thank you
     */
    private CardAgentLookup () {
	agents = new ArrayList();
    }
    /**
     * register an Agent (at the end of the list)
     * @param agent Agent to add
     */
    synchronized public static void add (CardAgent agent) {
	instance.agents.add (agent);
    }
    /**
     * remove all ocurrences of agent
     * @param agent Agent to remove
     */
    synchronized public static void remove (CardAgent agent) {
	ArrayList a = instance.agents;
	int i;
	while ( (i=a.indexOf(agent)) >= 0)
	    a.remove(i);
    }
    /**
     * @return all available agents
     */
    synchronized public static CardAgent[] getAgents() {
	ArrayList a = instance.agents;
	return (CardAgent[]) a.toArray(new CardAgent[a.size()]);
    }

    /**
     * locate agent able to process a given CardTransaction
     * @param t CardTransaction holding an Operation to be performed
     * @return suitable array of agents
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent[] getAgents (CardTransaction t) {
	ArrayList l = new ArrayList();
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if (a.canHandle (t)) 
		l.add (a);
	}
	return (CardAgent[]) l.toArray(new CardAgent[l.size()]);
    }
    /**
     * locate an agent giving its class Name
     * @param class name
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (String name)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if ( (a.getClass().getName()).endsWith (name) ) 
		return a;
	}
	throw new CardAgentNotFoundException (name);
    }
    /**
     * locate an agent giving its unique agent ID
     * @param id
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (int id)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if (a.getID() == id)
		return a;
	}
	throw new CardAgentNotFoundException (Integer.toString(id));
    }

    /**
     * locate an agent giving a transaction Image
     * @param b a transaction image
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (byte[] b)
	throws CardAgentNotFoundException
    {
	try {
	    ByteArrayInputStream i = new ByteArrayInputStream (b);
	    ObjectInputStream o    = new ObjectInputStream (i);
	    int id = o.readInt();
	    return getAgent (id);
	} catch (Exception e) { }
	throw new CardAgentNotFoundException ();
    }

    /**
     * locate an agent of a given class
     * @param class 
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (Class t)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if (a.getClass() == t)
		return a;
	}
	throw new CardAgentNotFoundException (t.getName());
    }
    /**
     * locate an agent able to process a given CardTransaction
     * @param t CardTransaction holding an Operation to be performed
     * @return suitable agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (CardTransaction t)
	throws CardAgentNotFoundException
    {
	Iterator i = instance.agents.iterator();
	while (i.hasNext()) {
	    CardAgent a = (CardAgent) i.next();
	    if (a.canHandle (t)) 
		return a;
	}
	throw new CardAgentNotFoundException ();
    }
}
