package org.jpos.q2;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

/**
 * @author dgrandemange
 * 
 */
public class CLICommandInterfaceTest {

    private CLICommandInterface cliCommandInterface;

    @Before
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
