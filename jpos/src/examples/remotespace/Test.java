package remotespace;

import org.jpos.iso.*;
import org.jpos.space.*;
import org.jpos.util.Profiler;
import javax.naming.*;

/*
   In order to run this example you should start Q2 with
   the following files:
 
10_bouncer.xml:

<server class="org.jpos.q2.qbean.ServerAdaptor" 
       logger="Q2" name="ExampleServer">
 <attr name="port" type="java.lang.Integer">9000</attr>
 <channel class="org.jpos.iso.channel.XMLChannel" 
         logger="Q2" packager="org.jpos.iso.packager.XMLPackager">
 </channel>
 <request-listener class="bouncer.Bouncer" logger="Q2" realm="bouncer" />
</server>

20_channel.xml:

<channel-adaptor class="org.jpos.q2.qbean.ChannelAdaptor" logger="Q2">
 <!-- ISOChannel configuration -->
 <channel class="org.jpos.iso.channel.XMLChannel" logger="Q2" 
       packager="org.jpos.iso.packager.XMLPackager">
  <property name="host" value="127.0.0.1" />
  <property name="port" value="9000" />
  <filter class="org.jpos.iso.filter.MacroFilter" direction="outgoing">
   <property name="srcid" value="123456" />
  </filter>
 </channel>
 <in>send</in>
 <out>receive</out>
 <reconnect-delay>1000</reconnect-delay>
</channel-adaptor>

30_spaceproxy.xml:

<spaceproxy class="org.jpos.q2.qbean.SpaceProxyAdaptor" 
       logger="Q2" name="SpaceProxy" />

  
NOTE: Change jndi.properties if you want to connect to a remote
      server.

*/

public class Test {
    public static void main (String[] args) {
        Profiler prof = new Profiler ();
        try {
            InitialContext ctx = new InitialContext ();
            RemoteSpace sp = (RemoteSpace) ctx.lookup ("SpaceProxy");
            prof.checkPoint ("get-proxy");
            ISOMsg m = new ISOMsg();
            m.setMTI ("0800");
            m.set (3, "000000");
            m.set (11, "000000");
            m.set (41, "29110001");
            sp.out ("send", m);
            prof.checkPoint ("send");
            m.dump (System.out, "--> ");

            ISOMsg r = (ISOMsg) sp.in ("receive");
            prof.checkPoint ("receive");
            r.dump (System.out, "<-- ");

            prof.dump (System.out, "");

        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
}

