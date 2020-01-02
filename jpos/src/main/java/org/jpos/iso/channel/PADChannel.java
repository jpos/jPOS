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

package org.jpos.iso.channel;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;

/**
 * Implements an ISOChannel suitable to be used to connect to an X.25 PAD.
 *
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class PADChannel extends BaseChannel {
    BufferedReader reader = null;
    long delay = 0L;
    /**
     * No-args constructor
     */
    public PADChannel () {
        super();
    }
    /**
     * Constructs client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public PADChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public PADChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public PADChannel (ISOPackager p, ServerSocket serverSocket)
        throws IOException
    {
        super (p, serverSocket);
        if (delay > 0L)
            ISOUtil.sleep(delay);
    }

    @Override
    public ISOMsg receive() throws IOException, ISOException {
        byte[] header = null;
        ISOMsg m = createISOMsg();
        m.setPackager (packager);
        m.setSource (this);
        int hLen = getHeaderLength();
        LogEvent evt = new LogEvent (this, "receive");
        try {
            synchronized (serverInLock) {
                if (hLen > 0) {
                    header = new byte [hLen];
                    serverIn.readFully(header);
                }
                m.unpack (serverIn);
            }
            m.setHeader (header);
            m.setDirection(ISOMsg.INCOMING);
            evt.addMessage (m);
            m = applyIncomingFilters (m, evt);
            m.setDirection(ISOMsg.INCOMING);
            cnt[RX]++;
            setChanged();
            notifyObservers(m);
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } catch (EOFException e) {
            evt.addMessage ("<peer-disconnect/>");
            throw e;
        } catch (InterruptedIOException e) {
            evt.addMessage ("<io-timeout/>");
            throw e;
        } catch (IOException e) {
            if (usable)
                evt.addMessage (e);
            throw e;
        } catch (Exception e) {
            evt.addMessage (e);
            throw new ISOException ("unexpected exception", e);
        } finally {
            Logger.log (evt);
        }
        return m;
    }
    @Override
    public void send (ISOMsg m) throws IOException, ISOException {
        super.send(m);
    }
    @Override
    public void setConfiguration (Configuration cfg)
            throws ConfigurationException {
        super.setConfiguration(cfg);
        delay = cfg.getLong("delay", 0L);
    }

    /**
     * @param header Hex representation of header
     */
    public void setHeader (String header) {
        super.setHeader (
            ISOUtil.hex2byte (header.getBytes(), 0, header.length() / 2)
        );
    }
}
