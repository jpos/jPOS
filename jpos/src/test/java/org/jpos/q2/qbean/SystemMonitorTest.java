/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.q2.qbean;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jpos.q2.Q2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SystemMonitorTest {
    @Mock
    Q2 q2;
    @Mock
    ThreadGroup threadGroup;
    @InjectMocks
    SystemMonitor systemMonitor;

    ByteArrayOutputStream baos;
    PrintStream printStream;

    @BeforeEach
    public void onSetup() throws Exception {
	baos = new ByteArrayOutputStream();
	printStream = new PrintStream(baos, true, "US-ASCII");
    }

    @Test
    public void testSetDetailRequired() throws Throwable {
	systemMonitor.setDetailRequired(true);
	assertTrue(systemMonitor.getDetailRequired(),
               "systemMonitor.getDetailRequired()");
	assertTrue(systemMonitor.isModified(), "systemMonitor.isModified()");
    }

    @Test
    public void testSetSleepTime() throws Throwable {
	systemMonitor.setSleepTime(1L);
	assertEquals(1L, systemMonitor.getSleepTime(),
               "systemMonitor.getSleepTime()");
	assertTrue(systemMonitor.isModified(), "systemMonitor.isModified()");
    }

    @Test
    public void testShowThreadGroupThrowsNullPointerException()
	    throws Throwable {
	assertThrows(NullPointerException.class, () -> {
	    String indent = "++";
	    systemMonitor.showThreadGroup(null, printStream, indent);
	});
    }

    @Test
    public void testShowThreadGroup() throws Throwable {
	String indent = "++";
	systemMonitor.showThreadGroup(threadGroup, printStream, indent);
	assertThat(baos.toString(), is(""));
    }

//    @Test
//    public void testDump() {
//        systemMonitor = new SystemMonitorExtendedForTesting();
//
//        String indent = "##";
//        systemMonitor.dump(printStream, indent);
//        String result = baos.toString();
//        // System.out.println(result);
//        assertThat(
//            result,
//            allOf(containsString("version"),
//                containsString("host"),
//                containsString("instance"),
//                containsString("name-registrar")));
//    }
//
//    static class SystemMonitorExtendedForTesting extends SystemMonitor {
//        Runtime runtime;
//        SecurityManager securityManager;
//
//        public SystemMonitorExtendedForTesting() {
//            super();
//            runtime = mock(Runtime.class);
//            securityManager = mock(SecurityManager.class);
//    	}
//    }
}
