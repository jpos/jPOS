/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2014 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso;

import java.util.BitSet;

/**
 * IF*_BITMAPStandalone classes extend this class instead of ISOFieldPackager
 * so packagers can check if this field is present needs special handling on pack/unpack.
 *  *
 * @author apr@cs.com.uy
 * @author marksalter@talktalk.net
 * 
 * @version $Id$
 *
 * @see ISOFieldPackager
 */
public abstract class ISOBitMapStandalonePackager extends ISOFieldPackager {

	private ISOBitMapStandalone bmap;
	private int key = 65;
	
	public ISOBitMapStandalonePackager() {
        super();
    }
    public ISOBitMapStandalonePackager(int len, String description) {
        super(len, description);
    }
    public ISOComponent createComponent(int fieldNumber) {
    	key = fieldNumber;
    	bmap = new ISOBitMapStandalone(key);
        return (ISOComponent)bmap;
    }
	public void adjustBitMapForUnpack(BitSet bitset) {
		
		
		if (key == 0) {
			// nothing to add, we have not been created.
		} else {
		
			/*
			 * Loop though our bits, adjusting by our field value and setting as 
			 * on in bmap.
			 */
			int base = (key - 1) << 1 ;
			int set = 0;
			
			BitSet myBmap = (BitSet) this.bmap.getValue();
			
	    	for (int i = myBmap.cardinality(); i > 0; i--) {
	    		set = myBmap.nextSetBit(set+1);
	    		bitset.set(set+base);
	    	}
		}
		
		
	}
	public int getMaxField() {
		return ((key - 1) << 1) + getLength() * 8 ;
	}
	public void adjustBitMapForPack(BitSet bitset) {
		if (key == 0) {
			/*
			 * we don't have a key, so we have nothing to add to this bitmap exchange.
			 */
		} else {
			
			
			
			/*
			 * Loop though bitset, unsetting 'our' bits and setting our bits on (relative to base).
			 */
			int base = (key - 1) << 1 ;
			
			BitSet myBmap = new BitSet();
			
			 for (int set = bitset.nextSetBit(base); set >= 0; set = bitset.nextSetBit(set+1)) {
					myBmap.set(set-base);
					bitset.clear(set);
			 }
			
			
			try {
				if (bmap == null) {
					bmap = new ISOBitMapStandalone(key);
				}
				bmap.setValue(myBmap);
			} catch (ISOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
}