package org.jpos.iso.filter;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOBinaryField;
import org.jpos.util.LogEvent;
import org.jpos.core.ReConfigurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOFilter.VetoException;

/**
 * Computes an MD5 based Message Authentication Code
 * on outgoing messages and checks that MAC on incoming
 * ones.
 *
 * @author Alejandro P. Revilla
 * @version $Revision$ $Date$
 * @since 1.2.8
 * @see org.jpos.iso.ISOFilter
 */
public class MD5Filter implements ISOFilter, ReConfigurable {
    String key;
    int[] fields;

    public MD5Filter() {
	super();
    }
   /**
    * @param cfg
    * <ul>
    * <li>key    - initial key
    * <li>fields - Space separated field list
    * </ul>
    */
    public void setConfiguration (Configuration cfg) 
	throws ConfigurationException 
    {
	key = cfg.get ("key");
	String fieldList = cfg.get ("fields");
	if (fieldList == null)
	    throw new ConfigurationException ("'fields' property not present");

	StringTokenizer st = new StringTokenizer (fieldList);
	int f[] = new int[st.countTokens()];

	for (int i=0; i<f.length; i++) 
	    f[i] = Integer.parseInt (st.nextToken());

	fields = f;
    }
    public void setFields (int[] fields) {
	this.fields = fields;
    }
    /**
     * factory method
     * @param m current ISOMsg
     * @return key fields associated with this ISOMsg
     */
    public int[] getFields (ISOMsg m) {
	return fields;
    }
    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
	throws VetoException
    {
	if (key == null || fields == null)
	    throw new VetoException ("MD5Filter not configured");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update (getKey());
	    int[] f = getFields (m);
	    for (int i=0; i<f.length; i++) {
		int fld = f[i];
		if (m.hasField (fld)) {
		    ISOComponent c = m.getComponent (fld);
		    if (c instanceof ISOBinaryField)
			md.update ((byte[]) c.getValue());
		    else
			md.update (((String)c.getValue()).getBytes());
		}
	    }
            byte[] digest = md.digest();
	    if (m.getDirection() == ISOMsg.OUTGOING) {
		m.set (new ISOBinaryField ( 64, digest, 0, 8));
		m.set (new ISOBinaryField (128, digest, 8, 8));
	    } else {
		byte[] rxDigest = new byte[16];
		if (m.hasField (64))
		    System.arraycopy (
			(byte[]) m.getValue(64), 0, rxDigest, 0, 8
		    );
		if (m.hasField (128))
		    System.arraycopy (
			(byte[]) m.getValue(128), 0, rxDigest, 8, 8
		    );
		if (!Arrays.equals (digest, rxDigest)) {
		    evt.addMessage (m);
		    evt.addMessage ("MAC  spected: "
			+ISOUtil.hexString (digest));
		    evt.addMessage ("MAC received: "
			+ISOUtil.hexString (rxDigest));
		    throw new VetoException ("invalid MAC");
		}
		m.unset  (64);
		m.unset (128);
	    }
        } catch (NoSuchAlgorithmException e) {
	    throw new VetoException (e);
        } catch (ISOException e) {
	    throw new VetoException (e);
	}
	return m;
    }
    /**
     * hook for custom key storage (i.e. crypto cards)
     */
    protected byte[] getKey() {
	return key.getBytes();
    }
}

