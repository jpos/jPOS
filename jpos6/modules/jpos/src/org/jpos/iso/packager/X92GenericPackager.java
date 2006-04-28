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

package org.jpos.iso.packager;

import java.io.InputStream;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.X92_BITMAP;

/**
 * @see GenericPackager
 * @author Alejandro Revilla
 * @version $$evision: $ $Date$
 */

public class X92GenericPackager extends GenericPackager {
    protected static ISOFieldPackager bitMapPackager = 
        new X92_BITMAP (16, "X9.2 BIT MAP");

    public X92GenericPackager() throws ISOException {
        super();
    }
    public X92GenericPackager(String filename) throws ISOException {
        super(filename);
    }
    public X92GenericPackager(InputStream stream) throws ISOException {
        super(stream);
    }
    /**
     * @return Bitmap's ISOFieldPackager
     */
    protected ISOFieldPackager getBitMapfieldPackager() {
        return bitMapPackager;
    }
    /**
     * Although field 1 is not a Bitmap ANSI X9.2 do have
     * a Bitmap field that have to be packed/unpacked
     * @see org.jpos.iso.ISOBasePackager
     * @return true
     */
    protected boolean emitBitMap () {
        return true;
    }
    /**
     * @return 64 for ANSI X9.2
     */
    protected int getMaxValidField() {
        return 64;
    }
}

