package misc;

import junit.framework.TestCase;
import org.jpos.iso.ISOCurrency;
import org.jpos.iso.ISOException;

/**
* 
* @author cdaszenies
*/
public class ISOCurrencyTest extends TestCase {
    public ISOCurrencyTest( String s ) {
        super(s);
    }

    public void testConvertToIsoMsg() {
        assertEquals("000000003848",
            ISOCurrency.convertToIsoMsg(38.48,"EUR"));
    } 
}

