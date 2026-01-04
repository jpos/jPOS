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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Declarative configuration validation framework.
 *
 * <p>Provides a fluent API for defining validation rules for jPOS configuration properties.
 *
 * <p>Example usage:
 * <pre>
 * ConfigValidator validator = new ConfigValidator()
 *     .required("bootstrap-servers")
 *     .required("app-id")
 *     .range("startup-timeout-seconds", 1, 3600)
 *     .range("max-retries", 0, 10)
 *     .nonEmpty("topic")
 *     .pattern("group-id", "^[a-zA-Z0-9._-]+$")
 *     .custom("processing-guarantee",
 *             v -> v.equals("at_least_once") || v.equals("exactly_once"),
 *             "must be 'at_least_once' or 'exactly_once'");
 *
 * validator.validate(config);
 * </pre>
 *
 * @author apr
 * @since 3.0.2
 */
public class ConfigValidator {
    private final List<ValidationRule> rules = new ArrayList<>();

    /**
     * Validate that a property is present and non-empty.
     * @param key the property key
     * @return this validator for chaining
     */
    public ConfigValidator required(String key) {
        rules.add(new ValidationRule(key, config -> {
            String value = config.get(key, null);
            if (value == null || value.trim().isEmpty()) {
                throw new ConfigurationException("Required property '" + key + "' is missing or empty");
            }
        }));
        return this;
    }

    /**
     * Validate that a property, if present, is non-empty.
     * @param key the property key
     * @return this validator for chaining
     */
    public ConfigValidator nonEmpty(String key) {
        rules.add(new ValidationRule(key, config -> {
            String value = config.get(key, null);
            if (value != null && value.trim().isEmpty()) {
                throw new ConfigurationException("Property '" + key + "' must not be empty");
            }
        }));
        return this;
    }

    /**
     * Validate that an integer property is within a specified range.
     * Only validates if the property is present.
     * @param key the property key
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return this validator for chaining
     */
    public ConfigValidator range(String key, int min, int max) {
        rules.add(new ValidationRule(key, config -> {
            String strValue = config.get(key, null);
            if (strValue != null && !strValue.trim().isEmpty()) {
                try {
                    int value = Integer.parseInt(strValue.trim());
                    if (value < min || value > max) {
                        throw new ConfigurationException(
                            "Property '" + key + "' must be between " + min + " and " + max + ": " + value
                        );
                    }
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Property '" + key + "' must be an integer: " + strValue);
                }
            }
        }));
        return this;
    }

    /**
     * Validate that a long property is within a specified range.
     * Only validates if the property is present.
     * @param key the property key
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return this validator for chaining
     */
    public ConfigValidator rangeLong(String key, long min, long max) {
        rules.add(new ValidationRule(key, config -> {
            String strValue = config.get(key, null);
            if (strValue != null && !strValue.trim().isEmpty()) {
                try {
                    long value = Long.parseLong(strValue.trim());
                    if (value < min || value > max) {
                        throw new ConfigurationException(
                            "Property '" + key + "' must be between " + min + " and " + max + ": " + value
                        );
                    }
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Property '" + key + "' must be a long: " + strValue);
                }
            }
        }));
        return this;
    }

    /**
     * Validate that a double property is within a specified range.
     * Only validates if the property is present.
     * @param key the property key
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return this validator for chaining
     */
    public ConfigValidator rangeDouble(String key, double min, double max) {
        rules.add(new ValidationRule(key, config -> {
            String strValue = config.get(key, null);
            if (strValue != null && !strValue.trim().isEmpty()) {
                try {
                    double value = Double.parseDouble(strValue.trim());
                    if (value < min || value > max) {
                        throw new ConfigurationException(
                            "Property '" + key + "' must be between " + min + " and " + max + ": " + value
                        );
                    }
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Property '" + key + "' must be a double: " + strValue);
                }
            }
        }));
        return this;
    }

