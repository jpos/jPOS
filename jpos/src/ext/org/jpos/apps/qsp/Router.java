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

package org.jpos.apps.qsp;
import java.io.IOException;
import java.util.Hashtable;

import org.jpos.core.ConfigurationException;
import org.jpos.core.NodeConfigurable;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequest;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.util.JepUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * QSP Router implements ISORequestListener
 * and forward all incoming messages to the given
 * destination MUX according to the conditions, handling back responses
 *
 * @author <a href="mailto:tzymail@163.com">Zhiyu Tang</a>
 * @version $Revision$ $Date$
 * @see org.jpos.iso.ISORequestListener
 */
public class Router
    implements ISORequestListener, LogSource, NodeConfigurable
{
  Logger logger;
  String realm;
  String condition[] = null;
  String type[] = null;
  int[] timeout = null;
  Hashtable dest = null;
  boolean[] bounce = null;
  int condNum;

  ThreadPool pool;

  public Router () {
    super();
    pool = new ThreadPool (1, 100);
    dest = new Hashtable();
  }

  public void setLogger (Logger logger, String realm) {
    this.logger = logger;
    this.realm  = realm;
  }

  public String getRealm () {
    return realm;
  }

  public Logger getLogger() {
    return logger;
  }

   /**
    * Destination can be a Channel or a MUX. If Destination is a Channel
    * then timeout applies (used on ISORequest to get a Response).
    * <ul>
    * <li>destination-mux
    * <li>destination-channel
    * <li>timeout
    * <li>bounce
    * </ul>
    * @param node Configuration
    */
  public void setConfiguration (Node node)
      throws ConfigurationException
  {
    NodeList nodes = node.getChildNodes();
    condNum = nodes.getLength();
    int j = 0;
    for( int i=0 ; i < condNum; i++ ){
      if (nodes.item(i).getNodeName().equals ("router"))
        j++;
    }

    condition = new String[j];
    type = new String[j];
    timeout = new int[j];
    bounce = new boolean[j];
    j = 0;
    for( int i=0 ; i < condNum; i++ ){
      if (nodes.item(i).getNodeName().equals ("router")) {
        condition[j] = nodes.item(i).getAttributes().
               getNamedItem ("switch").getNodeValue();
        type[j] = nodes.item(i).getAttributes().
           getNamedItem ("type").getNodeValue();
        String destination = nodes.item(i).getAttributes().
                      getNamedItem ("destination").getNodeValue();
        Node attrnode = nodes.item(i).getAttributes().
                        getNamedItem ("timeout");
        if( attrnode != null ){
          timeout[j] = Integer.parseInt( attrnode.getNodeValue() );
        }
        else
          timeout[j] = 0;
        attrnode =  nodes.item(i).getAttributes().getNamedItem ("bounce");
        if( attrnode != null ){
          if( attrnode.getNodeValue().equals("true") )
            bounce[j] = true;
        }else
          bounce[j] = false;
        try {
          dest.put( String.valueOf(j) ,
                    NameRegistrar.get (type[j]+"."+destination));
        } catch (NotFoundException e) {
          throw new ConfigurationException (e);
        }
        j++;
      }
    }
    condNum = j;
  }

    /**
     * hook used to optional bounce an unanswered message
     * to its source channel
     * @param s message source
     * @param m unanswered message
     * @exception ISOException
     * @exception IOException
     */
  protected void processNullResponse (ISOSource s, ISOMsg m,
                                      LogEvent evt,boolean bounce)
      throws ISOException, IOException
  {
    if ( bounce ) {
      ISOMsg c = (ISOMsg) m.clone();
      c.setResponseMTI();
      if (c.hasField (39))
        c.unset (39);
        s.send (c);
        evt.addMessage ("<bounced/>");
    } else
      evt.addMessage ("<null-response/>");
  }


  private int getDestination( LogEvent evt , ISOMsg m  , JepUtil jeputil)
      throws ISOException
  {
    for( int i = 0 ; i < condNum ; i++ )
    {
       if( jeputil.getResultBoolean(m , condition[i]) )
         return i;
    }
    return -1;
  }

  protected class Process implements Runnable {
    ISOSource source;
    ISOMsg m;
    JepUtil jeputil;

    Process (ISOSource source, ISOMsg m) {
      super();
      this.source = source;
      this.m = m;
      jeputil = new JepUtil();
    }
    
    public void run (){
      LogEvent evt = new LogEvent ( Router.this,
                "Router");
      try {
        ISOMsg c = (ISOMsg) m.clone();
        evt.addMessage (c);
        int direction = getDestination( evt, c , jeputil );
        if( direction == -1 ) return;
        String key = String.valueOf( direction );
        Object destobj = dest.get(key);

        if ( type[direction].equals ("mux") ) {
            ISOMUX destMux = (ISOMUX)( destobj );
            if ( timeout[direction] > 0) {
                ISOMsg response = null;
                if ( destMux.isConnected()) {
                    ISORequest req = new ISORequest (c);
                    destMux.queue (req);
                    evt.addMessage ("<queued/>");
                    response = req.getResponse (timeout[direction]);
                } else
                    evt.addMessage ("<mux-not-connected/>");
                if (response != null) {
                    evt.addMessage ("<got-response/>");
                    evt.addMessage (response);
                    response.setHeader (c.getISOHeader());
                    source.send(response);
                } else {
                    processNullResponse (source, m, evt, bounce[direction]);
                }
            } else {
                        evt.addMessage ("<sent-through-mux/>");
                        destMux.send (c);
                    }
                } else if (type[direction].equals ("channel")) {
                    evt.addMessage ("<sent-to-channel/>");
                    ISOChannel destChannel = (ISOChannel)( destobj );
                    destChannel.send (c);
                }
            } catch (ISOException e) {
                evt.addMessage (e);
            } catch (IOException e) {
                evt.addMessage (e);
            }
            Logger.log (evt);
        }
    }

    public boolean process (ISOSource source, ISOMsg m) {
        pool.execute (new Process (source, m));
        return true;
    }
}
