package org.jpos.apps.qsp;

import java.io.IOException;
import org.apache.xerces.parsers.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.ErrorHandler;

import org.jpos.util.SimpleLogListener;
import org.jpos.util.SystemMonitor;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogProducer;
import org.jpos.apps.qsp.QSPConfigurator.ConfigurationException;


/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see <a href="http://www.cebik.com/qsig.html">QSP</a>
 */

public class QSP implements ErrorHandler, LogProducer {
    Document config;
    Logger logger;
    String realm;

    public QSP () {
	super();
	setLogger (new Logger(), "qsp");
	logger.setName ("qsp");
    }
    public void setConfig (Document config) {
	this.config = config;
    }

    public void warning (SAXParseException e) throws SAXException {
	Logger.log (new LogEvent (this, "warning", e));
	throw e;
    }
    public void error (SAXParseException e) throws SAXException {
	Logger.log (new LogEvent (this, "error", e));
	throw e;
    }

    public void fatalError (SAXParseException e) throws SAXException {
	Logger.log (new LogEvent (this, "fatalError", e));
	throw e;
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger () {
	return logger;
    }
    public void configure (String tagname) throws ConfigurationException {
	QSPConfigurator configurator = QSPConfiguratorFactory.create (tagname);
	NodeList nodes = config.getElementsByTagName (tagname);
	for (int i=0; i<nodes.getLength(); i++) 
	    configurator.config (this, nodes.item(i));
    }
    public static void main (String args[]) {
	DOMParser parser = new DOMParser();
	QSP qsp = new QSP();
	// qsp.getLogger().addListener (new SimpleLogListener(System.out));
	try {
	    parser.setFeature("http://xml.org/sax/features/validation", true);
	    parser.setErrorHandler (qsp);
	    parser.parse (args[0]);
	    qsp.setConfig (parser.getDocument());
	    qsp.configure ("logger");
	    qsp.configure ("log-listener");
	    qsp.configure ("channel");
	    qsp.configure ("filter");
	    qsp.configure ("mux");
	    qsp.configure ("request-listener");
	    qsp.configure ("task");
	    new SystemMonitor (3600000, qsp.getLogger(), "monitor");
	} catch (IOException e) {
	    Logger.log (new LogEvent (qsp, "error", e));
	    System.out.println (e);
	} catch (SAXException e) {
	    Logger.log (new LogEvent (qsp, "error", e));
	    System.out.println (e);
	} catch (ConfigurationException e) {
	    Logger.log (new LogEvent (qsp, "error", e));
	    System.out.println (e);
	}
    }
}
