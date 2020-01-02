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

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.Channel;
import org.jpos.iso.FactoryChannel;
import org.jpos.iso.FilteredChannel;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOClientSocketFactory;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OneShotChannelAdaptorMK2 connects and disconnects a channel for every message
 * exchange. It is similar to OneShotChannelAdaptor but uses a thread pool instead
 * of opening threads statically and supports mux pooling by exposing channel readiness.
 *
 * @author Alejandro Revilla
 * @author Thomas L. Kjeldsen
 * @author Victor Salaman
 */
@SuppressWarnings({"UnusedDeclaration", "StatementWithEmptyBody"})
public class OneShotChannelAdaptorMK2
        extends QBeanSupport
        implements OneShotChannelAdaptorMK2MBean, Channel, Runnable
{
    Space<String, Object> sp;
    String in, out, ready;
    long delay;
    long checkInterval;
    int maxConnections;
    int[] handbackFields;
    ThreadPoolExecutor threadPool = null;
    AtomicInteger cnt;
    Element channelElement;

    ScheduledExecutorService checkTimer;

    public OneShotChannelAdaptorMK2()
    {
        super();
    }

    @SuppressWarnings("unchecked")
    private Space<String, Object> grabSpace(Element e)
    {
        return (Space<String, Object>) SpaceFactory.getSpace(e != null ? e.getText() : "");
    }

    @Override
    protected void initService() throws Exception
    {
        Element persist = getPersist();
        channelElement = persist.getChild("channel");
        if (channelElement == null)
        {
            throw new ConfigurationException("channel element missing");
        }
        sp = grabSpace(persist.getChild("space"));
        in = persist.getChildTextTrim("in");
        out = persist.getChildTextTrim("out");
        ready = getName() + ".ready";

        String s = persist.getChildTextTrim("max-connections");
        maxConnections = s != null ? Integer.parseInt(s) : 1;
        handbackFields = cfg.getInts("handback-field");

        s = persist.getChildTextTrim("delay");
        delay = s != null ? Integer.valueOf(s) : 2500;

        s = persist.getChildTextTrim("check-interval");
        checkInterval = s != null ? Integer.valueOf(s) : 60000;

        NameRegistrar.register(getName(), this);
    }

    public void startService()
    {
        setRealm(getName());
        cnt = new AtomicInteger(0);
        threadPool = new ThreadPoolExecutor(1,
                                            maxConnections,
                                            10,
                                            TimeUnit.SECONDS,
                                            new SynchronousQueue<Runnable>());
        new Thread(this).start();

        checkTimer=Executors.newScheduledThreadPool(1);
        checkTimer.scheduleAtFixedRate(new CheckChannelTask(), 0L, checkInterval,TimeUnit.MILLISECONDS);
    }

    public void stopService()
    {
        if(checkTimer!=null)
        {
            checkTimer.shutdown();
            checkTimer=null;
        }

        takeOffline();
        sp.out(in, new Object());
        threadPool.shutdown();
        while (!threadPool.isTerminated())
        {
            try
            {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e)
            {
            }
        }

        int c=0;
        while(running())
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
            }
            c++;
            if(c>10) break;
        }
    }

    public void destroyService()
    {
        NameRegistrar.unregister(getName());
    }

    public boolean isConnected()
    {
        return sp != null && sp.rdp(ready) != null;
    }

    @Override
    @SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
    public void run()
    {
        while (running())
        {
            try
            {
                Object o = sp.in(in, delay);
                if (o instanceof ISOMsg)
                {
                    if(!isConnected())
                    {
                        continue;
                    }
                    ISOMsg m = (ISOMsg) o;
                    int i = cnt.incrementAndGet();
                    if (i > 9999)
                    {
                        cnt.set(0);
                        i = cnt.incrementAndGet();
                    }
                    threadPool.execute(new Worker(m, i));
                }
            }
            catch (Exception e)
            {
                getLog().warn(getName(), e.getMessage());
            }
        }
    }

    private class CheckChannelTask implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                Date lastOnline = (Date) sp.rdp(ready);
                final LogEvent ev = getLog().createLogEvent("status");
                if (isChannelConnectable(true))
                {
                    if (lastOnline == null)
                    {
                        ev.addMessage("Channel is now online");
                        Logger.log(ev);
                        flushInput();
                    }
                    takeOnline();
                }
                else
                {
                    takeOffline();
                    if (lastOnline != null)
                    {
                        ev.addMessage("Channel is now offline");
                        Logger.log(ev);
                    }
                }
            }
            catch (Throwable e)
            {
                getLog().warn(getName(), e.getMessage());
            }
        }

        private boolean isChannelConnectable(boolean showExceptions)
        {
            boolean res = false;

            ISOChannel channel = null;
            try
            {
                channel = newChannel(channelElement, getFactory());
                if (channel instanceof BaseChannel)
                {
                    BaseChannel bc = (BaseChannel) channel;
                    bc.setLogger(null, null);
                }
                channel.connect();
                res = true;
            }
            catch (Exception e)
            {
                if (showExceptions)
                {
                    getLog().error(e.getMessage());
                }
            }
            finally
            {
                if (channel != null && channel.isConnected())
                {
                    try
                    {
                        channel.disconnect();
                    }
                    catch (IOException e)
                    {
                        getLog().error(e);
                    }
                    NameRegistrar.unregister("channel."+channel.getName());
                }
            }

            return res;
        }
    }

    private void flushInput()
    {
        SpaceUtil.wipe(sp,in);
    }

    private void takeOffline()
    {
        SpaceUtil.wipe(sp, ready);
    }

    private void takeOnline()
    {
        sp.put(ready, new Date());
    }

    public void send(ISOMsg m)
    {
        sp.out(in, m);
    }

    public void send(ISOMsg m, long timeout)
    {
        sp.out(in, m, timeout);
    }

    public ISOMsg receive()
    {
        return (ISOMsg) sp.in(out);
    }

    public ISOMsg receive(long timeout)
    {
        return (ISOMsg) sp.in(out, timeout);
    }

    private ISOChannel newChannel(Element e, QFactory f)
            throws ConfigurationException
    {
        String channelName = QFactory.getAttributeValue(e, "class");
        if (channelName == null)
        {
            throw new ConfigurationException("class attribute missing from channel element.");
        }

        String packagerName = QFactory.getAttributeValue(e, "packager");

        ISOChannel channel = (ISOChannel) f.newInstance(channelName);
        ISOPackager packager;
        if (packagerName != null)
        {
            packager = (ISOPackager) f.newInstance(packagerName);
            channel.setPackager(packager);
            f.setConfiguration(packager, e);
        }
        QFactory.invoke(channel, "setHeader", QFactory.getAttributeValue(e, "header"));
        f.setLogger(channel, e);
        f.setConfiguration(channel, e);

        if (channel instanceof FilteredChannel)
        {
            addFilters((FilteredChannel) channel, e, f);
        }

        String socketFactoryString = getSocketFactory();
        if (socketFactoryString != null && channel instanceof FactoryChannel)
        {
            ISOClientSocketFactory sFac = (ISOClientSocketFactory) getFactory().newInstance(socketFactoryString);
            if (sFac != null && sFac instanceof LogSource)
            {
                ((LogSource) sFac).setLogger(log.getLogger(), getName() + ".socket-factory");
            }
            getFactory().setConfiguration(sFac, e);
            ((FactoryChannel) channel).setSocketFactory(sFac);
        }

        return channel;
    }

    private void addFilters(FilteredChannel channel, Element e, QFactory fact)
            throws ConfigurationException
    {
        for (Object o : e.getChildren("filter"))
        {
            Element f = (Element) o;
            String clazz = QFactory.getAttributeValue(f, "class");
            ISOFilter filter = (ISOFilter) fact.newInstance(clazz);
            fact.setLogger(filter, f);
            fact.setConfiguration(filter, f);
            String direction = QFactory.getAttributeValue(f, "direction");
            if (direction == null)
            {
                channel.addFilter(filter);
            }
            else if ("incoming".equalsIgnoreCase(direction))
            {
                channel.addIncomingFilter(filter);
            }
            else if ("outgoing".equalsIgnoreCase(direction))
            {
                channel.addOutgoingFilter(filter);
            }
            else if ("both".equalsIgnoreCase(direction))
            {
                channel.addIncomingFilter(filter);
                channel.addOutgoingFilter(filter);
            }
        }
    }

    public String getInQueue()
    {
        return in;
    }

    public synchronized void setInQueue(String in)
    {
        String old = this.in;
        this.in = in;
        if (old != null)
        {
            sp.out(old, new Object());
        }

        getPersist().getChild("in").setText(in);
        setModified(true);
    }

    public String getOutQueue()
    {
        return out;
    }

    public synchronized void setOutQueue(String out)
    {
        this.out = out;
        getPersist().getChild("out").setText(out);
        setModified(true);
    }

    public String getHost()
    {
        return getProperty(getProperties("channel"), "host");
    }

    public synchronized void setHost(String host)
    {
        setProperty(getProperties("channel"), "host", host);
        setModified(true);
    }

    public int getPort()
    {
        int port = 0;
        try
        {
            port = Integer.parseInt(
                    getProperty(getProperties("channel"), "port")
            );
        }
        catch (NumberFormatException e)
        {
            getLog().error(e);
        }
        return port;
    }

    public synchronized void setPort(int port)
    {
        setProperty(
                getProperties("channel"), "port", Integer.toString(port)
        );
        setModified(true);
    }

    public String getSocketFactory()
    {
        return getProperty(getProperties("channel"), "socketFactory");
    }

    public synchronized void setSocketFactory(String sFac)
    {
        setProperty(getProperties("channel"), "socketFactory", sFac);
        setModified(true);
    }

    public class Worker implements Runnable
    {
        ISOMsg req;
        int id;

        public Worker(ISOMsg req, int id)
        {
            this.req = req;
            this.id = id;
        }

        public void run()
        {
            Thread.currentThread().setName("channel-worker-" + id);
            ISOChannel channel = null;

            try
            {
                channel = newChannel(channelElement, getFactory());
                if (getName() != null)
                {
                    channel.setName(getName() + id);
                }

                ISOMsg handBack = null;
                if (handbackFields.length > 0)
                {
                    handBack = (ISOMsg) req.clone(handbackFields);
                }
                try
                {
                    channel.connect();
                }
                catch (Throwable e)
                {
                    takeOffline();
                }
                if (channel.isConnected())
                {
                    takeOnline();
                    channel.send(req);
                    ISOMsg rsp = channel.receive();
                    channel.disconnect();
                    if (handBack != null)
                    {
                        rsp.merge(handBack);
                    }
                    sp.out(out, rsp);
                }
            }
            catch (Exception e)
            {
                getLog().warn("channel-worker-" + id, e.getMessage());
            }
            finally
            {
                try
                {
                    if (channel != null)
                    {
                        channel.disconnect();
                    }
                }
                catch (Exception e)
                {
                    getLog().warn("channel-worker-" + id, e.getMessage());
                }
                finally
                {
                    NameRegistrar.unregister("channel." + getName() + id);
                }
            }
        }
    }
}
