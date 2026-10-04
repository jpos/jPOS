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

package org.jpos.q2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author dgrandemange
 * 
 */
public class CLICommandInterfaceTest {
    private static final AtomicInteger WRONG_TYPE_INITIALIZATIONS = new AtomicInteger();
    private static final AtomicInteger WRONG_TYPE_CONSTRUCTIONS = new AtomicInteger();
    private static final AtomicInteger COMMAND_INITIALIZATIONS = new AtomicInteger();
    private static final AtomicInteger COMMAND_CONSTRUCTIONS = new AtomicInteger();

    private CLICommandInterface cliCommandInterface;

    @BeforeEach
    public void setUp() {
        CLIContext ctx = mock(CLIContext.class);
        cliCommandInterface = new CLICommandInterface(ctx);
    }

    @Test
    public void testparseCommand_LineIsNull() throws IOException {
        String[] args = cliCommandInterface.parseCommand(null);
        assertTrue(args == null || args.length == 0);
    }

    @Test
    public void testparseCommand_LineContainsSpacesOnly() throws IOException {
        String line = "     ";
        String[] args = cliCommandInterface.parseCommand(line);
        assertTrue(args == null || args.length == 0);
    }

    @Test
    public void testparseCommand_NoQuotes() throws IOException {
        String line = "arg1 arg2 arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "arg2", "arg3"});
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredArgWithoutSpaceWithin()
            throws IOException {
        String line = "arg1 'arg2' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "arg2", "arg3"});
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredArgWithSpacesWithin()
            throws IOException {
        String line = "arg1 'arg2 with spaces within' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "arg2 with spaces within", "arg3"});
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredArgWithSpacesAndDoubleQuotesWithin()
            throws IOException {
        String line = "arg1 'arg2 with spaces and \"double quotes\" within' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "arg2 with spaces and \"double quotes\" within", "arg3"});
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredArgWithoutSpacesWithin()
            throws IOException {
        String line = "arg1 \"arg2\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "arg2", "arg3"});
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredArgWithSpacesWithin()
            throws IOException {
        String line = "arg1 \"arg2 with spaces within\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "arg2 with spaces within", "arg3"});
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredArgWithSpacesAndSimplequotesWithin()
            throws IOException {
        String line = "arg1 \"arg2 with spaces and 'simple quotes' within\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "arg2 with spaces and 'simple quotes' within", "arg3"});
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredEmptyArg()
            throws IOException {
        String line = "arg1 '' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "", "arg3"});
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredEmptyArg()
            throws IOException {
        String line = "arg1 \"\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertArrayEquals(args, new String[] { "arg1", "", "arg3"});
    }

    @Test
    public void testGetCommandRejectsWrongTypeBeforeInitialization() throws Exception {
        InvocationTargetException ex = assertThrows(
          InvocationTargetException.class,
          () -> getCommand(WrongType.class.getName())
        );

        assertInstanceOf(ClassCastException.class, ex.getCause());
        assertEquals(0, WRONG_TYPE_INITIALIZATIONS.get());
        assertEquals(0, WRONG_TYPE_CONSTRUCTIONS.get());
    }

    @Test
    public void testGetCommandInitializesAndConstructsValidCommand() throws Exception {
        Object command = getCommand(TestCommand.class.getName());

        assertInstanceOf(CLICommand.class, command);
        assertEquals(1, COMMAND_INITIALIZATIONS.get());
        assertEquals(1, COMMAND_CONSTRUCTIONS.get());
    }

    private Object getCommand(String className) throws Exception {
        Method method = CLICommandInterface.class.getDeclaredMethod("getCommand", String.class);
        method.setAccessible(true);
        return method.invoke(cliCommandInterface, className);
    }

    public static class WrongType {
        static {
            WRONG_TYPE_INITIALIZATIONS.incrementAndGet();
        }

        public WrongType() {
            WRONG_TYPE_CONSTRUCTIONS.incrementAndGet();
        }
    }

    public static class TestCommand implements CLICommand {
        static {
            COMMAND_INITIALIZATIONS.incrementAndGet();
        }

        public TestCommand() {
            COMMAND_CONSTRUCTIONS.incrementAndGet();
        }

        @Override
        public void exec(CLIContext cli, String[] strings) {
            // Nothing to do.
        }
    }
    
}
