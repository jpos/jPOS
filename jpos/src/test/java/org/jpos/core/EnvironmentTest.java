package org.jpos.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentTest {
    static Properties testProperties;
    static String envVarName;
    @BeforeAll
    static void setUp() {
        testProperties = new Properties();
        Random rnd = new Random();
        envVarName = "HOME";
        if (System.getProperty("os.name").startsWith("Windows")) envVarName = "OS";

        testProperties.setProperty("env", "$env{"+ envVarName +"}");
        testProperties.setProperty("sys",  "$sys{"+ envVarName +"}");
        testProperties.setProperty("home", "${"+ envVarName +"}");
    }

    @Test
    public void testLoadPropertiesFromInputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        testProperties.store(os, "");
        Properties props = Environment.loadProperties(new ByteArrayInputStream(os.toByteArray()));

        assertEquals(System.getenv(envVarName), props.getProperty("env"));
        assertEquals(System.getenv(envVarName), props.getProperty("home"));
        assertTrue(props.getProperty("sys").isEmpty());

    }

    @Test
    public void testLoadPropertiesFromReader() throws IOException {
        StringWriter writer = new StringWriter();
        testProperties.store(writer, "");
        Properties props = Environment.loadProperties(new StringReader(writer.toString()));

        assertEquals(System.getenv(envVarName), props.getProperty("env"));
        assertEquals(System.getenv(envVarName), props.getProperty("home"));
        assertTrue(props.getProperty("sys").isEmpty());

    }

}
