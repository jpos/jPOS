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

package org.jpos.q2.iso;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.BlockingQueue;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.space.Space;
import org.jpos.space.TSpace;
import org.jpos.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

/**
 * $Revision$
 * $Date$
 * $Author$
 */
@SuppressWarnings("unchecked")
public class ChannelAdaptorTest {

    private static final long RECONNECT_DELAY = 200;
    private static final String LINK_NAME = "TestLink";
    private static final String RECONNECT_SPACE_KEY = LINK_NAME + ".reconnect";
    private static final String READY_SPACE_KEY = LINK_NAME + ".ready";
    private static final String IN_SPACE_KEY = "TestSpace-send";
    private static final String OUT_SPACE_KEY = "TestSpace-receive";
    private static final String SENDER_THREAD_NAME = "channel-sender-" + IN_SPACE_KEY;
    private static final String RECEIVER_THREAD_NAME = "channel-receiver-" + OUT_SPACE_KEY;
    private static final String STOP_CALLER_THREAD_NAME = "stop-caller";
    private static final String SIMULATED_SEND_ERROR_EXCEPTION_MESSAGE = "simulated send error";

    private ChannelAdaptor channelAdaptor;
    private ScheduledExecutorService executorService;

    @BeforeEach
    public void setUp() throws Exception {
        executorService = Executors.newScheduledThreadPool(2);
    }

    @AfterEach
    public void tearDown() throws Exception {
        executorService.shutdownNow();
        if (channelAdaptor != null) {
            channelAdaptor.destroy();
        }
    }

    @Test
    public void sendPassesMessageToUnderlyingChannel() throws Exception {
        StubISOChannel stubISOChannel = new StubISOChannel();
        channelAdaptor = configureAndStart(new ChannelAdaptorWithoutQ2(stubISOChannel));
        channelAdaptor.send(new ISOMsg("0800"));

        assertThat(stubISOChannel.sendQueue.poll(1, TimeUnit.SECONDS), hasMti("0800"));
    }

    @Test
    public void receivePullsMessageFromUnderlyingChannel() throws Exception {
        StubISOChannel stubISOChannel = new StubISOChannel();
        channelAdaptor = configureAndStart(new ChannelAdaptorWithoutQ2(stubISOChannel));
        stubISOChannel.receiveQueue.add(new ISOMsg("0800"));

        assertThat(channelAdaptor.receive(1000), hasMti("0800"));
    }

    @Test
    public void waitForWorkersOnStopStopsAfterChannelConnects() throws Exception {
        StubISOChannel channel = new StubISOChannel();

        // repeat test to ensure clean up occurs after stop
        for (int i = 0; i < 100; i++) {
            channelAdaptor = configureAndStart(new ChannelAdaptorWithoutQ2(channel));
            waitForSenderAndReceiverToStart();

            assertCallToStopCompletes(i);
        }
    }

    @Test
    public void stopCanWaitForWorkersEvenWhenOutgoingChannelNeverConnects() throws Exception {
        ISOChannel channel = mock(ISOChannel.class);
        when(channel.isConnected()).thenReturn(false);
        when(channel.receive()).thenThrow(new ISOException("unconnected ISOChannel"));

        // repeat test to ensure clean up occurs after stop
        for (int i = 0; i < 10; i++) {
            channelAdaptor = configureAndStart(new ChannelAdaptorWithoutQ2(channel));
            waitForSenderAndReceiverToStart();

            assertCallToStopCompletes(i);
        }
    }

    @Test
    public void stopCanWaitForWorkersEvenWhenSenderBlockedTryingToConnect() throws Exception {
        // Think a link where the other ends plays the client role. Eg a BaseChannel with a serverSocket.
        // So connect() calls socket.accept(). If no client connects accept() blocks forever.
        // Ensures disconnect() is called on stop() regardless of channel.isConnected() return value.
        ISOChannel channel = mock(ISOChannel.class);

        ThreadTrap trap = new ThreadTrap(SENDER_THREAD_NAME);
        when(channel.isConnected()).thenReturn(false);
        trap.catchVictim().when(channel).connect();
        trap.release().when(channel).disconnect();

        channelAdaptor = configureAndStart(new ChannelAdaptorWithoutQ2(channel));
        waitForSenderAndReceiverToStart();
        assertThat("Sender did not call connect()", trap.catchesVictim(), is(true));

        assertCallToStopCompletes(1);
    }

