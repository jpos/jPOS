package org.jpos.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentTest {
    @Test
    public void testEmptyDefault() {
        assertEquals("", Environment.get("${test:}"));
    }
}