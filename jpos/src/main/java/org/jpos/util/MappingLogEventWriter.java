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
import org.jpos.util.function.ByteArrayMapper;
import org.jpos.util.function.LogEventMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * LogEventWriter that can be configured with event and output mappers to modify
 * the events before writing to output stream and modify the output stream before writing
 * to the final destination respectfully.
 *
 * Example configuration: <br>
 *     <pre>
 *         <writer class="org.jpos.util.MappingLogEventWriter">
 *             <event-mapper class="...">
 *                 <property....  />
 *             </event-mapper>
 *             <event-mapper class="..."/>
 *             <output-mapper class="...">
 *                 <property.... />
 *             </output-mapper>
 *             <output-mapper class="..."/>
 *         </writer>
 *     </pre>
 *
 * @author Alwyn Schoeman
 * @since 2.1.4
 */
public class MappingLogEventWriter extends BaseLogEventWriter implements XmlConfigurable {
    List<LogEventMapper> eventMappers;
    List<ByteArrayMapper> outputMappers;
    ByteArrayOutputStream captureOutputStream;
    PrintStream capturePrintStream;

    @Override
    public void setPrintStream(PrintStream p) {
        super.setPrintStream(p);
        if (p != null && capturePrintStream == null) {
            configureCaptureStreams();
        }
    }

    @Override
    public synchronized void close() {
        if (capturePrintStream != null) {
            capturePrintStream.close();
            capturePrintStream = null;
            captureOutputStream = null;
        }
        super.close();
    }

    @Override
    public void write(LogEvent ev) {
        ev = mapEvents(ev);
        if (capturePrintStream != null) {
            writeToCaptureStream(ev);
            try {
                byte[] output = mapOutput(captureOutputStream.toByteArray());
                p.write(output);
            } catch (IOException e) {
                e.printStackTrace(p);
            } finally {
                p.flush();
                captureOutputStream.reset();
            }
        } else {
            delegateWriteToSuper(ev);
        }
    }

    @Override
    public void setConfiguration(Element e) throws ConfigurationException {
        configureEventMappers(e);
        configureOutputMappers(e);
    }

    protected void configureCaptureStreams() {
        if (outputMappers != null && !outputMappers.isEmpty()) {
            captureOutputStream = new ByteArrayOutputStream();
            capturePrintStream = new PrintStream(captureOutputStream);
        }
    }

    protected void configureEventMappers(Element e) throws ConfigurationException {
        List<Element> eventMappers = e.getChildren("event-mapper");
        LogEventMapper mapper;
        for (Element em : eventMappers) {
            String clazz = em.getAttributeValue("class");
            if (clazz != null) {
                try {
                    mapper = (LogEventMapper) Class.forName(clazz).newInstance();
                } catch (Exception ex) {
                    throw new ConfigurationException(ex);
                }
                if (mapper != null) {
                    if (mapper instanceof Configurable) {
                        SimpleConfigurationFactory factory = new SimpleConfigurationFactory();
                        ((Configurable) mapper).setConfiguration(factory.getConfiguration(em));
                    }
                    if (mapper instanceof XmlConfigurable) {
                        ((XmlConfigurable) mapper).setConfiguration(em);
                    }
                    if (this.eventMappers == null) {
                        this.eventMappers = new ArrayList<>();
                    }
                    this.eventMappers.add(mapper);
                }
            }
        }
    }

    protected void configureOutputMappers(Element e) throws ConfigurationException {
        List<Element> outputMappers = e.getChildren("output-mapper");
        ByteArrayMapper mapper;
        for (Element em : outputMappers) {
            String clazz = em.getAttributeValue("class");
            if (clazz != null) {
                try {
                    mapper = (ByteArrayMapper) Class.forName(clazz).newInstance();
                } catch (Exception ex) {
                    throw new ConfigurationException(ex);
                }
                if (mapper != null) {
                    if (mapper instanceof Configurable) {
                        SimpleConfigurationFactory factory = new SimpleConfigurationFactory();
                        ((Configurable) mapper).setConfiguration(factory.getConfiguration(em));
                    }
                    if (mapper instanceof XmlConfigurable) {
                        ((XmlConfigurable) mapper).setConfiguration(em);
                    }
                    if (this.outputMappers == null) {
                        this.outputMappers = new ArrayList<>();
                    }
                    this.outputMappers.add(mapper);
                }
            }
        }
    }

    protected LogEvent mapEvents(LogEvent ev) {
        if (eventMappers != null) {
            for (LogEventMapper mapper : eventMappers) {
                ev = mapper.apply(ev);
            }
        }
        return ev;
    }

    protected byte[] mapOutput(byte[] output) {
        if (outputMappers != null) {
            for (ByteArrayMapper mapper : outputMappers) {
                output = mapper.apply(output);
            }
        }
        return output;
    }

    /**
     * This method exists and is used so that we can verify the order of instructions
     * during a call to write in unit tests.
     *
     * @param ev LogEvent to write.
     */
    protected void delegateWriteToSuper(LogEvent ev) {
        super.write(ev);
    }

    /**
     * Write to capture print stream when defined.
     * @param ev LogEvent to write.
     */
    protected void writeToCaptureStream(LogEvent ev) {
        if (capturePrintStream != null && ev != null) {
            ev.dump(capturePrintStream, "");
            capturePrintStream.flush();
        }
    }
}
