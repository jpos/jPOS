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
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    public void cleanupTestProperties() {
        // Clear properties set by individual tests to prevent cross-test interference
        System.clearProperty("test.value");
        System.clearProperty("obf.value");
        System.clearProperty("loop");
        System.clearProperty("sys.one");
        System.clearProperty("enabled.value");
        // Clear all ut.* prefixed properties
        System.getProperties().stringPropertyNames().stream()
            .filter(name -> name.startsWith("ut."))
            .forEach(System::clearProperty);
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

    @Test
    public void nestedDefaultTwoLevels_usesFirstResolvedProperty() {
        System.clearProperty("ut.a");
        System.setProperty("ut.b", "B");
        System.setProperty("ut.c", "C");

        assertEquals("B", Environment.get("${ut.a:${ut.b:${ut.c:Z}}}"),
          "ut.a undefined -> use ut.b; ut.b defined -> B");
    }

    @Test
    public void nestedDefaultThreeLevels_fallsThroughToLiteral() {
        System.clearProperty("ut.a");
        System.clearProperty("ut.b");
        System.clearProperty("ut.c");

        assertEquals("Z", Environment.get("${ut.a:${ut.b:${ut.c:Z}}}"),
          "ut.a/ut.b/ut.c undefined -> literal default Z");
    }

    @Test
    public void nestedDefaultEmptyLiteral_isHonored() {
        System.clearProperty("ut.a");
        System.clearProperty("ut.b");

        assertEquals("", Environment.get("${ut.a:${ut.b:}}"),
          "Empty literal default must be honored");
    }

    @Test
    public void nestedDefaultContainsMultipleTokens_eachResolvesIndependently() {
        System.clearProperty("ut.host");
        System.setProperty("ut.port", "8080");

        // default contains another token and literal text
        assertEquals("http://localhost:8080",
          Environment.get("${ut.host:http://localhost:${ut.port:80}}"),
          "Default should be expanded after being selected");
    }

    @Test
    public void nestedDefaultContainsUnresolvedToken_preservesTokenText() {
        System.clearProperty("ut.url");
        System.clearProperty("ut.missing");

        // Here default is selected; it contains an unresolved token with its own default.
        // The inner token resolves to its default value "X".
        assertEquals("prefix X suffix",
          Environment.get("${ut.url:prefix ${ut.missing:X} suffix}"));
    }

    @Test
    public void negationOfUndefined_isTrue() {
        System.clearProperty("ut.undef");
        assertEquals("true", Environment.get("${!ut.undef}"),
          "Negated undefined is interpreted as true");
    }

    @Test
    public void negationWithDefaultLiteralBoolean_isNotNegated() {
        System.clearProperty("ut.undef");

        // Default literal is "true"; per current semantics, default literal boolean should not be negated.
        assertEquals("true", Environment.get("${!ut.undef:true}"),
          "Default literal boolean must not be negated");
        assertEquals("false", Environment.get("${!ut.undef:false}"),
          "Default literal boolean must not be negated");
    }

    @Test
    public void negationAppliedToResolvedBoolean() {
        System.setProperty("ut.bool", "true");
        assertEquals("false", Environment.get("${!ut.bool}"));

        System.setProperty("ut.bool", "false");
        assertEquals("true", Environment.get("${!ut.bool}"));
    }

    @Test
    public void negationIgnoredForNonBooleanishValues_evenWithWhitespace() {
        System.setProperty("ut.text", "  DOS  ");
        // trim/lower used for booleanish lookup; non-booleanish should return unchanged
        assertEquals("  DOS  ", Environment.get("${!ut.text}"));
    }

    @Test
    public void negationWithDefaultNonBooleanLiteral_isNotApplied() {
        System.clearProperty("ut.undef");
        // default literal "YES" is not booleanish; also defaults should not be negated anyway
        assertEquals("YES", Environment.get("${!ut.undef:YES}"));
    }

    @Test
    public void equalsRhsDereferencesNestedExpression_trueCase() {
        System.setProperty("ut.lhs", "UNO");
        System.setProperty("ut.rhs", "UNO");
        assertEquals("true", Environment.get("${ut.lhs=${ut.rhs}}"),
          "RHS must be dereferenced before comparison");
    }

    @Test
    public void equalsRhsDereferencesNestedExpression_falseCase() {
        System.setProperty("ut.lhs", "UNO");
        System.setProperty("ut.rhs", "DOS");
        assertEquals("false", Environment.get("${ut.lhs=${ut.rhs}}"));
    }

    @Test
    public void equalsWithDefaultOnLhs_unsetLhsIsFalseEvenIfDefaultMatches() {
        System.clearProperty("ut.lhs");
        System.setProperty("ut.rhs", "X");

        // Semantics: equals compares resolved LHS; if LHS is null, equals yields false.
        assertEquals("false", Environment.get("${ut.lhs=${ut.rhs}}"),
          "Unset LHS must yield false for '=' comparison");
    }

    @Test
    public void notEqualsWrapsEqualsResult() {
        System.setProperty("ut.lhs", "UNO");
        System.setProperty("ut.rhs", "UNO");
        assertEquals("false", Environment.get("${!ut.lhs=${ut.rhs}}"));

        System.setProperty("ut.rhs", "DOS");
        assertEquals("true", Environment.get("${!ut.lhs=${ut.rhs}}"));
    }

    @Test
    public void multipleExpressions_interactingThroughDefaults() {
        System.clearProperty("ut.a");
        System.setProperty("ut.b", "B");
        System.clearProperty("ut.c");

        // a defaults to b, c defaults to a (which becomes b)
        assertEquals("A=B C=B",
          Environment.get("A=${ut.a:${ut.b}} C=${ut.c:${ut.a:${ut.b}}}"));
    }

    @Test
    public void nestedDefaultsWithNegatedInnerToken() {
        System.clearProperty("ut.undef");
        System.setProperty("ut.bool", "true");

        // Outer selects default; inner is negation on a defined boolean.
        assertEquals("value=false", Environment.get("${ut.undef:value=${!ut.bool}}"));
    }

    @Test
    public void nestedDefaultWithInnerUndefinedNegated_isTrue() {
        System.clearProperty("ut.outer");
        System.clearProperty("ut.inner");

        assertEquals("inner=true",
          Environment.get("${ut.outer:inner=${!ut.inner}}"),
          "Inner undefined negation should resolve to true");
    }

    @Test
    public void loopTwoNodeCycle_doesNotOverflow_andStabilizes() {
        System.setProperty("ut.loop1", "${ut.loop2}");
        System.setProperty("ut.loop2", "${ut.loop1}");

        // The exact stabilized output depends on the cycle-break policy, but it must not overflow
        // and must return a non-null, non-empty string that still contains at least one token.
        String r = Environment.get("${ut.loop1}");
        assertNotNull(r);
        assertTrue(r.contains("${ut.loop1}") || r.contains("${ut.loop2}"),
          "Cycle should stabilize by leaving a token unresolved");
    }

    @Test
    public void longChainOfDefaults_resolvesWithinReasonableSteps() {
        System.clearProperty("ut.d0");
        System.clearProperty("ut.d1");
        System.clearProperty("ut.d2");
        System.clearProperty("ut.d3");
        System.clearProperty("ut.d4");
        System.setProperty("ut.d5", "OK");

        assertEquals("OK", Environment.get("${ut.d0:${ut.d1:${ut.d2:${ut.d3:${ut.d4:${ut.d5:FAIL}}}}}}"));
    }

    @Test
    public void defaultContainsEqualsExpression_evaluatesAfterDefaultSelection() {
        System.clearProperty("ut.top");
        System.setProperty("ut.a", "X");
        System.setProperty("ut.b", "X");

        assertEquals("true", Environment.get("${ut.top:${ut.a=${ut.b}}}"),
          "Default selected first, then inner equals evaluated");
    }

    @Test
    public void defaultContainsNegatedEqualsExpression_evaluatesAfterDefaultSelection() {
        System.clearProperty("ut.top");
        System.setProperty("ut.a", "X");
        System.setProperty("ut.b", "Y");

        assertEquals("true", Environment.get("${ut.top:${!ut.a=${ut.b}}}"),
          "Default selected first, then inner equals evaluated and negated");
    }

    @Test
    public void multipleTokens_samePropertyDifferentForms() {
        System.setProperty("ut.x", "UNO");
        assertEquals("UNO UNO UNO",
          Environment.get("${ut.x} $sys{ut.x} ${sys.ut.x:UNO}"),
          "Simple, sys-prefixed, and dotted sys property forms should behave as expected");
    }

    @Test
    public void verbatimFollowedByExpression_bothAreProcessed() {
        System.setProperty("ut.suffix", "ABC");
        assertEquals("fooABC",
          Environment.get("$verb{foo}${ut.suffix}"),
          "$verb{foo} should return 'foo' literally, then ${ut.suffix} should expand to 'ABC'");
    }

    @Test
    public void verbatimAlone_returnsContentVerbatim() {
        assertEquals("hello world",
          Environment.get("$verb{hello world}"),
          "$verb{...} should return content verbatim");
    }

    @Test
    public void verbatimWithDollarSign_returnsContentVerbatim() {
        assertEquals("price is $50",
          Environment.get("$verb{price is $50}"),
          "$verb{...} with literal $ should return content verbatim");
    }

    @Test
    public void verbatimBetweenExpressions_allAreProcessed() {
        System.setProperty("ut.a", "A");
        System.setProperty("ut.b", "B");
        assertEquals("A-literal-B",
          Environment.get("${ut.a}-$verb{literal}-${ut.b}"),
          "Expressions and verbatim tokens should all be processed correctly");
    }
    @Test
    public void inlineVerbatimPayloadContainingExpression_isNotExpanded() {
        System.setProperty("ut.b", "B");
        assertEquals("X-${ut.b}-B",
          Environment.get("X-$verb{${ut.b}}-${ut.b}"),
          "Inline verbatim payload must remain literal across expansion passes");
    }
    @Test
    public void inlineVerbatimPayloadContainingPrefixedTokens_isNotExpanded() {
        System.setProperty("ut.x", "UNO");
        assertEquals("P=$sys{ut.x} Q=$cfg{test.value} R=UNO",
          Environment.get("P=$verb{$sys{ut.x}} Q=$verb{$cfg{test.value}} R=${ut.x}"),
          "Inline verbatim payload must not expand prefixed tokens");
    }
    @Test
    public void inlineVerbatimPayloadWithLiteralDollar_isPreserved() {
        System.setProperty("ut.b", "B");
        assertEquals("price=$50-B",
          Environment.get("$verb{price=$50}-${ut.b}"));
    }
    @Test
    public void unknownPrefixAbortsExpansion_entireStringReturnedUnchanged() {
        System.setProperty("ut.a", "A");
        System.setProperty("ut.b", "B");
        assertEquals("${ut.a}-$foo{x}-${ut.b}",
          Environment.get("${ut.a}-$foo{x}-${ut.b}"),
          "Unknown prefix should abort expansion (legacy behavior)");
    }

    @Test
    public void unknownPrefixStillAbortsEvenIfInlineVerbatimIsPresent() {
        System.setProperty("ut.a", "A");
        System.setProperty("ut.b", "B");
        assertEquals("$verb{ok}-${ut.a}-$foo{x}-${ut.b}",
          Environment.get("$verb{ok}-${ut.a}-$foo{x}-${ut.b}"),
          "Unknown prefix should abort the whole evaluation, even if other parts could expand");
    }

    @Test
    public void invalidTokenMissingOpeningBrace_isTreatedAsLiteralDollar() {
        System.setProperty("ut.a", "A");
        assertEquals("$ut.a-A",
          Environment.get("$ut.a-${ut.a}"),
          "Non-token '$ut.a' must be treated as literal");
    }

    @Test
    public void invalidTokenWithIllegalCharInProperty_isTreatedAsLiteralDollar() {
        System.setProperty("ut.a", "A");
        assertEquals("$-{ut.a}-A",
          Environment.get("$-{ut.a}-${ut.a}"),
          "Illegal property characters should prevent token parsing and keep '$' literal");
    }

    @Test
    public void tokenWithHyphenAndDotInProperty_isSupported() {
        System.setProperty("ut.a-b.c", "OK");
        assertEquals("OK", Environment.get("${ut.a-b.c}"));
    }

    @Test
    public void unterminatedToken_isLeftUnchanged() {
        System.setProperty("ut.a", "A");
        assertEquals("X ${ut.a",
          Environment.get("X ${ut.a"),
          "Unterminated token should not be partially processed");
    }

    @Test
    public void nestedDefaultWithUnbalancedInnerToken_isLeftUnchanged() {
        System.clearProperty("ut.a");
        assertEquals("${ut.a:${ut.b}",
          Environment.get("${ut.a:${ut.b}"),
          "If token end cannot be found, expression should remain unchanged");
    }

    @Test
    public void fullLineVerbAtBeginning_returnsPayloadOnly() {
        System.setProperty("ut.b", "B");
        assertEquals("price=$50-${ut.b}",
          Environment.get("$verb{price=$50-${ut.b}}"),
          "Full-line $verb{...} must not expand inner tokens");
    }

    @Test
    public void leadingVerbButNotFullLine_isHandledInline() {
        System.setProperty("ut.b", "B");
        assertEquals("price=$50-B",
          Environment.get("$verb{price=$50}-${ut.b}"),
          "Leading $verb should be treated as inline when its closing brace is not end-of-string");
    }

    @Test
    public void providerTransformation_isAppliedToResolvedValue() {
        System.setProperty("ut.p", "obf::Kj73uwAAAANCVvNVSIVQhpk1nmCgEgFz+3ktIHQWwllU/gvWsJ7B"); // obf(ABC)
        assertEquals("ABC", Environment.get("${ut.p}"),
          "Provider should transform values with its prefix");
    }

    @Test
    public void providerTransformation_isChainedToResolvedValue() {
        // obf(obf(ABC))
        System.setProperty("ut.p", "obf::COQvoQAAADl5MVnPSmcixF5iX+jazAgji2i5GQ2qoJmOg3MOZ4hGkJIAy/qpwlbb0W5U27+WcY4gdWbhr/wrm4qeUI+IVcs3Bw==");
        assertEquals("ABC", Environment.get("${ut.p}"),
          "Provider should transform values with its prefix");
    }

    @Test
    public void deepExpansionStopsWithoutOverflow() {
        // Create a chain longer than MAX_EXPANSION_STEPS to ensure bounded behavior.
        // Keep it small enough to run fast, but above 256.
        for (int i = 0; i < 300; i++) {
            System.setProperty("ut.s" + i, "${ut.s" + (i + 1) + "}");
        }
        System.setProperty("ut.s300", "END");

        String r = Environment.get("${ut.s0}");
        assertNotNull(r);
        // Depending on where budget stops, it might be "END" or still a token; both are acceptable.
        assertTrue(r.equals("END") || r.contains("${ut.s"),
          "Expansion must stop safely without StackOverflowError");
    }

    @Test
    public void unknownPrefixInsideDefault_abortsEntireEvaluation() {
        System.clearProperty("ut.a");
        assertEquals("$foo{x}",
          Environment.get("${ut.a:$foo{x}}"));
    }

    @Test
    public void unknownPrefixInsideVerbatim_doesNotAbortOrExpand() {
        System.setProperty("ut.a", "A");
        assertEquals("$foo{x}-A",
          Environment.get("$verb{$foo{x}}-${ut.a}"),
          "Unknown tokens inside verbatim must be inert; rest should expand");
    }

    @Test
    public void defaultContainingEmptyTokenSequence_doesNotBreakOuterDefaultParsing() {
        System.clearProperty("jpos.xxx"); // ensure unset

        assertEquals("A$A default:value${ B${B",
          Environment.get("A$A ${jpos.xxx:default:value${} B${B"),
          "Sequence ${} inside default must be treated as literal text for nesting purposes");
    }

    @Test
    public void nestedDefault_priorityFallsBackToSecondThenLiteral() {
        // Ensure both priorities are unset so we fall through to the literal default.
        System.clearProperty("ut.first.priority");
        System.clearProperty("ut.second.priority");

        assertEquals("default",
          Environment.get("${ut.first.priority:${ut.second.priority:default}}"),
          "When ut.first.priority is unset, it must fall back to ut.second.priority; when that is also unset, it must return the literal default.");
    }

    @Test
    public void nestedDefault_priorityUsesSecondWhenFirstUnset() {
        System.clearProperty("ut.first.priority");
        System.setProperty("ut.second.priority", "SECOND");

        assertEquals("SECOND",
          Environment.get("${ut.first.priority:${ut.second.priority:default}}"),
          "When ut.first.priority is unset and ut.second.priority is set, it must return ut.second.priority.");
    }

    @Test
    public void nestedDefault_priorityUsesFirstWhenSet() {
        System.setProperty("ut.first.priority", "FIRST");
        System.setProperty("ut.second.priority", "SECOND");

        assertEquals("FIRST",
          Environment.get("${ut.first.priority:${ut.second.priority:default}}"),
          "When ut.first.priority is set, it must take precedence over ut.second.priority and the literal default.");
    }

    @Test
    public void testDefaultParameter() {
        assertNull(Environment.get("${not.present}", null));
    }

}
