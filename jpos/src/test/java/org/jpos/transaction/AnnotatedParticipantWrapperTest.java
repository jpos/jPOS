package org.jpos.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jpos.annotation.AnnotatedParticipant;
import org.jpos.annotation.ContextKey;
import org.jpos.annotation.ContextKeys;
import org.jpos.annotation.Prepare;
import org.jpos.annotation.Registry;
import org.jpos.annotation.Return;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.q2.Q2;
import org.jpos.q2.QFactory;
import org.jpos.rc.IRC;
import org.jpos.transaction.AnnotatedParticipantWrapperTest.ContextKeysResolverTest;
import org.jpos.transaction.AnnotatedParticipantWrapperTest.Handler;
import org.jpos.transaction.AnnotatedParticipantWrapperTest.TaggingAnnotatedParticipant;
import org.jpos.rc.CMF;
import org.jpos.util.NameRegistrar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.xml.sax.InputSource;

public class AnnotatedParticipantWrapperTest {
    
    private static final String HANDLER = "handler";
    
    public static class TxnSupport implements TransactionParticipant {
        public int  prepare(long id, Serializable context) {
            return TransactionConstants.ABORTED;
        }
        
        public void setConfiguration(Configuration cfg) {}
    }

    @AnnotatedParticipant
    public static class AnnotatedParticipantTest extends TxnSupport {
        @Prepare(result = TransactionConstants.PREPARED | TransactionConstants.READONLY)
        @Return("CARD")
        public Object getCard(Context ctx, @ContextKey("DB") Object db, @Registry Long someKey) throws Exception {
            Assertions.assertNotNull(ctx);
            Assertions.assertNotNull(db);
            Assertions.assertEquals(2, someKey);
            return new Object();
        }
    }
        
    public static class RegularParticipantTest extends TxnSupport {}
    
    @Test
    public void testClassAnnotation() throws ConfigurationException {
        Context ctx = new Context();
        ctx.put("DB", new Object());
        Configuration cfg = new SimpleConfiguration();
        NameRegistrar.register("someKey", 2L);
        
        // Test plain participant
        TxnSupport p = new RegularParticipantTest();
        p.setConfiguration(cfg);
        assertRegularParticipant(ctx, p, false);
        
        p = new AnnotatedParticipantTest();
        p.setConfiguration(cfg);
        // Test direct participant
        assertRegularParticipant(ctx, p, true);
        
        // Test wrapper annotation
        assertTrue(AnnotatedParticipantWrapper.isMatch(p));
        TxnSupport pw = AnnotatedParticipantWrapper.wrap(p);
        assertAnnotatedParticipant(ctx, pw);
        
    }
    
    @Test
    public void testClassAnnotationFromTxnMgr() throws ConfigurationException, JDOMException, IOException, MalformedObjectNameException {
        TransactionManager txnMgr = new TransactionManager();
        Q2 q2 = mock(Q2.class);
        QFactory f = spy(new QFactory(new ObjectName("Q2:type=system,service=loader"), q2));
        when(q2.getFactory()).thenReturn(f);
        doReturn(new RegularParticipantTest()).when(f).newInstance(RegularParticipantTest.class.getCanonicalName());
        doReturn(new AnnotatedParticipantTest()).when(f).newInstance(AnnotatedParticipantTest.class.getCanonicalName());
        txnMgr.setServer(q2);
        txnMgr.setName("txnMgr");
        txnMgr.setConfiguration(new SimpleConfiguration());
        
        String regParticipantXml = "<participant class=\"" + RegularParticipantTest.class.getCanonicalName() + "\"/>";
        String annotatedParticipantXml = "<participant class=\"" + AnnotatedParticipantTest.class.getCanonicalName() + "\"/>";
        
        Context ctx = new Context();
        ctx.put("DB", new Object());
        NameRegistrar.register("someKey", 2L);
        
        TxnSupport p = (TxnSupport) getParticipant(txnMgr, regParticipantXml);
        assertRegularParticipant(ctx, p, false);
        
        p = (TxnSupport) getParticipant(txnMgr, annotatedParticipantXml);
        assertAnnotatedParticipant(ctx, p);
    }
        
    
    @AnnotatedParticipant
    public static class InvalidParticipant extends TxnSupport {
        public InvalidParticipant(Object arg) {}
    }
    
