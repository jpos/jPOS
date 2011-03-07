package org.jpos.iso;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.jpos.util.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ISOBasePackagerTest {

    ISOBasePackager iSOBasePackager;
    ISOFieldPackager[] iSOFieldPackagers;
    @Mock
    ISOFieldPackager iSOFieldPackager;

    @Before
    public void setUp() throws Exception {
        iSOBasePackager = new ISOBasePackager() {
        };
        iSOFieldPackagers = new ISOFieldPackager[] { iSOFieldPackager };
    }

    @Test
    public void testGetDescription() {
        assertThat(iSOBasePackager.getDescription(), is("org.jpos.iso.ISOBasePackagerTest$1"));
    }

    @Test
    public void testGetHeaderLength() {
        iSOBasePackager.setHeaderLength(9876);
        assertThat(iSOBasePackager.getHeaderLength(), is(9876));
    }

    @Test
    public void testGetLogger() {
        Logger logger = mock(Logger.class);
        iSOBasePackager.setLogger(logger, "testRealm");
        assertThat(iSOBasePackager.getLogger(), is(logger));
    }

    @Test
    public void testGetRealm() {
        Logger logger = mock(Logger.class);
        iSOBasePackager.setLogger(logger, "testRealm");
        assertThat(iSOBasePackager.getRealm(), is("testRealm"));
    }

    @Test
    public void testGetMaxValidField() {
        assertThat(iSOBasePackager.getMaxValidField(), is(128));
    }

    @Test
    public void testCreateISOMsg() {
        assertThat(iSOBasePackager.createISOMsg(), is(instanceOf(ISOMsg.class)));
    }

    @Test
    public void testSetFieldPackager() {
        iSOBasePackager.setFieldPackager(iSOFieldPackagers);
        assertThat(iSOBasePackager.getFieldPackager(0), is(iSOFieldPackager));
    }

}
