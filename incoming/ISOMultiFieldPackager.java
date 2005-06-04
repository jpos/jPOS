/*
 * Copyright (c) 2005 jPOS.org.  All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;


/**
 * A sub field packager that takes a List of possible ISOFields as it's
 * constructor.
 * 
 * On the call to unpack the The List of ISOComponents will be seqentially
 * passed the byte[]. The first to successfully report an unpack success is the
 * field we have. Should none be satified with the incoming byte[] then a
 * ISOException will result.
 * 
 * Field 127 in the VISA spec is a particular field that migh use this approach.
 * 
 * @author <a href="mailto:marksalter@dsl.pipex.com">Mark Salter </a>
 * @version $Id: MultiSubFieldPackager.java,v 1.0 2005/06/03 20:20:00 marksalter
 *          Exp $
 * 
 * @see ISOComponent
 * 
 */

public class ISOMultiFieldPackager extends ISOFieldPackager {

	protected List possibles = null;

	protected String description;

	protected ISOFieldPackager current = null;

	/*
	 * @param desc      - ISOMultFieldPackager description
	 * @param possibles	- A List of the possible items in this field 
	 */
	public ISOMultiFieldPackager(String desc, List possibles) {
		this.description = desc;
		this.possibles = possibles;

	}

	/*
	 * @param desc      - ISOMultFieldPackager description.
	 * @param possibles	- An array of items to consider (build a List). 
	 */
	public ISOMultiFieldPackager(String desc, ISOFieldPackager[] a) {
		Vector v = new Vector(a.length);
		this.description = desc;

		for (int i = 0; i < a.length; i++) {
			v.add(a[i]);
		}
		possibles = v;
	}

	/**
	 * @param m      - the ISOComponent to receive the value consumed from ...
	 * @param b      - the byte[] holding the raw message
	 * @param offset - the current position within b.
	 */

	public int unpack(ISOComponent m, byte[] b, int offset) throws ISOException {
		ListIterator i = possibles.listIterator();

		while (i.hasNext()) {
			try {
				current = (ISOFieldPackager) i.next();
				ISOField f = new ISOField();
				int consumed = current.unpack(f, b, offset);
				m.setValue(f.getValue());
				return consumed;
			} catch (ISOException eok) {
				current = null;
			}
		}

		throw new ISOException("unpack failed in List process!");
	}
	
	/**
	 * Unpack the passed InputStream into an ISOComponent from our list of possibles.
	 * The InputStream *must* support mark!
	 */

	public void unpack(ISOComponent c, InputStream in) throws IOException, ISOException {
		if (!in.markSupported()){
			throw new ISOException("InputStream passed to ISOMultFieldPackager *must* support the mark method.");
		}
		
		ListIterator i = possibles.listIterator();
		
		// Save our position in the InputStream
		in.mark(1024);

		while (i.hasNext()) {
			in.reset();
			try {
				// One of our possibles will succeed in it's unpack from this Stream.
				current = (ISOFieldPackager) i.next();
				current.unpack(c,in);
				return ;
			} catch (ISOException eok) {
				// This one failed.
				current = null;
			}
			 catch (IOException eok) {
				// This one failed.
				current = null;
			}
		}

		throw new ISOException("unpack failed to find a match in the possible List!");
	}

	/**
	 * Pack the subfield into a byte array
	 */

	public byte[] pack(ISOComponent m) throws ISOException {
		if (current == null) {
			throw new ISOException(
					"I cannot pack a ISOMultFieldPackager without knowing which item to pack.\nEither previously pack unpack a message, or use my hint(String) method");
		} else {
			return current.pack(m);
		}
	}

	public void hint(String desc) throws ISOException {
		// Iterate through our List, asking for a ISOComponent that matches
		// desc.
		// Tag any element found as our current.
		ListIterator i = possibles.listIterator();

		while (i.hasNext()) {
			ISOFieldPackager c = (ISOFieldPackager) i.next();
			if (desc.equals(c.getDescription())) {
				current = c;
				return;
			}
		}
		throw new ISOException(
				"I did not find a ISOComponent witht the description(" + desc
						+ ") in my List of possibles.");
	}

	public int getMaxPackedLength() {
		return 0;
	}
}
