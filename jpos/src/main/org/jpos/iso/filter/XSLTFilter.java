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

package org.jpos.iso.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.LogEvent;

/**
 * Implements ISOFilter by means of XSL-Transformations
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class XSLTFilter implements ISOFilter, Configurable {
    boolean reread;
    String xsltfile;
    TransformerFactory tfactory;
    Transformer transformer;
    XMLPackager packager;

    /**
     * Default noargs constructor
     * @throws ISOException
     */
    public XSLTFilter () throws ISOException {
        super();
        packager    = new XMLPackager();
        tfactory    = TransformerFactory.newInstance();
        transformer = null;
        reread      = true;
    }

    /**
     * @param xsltfile XSL Transformation file
     * @param reread true if you want XSLT file re-read from disk
     * @throws ISOException
     */
    public XSLTFilter (String xsltfile, boolean reread) 
        throws ISOException
    {
        this();
        try {
            this.xsltfile    = xsltfile;
            this.reread      = reread;
            this.transformer = 
                tfactory.newTransformer(new StreamSource(xsltfile));
        } catch (TransformerConfigurationException e) {
            throw new ISOException (e);
        }

    }

   /**
    * configure filter.
    *
    * <ul>
    *  <li>xsltfile - source XSL-T file
    *  <li>reread   - something != "no" will re-read source file
    * </ul>
    *
    * @param cfg new ConfigurationFile
    */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        try {
            transformer = tfactory.newTransformer(
                new StreamSource(cfg.get("xsltfile"))
            );
            String s = cfg.get ("reread");
            reread   =  (s == null || s.equals ("no"));
        } catch (Exception e) {
            throw new ConfigurationException (e);
        }
    }

    /**
     * @param channel current ISOChannel instance
     * @param m ISOMsg to filter
     * @param evt LogEvent
     * @return an ISOMsg (possibly parameter m)
     * @throws VetoException
     */
    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
        throws VetoException
    {
        try {
            m.setPackager (packager);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            if (reread || transformer == null)
                transformer = tfactory.newTransformer(
                    new StreamSource(xsltfile)
                );
            transformer.transform (
                new StreamSource(new ByteArrayInputStream (m.pack())),
                new StreamResult(os)
            );
            m.unpack (os.toByteArray());
        } catch (Exception e) {
            throw new VetoException(e);
        }
        return m;
    }
}
