package org.jpos.apps.jetty;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.util.SimpleLogSource;

/**
 * WebServer is a QSP task that can be used to launch Jetty
 * WebServer in the same JVM as QSP.
 *
 * <pre>
 * Sample QSP config:
 *  &lt;task name="httpserver"
 *      class="uy.com.cs.sft.server.WebServer"&gt;
 *   &lt;property name="config" value="cfg/jetty.xml" /&gt;
 *  &lt;/task&gt;
 * </pre>
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro Revilla</a>
 * @version $Revision$ $Date$
 */
public class WebServer 
    extends SimpleLogSource 
    implements Runnable, Configurable
{
    Configuration cfg;

    public void run() {
        org.mortbay.jetty.Server.main (cfg.getAll ("config"));
    }
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }
}

