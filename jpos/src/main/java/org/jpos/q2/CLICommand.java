package org.jpos.q2;

public interface CLICommand
{
    public void exec(CLIContext cli, String[] strings) throws Exception;
}
