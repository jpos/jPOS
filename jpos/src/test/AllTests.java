import junit.framework.*;

import org.apache.log4j.*;
import org.apache.log4j.xml.*;
import org.jpos.util.*;

public class AllTests {
    static Logger logger;
    public static Test suite ( ) {
        DOMConfigurator.configureAndWatch (
            System.getProperty ("log4j.config"), 5000
        );
        logger = new Logger();
        logger.addListener (new Log4JListener());
        TestSuite suite = new TestSuite("jPOS Tests");
        suite.addTestSuite (packagers.Test.class);
        return suite;
    }
    public static Logger getLogger() {
        return logger;
    }
}

