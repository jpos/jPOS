package remotespace;

import org.jpos.iso.*;
import org.jpos.space.*;
import javax.naming.*;

/*
 * In order to run this example you should start QSP in another session
 * with the following configuration blocks:
 *
 * <channel name="xmlchannel_8001"
 *        class="org.jpos.iso.channel.XMLChannel"
 *        packager="org.jpos.iso.packager.XMLPackager"
 *        type="client" connect="no" logger="qsp" realm="xmlchannel_8001" >
 * <property name="host" value="127.0.0.1" />
 * <property name="port" value="8001" />
 *</channel>
 *
 * <task name="ISOChannelAdaptor" class="org.jpos.space.ISOChannelAdaptor">
 *  <property name="channel" value="xmlchannel_8001" />
 *  <property name="to"   value="xmlchannel_8001.tx" />
 *  <property name="from" value="xmlchannel_8001.rx" />
 * </task>
 *
 * <task name="SpaceProxy" class="org.jpos.space.SpaceProxy" 
 *     logger="qsp" realm="space-proxy" />
 *
 * Then you can use netcat with a command like this:
 * nc -v -p 8001 -l
 *
 * Every time you fire 'bin/example remotespace' you should read a message
 * like this on your nc console:
 *
 * <isomsg direction="outgoing">
 *   <field id="0" value="0800"/>
 *   <field id="3" value="000000"/>
 *   <field id="11" value="000000"/>
 *   <field id="41" value="29110001"/>
 * </isomsg>
 *
 * Change jndi.properties if you want to run this example on 
 * different machines.
 */

public class Test {
    public static void main (String[] args) {
        try {
            InitialContext ctx = new InitialContext ();
            RemoteSpace sp = (RemoteSpace) ctx.lookup ("SpaceProxy");
            ISOMsg m = new ISOMsg();
            m.setMTI ("0800");
            m.set (3, "000000");
            m.set (11, "000000");
            m.set (41, "29110001");
            sp.out ("xmlchannel_8001.tx", m);
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
}

