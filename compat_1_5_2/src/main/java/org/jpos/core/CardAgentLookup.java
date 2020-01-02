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

package org.jpos.core;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * Singleton in charge of registering and further locating Agents
 * @see CardAgent
 * @see CardTransaction
 */
@SuppressWarnings("unchecked")
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
     * @param name
     * @return given agent
     * @exception CardAgentNotFoundException
     */
    synchronized public static CardAgent getAgent (String name)
        throws CardAgentNotFoundException
    {
        Iterator i = instance.agents.iterator();
        while (i.hasNext()) {
            CardAgent a = (CardAgent) i.next();
            if ( a.getClass().getName().endsWith(name) )
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
     * @param t
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
