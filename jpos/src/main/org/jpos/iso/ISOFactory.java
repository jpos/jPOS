
package org.jpos.iso;

import org.jpos.core.Configuration;
import java.io.IOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Logger;
import org.jpos.core.Configuration;

/**
 * <b><i>j</i>POS</b> applications usually constructs
 * objects such as ISOChannels, ISOMUXs, etc. based on 
 * configuration parameters.<br>
 * We'll group here factory methods for those repeated tasks
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a> 
 * @version $Revision$ $Date$
 */
public class ISOFactory {
    /**
     * Builds a client or server ISOChannel based on Configuration properties:
     * <br>
     * If [prefix].host is available createChannel will create an unconnected
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
     * @see org.jpos.util.Configuration
     * @see ISOChannel
     *
     * @param cfg Configuration Object
     * @param prefix propertyPrefix
     * @param logger optional logger (may be null)
     * @param realm  optional realm  (may be null if logger is null)
     * @exception ISOException catches all possible exceptions into one
     */
    public static ISOChannel createChannel
	(Configuration cfg, String prefix, Logger logger, String realm)
	throws ISOException
    {
	String channelName  = cfg.get    (prefix + ".channel");
	String packagerName = cfg.get    (prefix + ".packager");
	String header       = cfg.get    (prefix + ".header");
	String host         = cfg.get    (prefix + ".host");
	int    port         = cfg.getInt (prefix + ".port");
        ISOChannel channel  = null;
        try {
            Class c = Class.forName(channelName);
            Class p = Class.forName(packagerName);
            if (c != null && p != null) {
		ISOPackager packager = (ISOPackager) p.newInstance();
                channel = (ISOChannel) c.newInstance();
		if (host != null)
		    channel.setHost (host, port);
                channel.setPackager(packager);
		if (logger != null)
		    channel.setLogger (logger, realm + ".channel");
		if (header != null) {
		    if (channel instanceof RawChannel) 
			((RawChannel)channel).setTPDU (
			    ISOUtil.str2bcd(header, false)
			);
		    else if (channel instanceof BASE24Channel) {
			((BASE24Channel)channel).setHeader (header.getBytes());
		    }
		}
            }
        } catch (ClassNotFoundException e) {
	    throw new ISOException ("createClientChannel", e);
        } catch (InstantiationException e) {
	    throw new ISOException ("createClientChannel", e);
        } catch (IllegalAccessException e) {
	    throw new ISOException ("createClientChannel", e);
	}
        return channel;
    }
}
