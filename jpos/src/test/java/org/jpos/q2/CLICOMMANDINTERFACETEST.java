package org.jpos.q2;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author dgrandemange
 *
 */
public class CLICOMMANDINTERFACETEST implements CLICommand {

	private CLICommandInterface cliCommandInterface;
	private CLIContext ctx;
	private static String[] gArgs;
	
	/* (non-Javadoc)
	 * @see org.jpos.q2.CLICommand#exec(org.jpos.q2.CLIContext, java.lang.String[])
	 */
	public void exec(CLIContext cli, String[] strings) throws Exception {
		gArgs = strings;
	}
	
	private static final String dummyCmdClassName = CLICOMMANDINTERFACETEST.class.getSimpleName();
	
	@Before
	public void setUp() {
		ctx = mock(CLIContext.class);		
		
		cliCommandInterface = new CLICommandInterface(ctx);
		cliCommandInterface.addPrefix(CLICOMMANDINTERFACETEST.class.getPackage().getName() + ".");
		
		gArgs = null;
	}

	@Test
	public void testExecCommand_LineIsNull() throws IOException {
		cliCommandInterface.execCommand(null);
		assertThat(gArgs).isNull();
	}

	@Test
	public void testExecCommand_LineContainsSpacesOnly() throws IOException {
		String line = dummyCmdClassName + "     ";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line);
	}
	
	@Test
	public void testExecCommand_NoQuotes() throws IOException {
		String line = dummyCmdClassName + " arg1 arg2 arg3";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line, "arg1", "arg2", "arg3");
	}

	@Test
	public void testExecCommand_SimpleQuotesArmuredArgWithoutSpaceWithin() throws IOException {
		String line = dummyCmdClassName + " arg1 'arg2' arg3";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line, "arg1", "arg2", "arg3");
	}

	@Test
	public void testExecCommand_SimpleQuotesArmuredArgWithSpacesWithin() throws IOException {
		String line = dummyCmdClassName + " arg1 'arg2 with spaces within' arg3";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line, "arg1", "arg2 with spaces within", "arg3");
	}

	@Test
	public void testExecCommand_SimpleQuotesArmuredArgWithSpacesAndDoubleQuotesWithin() throws IOException {
		String line = dummyCmdClassName + " arg1 'arg2 with spaces and \"double quotes\" within' arg3";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line, "arg1", "arg2 with spaces and \"double quotes\" within", "arg3");
	}
	
	@Test
	public void testExecCommand_DoubleQuotesArmuredArgWithoutSpacesWithin() throws IOException {
		String line = dummyCmdClassName + " arg1 \"arg2\" arg3";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line, "arg1", "arg2", "arg3");
	}

	@Test
	public void testExecCommand_DoubleQuotesArmuredArgWithSpacesWithin() throws IOException {
		String line = dummyCmdClassName + " arg1 \"arg2 with spaces within\" arg3";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line, "arg1", "arg2 with spaces within", "arg3");
	}

	@Test
	public void testExecCommand_DoubleQuotesArmuredArgWithSpacesAndSimplequotesWithin() throws IOException {
		String line = dummyCmdClassName + " arg1 \"arg2 with spaces and 'simple quotes' within\" arg3";
		cliCommandInterface.execCommand(line);
		assertThat(gArgs).containsOnly(line, "arg1", "arg2 with spaces and 'simple quotes' within", "arg3");
	}
	
}
