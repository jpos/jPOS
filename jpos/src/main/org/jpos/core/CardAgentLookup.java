/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

/*
 * $Log$
 * Revision 1.8  2003/10/13 10:46:15  apr
 * tabs expanded to spaces
 *
 * Revision 1.7  2003/05/16 04:07:35  alwyns
 * Import cleanups.
 *
 * Revision 1.6  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.5  2000/03/02 12:31:01  apr
 * Get rid of javadoc warnings - done
 *
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
