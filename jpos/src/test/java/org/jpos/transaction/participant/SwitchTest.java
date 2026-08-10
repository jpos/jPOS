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

package org.jpos.transaction.participant;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.transaction.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jpos.transaction.ContextConstants.TXNNAME;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwitchTest {
    private static final String UNKNOWN_GROUP = "unknown-group";
    private static final String GROUP_PROPERTY = "org.jpos.transaction.participant.SwitchTest.group";

    private Switch selector;
    private Configuration cfg;
    private String previousGroupProperty;

    @BeforeEach
    public void setUp() {
        previousGroupProperty = System.getProperty(GROUP_PROPERTY);
        selector = new Switch();
        cfg = configuration();
    }

    @AfterEach
    public void tearDown() {
        restoreSystemProperty(GROUP_PROPERTY, previousGroupProperty);
    }

    @Test
    public void testSelectExactMatchInDefaultStrictMode() {
        cfg.put("100.30", "balance-inquiry");
        selector.setConfiguration(cfg);

        assertEquals("balance-inquiry", select("100.30"));
    }

    @Test
    public void testDefaultStrictModeDoesNotUsePrefix() {
        cfg.put("100.30", "balance-inquiry");
        selector.setConfiguration(cfg);

        assertEquals(UNKNOWN_GROUP, select("100.30.182"));
    }

    @Test
    public void testExplicitStrictModeDoesNotUsePrefix() {
        cfg.put("mode", "strict");
        cfg.put("100.30", "balance-inquiry");
        selector.setConfiguration(cfg);

        assertEquals(UNKNOWN_GROUP, select("100.30.182"));
    }

    @Test
    public void testExactMatchTakesPrecedenceInPrefixMode() {
        cfg.put("mode", "prefix");
        cfg.put("100", "generic");
        cfg.put("100.30", "balance-inquiry");
        selector.setConfiguration(cfg);

        assertEquals("balance-inquiry", select("100.30"));
    }

    @Test
    public void testExactEmptyGroupDoesNotFallBack() {
        cfg.put("mode", "prefix");
        cfg.put("100", "generic");
        cfg.put("100.30", "");
        selector.setConfiguration(cfg);

        assertEquals("", select("100.30"));
    }

    @Test
    public void testLongestPrefixTakesPrecedence() {
        cfg.put("mode", "prefix");
        cfg.put("100.30", "specific");
        cfg.put("100", "generic");
        cfg.put("100.30.182", "most-specific");
        selector.setConfiguration(cfg);

        assertEquals("most-specific", select("100.30.182.001"));
    }

    @Test
    public void testFallsBackToShorterMatchingPrefix() {
        cfg.put("mode", "prefix");
        cfg.put("100.30", "specific");
        cfg.put("100", "generic");
        selector.setConfiguration(cfg);

        assertEquals("generic", select("100.20.001"));
    }

    @Test
    public void testPrefixMustStartTheTransactionName() {
        cfg.put("mode", "prefix");
        cfg.put("00.30", "substring");
        cfg.put("100.30.182", "more-specific-than-type");
        selector.setConfiguration(cfg);

        assertAll(
          () -> assertEquals(UNKNOWN_GROUP, select("100.30.001")),
          () -> assertEquals(UNKNOWN_GROUP, select("100.30"))
        );
    }

    @Test
    public void testUnknownGroupForMissingOrUnmatchedTransactionName() {
        cfg.put("mode", "prefix");
        cfg.put("100", "generic");
        selector.setConfiguration(cfg);

        assertAll(
          () -> assertEquals(UNKNOWN_GROUP, select(null)),
          () -> assertEquals(UNKNOWN_GROUP, select("200.30"))
        );
    }

    @Test
    public void testUnknownDefaultsToEmptyGroup() {
        cfg = new SimpleConfiguration();
        cfg.put("mode", "prefix");
        cfg.put("100", "generic");
        selector.setConfiguration(cfg);

        assertEquals("", select("200.30"));
    }

    @Test
    public void testConfiguredContextEntry() {
        cfg.put("mode", "prefix");
        cfg.put("txnname", "MY_TXNNAME");
        cfg.put("100.30", "balance-inquiry");
        selector.setConfiguration(cfg);

        Context ctx = new Context();
        ctx.put(TXNNAME.toString(), "does-not-match");
        ctx.put("MY_TXNNAME", "100.30.182");

        assertEquals("balance-inquiry", selector.select(1L, ctx));
    }

    @Test
    public void testPrefixValueIsResolvedAtSelectionTime() {
        System.setProperty(GROUP_PROPERTY, "original-group");
        cfg.put("mode", "prefix");
        cfg.put("100", "$sys{" + GROUP_PROPERTY + "}");
        selector.setConfiguration(cfg);

        assertEquals("original-group", select("100.30"));

        System.setProperty(GROUP_PROPERTY, "updated-group");
        assertEquals("updated-group", select("100.30"));
    }

    @Test
    public void testSetConfigurationReplacesConfiguration() {
        cfg.put("mode", "prefix");
        cfg.put("100", "old-group");
        selector.setConfiguration(cfg);

        Configuration newCfg = configuration();
        newCfg.put("mode", "prefix");
        newCfg.put("200", "new-group");
        selector.setConfiguration(newCfg);

        assertAll(
          () -> assertEquals("new-group", select("200.30")),
          () -> assertEquals(UNKNOWN_GROUP, select("100.30"))
        );
    }

    @Test
    public void testSetConfigurationDisablesPrefixMode() {
        cfg.put("mode", "prefix");
        cfg.put("100", "old-group");
        selector.setConfiguration(cfg);

        Configuration strictCfg = configuration();
        strictCfg.put("100", "strict-group");
        selector.setConfiguration(strictCfg);

        assertAll(
          () -> assertEquals("strict-group", select("100")),
          () -> assertEquals(UNKNOWN_GROUP, select("100.30"))
        );
    }

    @Test
    public void testSelectorPropertiesAreNotPrefixRoutes() {
        cfg.put("mode", "prefix");
        selector.setConfiguration(cfg);

        assertAll(
          () -> assertEquals(UNKNOWN_GROUP, select("mode.suffix")),
          () -> assertEquals(UNKNOWN_GROUP, select("txnname.suffix"))
        );
    }

    @Test
    public void testPrepareIsReadOnlyAndDoesNotJoin() {
        assertEquals(Switch.PREPARED | Switch.READONLY | Switch.NO_JOIN, selector.prepare(1L, new Context()));
    }

    private Configuration configuration() {
        Configuration configuration = new SimpleConfiguration();
        configuration.put("unknown", UNKNOWN_GROUP);
        return configuration;
    }

    private String select(String txnName) {
        Context ctx = new Context();
        if (txnName != null)
            ctx.put(TXNNAME.toString(), txnName);
        return selector.select(1L, ctx);
    }

    private void restoreSystemProperty(String name, String value) {
        if (value != null)
            System.setProperty(name, value);
        else
            System.clearProperty(name);
    }
}
