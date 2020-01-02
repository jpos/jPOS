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

package org.jpos.q2.iso;

import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;
import org.jpos.util.Loggeable;
import org.jpos.util.NameRegistrar;

import java.io.PrintStream;
import java.util.List;

/**
 * A utility QBean to provide the ability to monitor an 'in' queue for items that will be placed in
 *  Context, along with any specified context-values and then place on the 'out' queue - for a
 *  TransactionManager to process.

 * @author Mark Salter
 * @version $Revision: 2854 $ $Date: 2010-01-02 10:34:31 +0000 (Sat, 02 Jan 2010) $
 */
@SuppressWarnings("unchecked")
public class ContextMaker extends QBeanSupport implements Runnable,
		Loggeable {

	Space sp;

    String contextName = null;
    String in = null;
    String out = null;
    String source = null;

    Long timeout;

	private List<Element> contextValues = null;

	public void initService() {
		NameRegistrar.register(getName(), this);

	}

	public void startService() {
		// we re-register just in case the component was soft-stopped
		NameRegistrar.register(getName(), this);
        new Thread(this).start();
	}

	public void stopService() {
		NameRegistrar.unregister(getName());
	}

	public void run() {
		Thread.currentThread().setName(getName());
		while (running()) {

			Object o = sp.in(in, timeout);

            	if (o != null) {
    		            Context ctx = new Context();
                		ctx.put(contextName, o);
                		
                		if (contextValues != null) {
					for (Element e : contextValues) {
                				ctx.put(e.getName(),e.getValue());
	                		}
				}
                		
               		sp.out(out, ctx);
			}
		}
	}


	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		super.setConfiguration(cfg);

		Element persist = getPersist();

		String ssp = persist.getChildText("space");

		sp = SpaceFactory.getSpace(ssp != null ? ssp : "");

		String sTimeout = persist.getChildText("timeout");
		timeout = sTimeout == null ? 10000 : Long
				.parseLong(sTimeout);

		contextName = persist.getChildText("context-name");
        if (contextName == null) {
            throw new ConfigurationException(
                    "Missing 'context-name' property - the context name of the object received on 'in'");
		}
        
        in = persist.getChildText("in");
        if (in == null) {
            throw new ConfigurationException(
                    "Missing 'in' property - the queue to process objects from.");
		}
        
        out = persist.getChildText("out");
        if (out == null) {
            throw new ConfigurationException(
                    "Missing 'out' property - the target queue of the created context");
		}
        
        Element values = persist.getChild("context-values");
        if (values != null) {
        	contextValues = values.getChildren();
        }
        
	}
	
	public void dump(PrintStream p, String indent) {
		String inner = indent + "  ";
		p.println(indent + "<ContextMaker name='" + getName() + "'>");
		    for (Element e : contextValues) {
		        p.println(indent+"<"+indent+e.getName()+">"+e.getValue()+"</"+indent+e.getName()+">");
		    }
		p.println(indent + "</ContextMaker>");
	}
}
