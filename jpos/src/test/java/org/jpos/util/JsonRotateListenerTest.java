package org.jpos.util;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.log.format.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Properties;

import static org.jpos.util.LogFileTestUtils.getStringFromFile;
import static org.jpos.util.log.format.JSON.JSON_LABEL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by erlangga on 2019-10-19.
 */
public class JsonRotateListenerTest {

    private final LogRotationTestDirectory logRotationTestDirectory = new LogRotationTestDirectory();

    private static final String XML_SOURCE = "<isomsg direction=\"outgoing\">\n" +
            "              <!-- org.jpos.iso.packager.GenericPackager[cfg/packager/iso87ascii.XML_SOURCE] -->\n" +
            "              <header>49534F303036303030303636</header>\n" +
            "              <field id=\"0\" value=\"0800\"/>\n" +
            "              <field id=\"3\" value=\"990000\"/>\n" +
            "              <field id=\"7\" value=\"1019150454\"/>\n" +
            "              <field id=\"11\" value=\"000000\"/>\n" +
            "              <field id=\"70\" value=\"301\"/>\n" +
            "            </isomsg>";

    @Test
    public void testJsonLogDebug(){
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setBaseLogFormat(new JSON());
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNotNull(((DailyLogListener) dailyLogListener).p, "(DailyLogListener) dailyLogListener.p");
    }

    /*
       {"log": {"realm" : "channel","at" : "2019-10-16T09:55:20.180","lifespan" : "16 ms", "connect":"Try 0 127.0.0.1:1990  Connection refused (Connection refused)Unable to connect"}}
     */
    @Test
    public void testAddMessage() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("connect");
        logEvent.addMessage("Try 0 127.0.0.1:1990 ");
        logEvent.addMessage(" Connection refused (Connection refused)");
        logEvent.addMessage("Unable to connect");
        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    /*
        {"log": {"realm" : "org.jpos.q2.iso.QMUXCustom","at" : "2019-10-16T09:55:20.263","lifespan" : "1 ms", "info":"Mux :BCAMux_200Echo Interval :10000"}}
     */
    @Test
    public void testAddMessage2() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("info");
        logEvent.addMessage("Mux :BCAMux_200Echo Interval :10000");
        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    /*
        {"log": {"realm" : "channel/127.0.0.1:1990","at" : "2019-10-18T11:55:18.053","lifespan" : "141 ms", "receive":{ "exception" : { "name":"null","stackTrace":"java.lang.IndexOutOfBoundsException
            at java.io.BufferedInputStream.read(BufferedInputStream.java:338)
            at java.io.DataInputStream.read(DataInputStream.java:149)
            at org.jpos.iso.channel.Base24CustomChannel.getMessageLength(Base24CustomChannel.java:51)
            at org.jpos.iso.BaseChannel.receive(BaseChannel.java:712)
            at org.jpos.q2.iso.ChannelAdaptor$Receiver.run(ChannelAdaptor.java:331)
            at java.lang.Thread.run(Thread.java:748)
        "}}}}
     */
    @Test
    public void testAddMessageThrowable() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("receive");
        logEvent.addMessage(new IndexOutOfBoundsException());

        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    /*
        {"log": {"realm" : "org.jpos.q2.iso.ChannelAdaptor","at" : "2019-10-18T11:55:18.054","lifespan" : "1 ms", "warn":{ "exception" : { "name":"unexpected exception","stackTrace":"java.io.IOException: unexpected exception
            at org.jpos.iso.BaseChannel.receive(BaseChannel.java:787)
            at org.jpos.q2.iso.ChannelAdaptor$Receiver.run(ChannelAdaptor.java:331)
            at java.lang.Thread.run(Thread.java:748)
        Caused by: java.lang.IndexOutOfBoundsException
            at java.io.BufferedInputStream.read(BufferedInputStream.java:338)
            at java.io.DataInputStream.read(DataInputStream.java:149)
            at org.jpos.iso.channel.Base24CustomChannel.getMessageLength(Base24CustomChannel.java:51)
            at org.jpos.iso.BaseChannel.receive(BaseChannel.java:712)
            ... 2 more
        "},"text":"channel-receiver-BCAChannel_101-receive"}}}
     */
    @Test
    public void testAddMessageTagAndThrowable() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("warn");
        logEvent.addMessage(new IOException());
        logEvent.addMessage("channel-receiver-BCAChannel_101-receive");

        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    @Test
    public void testAddMessageAndSqlException() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("receive");
        logEvent.addMessage(new SQLException());

        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    @Test
    public void testParseXmlToJson(){
        String json = XML.toJSONObject(XML_SOURCE).toString();
        assertTrue(isJSONValid(json));
    }

    @Test
    public void testAddMessageLoggeable() throws ConfigurationException, IOException {
        SimpleMsg simpleMsg = new SimpleMsg(null,null){
            @Override
            public void dump(PrintStream p, String indent) {
                p.println(XML_SOURCE);
            }
        };

        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent();
        logEvent.addMessage(simpleMsg);

        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    @Test
    public void testAddMessageObjectArray() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        Object[] array = new Object[3];
        array[0] = "1";
        array[1] = "2";
        array[2] = "3";

        LogEvent logEvent = new LogEvent("array");
        logEvent.addMessage(array);
        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private RotateLogListener createRotateLogListenerWithIsoDateFormat(
            String logFileName,
            Properties customConfig) throws ConfigurationException {
        RotateLogListener listener = new RotateLogListener();
        Properties configuration = new Properties();
        configuration.setProperty("file", logRotationTestDirectory.getDirectory().getAbsolutePath() + "/" + logFileName);
        configuration.setProperty("copies", "10");
        configuration.setProperty("maxsize", "1000000");
        if (customConfig != null) {
            configuration.putAll(customConfig);
        }
        logRotationTestDirectory.allowNewFileCreation();
        listener.setConfiguration(new SimpleConfiguration(configuration));
        return listener;
    }

    @AfterEach
    public void cleanupLogRotateAbortsTestDir() {
        logRotationTestDirectory.delete();
    }

}
