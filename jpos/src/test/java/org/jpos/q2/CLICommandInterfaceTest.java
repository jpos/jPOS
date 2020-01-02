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

package org.jpos.q2;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

/**
 * @author dgrandemange
 * 
 */
public class CLICommandInterfaceTest {

    private CLICommandInterface cliCommandInterface;

    @BeforeEach
    public void setUp() {
        CLIContext ctx = mock(CLIContext.class);
        cliCommandInterface = new CLICommandInterface(ctx);
    }

    @Test
    public void testparseCommand_LineIsNull() throws IOException {
        String[] args = cliCommandInterface.parseCommand(null);
        assertThat(args).isEmpty();
    }

    @Test
    public void testparseCommand_LineContainsSpacesOnly() throws IOException {
        String line = "     ";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(args).isEmpty();
    }

    @Test
    public void testparseCommand_NoQuotes() throws IOException {
        String line = "arg1 arg2 arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args))
                .containsSequence("arg1", "arg2", "arg3");
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredArgWithoutSpaceWithin()
            throws IOException {
        String line = "arg1 'arg2' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args))
                .containsSequence("arg1", "arg2", "arg3");
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredArgWithSpacesWithin()
            throws IOException {
        String line = "arg1 'arg2 with spaces within' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args)).containsSequence("arg1",
                "arg2 with spaces within", "arg3");
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredArgWithSpacesAndDoubleQuotesWithin()
            throws IOException {
        String line = "arg1 'arg2 with spaces and \"double quotes\" within' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args)).containsSequence("arg1",
                "arg2 with spaces and \"double quotes\" within", "arg3");
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredArgWithoutSpacesWithin()
            throws IOException {
        String line = "arg1 \"arg2\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args))
                .containsSequence("arg1", "arg2", "arg3");
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredArgWithSpacesWithin()
            throws IOException {
        String line = "arg1 \"arg2 with spaces within\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args)).containsSequence("arg1",
                "arg2 with spaces within", "arg3");
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredArgWithSpacesAndSimplequotesWithin()
            throws IOException {
        String line = "arg1 \"arg2 with spaces and 'simple quotes' within\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args)).containsSequence("arg1",
                "arg2 with spaces and 'simple quotes' within", "arg3");
    }

    @Test
    public void testparseCommand_SimpleQuotesArmuredEmptyArg()
            throws IOException {
        String line = "arg1 '' arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args))
                .containsSequence("arg1", "", "arg3");
    }

    @Test
    public void testparseCommand_DoubleQuotesArmuredEmptyArg()
            throws IOException {
        String line = "arg1 \"\" arg3";
        String[] args = cliCommandInterface.parseCommand(line);
        assertThat(Arrays.asList(args))
                .containsSequence("arg1", "", "arg3");
    }
    
}
