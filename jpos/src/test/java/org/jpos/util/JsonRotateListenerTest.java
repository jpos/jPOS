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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String XML_TAG_PATTERN = "(?s).*(<(\\w+)[^>]*>.*</\\2>|<(\\w+)[^>]*/>).*";
    private static final String STACK_TRACE_TAG_PATTERN = "(^\\d+\\) .+)|(^.+Exception: .+)|(^\\s+at .+)|(^\\s+... \\d+ more)|(^\\s*Caused by:.+)";
    private static final Pattern STACK_TRACE_REGEX = Pattern.compile(STACK_TRACE_TAG_PATTERN, Pattern.MULTILINE);

    private static final String JSON_TAG_PATTERN = "\\{(?:[^{}]|(\\{(?:[^{}]|(\\{[^{}]*\\}))*\\}))*\\}";
    private static final Pattern JSON_REGEX = Pattern.compile(JSON_TAG_PATTERN);

    @Test
    public void testJsonLogDebug(){
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.setBaseLogFormat(new JSON());
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNotNull(((DailyLogListener) dailyLogListener).p, "(DailyLogListener) dailyLogListener.p");
    }

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

    @Test
    public void testAddMessage2() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("info");
        logEvent.addMessage("Mux :Mux_200Echo Interval :10000");
        listener.log(logEvent);

        // when: a rotation is executed
        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

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

    @Test
    public void testAddMessageTagAndThrowable() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("warn");
        logEvent.addMessage(new IOException());
        logEvent.addMessage("channel-receiver-Channel_101-receive");

        listener.log(logEvent);

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

    @Test
    public void testAddMessageUnpack() throws ConfigurationException, IOException {
        String unpack = "08002020010000870008450000000282062030303030303233350046DF9003084341383836363832DF900405312E302E35DF90080B3030303031363636363635DF90170630303030303601297B22706F705F6964223A2232626332633035332D366261652D343564642D616630352D666565363932663266353730222C2273617564616761725F6964223A2247373035353633363431222C227472616E73616374696F6E5F6964223A2232376365303534662D70732D6E766E642D396238322D4B626F623237494C313932227D0010FFFF98010003388003A00194600070000002003038458020E5901C000000000000002283099933001558200958120901062000376019002000008773D2204120845151023500003030303030323335303035303030373530333432202020474F4B414E4120544550414E204A616B617274612053656C61744A414B494420202020202020202000154130303030303030303030303633330010FFFF98010003388003A003606195A424FD3AF33C000631303030313100154A414B415254412053454C4154414E0006313030303132\n" +
                "    <bitmap>{3, 11, 24, 41, 46, 47, 48, 61}</bitmap>\n" +
                "    <unpack fld=\"3\" packager=\"org.jpos.iso.IFB_NUMERIC\">\n" +
                "      <value>450000</value>\n" +
                "    </unpack>\n" +
                "    <unpack fld=\"11\" packager=\"org.jpos.iso.IFB_NUMERIC\">\n" +
                "      <value>000282</value>\n" +
                "    </unpack>\n" +
                "    <unpack fld=\"24\" packager=\"org.jpos.iso.IFB_NUMERIC\">\n" +
                "      <value>620</value>\n" +
                "    </unpack>\n" +
                "    <unpack fld=\"41\" packager=\"org.jpos.iso.IF_CHAR\">\n" +
                "      <value>00000235</value>\n" +
                "    </unpack>\n" +
                "    <unpack fld=\"46\" packager=\"org.jpos.iso.IFB_LLLBINARY\">\n" +
                "      <value type='binary'>DF9003084341383836363832DF900405312E302E35DF90080B3030303031363636363635DF901706303030303036</value>\n" +
                "    </unpack>\n" +
                "    <unpack fld=\"47\" packager=\"org.jpos.iso.IFB_LLLBINARY\">\n" +
                "      <value type='binary'>7B22706F705F6964223A2232626332633035332D366261652D343564642D616630352D666565363932663266353730222C2273617564616761725F6964223A2247373035353633363431222C227472616E73616374696F6E5F6964223A2232376365303534662D70732D6E766E642D396238322D4B626F623237494C313932227D</value>\n" +
                "    </unpack>\n" +
                "    <unpack fld=\"48\" packager=\"org.jpos.iso.IFB_LLLBINARY\">\n" +
                "      <value type='binary'>FFFF98010003388003A0</value>\n" +
                "    </unpack>\n" +
                "    <unpack fld=\"61\" packager=\"org.jpos.iso.IFB_LLLBINARY\">\n" +
                "      <value type='binary'>600070000002003038458020E5901C000000000000002283099933001558200958120901062000376019002000008773D2204120845151023500003030303030323335303035303030373530333432202020474F4B414E4120544550414E204A616B617274612053656C61744A414B494420202020202020202000154130303030303030303030303633330010FFFF98010003388003A003606195A424FD3AF33C000631303030313100154A414B415254412053454C4154414E0006313030303132</value>\n" +
                "    </unpack>";

        Pattern regex = Pattern.compile(XML_TAG_PATTERN);
        Matcher regexMatcher = regex.matcher(unpack);
        assertTrue(regexMatcher.find());
        assertTrue(regexMatcher.matches());

        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("unpack");
        logEvent.addMessage(unpack);
        listener.log(logEvent);

        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    @Test
    public void testAddMessageUnpack2() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("unpack");
        logEvent.addMessage("08002020010000870008450000000282062030303030303233350046DF9003084341383836363832DF900405312E302E35DF90080B3030303031363636363635DF90170630303030303601297B22706F705F6964223A2232626332633035332D366261652D343564642D616630352D666565363932663266353730222C2273617564616761725F6964223A2247373035353633363431222C227472616E73616374696F6E5F6964223A2232376365303534662D70732D6E766E642D396238322D4B626F623237494C313932227D0010FFFF98010003388003A00190600070000002003020458020E5901C00000000000002200000000158120901062000376019002000008773D2204120845151023500003030303030323335303035303030373530333432202020474F4B414E4120544550414E204A616B617274612053656C61744A414B494420202020202020202000154130303030303030303030303633330010FFFF98010003388003A003606195A424FD3AF33C000630303030303300154A414B415254412053454C4154414E000730303030303131");
        logEvent.addMessage("<bitmap>{3, 11, 24, 41, 46, 47, 48, 61}</bitmap>");
        logEvent.addMessage("<unpack fld=\"3\" packager=\"org.jpos.iso.IFB_NUMERIC\">");
        logEvent.addMessage("<value>450000</value>");
        logEvent.addMessage("</unpack>");
        logEvent.addMessage("<unpack fld=\"46\" packager=\"org.jpos.iso.IFB_LLLBINARY\">");
        logEvent.addMessage("<value type='binary'>DF9003084341383836363832DF900405312E302E35DF90080B3030303031363636363635DF901706303030303036</value>");
        logEvent.addMessage("</unpack>");

        listener.log(logEvent);

        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    //@Test
    public void testAddMessageContainsJson() throws ConfigurationException, IOException {
        String data =
                " \t *Unique ID : null\n" +
                " JSON Request  : \n" +
                "{\n" +
                "  \"txn_id\" : \"27ce054f-ps-nvnd-9b82-Kbob27IL192\",\n" +
                "  \"custom_fields\" : {\n" +
                "    \"custom_field1\" : \"5812\"\n" +
                "  }\n" +
                "}";

        Matcher regexMatcher = JSON_REGEX.matcher(data);
        assertTrue(regexMatcher.find());

        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("info");
        logEvent.addMessage(data);
        listener.log(logEvent);

        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    @Test
    public void testAddMessageError() throws ConfigurationException, IOException {
        String error = "com.exception.UnLogonException: UnLogon \tat org.jpos.q2.iso.QMUXCustom.request(QMUXCustom.java:93) \tat org.jpos.q2.iso.MUXPool.request(MUXPool.java:79) \tat com.ranggalabs.swc.Switcher.SessionManagerBean.access$000(SessionManagerBean.java:25) \tat com.ranggalabs.swc.Switcher.SessionManagerBean$1.run(SessionManagerBean.java:65) \tat java.lang.Thread.run(Thread.java:748) ";

        Matcher regexMatcher = STACK_TRACE_REGEX.matcher(error);
        assertTrue(regexMatcher.find());
        assertTrue(STACK_TRACE_REGEX.matcher(error).find());

        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("error");
        logEvent.addMessage(error);
        listener.log(logEvent);

        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    @Test
    public void testAddMessageError2() throws ConfigurationException, IOException {
        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("error");
        logEvent.addMessage("com.ranggalabs.swc.exception.UnLogonException: UnLogon ");
        logEvent.addMessage("at org.jpos.q2.iso.QMUXCustom.request(QMUXCustom.java:93) ");
        logEvent.addMessage("at org.jpos.q2.iso.MUXPool.request(MUXPool.java:79) ");
        logEvent.addMessage("at com.ranggalabs.swc.Switcher.SessionManagerBean.sessionManaging(SessionManagerBean.java:80) ");

        listener.log(logEvent);

        listener.logRotate();

        String archivedLogFile1Contents = getStringFromFile(logRotationTestDirectory.getFile(logFileName + ".1"));
        System.out.print(">>> " + archivedLogFile1Contents);
        assertTrue(isJSONValid(archivedLogFile1Contents));
    }

    @Test
    public void testAddMessageStackTrace() throws ConfigurationException, IOException {
        String stackTrace = "java.security.spec.InvalidKeySpecException: java.security.InvalidKeyException: IOException: Detect premature EOF\n" +
                "\tat sun.security.rsa.RSAKeyFactory.engineGeneratePublic(RSAKeyFactory.java:205)\n" +
                "\tat java.security.KeyFactory.generatePublic(KeyFactory.java:334)\n" +
                "\tat java.lang.Thread.run(Thread.java:748)\n" +
                "Caused by: java.security.InvalidKeyException: IOException: Detect premature EOF\n" +
                "\tat sun.security.x509.X509Key.decode(X509Key.java:398)\n" +
                "\tat sun.security.x509.X509Key.decode(X509Key.java:403)\n" +
                "\tat sun.security.rsa.RSAPublicKeyImpl.<init>(RSAPublicKeyImpl.java:86)\n" +
                "\tat sun.security.rsa.RSAKeyFactory.generatePublic(RSAKeyFactory.java:298)\n" +
                "\tat sun.security.rsa.RSAKeyFactory.engineGeneratePublic(RSAKeyFactory.java:201)\n" +
                "\t... 9 more";

        Properties configuration = new Properties();
        configuration.setProperty("format", JSON_LABEL);

        String logFileName = "JsonRotateWorksTestLog";
        RotateLogListener listener = createRotateLogListenerWithIsoDateFormat(logFileName, configuration);

        LogEvent logEvent = new LogEvent("error");
        logEvent.addMessage(stackTrace);
        listener.log(logEvent);

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
