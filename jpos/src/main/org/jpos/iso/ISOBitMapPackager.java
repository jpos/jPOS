package uy.com.cs.jpos.iso;

/**
 * IF*_BITMAP classes extends this class instead of ISOFieldPackager
 * so packagers can check if field-1 ISOFieldPackager is an instance
 * of an ISOBitMapPackager and handle differences between ANSI X9.2
 * and ISO-8583 packaging schemes.<br>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 *
 * @see ISOFieldPackager
 */
public abstract class ISOBitMapPackager extends ISOFieldPackager {
	public ISOBitMapPackager(int len, String description) {
		super(len, description);
	}
}
