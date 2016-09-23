package org.jpos.q2.cli;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

public class DEPLOY implements CLICommand {

	@Override
	public void exec(CLIContext ctx, String[] args) throws Exception {
		if (args.length != 2) {
			ctx.println("Usage: deploy [operation]");
			return;
		}
		switch (args[1]) {
			case "list":
				this.listDeployDir(ctx);
		}

		return;
	}
	public void listDeployDir(CLIContext ctx){
		Q2 q2 = ctx.getCLI().getQ2();
	}

}
