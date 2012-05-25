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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

public class NameRegistrarTest {

    private static final boolean WITH_DETAIL = true;

    @Before
    public void setup() {
	NameRegistrar.register("test1", "testValue1");
	NameRegistrar.register("test2", "testValue2");
    }

    @Test
    public void testDumpWithoutDetailAndIndent() throws Throwable {
	String expected = "testNameRegistrarIndent--- name-registrar ---\n"
		+ "testNameRegistrarIndent  test1: java.lang.String\n"
		+ "testNameRegistrarIndent  test2: java.lang.String\n";

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	NameRegistrar.getInstance().dump(new PrintStream(out),
		"testNameRegistrarIndent");

	String result = out.toString();
	assertThat(result, is(expected));
    }

    @Test
    public void testDumpWithDetailAndIndent() throws Throwable {
	String expected = "+--- name-registrar ---\n"
		+ "+  test1: java.lang.String\n"
		+ "+  test2: java.lang.String\n";

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	NameRegistrar.getInstance()
		.dump(new PrintStream(out), "+", WITH_DETAIL);

	String result = out.toString();
	assertThat(result, is(expected));
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
	NameRegistrar.unregister("test1");
	NameRegistrar.get("test1");
    }

    @Test
    public void testNotFoundExceptionConstructor1() throws Throwable {
	NameRegistrar.NotFoundException notFoundException = new NameRegistrar.NotFoundException(
		"testNotFoundExceptionDetail");
	assertEquals("notFoundException.getMessage()",
		"testNotFoundExceptionDetail", notFoundException.getMessage());
    }

}
