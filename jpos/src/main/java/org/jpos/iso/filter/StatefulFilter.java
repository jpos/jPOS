/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.iso.filter;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.LogEvent;

/**
 * Filter that maintains some fields of arriving messages in one direction and put
 * that fields in the message going in the oposite direction that is the answer,
 * based on a key formed by some fields.
 * @author <a href="mailto:aalcarraz@cabal.com.uy">Andr&eacute;s Alcarraz </a>
 */
@SuppressWarnings("unchecked")
public class StatefulFilter implements ISOFilter, Configurable{
    
    /**
     * Space to hold the queues
     */
    private Space space = SpaceFactory.getSpace();

    /**
     * Holds value of property keyPrefix.
     */
    private String keyPrefix=""+hashCode();

    /**
     * Holds value of property vetoUnmatched.
     */
    private boolean vetoUnmatched;

    /**
     * what fields conorfm the key of the message?
     */
    private int[] key = {11,41};
    
    
    /**
     * 
     */
    
    /**
     * What messages should I match?
     * INCOMING: outgoing messages will be matched against the previous incoming messages
     * OUTGOING: incoming messages will be matched against the previous outgoing messages
     */
    private int matchDirection = ISOMsg.INCOMING;

    /**
     * Holds value of property ignoredFields.
     */
    private int[] ignoredFields = {};

    /**
     * Holds value of property savedFields.
     */
    private int[] savedFields = {};

    
    private static final long TIMEOUT=60000;

    /**
     * Holds value of property timeout.
     */
    private long timeout = TIMEOUT;

    /**
     * Holds value of property overwriteOriginalFields.
     */
    private boolean overwriteOriginalFields;

    /** Creates a new instance of StateFulFilter */
    public StatefulFilter() {
    }

    /**
     * Getter for property space.
     * @return Value of property space.
     */
    protected Space getSpace() {

        return this.space;
    }

    /**
     * Setter for property space.
     * @param space New value of property space.
     */
    protected void setSpace(Space space) {

        this.space = space;
    }

    /**
     * Getter for property keyPrefix.
     * @return Value of property keyPrefix.
     */
    public String getKeyPrefix() {

        return this.keyPrefix;
    }

    /**
     * Setter for property keyPrefix.
     * @param keyPrefix New value of property keyPrefix.
     */
    public void setKeyPrefix(String keyPrefix) {

        this.keyPrefix = keyPrefix;
    }

    public void setConfiguration(Configuration cfg) 
        throws ConfigurationException 
    {
        setVetoUnmatched(cfg.getBoolean("veto-unmatched"));
        setSpace(cfg.get("space",""));
        setKeyPrefix(cfg.get("key-prefix", ""+hashCode()));
        setTimeout(cfg.getLong("timeout", TIMEOUT));
        setOverwriteOriginalFields(cfg.getBoolean("overwrite-original-fields", true));
        int dir = cfg.get("direction","incoming").equals("incoming") ? 
            ISOMsg.INCOMING : ISOMsg.OUTGOING;
        setMatchDirection(dir);
        
        setKey(ISOUtil.toIntArray(cfg.get("key", "11 41")));
        setSavedFields(ISOUtil.toIntArray(cfg.get("saved-fields", "")));
        setIgnoredFields(ISOUtil.toIntArray(cfg.get("ignored-fields", "")));
    }
    public void setSpace(String uri){
        setSpace(SpaceFactory.getSpace(uri));
    }
    
    public ISOMsg filter(ISOChannel iSOChannel, ISOMsg m, LogEvent evt) 
        throws ISOFilter.VetoException 
    {
        int[] key = getKey();
        String keyPrefix = getKeyPrefix();
        if (keyPrefix == null)
            throw new NullPointerException("key prefix can not be null");
        StringBuilder b = new StringBuilder(keyPrefix);
        for (int aKey : key) {
            b.append("|");
            b.append(m.getString(aKey));
        }
        String skey = b.toString();
        if(m.getDirection() == getMatchDirection()){
            int[] savedFields = getSavedFields();
            ISOMsg saved = (ISOMsg)(
                savedFields != null && savedFields.length != 0 ?
                    m.clone(savedFields) : m.clone());
            int[] ignoredFields = getIgnoredFields();
            if (ignoredFields != null) saved.unset(ignoredFields);
            getSpace().out(skey, saved, getTimeout());
            return m;
        } else {
            ISOMsg saved = (ISOMsg)getSpace().inp(skey);
            if (saved == null && isVetoUnmatched()) 
                throw new VetoException("unmatched iso message");
            else if(saved != null) {
                if (!isOverwriteOriginalFields()) m.merge(saved);
                else {
                    saved.merge(m);
                    m = saved;
                }
            }
            return m;
        }
    }

