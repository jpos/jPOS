/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.metrics;

import com.sun.net.httpserver.HttpServer;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.annotation.Config;
import org.jpos.q2.QBeanSupport;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class PrometheusService extends QBeanSupport {
    @Config("port")
    private int port;
    @Config("path")
    private String path;
    @Config("status-path")
    private String statusPath;
    private HttpServer server;

    @Override
    protected void initService() throws ConfigurationException {
        try {
            final var registry =  getServer().getPrometheusMeterRegistry();
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(path, httpExchange -> {
                String response = registry.scrape();
                httpExchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4");
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            if (statusPath != null) {
                server.createContext(statusPath, httpExchange -> {
                    String response = getServer().running() ? "running\n" : "stopping\n";
                    httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
                    httpExchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                });
            }
            Thread.ofVirtual().start(server::start);
        } catch (IOException e) {
            getLog().warn(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void stopService() {
        server.stop(2);
    }

    public static Element createDescriptor (int port, String path, String statusPath) {
        return new Element("prometheus")
          .addContent(createProperty ("port", Integer.toString(port)))
          .addContent (createProperty ("path", path))
          .addContent (createProperty ("status-path", statusPath));

    }
    private static Element createProperty (String name, String value) {
        return new Element ("property")
          .setAttribute("name", name)
          .setAttribute("value", value);
    }
}
