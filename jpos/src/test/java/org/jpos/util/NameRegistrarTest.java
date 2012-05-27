/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.util;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NameRegistrarTest {

    private static final boolean WITH_DETAIL = true;

    @BeforeClass
    public static void onSetup() {
	NameRegistrar.register("test1", "testValue1");
	NameRegistrar.register("test2", "testValue2");
    }

    @AfterClass
    public static void tearDown() {
	NameRegistrar.unregister("test1");
	NameRegistrar.unregister("test2");
    }

    @Test
    public void testDumpWithoutDetail() throws Throwable {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	NameRegistrar.getInstance().dump(new PrintStream(out), ">");

	assertThat(
		out.toString(),
		allOf(containsString(">--- name-registrar ---\n"),
			containsString("test1"), containsString("test2")));
    }

    @Test
    public void testDumpWithDetail() throws Throwable {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	NameRegistrar.getInstance()
		.dump(new PrintStream(out), "+", WITH_DETAIL);

	assertThat(
		out.toString(),
		allOf(containsString("+--- name-registrar ---\n"),
			containsString("test1"), containsString("test2")));
    }

    @Test
    public void testGetInstance() throws Throwable {
	NameRegistrar result = NameRegistrar.getInstance();
	assertThat(result, is(sameInstance(NameRegistrar.getInstance())));
    }

    @Test
    public void testGet() throws Exception {
	String value = (String) NameRegistrar.get("test1");
	assertThat(value, is("testValue1"));
    }

    @Test(expected = NameRegistrar.NotFoundException.class)
    public void testGetThrowsNotFoundException() throws Throwable {
	NameRegistrar.get("NonexistentKey");
    }

    @Test
    public void testGetIfExists() throws Throwable {
	String value = (String) NameRegistrar.getIfExists("test2");
	assertThat(value, is("testValue2"));
    }

    @Test
    public void testGetIfExistsWithNotFoundKey() throws Exception {
	String value = (String) NameRegistrar.getIfExists("NonexistentKey");
	assertThat(value, is(nullValue()));
    }

    @Test(expected = NameRegistrar.NotFoundException.class)
    public void testUnregister() throws Exception {
	NameRegistrar.register("test3", "someTest3Value");
	assertThat((String) NameRegistrar.get("test3"), is("someTest3Value"));
	NameRegistrar.unregister("test3");
	NameRegistrar.get("test3");
    }

    @Test
    public void testUnregisterUnknownKeyDoesNotThrowException()
	    throws Exception {
	NameRegistrar.unregister("unknownKey");
    }

    @Test
    public void testNotFoundExceptionConstructor1() throws Throwable {
	NameRegistrar.NotFoundException notFoundException = new NameRegistrar.NotFoundException(
		"testNotFoundExceptionDetail");
	assertEquals("notFoundException.getMessage()",
		"testNotFoundExceptionDetail", notFoundException.getMessage());
    }

}