    /**
     * Getter for property vetoUnmatched.
     * @return Value of property vetoUnmatched.
     */
    public boolean isVetoUnmatched() {

        return this.vetoUnmatched;
    }

    /**
     * Setter for property vetoUnmatched.
     * @param vetoUnmatched New value of property vetoUnmatched.
     */
    public void setVetoUnmatched(boolean vetoUnmatched) {

        this.vetoUnmatched = vetoUnmatched;
    }

    /**
     * Getter for property matchDirection.
     * @return Value of property matchDirection.
     */
    public int getMatchDirection() {

        return this.matchDirection;
    }

    /**
     * Setter for property matchDirection.
     * @param matchDirection New value of property matchDirection.
     */
    public void setMatchDirection(int matchDirection) {

        this.matchDirection = matchDirection;
    }

    /**
     * Indexed getter for property ignoredFields.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public int getIgnoredField(int index) {

        return this.ignoredFields[index];
    }

    /**
     * Getter for property ignoredFields.
     * @return Value of property ignoredFields.
     */
    public int[] getIgnoredFields() {

        return this.ignoredFields;
    }

    /**
     * Indexed setter for property ignoredFields.
     * @param index Index of the property.
     * @param ignoredFields New value of the property at <CODE>index</CODE>.
     */
    public void setIgnoredField(int index, int ignoredFields) {

        this.ignoredFields[index] = ignoredFields;
    }

    /**
     * Setter for property ignoredFields.
     * @param ignoredFields New value of property ignoredFields.
     */
    public void setIgnoredFields(int[] ignoredFields) {

        this.ignoredFields = ignoredFields;
    }

    /**
     * Indexed getter for property savedFields.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public int getSavedField(int index) {

        return this.savedFields[index];
    }

    /**
     * Getter for property savedFields.
     * @return Value of property savedFields.
     */
    public int[] getSavedFields() {

        return this.savedFields;
    }

    /**
     * Indexed setter for property savedFields.
     * @param index Index of the property.
     * @param savedFields New value of the property at <CODE>index</CODE>.
     */
    public void setSavedField(int index, int savedFields) {

        this.savedFields[index] = savedFields;
    }

    /**
     * Setter for property savedFields.
     * @param savedFields New value of property savedFields.
     */
    public void setSavedFields(int[] savedFields) {

        this.savedFields = savedFields;
    }

    /**
     * Indexed getter for property key.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public int getKey(int index) {

        return this.key[index];
    }

    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public int[] getKey() {

        return this.key;
    }

    /**
     * Indexed setter for property key.
     * @param index Index of the property.
     * @param key New value of the property at <CODE>index</CODE>.
     */
    public void setKey(int index, int key) {

        this.key[index] = key;
    }

    /**
     * Setter for property key.
     * @param key New value of property key.
     */
    public void setKey(int[] key) {

        this.key = key;
    }

    /**
     * Getter for property timeout.
     * @return Value of property timeout.
     */
    public long getTimeout() {

        return this.timeout;
    }

    /**
     * Setter for property timeout.
     * @param timeout New value of property timeout.
     */
    public void setTimeout(long timeout) {

        this.timeout = timeout;
    }

    /**
     * Getter for property overwriteOriginalFields.
     * @return Value of property overwriteOriginalFields.
     */
    public boolean isOverwriteOriginalFields() {

        return this.overwriteOriginalFields;
    }

    /**
     * Setter for property overwriteOriginalFields.
     * @param overwriteOriginalFields New value of property overwriteOriginalFields.
     */
    public void setOverwriteOriginalFields(boolean overwriteOriginalFields) {

        this.overwriteOriginalFields = overwriteOriginalFields;
    }
    

}
