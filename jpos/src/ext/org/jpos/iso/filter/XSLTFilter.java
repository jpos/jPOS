package org.jpos.iso.filter;

import java.io.*;
import org.jpos.iso.*;
import org.xml.sax.SAXException;
import org.apache.xalan.xpath.XPathException;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xpath.XString;


import org.jpos.util.LogEvent;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOFilter.VetoException;

/**
 * Implements ISOFilter by means of XSL-Transformations
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class XSLTFilter implements ISOFilter, Configurable {
    boolean reread;
    String xsltfile;
    XSLTProcessor processor;
    XSLTInputSource xslt;
    XMLPackager packager;

    /**
     * Default noargs constructor
     * @throws SAXException
     * @throws ISOException
     */
    public XSLTFilter () throws ISOException, SAXException  {
	super();
	packager = new XMLPackager();
	processor = XSLTProcessorFactory.getProcessor();
        xsltfile  = null;
	reread    = true;
    }

    /**
     * @param xsltfile XSL Transformation file
     * @param reread true if you want XSLT file re-read from disk
     * @throws SAXException
     * @throws ISOException
     */
    public XSLTFilter (String xsltfile, boolean reread) 
	throws ISOException, SAXException
    {
	this();
	this.xsltfile = xsltfile;
	this.reread   = reread;
	xslt = new XSLTInputSource(xsltfile);
    }

   /**
    * <ul>
    *  <li>xsltfile - source XSL-T file
    *  <li>reread   - something != "no" will re-read source file
    * </ul>
    * @param cfg new ConfigurationFile
    */
    public void setConfiguration (Configuration cfg) 
	throws ConfigurationException
    {
	try {
	    xslt     = new XSLTInputSource(cfg.get("xsltfile"));
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

	    if (reread || xslt == null)
		xslt = new XSLTInputSource(xsltfile);

	    processor.process(
		new XSLTInputSource(new ByteArrayInputStream (m.pack())),
		xslt, 
		new XSLTResultTarget(os)
	    );
	    m.unpack (os.toByteArray());
	} catch (SAXException e) {
	    throw new VetoException(e);
	} catch (ISOException e) {
	    throw new VetoException(e);
	}
	return m;
    }
}
