/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
package org.jpos.iso.packager;

import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * packs/unpacks ISOMsgs into XML representation
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 */
@SuppressWarnings("unchecked")
public class JSONPackager implements ISOPackager, LogSource {

    protected Logger logger = null;
    protected String realm = null;
    private ByteArrayOutputStream out;
    private PrintStream p;

    public static final String ISOMSG_TAG = "isomsg";
    public static final String ISOFIELD_TAG = "field";
    public static final String ID_ATTR = "id";
    public static final String VALUE_ATTR = "value";
    public static final String TYPE_ATTR = "type";
    public static final String TYPE_BINARY = "binary";
    public static final String TYPE_BITMAP = "bitmap";
    public static final String HEADER_TAG = "header";
    public static final String ENCODING_ATTR = "encoding";
    public static final String ASCII_ENCODING = "ascii";

    public JSONPackager() throws ISOException {
        super();
        out = new ByteArrayOutputStream();
        try {
            p = new PrintStream(out, false, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
            // utf-8 is a supported encoding
        }
    }

    public ISOMsg parseMessage(String jsonString) throws ISOException {
        JSONObject got = new JSONObject(jsonString);

        ISOMsg msg = new ISOMsg();
        msg.setMTI(got.getString("mti"));

        JSONObject jsonObject = (JSONObject) got.get("data");
        populateMessage(msg, jsonObject, "");
        return msg;
    }

    public static void populateMessage(ISOMsg msg, JSONObject jsonObject, String prefix) throws ISOException {
        Iterator<?> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (jsonObject.get(key) instanceof JSONObject) {
                JSONObject data = (JSONObject) ((JSONObject) jsonObject.get(key)).get("data");
                populateMessage(msg, data, key + ".");
            } else {
                String val = "";
                if (!jsonObject.isNull(key)) {
                    val = jsonObject.get(key).toString();
                }
                if ((prefix + key).equals("11") && val.length() > 6) {
                    val = val.substring(val.length() - 6);
                }
                msg.set(prefix + key, val);
            }
        }
    }

    public JSONObject toJSON(ISOMsg msg) throws ISOException {
        JSONObject q = new JSONObject();
        JSONObject o = new JSONObject();
        if (msg == null) {
            if (!(msg instanceof ISOMsg)) {
                throw new ISOException("Can't call packager on non Composite");
            }
        } else {
            Map children = msg.getChildren();
            children.keySet().forEach((key) -> {
                //            org.jpos.iso.ISOField
                if (key.toString().equals("0")) {
                    q.put("mti", ((ISOField) children.get(key)).getValue().toString());
                } else if (children.get(key) instanceof ISOField) {
                    o.put(key.toString(), ((ISOField) children.get(key)).getValue().toString());
                } else if (children.get(key) instanceof ISOBitMap) {
                    q.put("bitmap", new JSONArray(((ISOBitMap) children.get(key)).getValue().toString().replace("{", "[").replace("}", "]")));
                } else if (children.get(key) instanceof ISOMsg) {
                    try {
                        o.put(key.toString(), toJSON((ISOMsg) children.get(key)));
                    } catch (ISOException ignored) {

                    }
                } else {
                    o.put(key.toString(), children.get(key).getClass());
                }
            });
        }
        q.put("data", o);
        return q;
    }

    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        LogEvent evt = new LogEvent(this, "pack");
        try {
            if (!(c instanceof ISOMsg)) {
                throw new ISOException("cannot pack " + c.getClass());
            }
            ISOMsg m = (ISOMsg) c;
            byte[] b;
            synchronized (this) {
                m.setDirection(0);  // avoid "direction=xxxxxx" in XML msg
                p.println(toJSON(m).toString());
                b = out.toByteArray();
                out.reset();
            }
            if (logger != null) {
                evt.addMessage(m);
            }
            return b;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } finally {
            Logger.log(evt);
        }
    }

    @Override
    public synchronized int unpack(ISOComponent c, byte[] b)
            throws ISOException {
        LogEvent evt = new LogEvent(this, "unpack");
        try {
            if (!(c instanceof ISOMsg)) {
                throw new ISOException("Can't call packager on non Composite");
            }

            ISOMsg m1 = parseMessage(new String(b));

            if (m1 == null) {
                throw new ISOException("error parsing");
            }

            ISOMsg m = (ISOMsg) c;
            m.merge(m1);
            m.setHeader(m1.getHeader());

            if (logger != null) {
                evt.addMessage(m);
            }
            return b.length;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } finally {
            Logger.log(evt);
        }
    }

    @Override
    public synchronized void unpack(ISOComponent c, InputStream in)
            throws ISOException, IOException {
        LogEvent evt = new LogEvent(this, "unpack");
        try {
            if (!(c instanceof ISOMsg)) {
                throw new ISOException("Can't call packager on non Composite");
            }

            BufferedReader inr = new BufferedReader(new InputStreamReader(in));
            String clientJson = inr.readLine();
            ISOMsg m1 = parseMessage(clientJson);

            if (m1 == null) {
                throw new ISOException("error parsing");
            }

            ISOMsg m = (ISOMsg) c;
            m.merge(m1);
            m.setHeader(m1.getHeader());

            if (logger != null) {
                evt.addMessage(m);
            }
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } finally {
            Logger.log(evt);
        }
    }

    public void startElement(String ns, String name, String qName, Attributes atts)
            throws SAXException {
        // do nothing; 
    }

    public void characters(char ch[], int start, int length) {
        // do nothing
    }

    public void endElement(String ns, String name, String qname)
            throws SAXException {
        // do nothing
    }

    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return "<notavailable/>";
    }

    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }

    public Logger getLogger() {
        return logger;
    }

    public ISOMsg createISOMsg() {
        return new ISOMsg();
    }

    public String getDescription() {
        return getClass().getName();
    }

}
