package org.jpos.metrics.iso;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.Q2;
import org.jpos.q2.iso.ChannelAdaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ISOMsgCounterTest{

    @BeforeEach
    public void setupEnv() {
        System.setProperty("metrics.channel.tags",
                            "type:unknown  direction , , whatever:hello"); // global default tags size 3

        System.setProperty("metrics.channel.fields",
                            "  , itc, rc, geo:113.27"); // global fieldset size 3
    }

    @Test
    public void testMetricSginature() throws ConfigurationException {
        var mc= new ISOMsgCounter();
        mc.setMetricName("test.metric");
        assertEquals("test.metric|direction,geo,itc,rc,type,whatever",
                mc.getMetricSignature());
    }

    @Test
    public void testFieldSets() throws ConfigurationException {
        Configuration cfg = new SimpleConfiguration();
        cfg.put("fields", "rc:39, geo:scheme");               // overrides

        var mc= new ISOMsgCounter();
        mc.setConfiguration(cfg);
        var fs = mc.getFieldSet();

        assertEquals(3,         fs.size());
        assertEquals("itc",     fs.get("itc"));     // not overridden, single alias
        assertEquals("39",      fs.get("rc"));      // overridden, from rc alias to 39
        assertEquals("scheme",  fs.get("geo"));     // overridden from 113.27 to scheme
    }

    @Test
    public void testUnknownFieldSets() throws ConfigurationException {
        var mc= new ISOMsgCounter();                        // constructor configures global defaults

        Configuration cfg = new SimpleConfiguration();
        cfg.put("fields", "rc:39, amount:4");               // amount is unknown in metrics.channel.fields
        ConfigurationException ex = assertThrows(ConfigurationException.class,
                     () -> mc.setConfiguration(cfg));

        assertTrue(ex.getMessage().matches(".*unknown.*'amount'.*"),
                "Expected exception about unknown isofield tag 'amount', but got \""+ex.getMessage()+"\"");
    }

    @Test
    public void testExtraCustomTags() throws ConfigurationException {
        var mc= new ISOMsgCounter();
        Map<String,String> tm= mc.getTagsAsMap();

        // first test the unchanged defaults
        assertEquals(3,             tm.size());
        assertEquals("unknown",     tm.get("type"));
        assertEquals("",            tm.get("direction"));       // direction given as single tag, so default value empty
        assertEquals("hello",       tm.get("whatever"));
        assertEquals(null,          tm.get(""));

        // now add overrides
        Configuration cfg = new SimpleConfiguration();
        cfg.put("tags",
                "type:server, whatever:goodbye,,, whatever:adios");
                // last occurrence of "whatever" should override the others (useless, but we don't throw error)
        mc.setConfiguration(cfg);
        tm= mc.getTagsAsMap();                                      // get updated copy of tags

        assertEquals(3,             tm.size());
        assertEquals("server",      tm.get("type"));                // overridden
        assertEquals("",            tm.get("direction"));           // unchanged
        assertEquals("adios",       tm.get("whatever"));            // overridden, keeping last value
    }


    @Test
    public void testUnknownTags() throws ConfigurationException {
        var mc= new ISOMsgCounter();                        // constructor configures global defaults

        Configuration cfg = new SimpleConfiguration();
        cfg.put("tags", "type:server, color:green");        // color is unknown in metrics.channel.tags

        ConfigurationException ex = assertThrows(ConfigurationException.class,
                     () -> mc.setConfiguration(cfg));

        assertTrue(ex.getMessage().matches(".*unknown.*'color'.*"),
                "Expected exception about unknown tag 'color', but got \""+ex.getMessage()+"\"");
    }


    @Test
    public void testCustomMetricWithTags() throws ConfigurationException {
        var mc= new ISOMsgCounter();                        // constructor configures global defaults
        mc.setMetricName("test.metric");

        Configuration cfg = new SimpleConfiguration();
        cfg.put("tags", "planet:jupiter, color:green");     // custom tags are reset for custom metric name
        mc.setConfiguration(cfg);

        Map<String,String> tm= mc.getTagsAsMap();

        assertEquals(2,             tm.size());
        assertEquals("jupiter",     tm.get("planet"));
        assertEquals("green",       tm.get("color"));
        assertEquals(null,          tm.get("whatever"));

        // default isofield tags added to our custom extra tags
        assertEquals("test.metric|color,geo,itc,planet,rc", mc.getMetricSignature());
    }


    @Test
    public void testCustomMetricFromXML() throws Exception  {
        String chxml = """
        <channel    class="org.jpos.iso.channel.XMLChannel"
                    packger="org.jpos.iso.packager.XMLPackager">

            <metrics type="counter"  logger="Q2" realm="the-counter">
                <property name="name"  value="bbb_counter" />
                <property name="description"  value="Let's count!" />
                <property name="tags"  value="type:some_type, port:5432   ,,  chan_name:my_name empty" />
                <property name="fields" value="ttt:itc" />
            </metrics>
        </channel>
        """;

        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(new StringReader(chxml));
        Element el=  doc.getRootElement();

        Q2 q2= new Q2(new String[0]);
        try {
            q2.start();
            boolean ready = q2.ready(10000);
            assertTrue(ready,  "Could not start Q2");

            MeterRegistry registry = q2.getMeterRegistry();
            assertNotNull(registry, "Can't find Q2 MeterRegistry");

            ChannelAdaptor adp= new ChannelAdaptor();
            BaseChannel ch = (BaseChannel)adp.newChannel(el, q2.getFactory());
            ISOMsgCounter isom = (ISOMsgCounter) ch.getISOMsgMetrics();
            isom.register(registry);

            var tagMap = isom.getTagsAsMap();

            assertNotNull(isom, "Channel should have an ISOMsgMetrics but it was null");
            assertEquals("bbb_counter", isom.getMetricName());
            assertEquals("some_type",   tagMap.get("type"));
            assertEquals("5432",        tagMap.get("port"));
            assertEquals("",            tagMap.get("empty"));

            var meters = isom.getMeters();
            assertEquals(0, meters.size());

            var m = new ISOMsg();
            m.setMTI("0200");
            m.set(3, "20");
            isom.recordMessage(m);

            meters = isom.getMeters();
            assertEquals(1, meters.size());

            var someMeterOpt = meters.stream().findAny();
            assertNotNull(someMeterOpt.get(), "No meters were registered after recording a message");

            var someMeter = someMeterOpt.get();
            assertInstanceOf(Counter.class, someMeter,  "The meter should be a Counter but it's a "+someMeter.getClass());
            Counter c1 = (Counter)someMeter;
            assertEquals(1, c1.count(), "The count should be 1 after recording just one message");

            Meter c2 = registry.find(someMeter.getId().getName()).meter();
            assertSame(c1, c2);

            isom.removeMeters();
            meters = isom.getMeters();
            assertEquals(0, meters.size(),  "Meter not removed from ISOMsgCounter");

            c2 = registry.find(someMeter.getId().getName()).meter();
            assertNull(c2, "Meter not removed from ISOMsgCounter");


            System.out.println(isom.getTags());
        } finally {
            q2.shutdown();
        }


    }


}
