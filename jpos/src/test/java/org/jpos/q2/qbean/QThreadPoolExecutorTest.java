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

package org.jpos.q2.qbean;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jpos.core.ConfigurationException;
import org.jpos.util.Log;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author dgrandemange
 * 
 */
public class QThreadPoolExecutorTest {

    private static final String QBEAN_DEFAULT_NAME = "dummy";

    private QThreadPoolExecutor qbean;
    private PrintWriter qbeanConfigPw;
    private ByteArrayOutputStream qbeanConfigBos;
    private Log mockedLog;
    private ThreadPoolExecutor executor;

    class DummyThreadPoolExecutor extends ThreadPoolExecutor {

        public DummyThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                long keepAliveTime, TimeUnit unit,
                BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

    }

    @BeforeEach
    public void setUp() {

        mockedLog = mock(Log.class);

        qbean = new QThreadPoolExecutor() {

            @Override
            public Log getLog() {
                return mockedLog;
            }

            @Override
            public String getName() {
                return QBEAN_DEFAULT_NAME;
            }

        };

        qbeanConfigBos = new ByteArrayOutputStream();
        qbeanConfigPw = new PrintWriter(new OutputStreamWriter(qbeanConfigBos));

        NameRegistrar.getAsMap().clear();
    }

    @AfterEach
    public void tearDown() {
        NameRegistrar.getAsMap().clear();
        if (null != executor) {
            try {
                executor.shutdownNow();
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testStaticGetThreadPoolExecutor() throws NotFoundException {
        executor = new DummyThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1)
        );
        NameRegistrar.register(qbean.getRegistrationName(), executor);
        ThreadPoolExecutor expected = NameRegistrar.get(qbean.getRegistrationName());
        assertSame(expected, executor);

        expected = QThreadPoolExecutor.getThreadPoolExecutor(QBEAN_DEFAULT_NAME);
        assertSame(expected, executor);
    }

    @Test
    public void testStaticGetThreadPoolExecutor_SpecifyingExpectedClass()
            throws NotFoundException {
        executor = new DummyThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1)
        );
        NameRegistrar.register(qbean.getRegistrationName(), executor);
        ThreadPoolExecutor expected = NameRegistrar.get(qbean.getRegistrationName());
        assertSame(expected, executor);

