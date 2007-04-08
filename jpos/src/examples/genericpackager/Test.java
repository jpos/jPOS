

package genericpackager;

import java.io.*;
import java.util.*;

import org.jpos.iso.*;
import org.jpos.iso.packager.GenericPackager;

public class Test
{
 /**
     * Test harness for GenericPackager
     *
     * <pre>
     * Takes 2 arguments
     * args[0] = xml field description file
     * args[1] = file containing a hex dump of the message to parse
     * </pre>
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println ("Usage: GenericPackager packager datafile");
            System.out.println ("       Where packager is an XML packager description");
            System.out.println ("       And   datafile is a hex dump of an ISOMessage");
            System.exit(1);
        }
        GenericPackager p = new GenericPackager(args[0]);

        InputStream in = new FileInputStream (args[1]);

        int nbytes = in.available();

        // Read in input file and strip any training white space
        byte[] hexbytes = new byte[nbytes];
        in.read (hexbytes, 0, nbytes);
        while (Character.isWhitespace ((char)hexbytes[nbytes-1]))
            nbytes--;

        // Convert it to a byte array
        byte b[] = ISOUtil.hex2byte (hexbytes,0,nbytes/2);

        // And upack it into an ISOMsg
        ISOMsg msg = new ISOMsg();
        msg.setPackager (p);
        msg.unpack (b);

        msg.dump (new PrintStream(System.out), "");

        // Now Re-Pack the message
        byte b2[] = msg.pack();

        // The byte arrays should be the same!
        if (!Arrays.equals (b, b2))
        {
            System.out.println ("Error Re-Packing Message");
            System.out.println (new String (ISOUtil.hexString(b)));
            System.out.println (new String (ISOUtil.hexString(b2)));
        }
        else 
            System.out.println ("Message re-packed ok");
    }

}

