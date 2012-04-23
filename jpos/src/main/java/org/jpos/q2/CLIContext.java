package org.jpos.q2;

import jline.ConsoleReader;

import java.io.IOException;
import java.io.PrintStream;

public class CLIContext
{
    Q2 q2;
    ConsoleReader consoleReader;
    PrintStream outputStream;
    boolean stopped = false;

    public boolean isStopped()
    {
        return stopped;
    }

    public void setStopped(boolean stopped)
    {
        this.stopped = stopped;
    }

    public Q2 getQ2()
    {
        return q2;
    }

    public void setQ2(Q2 q2)
    {
        this.q2 = q2;
    }

    public ConsoleReader getConsoleReader()
    {
        return consoleReader;
    }

    public void setConsoleReader(ConsoleReader consoleReader)
    {
        this.consoleReader = consoleReader;
    }

    public PrintStream getOutputStream()
    {
        return outputStream;
    }

    public void setOutputStream(PrintStream outputStream)
    {
        this.outputStream = outputStream;
    }

    public void printThrowable(Throwable t)
    {
        t.printStackTrace(outputStream);
        outputStream.flush();
    }

    public void print(String s)
    {
        try
        {
            consoleReader.printString(s);
        }
        catch (IOException e)
        {
            e.printStackTrace(outputStream);
            outputStream.flush();
        }
    }

    public void println(String s)
    {
        try
        {
            consoleReader.printString(s);
            consoleReader.printNewline();
        }
        catch (IOException e)
        {
            e.printStackTrace(outputStream);
            outputStream.flush();
        }
    }

    public boolean confirm(String prompt) throws IOException
    {
        return "yes".equalsIgnoreCase(consoleReader.readLine(prompt));
    }
}
