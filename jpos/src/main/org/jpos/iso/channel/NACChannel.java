package org.jpos.iso.channel;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.jpos.iso.*;

/**
 * Talks with TCP based NACs
 * Sends [LEN][TPDU][ISOMSG]
 * (len=2 bytes network byte order)
 *
 * @author Alejandro P. Revilla
 * @version $Revision$ $Date$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class NACChannel extends BaseChannel {
    byte[] TPDU;

    /**
     * Public constructor 
     */
    public NACChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @param TPDU  an optional raw header (i.e. TPDU)
     * @see ISOPackager
     */
    public NACChannel (String host, int port, ISOPackager p, byte[] TPDU) {
        super(host, port, p);
        this.TPDU = TPDU;
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @param TPDU  an optional raw header (i.e. TPDU)
     * @exception IOException
     * @see ISOPackager
     */
    public NACChannel (ISOPackager p, byte[] TPDU) throws IOException {
        super(p);
        this.TPDU = TPDU;
    }
    /**
     * constructs server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param TPDU  an optional raw header (i.e. TPDU)
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public NACChannel (ISOPackager p, byte[] TPDU, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
        this.TPDU = TPDU;
    }
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (len >> 8);
        serverOut.write (len);
    }
    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[2];
        if (serverIn.read(b,0,2) != 2)
            throw new ISOException("error reading message length");
        return (int) (
            ((((int)b[0])&0xFF) << 8) | 
            (((int)b[1])&0xFF));
    }
    protected void sendMessageHeader(ISOMsg m, int len) throws IOException { 
        if (TPDU != null) 
            serverOut.write(TPDU);
    }
    protected int getHeaderLength() { 
        return TPDU != null ? TPDU.length : 0;
    }
    public void setTPDU (byte[] TPDU) {
	this.TPDU = TPDU;
    }

    /**
     * New QSP compatible signature (see QSP's ConfigChannel)
     * @param header String as seen by QSP
     */
    public void setHeader (String header) {
	setTPDU (ISOUtil.str2bcd(header, false));
    }
}
