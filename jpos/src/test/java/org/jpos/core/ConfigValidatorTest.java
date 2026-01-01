/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2025 jPOS Software SRL
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ConfigValidator.
 *
 * @author apr
 */
class ConfigValidatorTest {

    @Test
    void testRequired() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("bootstrap-servers", "localhost:9092");

        ConfigValidator validator = new ConfigValidator()
            .required("bootstrap-servers");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testRequiredMissing() {
        SimpleConfiguration config = new SimpleConfiguration();

        ConfigValidator validator = new ConfigValidator()
            .required("bootstrap-servers");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("Required property 'bootstrap-servers' is missing"));
    }

    @Test
    void testRequiredEmpty() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("bootstrap-servers", "   ");

        ConfigValidator validator = new ConfigValidator()
            .required("bootstrap-servers");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("Required property 'bootstrap-servers' is missing or empty"));
    }

    @Test
    void testNonEmpty() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("topic", "test-topic");

        ConfigValidator validator = new ConfigValidator()
            .nonEmpty("topic");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testNonEmptyWithEmptyValue() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("topic", "   ");

        ConfigValidator validator = new ConfigValidator()
            .nonEmpty("topic");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("Property 'topic' must not be empty"));
    }

    @Test
    void testRange() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("timeout", "30");

        ConfigValidator validator = new ConfigValidator()
            .range("timeout", 1, 60);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testRangeOutOfBounds() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("timeout", "100");

        ConfigValidator validator = new ConfigValidator()
            .range("timeout", 1, 60);

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("must be between 1 and 60"));
    }

    @Test
    void testRangeLong() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("delay", "5000");

        ConfigValidator validator = new ConfigValidator()
            .rangeLong("delay", 100, 10000);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testRangeDouble() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("multiplier", "2.5");

        ConfigValidator validator = new ConfigValidator()
            .rangeDouble("multiplier", 1.0, 5.0);

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testPattern() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("group-id", "my-consumer-group");

        ConfigValidator validator = new ConfigValidator()
            .pattern("group-id", "^[a-zA-Z0-9._-]+$");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testPatternInvalid() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("group-id", "invalid group!");

        ConfigValidator validator = new ConfigValidator()
            .pattern("group-id", "^[a-zA-Z0-9._-]+$");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("does not match pattern"));
    }

    @Test
    void testCustom() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("guarantee", "at_least_once");

        ConfigValidator validator = new ConfigValidator()
            .custom("guarantee",
                v -> v.equals("at_least_once") || v.equals("exactly_once"),
                "must be 'at_least_once' or 'exactly_once'");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testCustomInvalid() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("guarantee", "invalid");

        ConfigValidator validator = new ConfigValidator()
            .custom("guarantee",
                v -> v.equals("at_least_once") || v.equals("exactly_once"),
                "must be 'at_least_once' or 'exactly_once'");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("must be 'at_least_once' or 'exactly_once'"));
    }

    @Test
    void testRequireAtLeastOne() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("host", "localhost");

        ConfigValidator validator = new ConfigValidator()
            .requireAtLeastOne("host", "bootstrap-servers");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testRequireAtLeastOneMissing() {
        SimpleConfiguration config = new SimpleConfiguration();

        ConfigValidator validator = new ConfigValidator()
            .requireAtLeastOne("host", "bootstrap-servers");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("At least one of these properties must be present"));
    }

    @Test
    void testRequireExactlyOne() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("host", "localhost");

        ConfigValidator validator = new ConfigValidator()
            .requireExactlyOne("host", "bootstrap-servers");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testRequireExactlyOneMissing() {
        SimpleConfiguration config = new SimpleConfiguration();

        ConfigValidator validator = new ConfigValidator()
            .requireExactlyOne("host", "bootstrap-servers");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("Exactly one of these properties must be present"));
    }

    @Test
    void testRequireExactlyOneMultiple() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("host", "localhost");
        config.put("bootstrap-servers", "localhost:9092");

        ConfigValidator validator = new ConfigValidator()
            .requireExactlyOne("host", "bootstrap-servers");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("Only one of these properties can be present"));
    }

    @Test
    void testValidBoolean() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("enabled", "true");
        config.put("verbose", "yes");
        config.put("debug", "1");

        ConfigValidator validator = new ConfigValidator()
            .validBoolean("enabled")
            .validBoolean("verbose")
            .validBoolean("debug");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testValidBooleanInvalid() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("enabled", "invalid");

        ConfigValidator validator = new ConfigValidator()
            .validBoolean("enabled");

        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> validator.validate(config)
        );
        assertTrue(ex.getMessage().contains("must be a boolean"));
    }

    @Test
    void testChainedValidation() {
        SimpleConfiguration config = new SimpleConfiguration();
        config.put("bootstrap-servers", "localhost:9092");
        config.put("timeout", "30");
        config.put("topic", "test-topic");
        config.put("enabled", "true");

        ConfigValidator validator = new ConfigValidator()
            .required("bootstrap-servers")
            .range("timeout", 1, 60)
            .nonEmpty("topic")
            .validBoolean("enabled");

        assertDoesNotThrow(() -> validator.validate(config));
    }

    @Test
    void testOptionalPropertiesNotValidatedWhenMissing() {
        SimpleConfiguration config = new SimpleConfiguration();
        // Only required property is present, optional properties are missing
        config.put("bootstrap-servers", "localhost:9092");

        ConfigValidator validator = new ConfigValidator()
            .required("bootstrap-servers")
            .range("timeout", 1, 60)         // Optional, not validated if missing
            .pattern("topic", "^[a-z]+$")   // Optional, not validated if missing
            .validBoolean("enabled");        // Optional, not validated if missing

        assertDoesNotThrow(() -> validator.validate(config));
    }
}