    @AnnotatedParticipant
    static class InvalidParticipantInvocation extends TxnSupport {
        public InvalidParticipantInvocation() throws Exception {
        }
        
        @Prepare
        public void prepare() {}
    }
    
    @Test
    public void testWrapperInstantiationFailure() {
        assertThrows(ConfigurationException.class, ()-> AnnotatedParticipantWrapper.wrap(new InvalidParticipant(null) {}));
        assertThrows(ConfigurationException.class, ()-> AnnotatedParticipantWrapper.wrap(new InvalidParticipant(null)));
        assertThrows(ConfigurationException.class, ()-> AnnotatedParticipantWrapper.wrap(new InvalidParticipantInvocation()));
    }
    
    public static class PassthruReturn extends TaggingAnnotatedParticipant {
        @Prepare
        public int doWork() {
            return 5;
        }
    }
    
    public static class MapMultiReturn extends TaggingAnnotatedParticipant {
        @Prepare(result = 5)
        @Return({"key1", "key2", "key3"})
        public Map<String, Object> doWork(@ContextKey(HANDLER) Handler handler) throws Exception {
            return handler == null ? null : (Map<String, Object>) handler.call();
        }
    }
    
    public static class MapSingleReturn extends TaggingAnnotatedParticipant {
        @Prepare(result = 5)
        @Return({"key1"})
        public Map<String, Object> doWork(@ContextKey(HANDLER) Handler handler) throws Exception {
            return handler == null ? null : (Map<String, Object>) handler.call();
        }
    }
    
    @Test
    public void testReturnTypes() throws ConfigurationException {
        assertEquals(5, AnnotatedParticipantWrapper.wrap(new PassthruReturn()).prepare(0, new Context()));
        Context ctx = new Context();
        assertEquals(5, AnnotatedParticipantWrapper.wrap(new MapMultiReturn()).prepare(0, ctx));
        assertEquals(0, ctx.getMap().size());
        testWithHandler(new MapMultiReturn(), ctx, (c)-> new HashMap<String, String>() {{
            put("a", "b");
            put("c", "d");
        }});
        assertEquals(1, ctx.getMap().size());
        ctx.getMap().clear();
        
        assertEquals(0, ctx.getMap().size());
        testWithHandler(new MapMultiReturn(), ctx, (c)-> new HashMap<String, String>() {{
            put("key1", "b");
            put("c", "d");
        }});
        assertEquals(2, ctx.getMap().size());
        ctx.getMap().clear();
        
        assertEquals(0, ctx.getMap().size());
        testWithHandler(new MapSingleReturn(), ctx, (c)-> new HashMap<String, String>() {{
            put("b", "b");
            put("c", "d");
        }});
        assertEquals(2, ctx.getMap().size());
        ctx.getMap().clear();
        
        testWithHandler(new MapSingleReturn(), ctx, null);
        assertEquals(1, ctx.getMap().size());
        assertTrue(ctx.getMap().containsKey(HANDLER));
    }
    
    void testWithHandler(TransactionParticipant p, Context ctx, Handler handler) throws ConfigurationException {
        ctx.put(HANDLER, handler);
        assertEquals(5, AnnotatedParticipantWrapper.wrap(p).prepare(0, ctx));
    }
    
    @AnnotatedParticipant
    public static class TaggingAnnotatedParticipant implements TransactionParticipant {
        @Override
        public int prepare(long id, Serializable context) {
            return TransactionConstants.ABORTED;
        }
    }
    
    public static interface Handler {
       default Object call() throws Exception {
           return call(null);
       }
       
       Object call(Context ctx) throws Exception;
    }
    
    public static class HappyPass extends TaggingAnnotatedParticipant {
        @Prepare
        public void doNothing(@ContextKey(HANDLER) Handler handler, @ContextKey("DB") Object db) throws Exception {
            if (handler != null) handler.call();
        }
    }
    
    public static class DoublePrepareDefined extends TaggingAnnotatedParticipant {
        @Prepare
        public void doNothing() {}
        @Prepare
        public void doNothing1() {}
    }
    public static class PreparedUnboundArg extends TaggingAnnotatedParticipant {
        @Prepare
        public void invalidParams(Object arg) {}
    }
    public static class MissingReturn extends TaggingAnnotatedParticipant {
        @Prepare
        public Map invalidParams() {
            return null;
        }
    }
    public static class UnusedReturn extends TaggingAnnotatedParticipant {
        @Prepare
        @Return("key")
        public void invalidParams() {}
    }
    
