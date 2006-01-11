package misc;

import junit.framework.TestCase;
import java.util.Date;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;

/**
* 
* @author cdaszenies
*/
public class ISODateTest extends TestCase {
    public ISODateTest( String s ) {
        super(s);
    }
    public void testDates () throws Exception {
        check ("20041231120000", "0101000000", "050101");
        check ("20050101000000", "0101000000", "050101");
        check ("20051231222456", "0101000000", "060101");
        check ("20060101000000", "0101000000", "060101");
        check ("20060601000000", "0101000000", "060101");
        check ("20060601000000", "1231000000", "051231");
        check ("20060601000000", "0601000000", "060601");
    }
    private void check (String now, String received, String expected) throws Exception {
        Date n = ISODate.parseISODate (now);
        Date p = ISODate.parseISODate (received, n.getTime());
        assertEquals (expected, ISODate.getANSIDate (p));
    }
}

