package org.jpos.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fest.assertions.Assert;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.log.format.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.jpos.util.LogFileTestUtils.getStringFromFile;
import static org.jpos.util.log.format.JSON.JSON_LABEL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by erlangga on 2019-10-19.
 */
public class JsonRotateListenerTest {

    private final LogRotationTestDirectory logRotationTestDirectory = new LogRotationTestDirectory();

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
        assertTrue(isJSONValidJackson(archivedLogFile1Contents));
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
        assertTrue(isJSONValidJackson(archivedLogFile1Contents));
    }

    private boolean isJSONValidJackson(String jsonInString ) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private RotateLogListener createRotateLogListenerWithIsoDateFormat(String logFileName) throws ConfigurationException {
        return createRotateLogListenerWithIsoDateFormat(logFileName, null);
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
