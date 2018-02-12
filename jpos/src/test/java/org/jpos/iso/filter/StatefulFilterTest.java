/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

package org.jpos.iso.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.BASE24TCPChannel;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.GZIPChannel;
import org.jpos.iso.channel.LogChannel;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.channel.PostChannel;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.EuroSubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.GenericSubFieldPackager;
import org.jpos.iso.packager.GenericValidatingPackager;
import org.jpos.iso.packager.PostPackager;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.space.Space;
import org.jpos.util.LogEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StatefulFilterTest {

    StatefulFilter m_statefulFilter2;
    StatefulFilter m_statefulFilter3;

    @Test
    public void testConstructor() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	assertEquals("m_statefulFilter.getSavedFields().length", 0,
		statefulFilter.getSavedFields().length);
	assertEquals("m_statefulFilter.getIgnoredFields().length", 0,
		statefulFilter.getIgnoredFields().length);
	assertEquals("m_statefulFilter.getKey().length", 2,
		statefulFilter.getKey().length);
	assertEquals("m_statefulFilter.getTimeout()", 60000L,
		statefulFilter.getTimeout());
	assertEquals("m_statefulFilter.getMatchDirection()", 1,
		statefulFilter.getMatchDirection());
    }

    @Test
    public void testFilter() throws Throwable {
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setMatchDirection(61);
	statefulFilter.setIgnoredFields(null);
	ISOMsg m = new ISOMsg("testStatefulFilterMti");
	m.setDirection(61);
	ISOMsg result = statefulFilter.filter(null, m, null);
	assertSame("result", m, result);
    }

    @Test
    public void testFilter1() throws Throwable {
	ISOMsg m = new ISOMsg("testStatefulFilterMti");
	m.setDirection(1);
	ISOMsg result = new StatefulFilter().filter(new NACChannel(), m,
		new LogEvent());
	assertSame("result", m, result);
    }

    @Test
    public void testFilter2() throws Throwable {
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	ISOChannel iSOChannel = new PADChannel();
	LogEvent evt = new LogEvent("testStatefulFilterTag");
	ISOMsg m = mock(ISOMsg.class);

	given(m.getDirection()).willReturn(58);

	ISOMsg result = statefulFilter.filter(iSOChannel, m, evt);
	assertSame("result", m, result);
    }

    @Test
    public void testFilter3() throws Throwable {
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setMatchDirection(58);
	statefulFilter.setSavedFields(null);
	statefulFilter.setIgnoredFields(null);
	ISOMsg m = new ISOMsg("testStatefulFilterMti");
	m.setDirection(58);
	ISOMsg result = statefulFilter
		.filter(new PostChannel("testStatefulFilterHost", 100,
			new XMLPackager()), m, new LogEvent(
			"testStatefulFilterTag", new CTCSubFieldPackager()));
	assertSame("result", m, result);
    }

    @Test
    public void testFilter4() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setConfiguration(new SimpleConfiguration());
	statefulFilter.setSavedFields(null);
	ISOMsg m = new ISOMsg("testStatefulFilterMti");
	m.setDirection(1);
	ISOMsg result = statefulFilter.filter(new LogChannel(), m,
		new LogEvent("testStatefulFilterTag", new Object()));
	assertSame("result", m, result);
    }

    @Test
    public void testFilter5() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	BASE24TCPChannel iSOChannel = mock(BASE24TCPChannel.class);
	LogEvent evt = mock(LogEvent.class);
	ISOMsg m = mock(ISOMsg.class);
	given(m.getString(11)).willReturn(null);
	given(m.getString(41)).willReturn(null);
	given(m.getDirection()).willReturn(0);
	ISOMsg result = statefulFilter.filter(iSOChannel, m, evt);
	assertSame("result", m, result);
    }

    @Test
    public void testFilter6() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setMatchDirection(58);
	statefulFilter.setSavedFields(null);
	statefulFilter.setIgnoredFields(null);
	ISOMsg m = new ISOMsg("testStatefulFilterMti");
	m.setDirection(58);
	ISOMsg result = statefulFilter
		.filter(new PostChannel("testStatefulFilterHost", 100,
			new XMLPackager()), m, new LogEvent(
			"testStatefulFilterTag", new CTCSubFieldPackager()));
	assertEquals("result.getDirection()", 58, result.getDirection());
    }

    @Test
    public void testFilter7() throws Throwable {
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setMatchDirection(0);
	statefulFilter.setSavedFields(null);
	ISOMsg m = new ISOMsg("testStatefulFilterMti");
	ISOMsg result = statefulFilter.filter(
		new PADChannel(new XMLPackager()), m, new LogEvent(
			new Base1SubFieldPackager(), "testStatefulFilterTag"));
	assertSame("result", m, result);
    }

    @Test
    public void testFilterThrowsNullPointerException() throws Throwable {
	ISOMsg m = new ISOMsg(100);
	m.setDirection(100);
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setSpace((Space) null);
	statefulFilter.setMatchDirection(100);
	try {
	    statefulFilter.filter(new PostChannel("testStatefulFilterHost",
		    100, new GenericSubFieldPackager()), m, new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException1() throws Throwable {
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setSpace((Space) null);
	statefulFilter.setMatchDirection(0);
	statefulFilter.setIgnoredFields(null);
	try {
	    statefulFilter.filter(new CSChannel("testStatefulFilterHost", 100,
		    new PostPackager()), new ISOMsg("testStatefulFilterMti"),
		    new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException10() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setSpace((Space) null);
	statefulFilter.setMatchDirection(0);
	statefulFilter.setSavedFields(null);
	statefulFilter.setIgnoredFields(null);
	try {
	    statefulFilter.filter(new CSChannel("testStatefulFilterHost", 100,
		    new PostPackager()), new ISOMsg("testStatefulFilterMti"),
		    new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException2() throws Throwable {
	int[] key = new int[1];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	try {
	    statefulFilter.filter(new BASE24TCPChannel(
		    new GenericValidatingPackager()), null, new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException3() throws Throwable {
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setSpace((Space) null);
	try {
	    statefulFilter.filter(new CSChannel("testStatefulFilterHost", 100,
		    new GenericPackager()),
		    new ISOMsg("testStatefulFilterMti"), new LogEvent(
			    "testStatefulFilterTag", "testString"));
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException4() throws Throwable {
	ISOMsg m = new ISOMsg(100);
	m.setDirection(100);
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setSpace((Space) null);
	statefulFilter.setMatchDirection(100);
	statefulFilter.setSavedFields(null);
	try {
	    statefulFilter.filter(new PostChannel("testStatefulFilterHost",
		    100, new GenericSubFieldPackager()), m, new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException5() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setSpace((Space) null);
	try {
	    statefulFilter.filter(new GZIPChannel(), new ISOMsg(
		    "testStatefulFilterMti"), new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException6() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKeyPrefix(null);
	try {
	    statefulFilter.filter(new PostChannel(new CTCSubFieldPackager()),
		    new ISOMsg("testStatefulFilterMti"), new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException7() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(null);
	try {
	    statefulFilter.filter(new BASE24TCPChannel(
		    new EuroSubFieldPackager()), new ISOMsg(), new LogEvent(
		    new PostPackager(), "testStatefulFilterTag"));
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException8() throws Throwable {
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setSpace((Space) null);
	statefulFilter.setMatchDirection(0);
	statefulFilter.setSavedFields(null);
	statefulFilter.setIgnoredFields(null);
	try {
	    statefulFilter.filter(new CSChannel("testStatefulFilterHost", 100,
		    new PostPackager()), new ISOMsg("testStatefulFilterMti"),
		    new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsNullPointerException9() throws Throwable {
	ISOMsg m = new ISOMsg(100);
	m.setDirection(100);
	int[] key = new int[0];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	statefulFilter.setSpace((Space) null);
	statefulFilter.setMatchDirection(100);
	statefulFilter.setSavedFields(null);
	try {
	    statefulFilter.filter(new PostChannel("testStatefulFilterHost",
		    100, new GenericSubFieldPackager()), m, new LogEvent());
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testFilterThrowsVetoException() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setSpace("testStatefulFilterUri");
	int[] key = new int[0];
	statefulFilter.setVetoUnmatched(true);
	statefulFilter.setKey(key);
	try {
	    statefulFilter.filter(new BASE24TCPChannel(), new ISOMsg(
		    "testStatefulFilterMti"), new LogEvent());
	    fail("Expected VetoException to be thrown");
	} catch (ISOFilter.VetoException ex) {
	    assertEquals("ex.getMessage()", "unmatched iso message",
		    ex.getMessage());
	    assertNull("ex.getNested()", ex.getNested());
	}
    }

    @Test
    public void testFilterThrowsVetoException1() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setVetoUnmatched(true);
	ISOMsg m = new ISOMsg("testStatefulFilterMti");
	m.setDirection(100);
	try {
	    statefulFilter.filter(null, m, null);
	    fail("Expected VetoException to be thrown");
	} catch (ISOFilter.VetoException ex) {
	    assertEquals("ex.getMessage()", "unmatched iso message",
		    ex.getMessage());
	    assertNull("ex.getNested()", ex.getNested());
	}
    }

    @Test
    public void testGetIgnoredField() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] ignoredFields = new int[1];
	ignoredFields[0] = -19;
	statefulFilter.setIgnoredFields(ignoredFields);
	int result = statefulFilter.getIgnoredField(0);
	assertEquals("result", -19, result);
    }

    @Test
    public void testGetIgnoredFieldThrowsArrayIndexOutOfBoundsException()
	    throws Throwable {
	try {
	    new StatefulFilter().getIgnoredField(100);
	    fail("Expected ArrayIndexOutOfBoundsException to be thrown");
	} catch (ArrayIndexOutOfBoundsException ex) {
	    assertEquals("ex.getMessage()", "100", ex.getMessage());
	}
    }

    @Test
    public void testGetIgnoredFieldThrowsNullPointerException()
	    throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setIgnoredFields(null);
	try {
	    statefulFilter.getIgnoredField(100);
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test
    public void testGetKey() throws Throwable {
	int[] key = new int[1];
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKey(key);
	int[] key2 = new int[3];
	statefulFilter.setKey(key2);
	statefulFilter.setIgnoredFields(null);
	statefulFilter.setOverwriteOriginalFields(true);
	int[] key3 = new int[3];
	statefulFilter.setKey(key3);
	int result = statefulFilter.getKey(0);
	assertEquals("result", 0, result);
    }

    @Test
    public void testGetKey1() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] ignoredFields = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields);
	int[] ignoredFields2 = new int[2];
	statefulFilter.setIgnoredFields(ignoredFields2);
	int[] key = new int[0];
	statefulFilter.setKey(key);
	int[] ignoredFields3 = new int[1];
	int[] key2 = new int[0];
	statefulFilter.setKey(key2);
	int[] ignoredFields4 = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields4);
	statefulFilter.setIgnoredFields(ignoredFields3);
	int[] key3 = new int[4];
	key3[1] = 19;
	StatefulFilter statefulFilter2 = new StatefulFilter();
	statefulFilter2.setOverwriteOriginalFields(true);
	int[] key4 = new int[3];
	statefulFilter2.setKey(key4);
	int[] ignoredFields5 = new int[0];
	statefulFilter2.setIgnoredFields(ignoredFields5);
	statefulFilter2.setKey(key3);
	int result = statefulFilter2.getKey(1);
	assertEquals("result", 19, result);
    }

    @Test
    public void testGetKeyThrowsArrayIndexOutOfBoundsException()
	    throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	StatefulFilter statefulFilter2 = new StatefulFilter();
	statefulFilter2.setOverwriteOriginalFields(true);
	int[] key = new int[3];
	statefulFilter2.setKey(key);
	int[] ignoredFields = new int[0];
	statefulFilter2.setIgnoredFields(ignoredFields);
	statefulFilter.setIgnoredFields(ignoredFields);
	statefulFilter.setOverwriteOriginalFields(false);
	int[] key2 = new int[0];
	statefulFilter.setKey(key2);
	try {
	    statefulFilter.getKey(100);
	    fail("Expected ArrayIndexOutOfBoundsException to be thrown");
	} catch (ArrayIndexOutOfBoundsException ex) {
	    assertEquals("ex.getMessage()", "100", ex.getMessage());
	}
    }

    @Test
    public void testGetSavedField() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] savedFields = new int[3];
	statefulFilter.setSavedFields(savedFields);
	int result = statefulFilter.getSavedField(0);
	assertEquals("result", 0, result);
    }

    @Test
    public void testGetSavedField1() throws Throwable {
	int[] savedFields = new int[3];
	savedFields[0] = -4;
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setSavedFields(savedFields);
	int result = statefulFilter.getSavedField(0);
	assertEquals("result", -4, result);
    }

    @Test
    public void testGetSavedFieldThrowsArrayIndexOutOfBoundsException()
	    throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] ignoredFields = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields);
	StatefulFilter statefulFilter2 = new StatefulFilter();
	int[] ignoredFields2 = new int[2];
	statefulFilter.setIgnoredFields(ignoredFields2);
	int[] key = new int[0];
	statefulFilter.setKey(key);
	int[] ignoredFields3 = new int[1];
	int[] key2 = new int[0];
	statefulFilter.setKey(key2);
	int[] ignoredFields4 = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields4);
	statefulFilter.setIgnoredFields(ignoredFields3);
	StatefulFilter statefulFilter3 = new StatefulFilter();
	statefulFilter3.setOverwriteOriginalFields(true);
	int[] key3 = new int[3];
	statefulFilter3.setKey(key3);
	int[] ignoredFields5 = new int[0];
	statefulFilter3.setIgnoredFields(ignoredFields5);
	statefulFilter2.setIgnoredFields(ignoredFields5);
	int[] ignoredFields6 = new int[0];
	statefulFilter2.setIgnoredFields(ignoredFields6);
	statefulFilter2.setConfiguration(new SimpleConfiguration());
	statefulFilter2.setOverwriteOriginalFields(false);
	int[] key4 = new int[0];
	statefulFilter2.setKey(key4);
	try {
	    statefulFilter2.getSavedField(100);
	    fail("Expected ArrayIndexOutOfBoundsException to be thrown");
	} catch (ArrayIndexOutOfBoundsException ex) {
	    assertEquals("ex.getMessage()", "100", ex.getMessage());
	}
    }

    @Test
    public void testSetConfiguration() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] ignoredFields = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields);
	statefulFilter.setConfiguration(new SimpleConfiguration());
	assertTrue("m_statefulFilter.isOverwriteOriginalFields()",
		statefulFilter.isOverwriteOriginalFields());
	assertEquals("m_statefulFilter.getSavedFields().length", 0,
		statefulFilter.getSavedFields().length);
	assertEquals("m_statefulFilter.getIgnoredFields().length", 0,
		statefulFilter.getIgnoredFields().length);
	assertEquals("m_statefulFilter.getKey().length", 2,
		statefulFilter.getKey().length);
	assertFalse("m_statefulFilter.isVetoUnmatched()",
		statefulFilter.isVetoUnmatched());
	assertEquals("m_statefulFilter.getMatchDirection()", 1,
		statefulFilter.getMatchDirection());
	assertEquals("m_statefulFilter.getTimeout()", 60000L,
		statefulFilter.getTimeout());
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException()
	    throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	Configuration cfg = new SubConfiguration();
	try {
	    statefulFilter.setConfiguration(cfg);
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	    assertFalse("m_statefulFilter.isOverwriteOriginalFields()",
		    statefulFilter.isOverwriteOriginalFields());
	    assertEquals("m_statefulFilter.getSavedFields().length", 0,
		    statefulFilter.getSavedFields().length);
	    assertEquals("m_statefulFilter.getIgnoredFields().length", 0,
		    statefulFilter.getIgnoredFields().length);
	    assertEquals("m_statefulFilter.getKey().length", 2,
		    statefulFilter.getKey().length);
	    assertFalse("m_statefulFilter.isVetoUnmatched()",
		    statefulFilter.isVetoUnmatched());
	    assertEquals("m_statefulFilter.getMatchDirection()", 1,
		    statefulFilter.getMatchDirection());
	    assertEquals("m_statefulFilter.getTimeout()", 60000L,
		    statefulFilter.getTimeout());
	}
    }

    @Test
    public void testSetIgnoredFieldThrowsArrayIndexOutOfBoundsException()
	    throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	StatefulFilter statefulFilter2 = new StatefulFilter();
	statefulFilter2.setOverwriteOriginalFields(true);
	int[] key = new int[3];
	statefulFilter2.setKey(key);
	int[] ignoredFields = new int[0];
	statefulFilter2.setIgnoredFields(ignoredFields);
	statefulFilter.setIgnoredFields(ignoredFields);
	statefulFilter.setOverwriteOriginalFields(false);
	int[] key2 = new int[0];
	statefulFilter.setKey(key2);
	try {
	    statefulFilter.setIgnoredField(100, 1000);
	    fail("Expected ArrayIndexOutOfBoundsException to be thrown");
	} catch (ArrayIndexOutOfBoundsException ex) {
	    assertEquals("ex.getMessage()", "100", ex.getMessage());
	    assertSame("m_statefulFilter.getIgnoredFields()", ignoredFields,
		    statefulFilter.getIgnoredFields());
	}
    }

    @Test
    public void testSetKey() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setOverwriteOriginalFields(true);
	int[] key = new int[3];
	statefulFilter.setKey(key);
	assertSame("m_statefulFilter.getKey()", key, statefulFilter.getKey());
	assertEquals("m_statefulFilter.getKey()[0]", 0,
		statefulFilter.getKey()[0]);
    }

    @Test
    public void testSetKey1() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setOverwriteOriginalFields(true);
	int[] key = new int[3];
	statefulFilter.setKey(key);
	int[] ignoredFields = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields);
	StatefulFilter statefulFilter2 = new StatefulFilter();
	statefulFilter2.setOverwriteOriginalFields(false);
	int[] key2 = new int[2];
	statefulFilter2.setKey(key2);
	int[] ignoredFields2 = new int[2];
	statefulFilter2.setIgnoredFields(ignoredFields2);
	StatefulFilter statefulFilter3 = new StatefulFilter();
	int[] key3 = new int[2];
	statefulFilter3.setKey(key3);
	statefulFilter3.setOverwriteOriginalFields(true);
	int[] ignoredFields3 = new int[1];
	statefulFilter3.setIgnoredFields(ignoredFields3);
	statefulFilter3.setKey(0, 100);
	assertEquals("m_statefulFilter3.getKey()[0]", 100,
		statefulFilter3.getKey()[0]);
	assertSame("m_statefulFilter3.getKey()", key3, statefulFilter3.getKey());
    }

    @Test
    public void testSetKeyPrefix() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setKeyPrefix("testString");
	assertEquals("m_statefulFilter.getKeyPrefix()", "testString",
		statefulFilter.getKeyPrefix());
    }

    @Test
    public void testSetKeyThrowsArrayIndexOutOfBoundsException()
	    throws Throwable {
	int[] key = new int[1];
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] ignoredFields = new int[2];
	statefulFilter.setIgnoredFields(ignoredFields);
	int[] key2 = new int[2];
	statefulFilter.setKey(key2);
	statefulFilter.setKey(key);
	try {
	    statefulFilter.setKey(100, 1000);
	    fail("Expected ArrayIndexOutOfBoundsException to be thrown");
	} catch (ArrayIndexOutOfBoundsException ex) {
	    assertEquals("ex.getMessage()", "100", ex.getMessage());
	    assertSame("m_statefulFilter.getKey()", key,
		    statefulFilter.getKey());
	}
    }

    @Test
    public void testSetKeyThrowsNullPointerException() throws Throwable {
	StatefulFilter statefulFilter2 = setupStatefulFilterState();
	try {
	    statefulFilter2.setKey(100, 1000);
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	    assertNull("m_statefulFilter2.getKey()", statefulFilter2.getKey());
	}
    }

    @Test
    public void testSetMatchDirection() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setMatchDirection(100);
	assertEquals("m_statefulFilter.getMatchDirection()", 100,
		statefulFilter.getMatchDirection());
    }

    @Test
    public void testSetOverwriteOriginalFields() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setSpace("testStatefulFilterUri");
	statefulFilter.setOverwriteOriginalFields(true);
	assertTrue("m_statefulFilter.isOverwriteOriginalFields()",
		statefulFilter.isOverwriteOriginalFields());
    }

    @Test
    public void testSetSavedFields() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] savedFields = new int[3];
	statefulFilter.setSavedFields(savedFields);
	assertSame("m_statefulFilter.getSavedFields()", savedFields,
		statefulFilter.getSavedFields());
    }

    @Test
    public void testSetSavedFieldThrowsArrayIndexOutOfBoundsException()
	    throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] savedFields = new int[0];
	statefulFilter.setSavedFields(savedFields);
	try {
	    statefulFilter.setSavedField(100, 1000);
	    fail("Expected ArrayIndexOutOfBoundsException to be thrown");
	} catch (ArrayIndexOutOfBoundsException ex) {
	    assertEquals("ex.getMessage()", "100", ex.getMessage());
	    assertSame("m_statefulFilter.getSavedFields()", savedFields,
		    statefulFilter.getSavedFields());
	}
    }

    @Test(expected = NullPointerException.class)
    public void testSetSpaceThrowsNullPointerException() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setSpace("Invalid space: ");

    }

    @Test
    public void testSetTimeout() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	statefulFilter.setTimeout(100L);
	assertEquals("m_statefulFilter.getTimeout()", 100L,
		statefulFilter.getTimeout());
    }

    @Test
    public void testSetVetoUnmatched() throws Throwable {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] ignoredFields = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields);
	statefulFilter.setVetoUnmatched(true);
	assertTrue("m_statefulFilter.isVetoUnmatched()",
		statefulFilter.isVetoUnmatched());
    }

    private StatefulFilter setupStatefulFilterState() {
	StatefulFilter statefulFilter = new StatefulFilter();
	int[] ignoredFields = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields);
	m_statefulFilter2 = new StatefulFilter();
	int[] ignoredFields2 = new int[2];
	statefulFilter.setIgnoredFields(ignoredFields2);
	int[] key = new int[0];
	statefulFilter.setKey(key);
	int[] ignoredFields3 = new int[1];
	int[] key2 = new int[0];
	statefulFilter.setKey(key2);
	int[] ignoredFields4 = new int[0];
	statefulFilter.setIgnoredFields(ignoredFields4);
	statefulFilter.setIgnoredFields(ignoredFields3);
	m_statefulFilter3 = new StatefulFilter();
	m_statefulFilter3.setOverwriteOriginalFields(true);
	int[] key3 = new int[3];
	m_statefulFilter3.setKey(key3);
	int[] ignoredFields5 = new int[0];
	m_statefulFilter3.setIgnoredFields(ignoredFields5);
	m_statefulFilter2.setIgnoredFields(ignoredFields5);
	int[] ignoredFields6 = new int[0];
	m_statefulFilter2.setIgnoredFields(ignoredFields6);
	m_statefulFilter2.setOverwriteOriginalFields(false);
	int[] key4 = new int[0];
	m_statefulFilter2.setKey(key4);
	m_statefulFilter2.setKey(null);
	return m_statefulFilter2;
    }
}