    public static class TooManyKeysDefined extends TaggingAnnotatedParticipant {
        @Prepare
        @Return({"key", "key1"})
        public Object invalidReturn() {
            return null;
        }       
    }
    
    
    @Test
    public void testWrapperInvalidAnnotationUse() throws ConfigurationException {
        assertNotNull(AnnotatedParticipantWrapper.wrap(new HappyPass()));
        
        assertThrows(ConfigurationException.class, ()-> {
            AnnotatedParticipantWrapper.wrap(new TaggingAnnotatedParticipant());
        });
        
        assertThrows(ConfigurationException.class, ()-> {
            AnnotatedParticipantWrapper.wrap(new DoublePrepareDefined());
        });
        
        assertThrows(ConfigurationException.class, ()-> {
            AnnotatedParticipantWrapper.wrap(new PreparedUnboundArg());
        });
        
        assertThrows(ConfigurationException.class, ()-> {
            AnnotatedParticipantWrapper.wrap(new MissingReturn());
        });
        
        assertThrows(ConfigurationException.class, ()-> {
            AnnotatedParticipantWrapper.wrap(new UnusedReturn());
        });
        
        assertThrows(ConfigurationException.class, ()-> {
            AnnotatedParticipantWrapper.wrap(new TooManyKeysDefined());
        });
    }
    
    
    @Test
    public void testParticipantExecutionErrorHandling() throws ConfigurationException {
        TransactionParticipant p = AnnotatedParticipantWrapper.wrap(new HappyPass());
        testException(p, (c)-> {throw new RuntimeException();}, CMF.INTERNAL_ERROR);
        final Exception circularCause = new Exception() {
            public synchronized Throwable getCause() {
                return this;
            };
        };
        testException(p, (c)-> {throw circularCause;}, CMF.INTERNAL_ERROR);        
    }
    
    @Test
    public void testUncaughtExceptionHandling() throws ConfigurationException {
        TransactionParticipant p = AnnotatedParticipantWrapper.wrap(new HappyPass());
        Handler h = (c)->{throw new Error();};
        Context ctx = new Context();
        ctx.put(HANDLER, h);
        
        assertThrows(RuntimeException.class, ()->p.prepare(0, ctx));
        
    }

    private void testException(TransactionParticipant p, Handler handler, IRC irc) {
        Context ctx = new Context();
        ctx.put(HANDLER, handler);
        assertEquals(TransactionConstants.ABORTED, p.prepare(0, ctx));
        assertEquals(irc, ctx.get(ContextConstants.IRC));
    }
    
