/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.core;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnvironmentTest {
    static String oldEnv;
    static String oldEnvdir;
    static final String TEST_ENVDIR = "build/resources/test/org/jpos/core/";

    @BeforeAll
    public static void setUp() throws IOException
    {
        oldEnv = System.getProperty("jpos.env");            // save it to restore it later
        oldEnvdir = System.getProperty("jpos.envdir");      // save it to restore it later

        System.setProperty("jpos.env", "testenv");
        System.setProperty("jpos.envdir", TEST_ENVDIR);
        Environment.reload();                               // reload new env from new dir
    }

    @AfterAll
    public static void tearDown() throws Exception {
        // restore old env and envdir
        if (oldEnv != null)
            System.setProperty("jpos.env", oldEnv);
        else
            System.clearProperty("jpos.env");

        if (oldEnvdir != null)
            System.setProperty("jpos.envdir", oldEnvdir);
        else
            System.clearProperty("jpos.envdir");

        Environment.reload();
    }


    // This test only uses System properties to try to override the values in the env file.
    // A more complete version would also set an OS environment variable, which can't be
    // easily done from Java.
    // Changing env vars for tests can be done by using this JUnit extension.
    //      https://junit-pioneer.org/
    @Test
    public void testFromCfg() {
        System.setProperty("test.value", "from sys prop");
        assertEquals("from testenv.yml", Environment.get("$cfg{test.value}"));
        assertEquals("from testenv.yml", System.getProperty("test.sys"));
    }


    @Test
    public void testEmptyDefault() {
        assertEquals("", Environment.get("${test:}"));
    }

    @Test
    public void testObfuscated() {
        System.setProperty("obf.value", "obf::D4sCOgAAAASneiqWUPCruOtNmAU78cg6uBAv3N0/8DSNK6ptaozLAg==");
        assertEquals("OBFUSCATED ABCD", Environment.get("OBFUSCATED ${obf.value}"));
    }

    @Test
    public void testLoop() {
        System.setProperty("loop", "${loop}");
        assertEquals("${loop}", Environment.get("${loop}"));
    }

    @Test
    public void multiExpr() {
        assertEquals("the numbers UNO and DOS and NaN",
                    Environment.get("the numbers ${test.one} and ${test.two} and ${test.three:NaN}"));
    }

    @Test
    public void nestedExprUNO() {
        assertEquals("the nested number is UNO",
                    Environment.get("the nested number is ${test.one:${test.two:NaN}}"));
    }

    @Test
    public void nestedExprDOS() {
        assertEquals("the nested number is DOS",
                    Environment.get("the nested number is ${test.ABC:${test.two:NaN}}"));
    }

    @Test
    public void nestedExprNaN() {
        assertEquals("the nested number is NaN",
                    Environment.get("the nested number is ${test.ABC:${test.XYZ:NaN}}"));
    }

    @Test
    public void equalsLogModeXMLTrue() {
        assertEquals("true", Environment.get("${test.log_mode=xml}"));
    }
    @Test
    public void notEqualsLogModeXMLFalse() {
        assertEquals("false", Environment.get("${!test.log_mode=xml}"));
    }

    @Test
    public void equalsLogModeJSONFalse() {
        assertEquals("false", Environment.get("${test.log_mode=json}"));
    }
    @Test
    public void equalsLogModeJSONTrue() {
        assertEquals("true", Environment.get("${!test.log_mode=json}"));
    }

    @Test
    public void equalsUnsetPropFalse() {
        assertEquals("false", Environment.get("${test.unset=abc}"));
    }
    @Test
    public void notEqualsUnsetPropTrue() {
        assertEquals("true", Environment.get("${!test.unset=abc}"));
    }

    @Test
    public void equalsWithNestedValue() {
        System.setProperty("sys.one", "UNO");
        assertEquals("true", Environment.get("${sys.one=${test.one}}"));
    }
    @Test
    public void notEqualsWithNestedValue() {
        System.setProperty("sys.one", "UNO");
        assertEquals("false", Environment.get("${!sys.one=${test.one}}"));
    }


    @Test
    public void multiLineExpression() {
        assertEquals("The next sentence is true\nThe previous sentence is false\n",
                Environment.getEnvironment().getProperty("The next sentence is ${undefined-property:true}\n" +
                        "The previous sentence is ${undefined-property:false}\n"));
    }

    @Test
    public void multiLineExpressionWithCR() {
        assertEquals("The next sentence is true\rThe previous sentence is false\r",
                Environment.getEnvironment().getProperty("The next sentence is ${undefined-property:true}\r" +
                        "The previous sentence is ${undefined-property:false}\r"));
    }

    @Test
    public void multiLineExpressionWithCRLF() {
        assertEquals("The next sentence is true\r\nThe previous sentence is false\r\n",
                Environment.getEnvironment().getProperty("The next sentence is ${undefined-property:true}\r\n" +
                        "The previous sentence is ${undefined-property:false}\r\n"));
    }

    @Test
    public void testNegateExprFromEnvironment() {
        assertEquals("true", Environment.get("${test.true_boolean}"),
                    "${test.true_boolean} should return \"true\"");

        assertEquals("false", Environment.get("${!test.true_boolean}"),
                    "${!test.true_boolean} should return \"false\"");

        assertEquals("true", Environment.get("${!test.false_boolean}"),
                    "${!test.false_boolean} should return \"true\"");

        // In the yaml file the definition is "test.no_upper: NO",
        // but it's converted to a boolean false by yaml parser.
        // This is converted into a string "false" by the Environment flattening process.
        assertEquals("true", Environment.get("${!test.no_upper}"),
                    "test.no_upper: NO, so ${!test.no_upper} should return \"true\"");

        // The system properties are already strings, soy "YES" is maintained as is
        System.setProperty("enabled.value", "YES");
        assertEquals("no", Environment.get("${!enabled.value}"),
                    "enabled.value=\"YES\", so ${!enabled.value} should return \"no\"");

        assertEquals("DOS", Environment.get("${!test.two}"),
                    "${!test.two} should return DOS, since negate operator is ignored for non-boolean strings");
    }

    @Test
    public void testNegateExprFromSimpleConfiguration() {
        Properties props = new Properties();
        props.put("literal-true", "true");
        props.put("literal-NO",   "NO");

        // In the yaml file the definition is "test.two: DOS",
        props.put(    "expr-test-two",              "${test.two}");                    // must return false, since getBoolean is false for non-booleanish values
        props.put("neg-expr-test-two",              "${!test.two}");                   // same as above, the negation op has no effect on non-booleanish values

        props.put(    "expr-test-true-no-def",      "${test.true_boolean}");
        props.put(    "expr-test-true-def",         "${test.true_boolean:false}");      // must return true, ignoring default

        props.put(    "expr-test-no_upper-no-def",  "${test.no_upper}");
        props.put("neg-expr-test-false-def",        "${!test.false_boolean:false}");    // must return true, ignoring default

        // unresolved properties (they aren't defined anywhere)
        props.put(    "undefined-no-def",  "${__undefined__}");
        props.put(    "undefined-def",     "${__undefined__:true}");
        props.put("neg-undefined-no-def",  "${!__undefined__}");
        props.put("neg-undefined-def",     "${!__undefined__:true}");

        SimpleConfiguration conf = new SimpleConfiguration(props);


        assertTrue(conf.getBoolean("literal-true"), "literal-true");
        assertFalse(conf.getBoolean("literal-NO"),  "literal-NO");

        assertFalse(conf.getBoolean(    "expr-test-two"),        "expr-test-two: ${test.two} must return \"false\" for getBoolean");
        assertFalse(conf.getBoolean("neg-expr-test-two"),    "neg-expr-test-two: ${!test.two} must return \"false\" for getBoolean");

        assertTrue(conf.getBoolean("expr-test-true-no-def"),    "expr-test-true-no-def");
        assertTrue(conf.getBoolean("expr-test-true-def"),       "expr-test-true-def must be true, ignoring default");


        assertFalse(conf.getBoolean("expr-test-no_upper-no-def"),
                "expr-test-no_upper-no-def");
        assertTrue(conf.getBoolean("neg-expr-test-false-def"),
                "neg-expr-test-false-def: ${!test.false_boolean:false} must be true, ignoring default");


        assertFalse(conf.getBoolean("undefined-no-def"),
                "undefined-no-def must be false, since it can't resolve");
        assertTrue(conf.getBoolean("undefined-def"),
                "undefined-def must be true, since the default is true and must be honored");

        assertTrue(conf.getBoolean("neg-undefined-no-def"),
            "neg-undefined-no-def: ${!__undefined__} must be true, since it's the opposite of undefined");
        assertTrue(conf.getBoolean("neg-undefined-def"),
            "neg-undefined-def: ${!__undefined__:true} must be true, since the default is true and must be honored");
    }

}
