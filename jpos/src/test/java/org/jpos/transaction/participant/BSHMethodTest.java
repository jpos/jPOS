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

package org.jpos.transaction.participant;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import bsh.EvalError;
import bsh.ParseException;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class BSHMethodTest {
    @Mock
    Element e;
    private Map arguments;

    @Before
    public void onSetup() {
        arguments = new HashMap();
    }

    @Test
    public void testCreateBshMethod() throws Throwable {
        BSHMethod result = BSHMethod.createBshMethod(null);
        assertNull("result", result);
    }

    @Test
    public void testCreateBshMethod1() throws Throwable {
        BSHMethod result = BSHMethod.createBshMethod(new Element("testBSHMethodName", Namespace.NO_NAMESPACE));
        assertNull("result", result);
    }

    @Test
    public void testCreateBshMethod2() throws Throwable {
        when(e.getTextTrim()).thenReturn("testStringtestStringtestStringIll(gal Surrogate Pair");
        when(e.getAttributeValue("file")).thenReturn(null);
        BSHMethod result = BSHMethod.createBshMethod(e);
        assertNotNull("result", result);
    }

    @Test
    public void testExecute() throws Throwable {
        HashMap result = (HashMap) new BSHMethod("testBSHMethodBshData", false).execute(arguments, new ArrayList());
        assertEquals("result.size()", 0, result.size());
    }

    @Test
    public void testExecute1() throws Throwable {
        Collection<String> returnNames = new ArrayList();
        returnNames.add("testString");
        HashMap result = (HashMap) new BSHMethod("testBSHMethodBshData", false).execute(new HashMap(), returnNames);
        assertEquals("result.size()", 1, result.size());
        assertNull("(HashMap) result.get(\"testString\")", result.get("testString"));
    }

    @Test
    public void testExecute2() throws Throwable {
        HashMap arguments = new HashMap();
        arguments.put("testString", "testString");
        String result = (String) new BSHMethod("testBSHMethodBshData", false).execute(arguments, "testString");
        assertEquals("result", "testString", result);
    }

    @Test
    public void testExecute3() throws Throwable {
        Map arguments = new HashMap(100, 100.0F);
        Integer integer = Integer.valueOf(33);
        arguments.put("testString", integer);
        Integer result = (Integer) new BSHMethod("testBSHMethodBshData", false).execute(arguments, "testString");
        assertSame("result", integer, result);
    }

    @Test
    public void testExecute4() throws Throwable {
        Integer integer = Integer.valueOf(0);
        arguments.put("testString", integer);
        Integer result = (Integer) new BSHMethod("testBSHMethodBshData", false).execute(arguments, "testString");
        assertSame("result", integer, result);
    }

    @Test
    public void testExecute5() throws Throwable {
        Object result = new BSHMethod("testBSHMethodBshData", false).execute(new HashMap(), "testBSHMethodResultName");
        assertNull("result", result);
    }

    @Test
    public void testExecuteThrowsClassCastException() throws Throwable {
        Map<Integer, String> arguments = new HashMap();
        arguments.put(Integer.valueOf(1), "1");
        Collection<String> returnNames = new ArrayList();
        try {
            new BSHMethod("testBSHMethodBshData", true).execute(arguments, returnNames);
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
            assertEquals("(HashMap) arguments.size()", 1, arguments.size());
            assertEquals("(ArrayList) returnNames.size()", 0, returnNames.size());
        }
    }

    @Test
    public void testExecuteThrowsClassCastException1() throws Throwable {
        HashMap<Object, Comparable> arguments = new HashMap();
        arguments.put(new Object(), "testString");
        try {
            new BSHMethod("testBSHMethodBshData", true).execute(arguments, "testBSHMethodResultName");
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
            assertEquals("(HashMap) arguments.size()", 1, arguments.size());
        }
    }

    @Test
    public void testExecuteThrowsEvalError() throws Throwable {
        Collection returnNames = new ArrayList();
        try {
            new BSHMethod("testBSHMethod\rBshData", false).execute(arguments, returnNames);
            fail("Expected EvalError to be thrown");
        } catch (EvalError ex) {
            assertEquals(
                    "ex.getMessage()",
                    "Sourced file: inline evaluation of: ``testBSHMethod BshData;'' : Typed variable declaration : Class: testBSHMethod not found in namespace",
                    ex.getMessage());
            assertEquals(
                    "ex.getMessage()",
                    "Sourced file: inline evaluation of: ``testBSHMethod BshData;'' : Typed variable declaration : Class: testBSHMethod not found in namespace",
                    ex.getMessage());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
            assertEquals("(ArrayList) returnNames.size()", 0, returnNames.size());
        }
    }

    @Test
    public void testExecuteThrowsEvalError1() throws Throwable {
        Map<Object, Comparable<String>> arguments = new HashMap();
        try {
            new BSHMethod("testBSHMethod\rBshData", false).execute(arguments, "testBSHMethodResultName");
            fail("Expected EvalError to be thrown");
        } catch (EvalError ex) {
            assertEquals(
                    "ex.getMessage()",
                    "Sourced file: inline evaluation of: ``testBSHMethod BshData;'' : Typed variable declaration : Class: testBSHMethod not found in namespace",
                    ex.getMessage());
            assertEquals(
                    "ex.getMessage()",
                    "Sourced file: inline evaluation of: ``testBSHMethod BshData;'' : Typed variable declaration : Class: testBSHMethod not found in namespace",
                    ex.getMessage());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
        }
    }

    @Test
    public void testExecuteThrowsFileNotFoundException() throws Throwable {
        Map<Integer, ?> arguments = new HashMap();
        Collection<?> returnNames = new ArrayList();
        try {
            new BSHMethod("testBSHMethodBshData", true).execute(arguments, returnNames);
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals("ex.getClass()", FileNotFoundException.class, ex.getClass());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
            assertEquals("(ArrayList) returnNames.size()", 0, returnNames.size());
        }
    }

    @Test
    public void testExecuteThrowsFileNotFoundException1() throws Throwable {
        try {
            new BSHMethod("testBSHMethodBshData", true).execute(arguments, "testBSHMethodResultName");
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals("ex.getClass()", FileNotFoundException.class, ex.getClass());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
        }
    }

    @Test
    public void testExecuteThrowsNullPointerException() throws Throwable {
        Collection returnNames = new ArrayList();
        try {
            new BSHMethod(null, false).execute(arguments, returnNames);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
            assertEquals("(ArrayList) returnNames.size()", 0, returnNames.size());
        }
    }

    @Test
    public void testExecuteThrowsNullPointerException1() throws Throwable {
        try {
            new BSHMethod("testBSHMethodBshData", false).execute(arguments, (Collection) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
        }
    }

    @Test
    public void testExecuteThrowsNullPointerException2() throws Throwable {
        Collection returnNames = new ArrayList();
        try {
            new BSHMethod(null, true).execute(arguments, returnNames);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
            assertEquals("(ArrayList) returnNames.size()", 0, returnNames.size());
        }
    }

    @Test
    public void testExecuteThrowsParseException() throws Throwable {
        Element e = new Element("testBSHMethodName", Namespace.NO_NAMESPACE);
        e.addContent("XXXXXXX XXXXXXXXX XXXX");
        e.setAttributes(new ArrayList());
        Map<Integer, ?> arguments = new HashMap();
        Collection<?> returnNames = new ArrayList();
        try {
            BSHMethod.createBshMethod(e).execute(arguments, returnNames);
            fail("Expected ParseException to be thrown");
        } catch (ParseException ex) {
            assertThat(ex.getMessage(), allOf(notNullValue(), containsString("line 1, column 19")));
        }
    }

    @Test
    public void testExecuteThrowsParseException1() throws Throwable {
        Element e = new Element("testBSHMethodName", Namespace.NO_NAMESPACE);
        e.addContent("XXXXXXX XXXXXXXXX XXXX");
        e.setAttributes(new ArrayList());
        try {
            BSHMethod.createBshMethod(e).execute(arguments, "testBSHMethodResultName");
            fail("Expected ParseException to be thrown");
        } catch (ParseException ex) {
            assertThat(ex.getMessage(), allOf(notNullValue(), containsString("line 1, column 19")));
        }
    }

    @Test
    public void testInitInterpreterThrowsClassCastException() throws Throwable {
        BSHMethod bSHMethod = new BSHMethod("testBSHMethodBshData", true);
        Map<Object, Object> arguments = new HashMap();
        arguments.put(new Object(), "char7set");
        try {
            bSHMethod.initInterpreter(arguments);
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
            assertEquals("(HashMap) arguments.size()", 1, arguments.size());
        }
    }

    @Test
    public void testInitInterpreterThrowsEvalError() throws Throwable {
        Element e = new Element("testBSHMethodName", Namespace.NO_NAMESPACE);
        e.addContent("testBSHMethod\rStr");
        e.setAttributes(new ArrayList());
        BSHMethod bshMethod = BSHMethod.createBshMethod(e);
        try {
            bshMethod.initInterpreter(arguments);
            fail("Expected EvalError to be thrown");
        } catch (EvalError ex) {
            assertEquals(
                    "ex.getMessage()",
                    "Sourced file: inline evaluation of: ``testBSHMethod Str;'' : Typed variable declaration : Class: testBSHMethod not found in namespace",
                    ex.getMessage());
            assertEquals(
                    "ex.getMessage()",
                    "Sourced file: inline evaluation of: ``testBSHMethod Str;'' : Typed variable declaration : Class: testBSHMethod not found in namespace",
                    ex.getMessage());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
        }
    }

    @Test
    public void testInitInterpreterThrowsFileNotFoundException() throws Throwable {
        BSHMethod bSHMethod = new BSHMethod("testBSHMethodBshData", true);
        arguments.put("lt", "");
        try {
            bSHMethod.initInterpreter(arguments);
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals("ex.getClass()", FileNotFoundException.class, ex.getClass());
            assertEquals("(HashMap) arguments.size()", 1, arguments.size());
        }
    }

    @Test
    public void testInitInterpreterThrowsFileNotFoundException1() throws Throwable {
        BSHMethod bSHMethod = new BSHMethod("testBSHMethodBshData", true);
        try {
            bSHMethod.initInterpreter(arguments);
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals("ex.getClass()", FileNotFoundException.class, ex.getClass());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
        }
    }

    @Test
    public void testInitInterpreterThrowsNullPointerException() throws Throwable {
        BSHMethod bSHMethod = new BSHMethod(null, false);
        try {
            bSHMethod.initInterpreter(arguments);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(HashMap) arguments.size()", 0, arguments.size());
        }
    }

    @Test
    public void testInitInterpreterThrowsNullPointerException1() throws Throwable {
        BSHMethod bSHMethod = new BSHMethod("testBSHMethodBshData", true);
        try {
            bSHMethod.initInterpreter(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testInitInterpreterThrowsParseException() throws Throwable {
        arguments.put("", new Object());
        BSHMethod bSHMethod = new BSHMethod(":]Z", false);
        try {
            bSHMethod.initInterpreter(arguments);
            fail("Expected ParseException to be thrown");
        } catch (ParseException ex) {
            assertThat(ex.getMessage(), allOf(notNullValue(), containsString("line 1, column 1")));
        }
    }
}
