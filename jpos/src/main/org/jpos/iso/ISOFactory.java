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


package org.jpos.iso;

import org.jpos.core.Configuration;
import java.io.IOException;
import java.net.SocketException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.core.Configuration;
import org.jpos.iso.channel.*;
import org.jpos.util.NameRegistrar;

/**
 * <b><i>j</i>POS</b> applications usually constructs
 * objects such as ISOChannels, ISOMUXs, etc. based on 
 * configuration parameters.<br>
 * We'll group here factory methods for those repeated tasks
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a> 
 * @version $Revision$ $Date$
 * @deprecated This whole class will be deprecated, use QSP application instead
 *
 * @deprecated Use QSP instead
 */
public class ISOFactory {
    /**
     * Creates a client or server ISOChannel based on Configuration properties:
     * <br>
     * If [prefix].host is available newChannel will create an unconnected
     * client ISOChannel.
     * <br>
     * If [prefix].host is not available (null) it will create an unconnected
     * server ISOChannel.
     *
     * <ul>
     *  <li>[prefix].channel
     *  <li>[prefix].packager
     *  <li>[prefix].header
     *  <li>[prefix].port
     *  <li>[prefix].host
     * </ul>
     *
     * @see org.jpos.core.Configuration
     * @see ISOChannel
     *
     * @param cfg Configuration Object
     * @param prefix propertyPrefix
     * @param logger optional logger (may be null)
     * @param realm  optional realm  (may be null if logger is null)
     * @exception ISOException catches all possible exceptions into one
     */
    public static ISOChannel newChannel
	(Configuration cfg, String prefix, Logger logger, String realm)
	throws ISOException
    {
	String channelName  = cfg.get    (prefix + ".channel", null);
	String packagerName = cfg.get    (prefix + ".packager", null);
	String header       = cfg.get    (prefix + ".header", null);
	String host         = cfg.get    (prefix + ".host", null);
	int    port         = cfg.getInt (prefix + ".port");
	int    timeout      = cfg.getInt (prefix + ".timeout");
        ISOChannel channel  = newChannel
	    (channelName, packagerName, logger, realm);
	if (timeout != 0 && channel instanceof BaseChannel) {
	    try {
		((BaseChannel)channel).setTimeout (timeout);
	    } catch (SocketException e) {
		throw new ISOException (e);
	    }
	}
	if (host != null && channel instanceof ClientChannel)
	    ((ClientChannel)channel).setHost (host, port);
	if (header != null) {
	    if (channel instanceof RawChannel) {
		((RawChannel)channel).setHeader (
		    ISOUtil.str2bcd(header, false)
		);
	    } else if (channel instanceof BASE24Channel) {
		((BASE24Channel)channel).setHeader (header.getBytes());
	    } else if (channel instanceof PADChannel) {
		((PADChannel)channel).setHeader (
		    ISOUtil.hex2byte
			(header.getBytes(), 0, header.getBytes().length)
		);
	    }
	}
        return channel;
    }

    /**
     * Creates an ISOChannel, possibly assigning packager and Logger
     * @see ISOChannel
     *
     * @param channelName channel class name
     * @param packagerName packager class name
     * @param logger optional logger (may be null)
     * @param realm  optional realm  (may be null if logger is null)
     * @exception ISOException catches all possible exceptions into one
     */
    public static ISOChannel newChannel
	(String channelName, String packagerName, Logger logger, String realm)
	throws ISOException
    {
        ISOChannel channel  = null;
	ISOPackager packager= null;
        try {
            Class c = Class.forName(channelName);
            if (c != null) {
                channel = (ISOChannel) c.newInstance();
		if (packagerName != null) {
		    Class p = Class.forName(packagerName);
		    packager = (ISOPackager) p.newInstance();
		    channel.setPackager(packager);
		}
		if (logger != null && (channel instanceof LogSource))
		    ((LogSource) channel) .
			setLogger (logger, realm + ".channel");
            }
        } catch (ClassNotFoundException e) {
	    throw new ISOException ("newChannel:"+channelName, e);
        } catch (InstantiationException e) {
	    throw new ISOException ("newChannel:"+channelName, e);
        } catch (IllegalAccessException e) {
	    throw new ISOException ("newChannel:"+channelName, e);
	}
        return channel;
    }

    /**
     * Creates an ISOChannel based on Configuration property [prefix].packager
     *
     * @see org.jpos.core.Configuration
     * @see ISOPackager
     *
     * @param cfg Configuration Object
     * @param prefix propertyPrefix
     * @param logger optional logger (may be null)
     * @param realm  optional realm  (may be null if logger is null)
     * @exception ISOException catches all possible exceptions into one
     */
    public static ISOPackager newPackager
	(Configuration cfg, String prefix, Logger logger, String realm)
	throws ISOException
    {
	ISOPackager packager = null;
	String packagerName = cfg.get    (prefix + ".packager");
        try {
            Class p = Class.forName(packagerName);
            if (p != null) {
		packager = (ISOPackager) p.newInstance();
		if (logger != null)
		    packager.setLogger (logger, realm + ".packager");
            }
        } catch (ClassNotFoundException e) {
	    throw new ISOException ("newPackager", e);
        } catch (InstantiationException e) {
	    throw new ISOException ("newPackager", e);
        } catch (IllegalAccessException e) {
	    throw new ISOException ("newPackager", e);
	}
        return packager;
    }

    /**
     * Create an ISOChannel, builds an ISOMUX around it and start it in
     * a newly created daemon Thread.
     * @see ISOFactory#newChannel
     * @see org.jpos.core.Configuration
     * @see ISOChannel
     *
     * @param cfg Configuration Object
     * @param prefix propertyPrefix
     * @param logger optional logger (may be null)
     * @param realm  optional realm  (may be null if logger is null)
     * @exception ISOException catches all possible exceptions into one
     */
    public static ISOMUX newMUX
	(Configuration cfg, String prefix, Logger logger, String realm)
	throws ISOException
    {
	ISOChannel channel = newChannel (cfg, prefix, logger, realm);
	ISOMUX mux = new ISOMUX (channel, logger, realm + ".mux");
	Thread t = new Thread (mux);
	t.setName (realm + ".mux");
	t.setDaemon (true);
	t.start();
	return mux;
    }

    /**
     * @return ISOChannel instance with given name.
     * @throws NameRegistrar.NotFoundException;
     * @see NameRegistrar
     */
    public static ISOChannel getChannel (String name)
	throws NameRegistrar.NotFoundException
    {
	return (ISOChannel) NameRegistrar.get ("channel."+name);
    }
}