    /**
     * Validate that a property matches a regex pattern.
     * Only validates if the property is present.
     * @param key the property key
     * @param regex the regular expression pattern
     * @return this validator for chaining
     */
    public ConfigValidator pattern(String key, String regex) {
        Pattern compiled = Pattern.compile(regex);
        rules.add(new ValidationRule(key, config -> {
            String value = config.get(key, null);
            if (value != null && !value.trim().isEmpty()) {
                if (!compiled.matcher(value.trim()).matches()) {
                    throw new ConfigurationException(
                        "Property '" + key + "' does not match pattern '" + regex + "': " + value
                    );
                }
            }
        }));
        return this;
    }

    /**
     * Validate a property using a custom predicate.
     * Only validates if the property is present.
     * @param key the property key
     * @param predicate the validation predicate (returns true if valid)
     * @param errorMessage error message if validation fails
     * @return this validator for chaining
     */
    public ConfigValidator custom(String key, Predicate<String> predicate, String errorMessage) {
        rules.add(new ValidationRule(key, config -> {
            String value = config.get(key, null);
            if (value != null && !value.trim().isEmpty()) {
                if (!predicate.test(value.trim())) {
                    throw new ConfigurationException("Property '" + key + "' " + errorMessage + ": " + value);
                }
            }
        }));
        return this;
    }

    /**
     * Validate that at least one of the specified properties is present.
     * @param keys the property keys
     * @return this validator for chaining
     */
    public ConfigValidator requireAtLeastOne(String... keys) {
        rules.add(new ValidationRule("requireAtLeastOne", config -> {
            for (String key : keys) {
                String value = config.get(key, null);
                if (value != null && !value.trim().isEmpty()) {
                    return;
                }
            }
            throw new ConfigurationException(
                "At least one of these properties must be present: " + String.join(", ", keys)
            );
        }));
        return this;
    }

    /**
     * Validate that exactly one of the specified properties is present.
     * @param keys the property keys
     * @return this validator for chaining
     */
    public ConfigValidator requireExactlyOne(String... keys) {
        rules.add(new ValidationRule("requireExactlyOne", config -> {
            int count = 0;
            for (String key : keys) {
                String value = config.get(key, null);
                if (value != null && !value.trim().isEmpty()) {
                    count++;
                }
            }
            if (count == 0) {
                throw new ConfigurationException(
                    "Exactly one of these properties must be present: " + String.join(", ", keys)
                );
            } else if (count > 1) {
                throw new ConfigurationException(
                    "Only one of these properties can be present: " + String.join(", ", keys)
                );
            }
        }));
        return this;
    }

    /**
     * Validate that a boolean property has a valid value.
     * Accepted values: true, false, yes, no, 1, 0 (case-insensitive).
     * Only validates if the property is present.
     * @param key the property key
     * @return this validator for chaining
     */
    public ConfigValidator validBoolean(String key) {
        rules.add(new ValidationRule(key, config -> {
            String value = config.get(key, null);
            if (value != null && !value.trim().isEmpty()) {
                String v = value.trim().toLowerCase();
                if (!v.equals("true") && !v.equals("false") &&
                    !v.equals("yes") && !v.equals("no") &&
                    !v.equals("1") && !v.equals("0")) {
                    throw new ConfigurationException(
                        "Property '" + key + "' must be a boolean (true/false/yes/no/1/0): " + value
                    );
                }
            }
        }));
        return this;
    }

    /**
     * Validate the configuration.
     * @param config the configuration to validate
     * @throws ConfigurationException if validation fails
     */
    public void validate(Configuration config) throws ConfigurationException {
        for (ValidationRule rule : rules) {
            rule.validate(config);
        }
    }

    /**
     * Internal validation rule.
     */
    private static class ValidationRule {
        private final String key;
        private final ValidationFunction validator;

        ValidationRule(String key, ValidationFunction validator) {
            this.key = key;
            this.validator = validator;
        }

        void validate(Configuration config) throws ConfigurationException {
            validator.validate(config);
        }
    }

    /**
     * Validation function interface.
     */
    @FunctionalInterface
    private interface ValidationFunction {
        void validate(Configuration config) throws ConfigurationException;
    }
}
