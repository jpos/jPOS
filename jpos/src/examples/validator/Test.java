/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */
package validator;

import java.util.BitSet;
import org.jpos.util.*;
import org.jpos.core.*;
import org.jpos.iso.*;
import org.jpos.iso.validator.*;
import org.jpos.iso.packager.*;


/**
 * @author Jose Eduardo Leon
 * @version $Revision$ $Date$
 */
public class Test {
    public static void main( String[] args ) {
        if (args.length != 1) {
            System.out.println ("Usage: bin/example validator [in]valid.xml");
            System.exit (0);
        }
        try {
             SimpleLogListener l = new SimpleLogListener( System.out );
             Logger logger = new Logger(  );
             logger.addListener( l );
             ISOPackager p = new org.jpos.iso.packager.XMLPackager();
             p.setLogger( logger, "xml-input-file-packager" );
             java.io.FileInputStream fis = new java.io.FileInputStream ( "src/examples/validator/" + args[0] );
             byte[] b = new byte[fis.available()];
             fis.read (b);
             ISOMsg m = new ISOMsg ();
             m.setPackager ( p );
             m.unpack (b);

             boolean Break = false;
             ISOValidator v = new GenericValidatingPackager(
                "src/config/packager/test-generic-validating-packager.xml"  
             );
             ((GenericValidatingPackager)v).setLogger( logger, "main-validator" );
             try {
                 m = (ISOMsg)v.validate( m );
             }
             catch (ISOVException ex) {
                 Break = true;
             }
             m.dump( System.out, "" );
             org.jpos.iso.validator.VErrorParser parser = 
                 new org.jpos.iso.validator.VErrorParser();
             parser.setLogger( logger, "erro-parser" );
             java.util.Vector err = parser.getVErrors( m );
             if ( err.size() <= 0 )
                 System.out.println( "There are no errors!" );
             else{
                 System.out.println( "There are " + err.size() + " errors. [Break: " + Break+"]");
                 for (int i = 0; i < err.size(); i++) {
                     ISOVError e = (ISOVError)err.elementAt( i );
                     System.out.println( "( " + (i+1) + " ) " + " Id Path: <<" + e.getId() + ">> Description: <<"+ e.getDescription() + ">> Reject Code: <<" + e.getRejectCode() + ">>" );
                 }
             }
         }
         catch (Exception ex) {
             ex.printStackTrace();
        }
    }
}
