/**
 * ISOComponent implementa un <b>Component</b>
 * dentro de un <b>Composite pattern</b>
 *
 * @see "Design Patterns ISBN 0-201-63361-2"
 *
 * ISOComponent es una abstract class de la cual derivan
 * ISOMsg (the Composite) y ISOField (the Leaf)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOField
 * @see ISOException
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:24  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;

public abstract class ISOComponent implements Cloneable {
	public void set (ISOComponent c) throws ISOException {
		throw new ISOException ("Can't add to Leaf");
	}
	public void unset (int fldno) throws ISOException {
		throw new ISOException ("Can't remove from Leaf");
	}
	public ISOComponent getComposite() {
		return null;
	}
	public Object getKey() throws ISOException {
		throw new ISOException ("N/A in Composite");
	}
	public Object getValue() throws ISOException {
		throw new ISOException ("N/A in Composite");
	}
	public int getMaxField() {
		return 0;
	}
	public Hashtable getChildren() {
		return new Hashtable();
	}
	public abstract void setValue(Object obj) throws ISOException;
	public abstract byte[] pack() throws ISOException;
	public abstract int unpack(byte[] b) throws ISOException;
	public abstract void dump (PrintStream p, String indent);
}
