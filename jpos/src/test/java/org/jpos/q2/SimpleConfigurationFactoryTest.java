/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

package org.jpos.q2;

import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Environment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleConfigurationFactoryTest {
    static String oldEnv;
    static final String TEST_BASE_FILE = "build/resources/test/org/jpos/q2/configFactoryTest.yml";

	SimpleConfigurationFactory f = new SimpleConfigurationFactory();

    @BeforeAll
    public static void setUp() throws IOException
    {
        oldEnv = System.getProperty("jpos.env");            // save it to restore it later
    }

    @AfterAll
    public static void tearDown() throws Exception {
        // restore old env
        if (oldEnv != null)
            System.setProperty("jpos.env", oldEnv);
        else
            System.clearProperty("jpos.env");

        Environment.reload();
    }


    Element getBaseElement(String fileName, boolean withEnv) {
    	Element root = new Element("root");
    	Element property = new Element("property");
    	property.setAttribute("file", fileName);
    	if (withEnv) {
    		property.setAttribute("env", "true");
    	}
    	root.addContent(property);
    	return root;
    }
    
    void setEnv(String env) throws IOException {
    	System.setProperty("jpos.env", env);
    	Environment.reload();
    }

    @Test
    public void testFileList() {
    	assertEquals(Arrays.asList("file.yml", "file-env1.yml", "file-env1.properties", "file-env2.yml", "file-env2.properties"), 
    			f.getFiles("file.yml", "env1,env2"));
    }

    @Test
    public void testFromSimpleFile() throws ConfigurationException {    	
    	Element e = getBaseElement(TEST_BASE_FILE, false);
    	Configuration cfg = f.getConfiguration(e);
    	assertEquals("configFactoryTest", cfg.get("base.name"));
    	assertEquals("true", cfg.get("configFactoryTest"));
    	assertEquals("", cfg.get("configFactoryTest-testenv"));
    	assertEquals("", cfg.get("configFactoryTest-testenv2"));
    }

    @Test
    public void testFromSimpleFileWithEnvironment() throws ConfigurationException, IOException {
    	setEnv("testenv");
    	Element e = getBaseElement(TEST_BASE_FILE, false);
    	Configuration cfg = f.getConfiguration(e);
    	assertEquals("configFactoryTest", cfg.get("base.name"));
    	assertEquals("true", cfg.get("configFactoryTest"));
    	assertEquals("", cfg.get("configFactoryTest1"));
    	assertEquals("", cfg.get("configFactoryTest2"));
    }

    @Test
    public void testFromSimpleFileWithEnvironmentSet() throws ConfigurationException, IOException {
    	setEnv("testenv");
    	Element e = getBaseElement(TEST_BASE_FILE, true);
    	Configuration cfg = f.getConfiguration(e);
    	assertEquals("configFactoryTest-testenv", cfg.get("base.name"));
    	assertEquals("true", cfg.get("configFactoryTest"));
    	assertEquals("true", cfg.get("configFactoryTest1"));
    	assertEquals("", cfg.get("configFactoryTest2"));
    }

    @Test
    public void testFromSimpleFileWithEnvironmentSet2() throws ConfigurationException, IOException {
    	setEnv("testenv2");
    	Element e = getBaseElement(TEST_BASE_FILE, true);
    	Configuration cfg = f.getConfiguration(e);
    	assertEquals("configFactoryTest-testenv2", cfg.get("base.name"));
    	assertEquals("true", cfg.get("configFactoryTest"));
    	assertEquals("", cfg.get("configFactoryTest1"));
    	assertEquals("true", cfg.get("configFactoryTest2"));
    }

    @Test
    public void testFromSimpleFileWithEnvironmentMultiple() throws ConfigurationException, IOException {
    	setEnv("testenv,testenv2");
    	Element e = getBaseElement(TEST_BASE_FILE, true);
    	Configuration cfg = f.getConfiguration(e);
    	assertEquals("configFactoryTest-testenv2", cfg.get("base.name"));
    	assertEquals("true", cfg.get("configFactoryTest"));
    	assertEquals("true", cfg.get("configFactoryTest1"));
    	assertEquals("true", cfg.get("configFactoryTest2"));
    }

}