    @Test
    public void testInvalidArgumentBinding() throws ConfigurationException {
        Context ctx = new Context();
        
        TransactionParticipant p = AnnotatedParticipantWrapper.wrap(new HappyPass());

        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));        
        
        ctx.put(HANDLER, new Object());
        assertEquals(TransactionConstants.ABORTED, p.prepare(0, ctx));        
        assertEquals(CMF.INTERNAL_ERROR, ctx.get(ContextConstants.IRC));
    }
    
    public static class RegistryNameResolverTest extends TaggingAnnotatedParticipant {
        @Prepare
        public void checkRegistry(@Registry("key1") String key) {
            assertEquals("key1", key);
        }
    }
    
    public static class RegistryTypeResolverTest extends TaggingAnnotatedParticipant {
        @Prepare
        public void checkRegistry(@Registry() String key) {
            assertEquals("key1", key);
        }
    }
    

    
    @Test
    public void testNamedRegistryResolver() throws ConfigurationException {
        Context ctx = new Context();
        NameRegistrar.register("key1", "key1");        
        TransactionParticipant p;
        
        p = AnnotatedParticipantWrapper.wrap(new RegistryTypeResolverTest());
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        
        p = AnnotatedParticipantWrapper.wrap(new RegistryNameResolverTest());
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        NameRegistrar.unregister("key1");
        
        assertThrows(ConfigurationException.class, ()->AnnotatedParticipantWrapper.wrap(new RegistryNameResolverTest()));
        assertThrows(ConfigurationException.class, ()->AnnotatedParticipantWrapper.wrap(new RegistryTypeResolverTest()));

        NameRegistrar.register("KEY1", "key1");
        p = AnnotatedParticipantWrapper.wrap(new RegistryTypeResolverTest());
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        
        p = AnnotatedParticipantWrapper.wrap(new RegistryNameResolverTest());
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        NameRegistrar.register("KeY1", "key1");
        assertThrows(ConfigurationException.class, ()->AnnotatedParticipantWrapper.wrap(new RegistryNameResolverTest()));
        
        NameRegistrar.unregister("KEY1");
        NameRegistrar.unregister("KeY1");
        
        assertThrows(ConfigurationException.class, ()->AnnotatedParticipantWrapper.wrap(new RegistryNameResolverTest()));
        assertThrows(ConfigurationException.class, ()->AnnotatedParticipantWrapper.wrap(new RegistryTypeResolverTest()));
        
        NameRegistrar.register("someKey", "key1");
        assertThrows(ConfigurationException.class, ()->AnnotatedParticipantWrapper.wrap(new RegistryNameResolverTest()));
        
        p = AnnotatedParticipantWrapper.wrap(new RegistryTypeResolverTest());
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        NameRegistrar.register("someKey1", "key1");
        assertThrows(ConfigurationException.class, ()->AnnotatedParticipantWrapper.wrap(new RegistryTypeResolverTest()));

        NameRegistrar.unregister("someKey");
        NameRegistrar.unregister("someKey1");
    }

    public static class ContextKeysResolverTest extends TaggingAnnotatedParticipant {
        @Prepare
        public void doWork(@ContextKey(HANDLER) Handler handler, 
                @ContextKeys(value = {"account", "RESULT"}, write = {"RC", "EXTRC"}, read = {"CARD"} ) Context ctx,
                @ContextKeys("account") Context ctx2,
                @ContextKeys(write= {"RC", "EXTRC"}) Context ctx3,
                @ContextKeys(read="CARD") Context ctx4,
                Context ctx5
                ) throws Exception {
            handler.call(ctx);
            assertEquals(ctx, ctx2);
            assertEquals(ctx, ctx3);
            assertEquals(ctx, ctx4);
            assertEquals(ctx, ctx5);
            assertEquals(ctx.hashCode(), ctx2.hashCode());
            assertEquals(ctx.hashCode(), ctx3.hashCode());
            assertEquals(ctx.hashCode(), ctx4.hashCode());
            assertEquals(ctx.hashCode(), ctx5.hashCode());
        }
    }    
    
    @Test
    public void assertContextKeysResolver() throws ConfigurationException {
        Context ctx = new Context();
        TransactionParticipant p;
        p = AnnotatedParticipantWrapper.wrap(new ContextKeysResolverTest());
        
        addHandler(ctx, (c)->null);
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        
        addHandler(ctx, (c)-> {
            assertNull(c.get("account"));
            assertNull(c.get("CARD"));
            c.put("account", "account");
            assertTrue(c.hasKey("account"));
            assertFalse(c.hasPersistedKey("account"));
            c.persist("account");
            assertTrue(c.hasPersistedKey("account"));
            c.evict("account");
            assertEquals("account", c.get("account"));
            c.remove("account");
            assertNull(c.get("account"));
            c.put("account", "account");
            c.put("RC", "rc");
            c.put("EXTRC", "extrarc");
            return true;
        });
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        assertEquals("rc", ctx.get("RC"));
        assertEquals("extrarc", ctx.get("EXTRC"));
        assertEquals("account", ctx.get("account"));
        assertEquals(4, ctx.getMap().size());
    }
        
    @Test
    public void assertContextKeysResolver2() throws ConfigurationException {
        Context ctx = new Context();
        TransactionParticipant p;
        p = AnnotatedParticipantWrapper.wrap(new ContextKeysResolverTest());
        ctx.put("account", "account");
        addHandler(ctx, (c)-> {
            assertEquals("account", c.getString("account"));
            assertEquals("account", c.getString("account", "dft"));
            assertEquals("account", c.get("account"));
            assertEquals("account", c.get("account", 1000L));
            assertEquals("account", c.get("account", "dft"));
            assertEquals("dft", c.get("CARD", "dft"));
            assertEquals("dft", c.getString("CARD", "dft"));
            c.move("account", "EXTRC");
            assertNull(c.get("account"));
            c.put("account", "account2", true);
            c.put("RC", "rc");
            assertEquals(c, ctx);
            assertEquals(c, c.clone());
            return true;
        });
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        assertEquals("rc", ctx.get("RC"));
        assertEquals("account", ctx.get("EXTRC"));
        assertEquals("account2", ctx.getString("account"));
        assertEquals(4, ctx.getMap().size());
    }
    
    @Test
    public void assertContextKeysResolver3() throws ConfigurationException {
        Context ctx = spy(new Context());
        doNothing().when(ctx).resume();
        TransactionParticipant p;
        p = AnnotatedParticipantWrapper.wrap(new ContextKeysResolverTest());
        ctx.put("account", "account");
        assertEquals(1, ctx.getMap().size());
        addHandler(ctx, (c)-> {
            c.getResult();
            c.getProfiler();
            c.checkPoint("myStuff");
            c.getLogEvent();
            c.log("my log");
            c.setTimeout(1000);
            c.setTrace(false);
            assertNull(c.getPausedTransaction());
            c.setPausedTransaction(mock(PausedTransaction.class));
            assertNotNull(c.getPausedTransaction(1));
            c.resume();
            assertEquals(1000, c.getTimeout());
            assertFalse(c.isTrace());
            return true;
        });
        assertEquals(TransactionConstants.PREPARED, p.prepare(0, ctx));
        assertEquals(6, ctx.getMap().size());
    }
    
    @Test
    public void assertContextKeyResolverErrors() throws ConfigurationException {
        TransactionParticipant p;
        p = AnnotatedParticipantWrapper.wrap(new ContextKeysResolverTest());
        
        testException(p, (c)-> {c.getString("account2"); return true;}, CMF.INTERNAL_ERROR);
        testException(p, (c)-> {c.getString("account2", "dft"); return true;}, CMF.INTERNAL_ERROR);
        
        testException(p, (c)-> {c.get("account2"); return true;}, CMF.INTERNAL_ERROR);
        testException(p, (c)-> {c.get("account2", "dft"); return true;}, CMF.INTERNAL_ERROR);
        testException(p, (c)-> {c.get("account2", 1000L); return true;}, CMF.INTERNAL_ERROR);
        
        testException(p, (c)-> {c.put("account2", "acct"); return true;}, CMF.INTERNAL_ERROR);
        testException(p, (c)-> {c.put("account2", "acct", false); return true;}, CMF.INTERNAL_ERROR);

        testException(p, (c)-> {c.hasKey("account2"); return true;}, CMF.INTERNAL_ERROR);
        testException(p, (c)-> {c.hasPersistedKey("account2"); return true;}, CMF.INTERNAL_ERROR);

        testException(p, (c)-> {c.move("account", "account2"); return true;}, CMF.INTERNAL_ERROR);
        testException(p, (c)-> {c.move("account2", "account"); return true;}, CMF.INTERNAL_ERROR);

        testException(p, (c)-> {c.remove("account2"); return true;}, CMF.INTERNAL_ERROR);

        testException(p, (c)-> {c.getMap(); return true;}, CMF.INTERNAL_ERROR);
    }

    public void addHandler(Context ctx, Handler handler) {
        ctx.put(HANDLER, handler);
    }

    protected void assertAnnotatedParticipant(Context ctx, TxnSupport p) {
        assertEquals(TransactionConstants.PREPARED | TransactionConstants.READONLY, p.prepare(0, ctx));
        assertNotNull(ctx.get("CARD"));
    }

    protected void assertRegularParticipant(Context ctx, TxnSupport p, boolean ignoreAnnotation) {
        if (!ignoreAnnotation) {
            assertFalse(AnnotatedParticipantWrapper.isMatch(p));
        }
        assertEquals(TransactionConstants.ABORTED, p.prepare(0, ctx));
        assertNull(ctx.get("CARD"));
    }

    private TransactionParticipant getParticipant(TransactionManager txnMgr, String participantXml) throws JDOMException, IOException, ConfigurationException {
        SAXBuilder builder = new SAXBuilder ();
        builder.setFeature("http://xml.org/sax/features/namespaces", true);
        builder.setFeature("http://apache.org/xml/features/xinclude", true);
        Document doc = builder.build(new InputSource(new StringReader(participantXml)));
        
        return txnMgr.createParticipant(doc.getRootElement());
        
    }

}