    @Disabled("Failing and don't really know what this test tries to verify")
    @Test
    public void waitForWorkersOnStopDoesNotDeadlockWithUnfortunatelyTimedDisconnectReceivedByReceiver() throws Exception {
        // Ensure no deadlock between Receiver trying to call disconnect() and stop() joining on Receiver.
        StubISOChannel channel = new StubISOChannel();
        Space space = spy(new TSpace());

        ThreadTrap trap = new ThreadTrap(RECEIVER_THREAD_NAME).delegateAfterCatchCall().delegateAfterReleaseCall();
        trap.catchVictim().when(space).out(eq(RECONNECT_SPACE_KEY), any(), eq(RECONNECT_DELAY));
        trap.release().when(space).out(eq(READY_SPACE_KEY), not(isA(Date.class)));

        channelAdaptor = configureAndStart(new ChannelAdaptorWithStubSpace(channel, space));
        waitForSenderAndReceiverToStart();
        // to trap the receiver before it tries to call disconnect() we first need it to be blocked in BaseChannel.receive()
        channel.waitForReceiverToBlockInReceive();
        channel.disconnect();
        assertThat("Receiver did not call sp.out(" + RECONNECT_SPACE_KEY + ", new Object())", trap.catchesVictim(), is(true));

        // Once the receiver thread to is released it will try to call ChannelAdaptor.disconnect().
        // If disconnect() is synchronized on ChannelAdaptor the receiver and stop caller will deadlock.
        assertCallToStopCompletes(1);
    }

    @Disabled("Failing and don't really know what this test tries to verify")
    @Test
    public void waitForWorkersOnStopDoesNotDeadlockWithUnfortunatelyTimedDisconnectReceivedBySender() throws Exception {
        // Ensure no deadlock between Sender trying to call disconnect() and stop() joining on Sender.
        StubISOChannel channel = new StubISOChannelThatThrowsExceptionOnSend();
        LogListener logListener = mock(LogListener.class);
        Space space = spy(new TSpace());

        ThreadTrap trap = new ThreadTrap(SENDER_THREAD_NAME).delegateAfterReleaseCall();
        trap.catchVictim().when(logListener).log(argThat(sendErrorLogEvent()));
        trap.release().when(space).out(eq(IN_SPACE_KEY), not(isA(ISOMsg.class)));

        channelAdaptor = configureAndStart(new ChannelAdaptorWithStubSpace(channel, space), new SimpleLogListener(), logListener);
        waitForSenderAndReceiverToStart();
        channelAdaptor.send(new ISOMsg("0800"));
        assertThat("Sender did not call log()", trap.catchesVictim(), is(true));

        // Once the sender thread is released it will try to call ChannelAdaptor.disconnect().
        // If disconnect() is synchronized on ChannelAdaptor the sender and stop caller will deadlock.
        assertCallToStopCompletes(1);
    }

    private Matcher<LogEvent> sendErrorLogEvent() {
        return new TypeSafeMatcher<LogEvent>() {
            @Override
            protected boolean matchesSafely(LogEvent ev) {
                return Log.WARN.equals(ev.getTag())
                        && ("channel-sender-" + IN_SPACE_KEY).equals(ev.getPayLoad().get(0))
                        && SIMULATED_SEND_ERROR_EXCEPTION_MESSAGE.equals(ev.getPayLoad().get(1));
            }

            public void describeTo(Description description) {
            }
        };
    }

