package q2.bouncer;

import org.jpos.iso.*;
import org.jpos.util.*;

public class Bouncer 
    extends SimpleLogSource
    implements ISORequestListener {
    public boolean process (ISOSource source, ISOMsg m) 
    {
        try {
            m = (ISOMsg) m.clone ();
            m.setResponseMTI ();
            m.set (39, "00");
            m.set (38, 
                ISOUtil.zeropad (
                    Long.toString (System.currentTimeMillis () % 1000000), 6
                )
            );
            source.send (m);
        } catch (Exception e) {
            Logger.log (new LogEvent (this, "warn", e));
        }
        return true;
    }
}
