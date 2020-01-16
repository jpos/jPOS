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

package org.jpos.util;

import org.jdom2.Element;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.q2.SimpleConfigurationFactory;

import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.core.Configurable
 * @since jPOS 1.2
 */
public class SimpleLogListener implements LogListener, XmlConfigurable, Consumer<LogEventWriter> {
    LogEventWriter writer = null;
    PrintStream p;

    public SimpleLogListener () {
        super();
        p = System.out;
    }
    public SimpleLogListener (PrintStream p) {
        this ();
        setPrintStream (p);
    }
    public synchronized void setPrintStream (PrintStream p) {
        this.p = p;
        if (writer != null) {
            writer.setPrintStream(p);
        }
    }
    public synchronized void close() {
        // writer either wraps or use same PrintStream
        if (writer != null) {
            writer.close();
            p = null;
        }
        if (p != null) {
            p.close();
            p = null;
        }
    }
    public synchronized LogEvent log (LogEvent ev) {
        if (writer != null) {
            writer.write(ev);
        } else {
            if (p != null) {
                ev.dump(p, "");
                p.flush();
            }
        }
        return ev;
    }

    @Override
    public void accept(LogEventWriter writer) {
        if (p != null) {
            writer.setPrintStream(p);
        }
        this.writer = writer;
    }

    @Override
    public void setConfiguration(Element e) throws ConfigurationException {
        Element ew = e.getChild("writer");
        LogEventWriter writer;
        if (ew != null) {
            String clazz = ew.getAttributeValue("class");
            if (clazz != null) {
                try {
                    writer = (LogEventWriter) Class.forName(clazz).newInstance();
                } catch (Exception ex) {
                    throw new ConfigurationException(ex);
                }
                if (writer != null) {
                    if (writer instanceof Configurable) {
                        SimpleConfigurationFactory factory = new SimpleConfigurationFactory();
                        ((Configurable) writer).setConfiguration(factory.getConfiguration(ew));
                    }
                    if (writer instanceof XmlConfigurable) {
                        ((XmlConfigurable) writer).setConfiguration(ew);
                    }
                    accept(writer);
                }
            } else {
                throw new ConfigurationException("The writer configuration requires a class attribute");
            }
        }
    }
}

