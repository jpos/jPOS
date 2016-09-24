package org.jpos.q2.cli.deploy;

import java.io.File;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

public class DISABLE implements CLICommand {

	@Override
	public void exec(CLIContext ctx, String[] args) throws Exception {
		if (args.length < 2) {
			ctx.println("Usage: enable <xml_file(no extenssion)>");
			return;
		}		
		File deployDir = ctx.getCLI().getQ2().getDeployDir();		
		File deploy = new File(deployDir.getAbsolutePath()+"/"+args[1]+".xml");
		if(deploy.exists() && deploy.isFile()){
			File dest = new File(deployDir.getAbsolutePath()+"/"+args[1]+".xml.off");
			deploy.renameTo(dest);
			ctx.println("DISABLED: " + args[1]);
			return;
		}
		File dest = new File(deployDir.getAbsolutePath()+"/"+args[1]+".xml.off");
		if(dest.exists()){
			ctx.println("Already disabled: "+args[1]);
			return;
		}
		ctx.println("Can't find the bean descriptor: "+args[1]);
		return;
	}
}