        expected = QThreadPoolExecutor.getThreadPoolExecutor(
                QBEAN_DEFAULT_NAME
                , DummyThreadPoolExecutor.class
        );
        assertSame(expected, executor);
    }

    @Test
    public void testInitService_FixedThreadPool() throws Exception {
        qbeanConfigPw
                .println("<QThreadPoolExecutor type='fixed' corePoolSize='10' />");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        qbean.initService();

        assertThat(qbean.getExecSrvType()).isEqualTo("fixed");
        assertThat(qbean.getInitialCorePoolSize()).isEqualTo(10);
        assertThat(qbean.getTerminationTimer()).isEqualTo(
                QThreadPoolExecutor.DEFAULT_TERMINATION_TIMER);
    }

    @Test
    public void testInitService_CachedThreadPool_CorePoolSizeAttributeIsNotSet()
            throws Exception {
        qbeanConfigPw.println("<QThreadPoolExecutor type='cached' />");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        qbean.initService();

        assertThat(qbean.getExecSrvType()).isEqualTo("cached");
        assertThat(qbean.getInitialCorePoolSize()).isEqualTo(0);
        assertThat(qbean.getTerminationTimer()).isEqualTo(
                QThreadPoolExecutor.DEFAULT_TERMINATION_TIMER);
    }

    @Test
    public void testInitService_CachedThreadPool_CorePoolSizeAttributeIsSet()
            throws Exception {
        qbeanConfigPw
                .println("<QThreadPoolExecutor type='cached' corePoolSize='100'/>");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        qbean.initService();

        assertThat(qbean.getExecSrvType()).isEqualTo("cached");
        assertThat(qbean.getInitialCorePoolSize()).isEqualTo(100);
        assertThat(qbean.getTerminationTimer()).isEqualTo(
                QThreadPoolExecutor.DEFAULT_TERMINATION_TIMER);
    }

    @Test
    public void testInitService_ScheduledThreadPool() throws Exception {
        qbeanConfigPw
                .println("<QThreadPoolExecutor type='scheduled' corePoolSize='15' />");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        qbean.initService();

        assertThat(qbean.getExecSrvType()).isEqualTo("scheduled");
        assertThat(qbean.getInitialCorePoolSize()).isEqualTo(15);
        assertThat(qbean.getTerminationTimer()).isEqualTo(
                QThreadPoolExecutor.DEFAULT_TERMINATION_TIMER);
    }

    @Test
    public void testInitService_OverrideDefaultTerminationTimer()
            throws Exception {
        qbeanConfigPw
                .println("<QThreadPoolExecutor type='fixed' corePoolSize='10' terminationTimer='30' />");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        qbean.initService();

        assertThat(qbean.getExecSrvType()).isEqualTo("fixed");
        assertThat(qbean.getInitialCorePoolSize()).isEqualTo(10);
        assertThat(qbean.getTerminationTimer()).isEqualTo(30);
    }

    @Test
    public void testInitService_InvalidThreadPoolType() throws Exception {
        qbeanConfigPw
                .println("<QThreadPoolExecutor type='somethingNotValid' />");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        try {
            qbean.initService();
            fail("ConfigurationException was expected");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage().contains(
                    QThreadPoolExecutor.XML_CONFIG_ATTR__EXEC_SRV_TYPE));
        }

    }

    @Test
    public void testInitService_FixedThreadPool_TypeAttributeIsMissing()
            throws Exception {
        qbeanConfigPw.println("<QThreadPoolExecutor />");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        try {
            qbean.initService();
            fail("ConfigurationException was expected");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage().contains(
                    QThreadPoolExecutor.XML_CONFIG_ATTR__EXEC_SRV_TYPE));
        }

    }

    @Test
    public void testInitService_FixedThreadPool_CorePoolSizeAttributeIsMissing()
            throws Exception {
        qbeanConfigPw.println("<QThreadPoolExecutor type='fixed' />");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        try {
            qbean.initService();
            fail("ConfigurationException was expected");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage().contains(
                    QThreadPoolExecutor.XML_CONFIG_ATTR__EXEC_SRV_COREPOOLSIZE));
        }

    }

    @Test
    public void testInitService_FixedThreadPool_CorePoolSizeAttributeValueIsNotANumber()
            throws Exception {
        qbeanConfigPw
                .println("<QThreadPoolExecutor type='fixed' corePoolSize='abc'/>");
        qbeanConfigPw.flush();
        setQBeanConfig(qbeanConfigBos.toByteArray());

        try {
            qbean.initService();
            fail(DataConversionException.class.getSimpleName()
                    + " was expected");
        } catch (DataConversionException e) {
        }

    }

    @Test
    public void testStartService_FixedThreadPool() throws Exception {
        qbean.setExecSrvType("fixed");
        qbean.setInitialCorePoolSize(2);

        qbean.startService();

        ThreadPoolExecutor executorService = QThreadPoolExecutor
                .getThreadPoolExecutor(QBEAN_DEFAULT_NAME,
                        ThreadPoolExecutor.class);
        assertThat(executorService.getCorePoolSize()).isEqualTo(2);
    }

    @Test
    public void testStartService_CachedThreadPool() throws Exception {
        qbean.setExecSrvType("cached");
        qbean.setInitialCorePoolSize(3);

        qbean.startService();

        ThreadPoolExecutor executorService = QThreadPoolExecutor
                .getThreadPoolExecutor(QBEAN_DEFAULT_NAME,
                        ThreadPoolExecutor.class);
        assertThat(executorService.getCorePoolSize()).isEqualTo(3);
    }

    @Test
    public void testStartService_ScheduledThreadPool() throws Exception {
        qbean.setExecSrvType("scheduled");
        qbean.setInitialCorePoolSize(4);

        qbean.startService();

        ScheduledThreadPoolExecutor executorService = QThreadPoolExecutor
                .getThreadPoolExecutor(QBEAN_DEFAULT_NAME,
                        ScheduledThreadPoolExecutor.class);
        assertThat(executorService.getCorePoolSize()).isEqualTo(4);
    }

    @Test
    public void testStartService_InvalidThreadPoolType() throws Exception {
        qbean.setExecSrvType("someInvalidType");

        try {
            qbean.startService();
            fail("An exception was expected");
        } catch (Exception e) {
            assertThat(e.getMessage().contains("Unable to start service"));
        }
    }

    @Test
    public void testStopService_NoTerminationTimeout() throws Exception {
        executor = mock(ThreadPoolExecutor.class);
        NameRegistrar.register(qbean.getRegistrationName(), executor);
        ThreadPoolExecutor expected = NameRegistrar.get(qbean.getRegistrationName());
        assertSame(expected, executor);
        when(
                executor.awaitTermination(Mockito.anyLong(),
                        Mockito.any(TimeUnit.class))).thenReturn(true);

        qbean.stopService();

        expected = NameRegistrar.getIfExists(qbean.getRegistrationName());
        assertNull(expected);
    }

    protected void setQBeanConfig(byte[] config) {
        ByteArrayInputStream bis = new ByteArrayInputStream(config);
        Document doc = getDocument(bis);
        qbean.setPersist(doc.getRootElement());
    }

    protected Document getDocument(InputStream is) {
        SAXBuilder builder = new SAXBuilder();

        builder.setValidation(false);

        Document doc = null;
        try {
            doc = builder.build(is);
            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
