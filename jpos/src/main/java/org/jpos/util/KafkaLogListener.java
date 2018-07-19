package org.jpos.util;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.packager.*;
import org.jpos.util.*;
import org.jpos.iso.*;
import org.jpos.q2.qbean.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.*;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
//import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
import java.sql.SQLException;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.simple.*;

/**
 * Kafka Listener
 *
 * @author andres@antoniuk.org
 */


public class KafkaLogListener implements LogListener, Configurable {
    private Configuration  cfg;
    Producer<String, String> producer;
    String topic;
    ObjectMapper objectMapper = new ObjectMapper();

    public KafkaLogListener () {
        super();
    }

    public synchronized LogEvent log (LogEvent ev) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<Object> payload;
        try {
            String[] realm = ev.getRealm().split("/", 2);
            m.put("Realm", realm[0]);
            String[] ipport = realm[1].split(":", 2);
            m.put("IP", ipport[0]);
            m.put("Port", ipport[1]);
        } catch (Exception e) {
            m.put("Realm", ev.getRealm());
        }
        m.put("Tag", ev.getTag());
        m.put("@timestamp", LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toString());

        payload = ev.getPayLoad();
        if (payload.size() > 0) {
            synchronized (payload) {
                int i = 0;
                for (Object o : payload) {
                    m.put("ElementType_" + i, o.getClass());
                    if (o instanceof String) {
                        m.put("ElementContent_" + i, o.toString());
                    }
                    else if (o instanceof ISOMsg){
                        ISOMsg msg = (ISOMsg)o;
                        ISOPackager p = msg.getPackager();
                        m.put("ISOMsgPackager_" + i, p.getDescription());
                        // Temporaly set JSONPackager
                        if (p instanceof ISOBasePackager){
                            try {
                                msg.setPackager(new JSONPackager(p));
                            }
                            catch(Exception e){
                                m.put("ISOMsgPackException_" + i, ((Throwable) e).getMessage());
                            }
                        }
                        else{
                            try {
                                msg.setPackager(new JSONPackager());
                            }
                            catch(Exception e){
                                m.put("ISOMsgPackException_" + i, ((Throwable) e).getMessage());
                            }
                        }
                        try {
                            m.put("ISOMsg_" + p.getDescription() + "_" + i, JSONValue.parse(new String(msg.pack())));
                        }
                        catch(Exception e){
                            m.put("ISOMsgString_" + i, o.toString());
                        }
                        // Restore original package
                        try {
                            msg.setPackager(p);
                        }
                        catch(Exception e){
                            m.put("ISOMsgSetPackagerException_" + i, o.toString());
                        }
                    }
                    else if (o instanceof SystemMonitor ){
                        // ignore SystemMonitor
                        assert true;
                    }
                    else if (o instanceof Throwable){
                        m.put("Exception" + i, ((Throwable) o).getMessage());
                        StringWriter errors = new StringWriter();
                        ((Throwable) o).printStackTrace(new PrintWriter(errors));
                        m.put("StackTrace_" + i,errors.toString());
                    }
                    else{
                        m.put("NonStringContent_" + i, o.toString());
                    }
                    i = i + 1;
                }
            }
        }
        else if (payload.size() == 0){
            m.put("EventDump", ev.toString());
        }

        try {
            producer.send(new ProducerRecord<String, String>(topic, objectMapper.writeValueAsString(m)));
        }
        catch (JsonProcessingException e) {
            producer.send(new ProducerRecord<String, String>(topic, e.toString()));;
        }
        return ev;
    }

    public void setConfiguration (Configuration cfg)
            throws ConfigurationException
    {
        this.cfg = cfg;
        try {
            Properties props = new Properties();
            topic = cfg.get ("topic", "test");

            for (String propname : cfg.keySet()){
                if (propname.startsWith("KafkaProducer.")) {
                    String kafkaprop = propname.replace("KafkaProducer.", "");
                    try {
                        props.put(kafkaprop, cfg.getInt (propname));
                    } catch (NumberFormatException e) {
                        props.put(kafkaprop, cfg.get (propname));
                    }
                }
            }
            producer = new KafkaProducer<>(props);
            producer.send(new ProducerRecord<String, String>(topic, "{\"message\":\"KafkaLogListener started\"}"));
        }
        catch (Exception e) {
            throw new ConfigurationException (e);
        }
    }

    public synchronized void close() {
        producer.close();
    }
}