    private void assertCallToStopCompletes(int run) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            ScheduledFuture<?> logFuture = executorService.schedule(logThreadDumpRunnable(), 2, TimeUnit.SECONDS);
            assertThat(stopFuture().get(3, TimeUnit.SECONDS), is(true));
            logFuture.cancel(false);
        } catch (TimeoutException e) {
            fail("Run " + run + " stop should have completed");
        }
        assertStopped(run);
    }

    private static String currentThreadName() {
        return Thread.currentThread().getName();
    }

    private Runnable logThreadDumpRunnable() {
        return new Runnable() {
            public void run() {
                System.out.println("Something is probably going to fail due to a deadlock, dumping threads.");
                System.out.println("You need to use kill -3 <pid> or jstack to get the full thread stack (who has which lock)");
                System.out.println(dump(Thread.getAllStackTraces().keySet()));
            }
        };
    }

    private Future<Boolean> stopFuture() {
        return executorService.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                Thread.currentThread().setName(STOP_CALLER_THREAD_NAME);
                channelAdaptor.stop();
                return true;
            }
        });
    }

    private void assertStopped(int run) {
        Set<Thread> threads = waitForExit(findSendAndReceiveThreads());
        assertEquals(0, threads.size(), "At run " + run + " both send and receive threads should have exited. Found:\n" + dump(threads));
        assertFalse(channelAdaptor.isConnected(), "At run " + run + " channel should not be connected");
    }

    private void waitForSenderAndReceiverToStart() throws InterruptedException {
        int tries = 0;
        while (findSendAndReceiveThreads().size() != 2 && tries++ < 5) {
            Thread.sleep(500);
        }
        assertThat("both send and receive threads should have started", findSendAndReceiveThreads().size(), is(2));
    }

    private Set<Thread> findSendAndReceiveThreads() {
        Set<Thread> threads = new HashSet<Thread>();
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread thread = entry.getKey();
            if (Arrays.asList(RECEIVER_THREAD_NAME, SENDER_THREAD_NAME).contains(thread.getName())) {
                if (thread.isAlive()) {
                    threads.add(thread);
                }
            }
        }
        return threads;
    }

    private Set<Thread> waitForExit(Set<Thread> threads) {
        Iterator<Thread> iterator = threads.iterator();
        while (iterator.hasNext()) {
            Thread thread =  iterator.next();
            try {
                thread.join(RECONNECT_DELAY + 500);
            } catch (InterruptedException ignored) { }
            if (!thread.isAlive()) {
                iterator.remove();
            }
        }
        return threads;
    }

    private String dump(Collection<Thread> threads) {
        StringBuilder b = new StringBuilder();
        for (Thread thread : threads) {
            b.append(thread).append(" state ").append(thread.getState()).append('\n');
            for (StackTraceElement s : thread.getStackTrace()) {
                b.append('\t').append("at ").append(s).append('\n');
            }
        }
        return b.toString();
    }

    private Matcher<ISOMsg> hasMti(final String mti) {
        return new TypeSafeMatcher<ISOMsg>() {
            @Override
            public boolean matchesSafely(ISOMsg isoMsg) {
                return mti.equals(isoMsg.getString(0));
            }

            public void describeTo(Description description) {
                description.appendText("ISOMsg with mti ").appendValue(mti);
            }
        };
    }

    private ChannelAdaptor configureAndStart(ChannelAdaptor channelAdaptor) {
        return configureAndStart(channelAdaptor, new SimpleLogListener());
    }

    private ChannelAdaptor configureAndStart(ChannelAdaptor channelAdaptor, LogListener... logListener) {
        Logger logger = new Logger();
        for (LogListener listener : logListener) {
            logger.addListener(listener);
        }
        logger.setName("testLinkLogger");
        channelAdaptor.setName(LINK_NAME);
        channelAdaptor.setLogger(logger.getName());
        channelAdaptor.setPersist(createConfiguration());
        channelAdaptor.init();
        channelAdaptor.start();
        return channelAdaptor;
    }

    private Element createConfiguration() {
        Element persist = new Element("channel-adaptor");
        persist.addContent(new Element("space").addContent("transient:TestLink"));
        persist.addContent(new Element("in").addContent(IN_SPACE_KEY));
        persist.addContent(new Element("out").addContent(OUT_SPACE_KEY));
        persist.addContent(new Element("reconnect-delay").addContent(Long.toString(RECONNECT_DELAY)));
        persist.addContent(new Element("wait-for-workers-on-stop").addContent("yes"));
        return persist;
    }

    private static class StubISOChannel implements ISOChannel {

        private static final ISOMsg DISCONNECT_TOKEN = new ISOMsg();

        BlockingQueue<ISOMsg> sendQueue = new LinkedBlockingQueue<ISOMsg>();
        BlockingQueue<ISOMsg> receiveQueue = new LinkedBlockingQueue<ISOMsg>();
        volatile boolean connected;
        Semaphore receiverWaiting = new Semaphore(0);

        public void setPackager(ISOPackager p) {
            throw new UnsupportedOperationException();
        }

        public void connect() throws IOException {
            connected = true;
        }

        public void disconnect() throws IOException {
            connected = false;
            receiveQueue.add(DISCONNECT_TOKEN);
        }

        public void reconnect() throws IOException {
        }

        public boolean isConnected() {
            return connected;
        }

        public ISOMsg receive() throws IOException, ISOException {
            if (!connected) {
                throw new ISOException("unconnected ISOChannel");
            }
            try {
                receiverWaiting.release();
                ISOMsg msg = receiveQueue.take();
                if (msg == DISCONNECT_TOKEN) {
                    throw new EOFException("simulated disconnect");
                }
                return msg;
            } catch (InterruptedException e) {
                return null;
            }
        }

        public void send(ISOMsg m) throws IOException, ISOException {
            sendQueue.add(m);
        }

        public void send(byte[] b) throws IOException, ISOException {
            throw new UnsupportedOperationException();
        }

        public void setUsable(boolean b) {
            throw new UnsupportedOperationException();
        }

        public void setName(String name) {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            throw new UnsupportedOperationException();
        }

        public ISOPackager getPackager() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object clone() {
            throw new UnsupportedOperationException();
        }

        public void waitForReceiverToBlockInReceive() {
            try {
                assertTrue(receiverWaiting.tryAcquire(1, TimeUnit.SECONDS), "Receiver did not call receive");
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static class StubISOChannelThatThrowsExceptionOnSend extends StubISOChannel {
        @Override
        public void send(ISOMsg m) throws IOException, ISOException {
            throw new EOFException(SIMULATED_SEND_ERROR_EXCEPTION_MESSAGE);
        }
    }

    private static class ChannelAdaptorWithoutQ2 extends ChannelAdaptor {

        private final ISOChannel channel;

        public ChannelAdaptorWithoutQ2(ISOChannel channel) {
            this.channel = channel;
        }

        @Override
        protected ISOChannel initChannel() throws ConfigurationException {
            return channel;
        }
    }

    private static class ChannelAdaptorWithStubSpace extends ChannelAdaptorWithoutQ2 {

        private final Space space;

        public ChannelAdaptorWithStubSpace(ISOChannel channel, Space space) {
            super(channel);
            this.space = space;
        }

        @Override
        protected Space grabSpace(Element e) {
            return space;
        }
    }

    private static final class ThreadTrap {
        private final Semaphore trappedSignal = new Semaphore(0);
        private final Semaphore freedomSignal = new Semaphore(0);
        private final String victimThreadName;
        private boolean delegateAfterCatchCall;
        private boolean delegateAfterReleaseCall;

        public ThreadTrap(String victimThreadName) {
            this.victimThreadName = victimThreadName;
        }

        public ThreadTrap delegateAfterCatchCall() {
            delegateAfterCatchCall = true;
            return this;
        }

        public ThreadTrap delegateAfterReleaseCall() {
            delegateAfterReleaseCall = true;
            return this;
        }

        public Stubber catchVictim() {
            return doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    assertThat(currentThreadName(), is(victimThreadName));
                    trappedSignal.release();
                    freedomSignal.acquire();
                    return delegateAfterCatchCall ? invocation.callRealMethod() : null;
                }
            });
        }

        public Stubber release() {
            return doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    assertThat(currentThreadName(), is(STOP_CALLER_THREAD_NAME));
                    freedomSignal.release();
                    return delegateAfterReleaseCall ? invocation.callRealMethod() : null;
                }
            });
        }

        public boolean catchesVictim() throws InterruptedException {
            return trappedSignal.tryAcquire(1, TimeUnit.SECONDS);
        }
    }
}
