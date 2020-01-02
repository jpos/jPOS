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

package org.jpos.iso.filter;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.LogEvent;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
            reread   = s == null || s.equals ("no");
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
