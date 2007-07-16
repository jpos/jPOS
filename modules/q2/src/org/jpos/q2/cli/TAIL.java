package org.jpos.q2.cli;

import java.util.Date;
import java.util.Map;
import java.util.Iterator;
import java.io.PrintStream;
import jline.ANSIBuffer;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.LogListener;
import org.jpos.q2.CLI;

public class TAIL implements CLI.Command, LogListener {
    PrintStream p;
    CLI cli;
    boolean ansi;
        
    public void exec (CLI cli, String[] args) throws Exception {
        this.p = cli.getOutputStream();
        this.cli = cli;
        this.ansi = cli.getConsoleReader().getTerminal().isANSISupported();
        if (args.length == 1) {
            usage(cli);
            return;
        }
        for (int i=1; i<args.length; i++) {
            try {
                Logger logger = (Logger) NameRegistrar.get ("logger." + args[i]);
                logger.addListener (this);
            } catch (NameRegistrar.NotFoundException e) {
                cli.println ("Logger " + args[i] + " not found -- ignored.");
            }
        }
        cli.getConsoleReader().readCharacter(new char[] { 'q', 'Q' });
        for (int i=1; i<args.length; i++) {
            try {
                Logger logger = (Logger) NameRegistrar.get ("logger." + args[i]);
                logger.removeListener (this);
            } catch (NameRegistrar.NotFoundException e) { }
        }
    }
    public void usage (CLI cli) {
        cli.println ("Usage: tail [log-name] [log-name] ...");
        showLoggers (cli);
    }
    public synchronized LogEvent log (LogEvent ev) {
        if (p != null) {
            Date d = new Date (System.currentTimeMillis());
            ANSIBuffer ab = new ANSIBuffer();
            ab.setAnsiEnabled (ansi);
            cli.println (
                ab.bold (
                    ev.getSource().getLogger().getName() + 
                    ": " + ev.getRealm() + " " + d.toString() +"." + d.getTime() % 1000 
                ).toString (ansi)
            );
            ev.dump (p, " ");
            p.flush();
        }
        return ev;
    }
    private void showLoggers (CLI cli) {
        NameRegistrar nr = NameRegistrar.getInstance();
        int maxw = 0;
        Iterator iter = nr.getMap().entrySet().iterator();
        StringBuffer sb = new StringBuffer ("available loggers:");
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next ();
            String key = (String) entry.getKey();
            if (key.startsWith ("logger.") && entry.getValue() instanceof Logger) {
                sb.append (' ');
                sb.append (key.substring(7));
            }
        }
        cli.println (sb.toString());
    }
}

