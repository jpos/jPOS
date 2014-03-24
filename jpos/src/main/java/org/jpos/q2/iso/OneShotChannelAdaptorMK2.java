/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2014 Alejandro P. Revilla
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

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
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
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
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
@SuppressWarnings("UnusedDeclaration")
public class OneShotChannelAdaptorMK2
        extends QBeanSupport
        implements OneShotChannelAdaptorMK2MBean, Channel, Runnable
{
    Space<String, Object> sp;
    String in, out, ready;
    long delay;
    int maxConnections;
    int[] handbackFields;
    ThreadPoolExecutor threadPool = null;
    AtomicInteger cnt;
    Element channelElement;

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
        delay = 2500;

        cnt = new AtomicInteger(0);
        String s = persist.getChildTextTrim("max-connections");
        maxConnections = (s != null) ? Integer.parseInt(s) : 1;
        handbackFields = cfg.getInts("handback-field");
        NameRegistrar.register(getName(), this);
    }

    public void startService()
    {
        threadPool = new ThreadPoolExecutor(1,
                                            maxConnections,
                                            10,
                                            TimeUnit.SECONDS,
                                            new ArrayBlockingQueue<Runnable>(2));
        new Thread(this).start();
    }

    public void stopService()
    {
        //noinspection StatementWithEmptyBody
        while (sp.inp(ready) != null)
        {
        }
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
                while (sp.rdp(ready) == null && running())
                {
                    ISOChannel channel = null;
                    try
                    {
                        channel = newChannel(channelElement, getFactory());
                        channel.connect();
                        sp.out(ready, new Date());
                        getLog().info("Channel " + getName() + " is now online");
                        break;
                    }
                    catch (Exception e)
                    {
                        getLog().error(getName(), e);
                    }
                    finally
                    {
                        if (channel != null)
                        {
                            channel.disconnect();
                        }
                    }
                    if (running())
                    {
                        Thread.sleep(delay);
                    }
                    //NOTE: We will eat all coming requests since we're offline!
                    while (sp.inp(in) != null)
                    {
                        ;
                    }
                }

                Object o = sp.in(in, delay);
                if (o instanceof ISOMsg)
                {
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
        String channelName = e.getAttributeValue("class");
        if (channelName == null)
        {
            throw new ConfigurationException("class attribute missing from channel element.");
        }

        String packagerName = e.getAttributeValue("packager");

        ISOChannel channel = (ISOChannel) f.newInstance(channelName);
        ISOPackager packager;
        if (packagerName != null)
        {
            packager = (ISOPackager) f.newInstance(packagerName);
            channel.setPackager(packager);
            f.setConfiguration(packager, e);
        }
        QFactory.invoke(channel, "setHeader", e.getAttributeValue("header"));
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
            String clazz = f.getAttributeValue("class");
            ISOFilter filter = (ISOFilter) fact.newInstance(clazz);
            fact.setLogger(filter, f);
            fact.setConfiguration(filter, f);
            String direction = f.getAttributeValue("direction");
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
                    //noinspection StatementWithEmptyBody
                    while (sp.inp(ready) != null)
                    {
                    }
                    getLog().info("Channel " + getName() + " is now offline");
                }
                if (channel.isConnected())
                {
                    sp.out(ready, new Date());
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
                    NameRegistrar.unregister(getName() + id);
                }
            }
        }
    }
}
