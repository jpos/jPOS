package qsp;

//
// This just a wrapper class in order to
// re-use example.(sh|bat) stuff.
//
public class Test {
    public static void main (String args[]) {
	if (args.length > 0) {
	    args[0] = "src/ext-examples/qsp/" + args[0];
	    System.out.println ("Configuration File: "+args[0]);
	}
	org.jpos.apps.qsp.QSP.main (args);
    }
}
