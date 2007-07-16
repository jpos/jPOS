package org.jpos.q2.cli;

import org.jpos.q2.CLI;
import java.util.Map;
import java.util.Iterator;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;
import org.jpos.util.NameRegistrar;

public class SHOWNR implements CLI.Command {
    public void exec (CLI cli, String[] args) throws Exception {
        boolean all = args.length > 1 && "-a".equals (args[1]);
        int i = 1;
        if (all)
            i++;
        if (args.length > i)
            showOne (cli, args[i], all);
        else
            showAll (cli, all);
    }
    private void showOne (CLI cli, String name, boolean detail) {
        try {
            Object obj = NameRegistrar.get (name);
            cli.println (name + " : " + obj.toString());
            if (detail && obj instanceof Loggeable) {
                ((Loggeable)obj).dump (cli.getOutputStream(), "   ");
                cli.getOutputStream().flush();
            }
        } catch (NameRegistrar.NotFoundException e) {
            cli.println ("Object not found in NameRegistrar");
        }
    }
    private void showAll (CLI cli, boolean detail) {
        NameRegistrar nr = NameRegistrar.getInstance();
        int maxw = 0;
        Iterator iter = nr.getMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next ();
            maxw = Math.max (maxw, entry.getKey().toString().length());
        }
        iter = nr.getMap().entrySet().iterator();
        maxw++;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next ();
            cli.println (
                ISOUtil.strpad (entry.getKey().toString(), maxw) + 
                entry.getValue().toString()
            );
            if (detail && entry.getValue() instanceof Loggeable) {
                ((Loggeable)entry.getValue()).dump (cli.getOutputStream(), "   ");
                cli.getOutputStream().flush();
            }
        }
    }
}

