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

package org.jpos.space;

import java.io.IOException;
import java.io.Serializable;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import jdbm.helper.Serializer;
import jdbm.helper.FastIterator;

/**
 * JDBM based persistent space implementation
 *
 * @author Alejandro Revilla
 * @author Kris Leite
 * @version $Revision$ $Date$
 * @since 1.4.7
 */
public class JDBMSpace implements Space {
    protected HTree htree;
    protected RecordManager recman;
    protected static Serializer refSerializer = new Ref ();
    protected static Map spaceRegistrar = new HashMap ();
    protected boolean autoCommit = true;
    protected String name;

    /**
     * protected constructor. 
     * @see getSpace()
     */
    protected JDBMSpace (String name, String filename) {
        super();
        this.name = name;
        try {
            Properties props = new Properties();
            props.put (RecordManagerOptions.CACHE_SIZE, "512");
            recman = RecordManagerFactory.createRecordManager (filename, props);
            long recid = recman.getNamedObject ("space");
            if (recid != 0) {
                htree = HTree.load (recman, recid);
            } else {
                htree = HTree.createInstance (recman);
                recman.setNamedObject ("space", htree.getRecid());
            }
            recman.commit ();
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    /**
     * @return reference to default JDBMSpace
     */
    public static final JDBMSpace getSpace () {
        return getSpace ("space");
    }
    /**
     * creates a named JDBMSpace 
     * (filename used for storage is the same as the given name)
     * @param name the Space name
     * @return reference to named JDBMSpace
     */
    public static final JDBMSpace getSpace (String name) {
        return getSpace (name, name);
    }
    /**
     * creates a named JDBMSpace
     * @param name the Space name
     * @param filename the storage file name
     * @return reference to named JDBMSpace
     */
    public synchronized static final JDBMSpace getSpace 
        (String name, String filename) 
    {
        JDBMSpace sp = (JDBMSpace) spaceRegistrar.get (name);
        if (sp == null) {
            sp = new JDBMSpace (name, filename);
            spaceRegistrar.put (name, sp);
        }
        return sp;
    }
    /**
     * Use with utmost care and at your own risk.
     *
     * If you are to perform several operations on the space you
     * should synchronize on the space, i.e:
     * <pre>
     *   synchronized (sp) {
     *     sp.setAutoCommit (false);
     *     sp.out (..., ...)
     *     sp.out (..., ...)
     *     ...
     *     ...
     *     sp.inp (...);
     *     sp.commit ();    // or sp.rollback ();
     *     sp.setAutoCommit (true);
     *   }
     * </pre>
     * @param b true or false
     */
    public void setAutoCommit (boolean b) {
        this.autoCommit = b;
    }
    /**
     * force commit
     * @see setAutoCommit(boolean)
     */
    public void commit () {
        try {
            recman.commit ();
            this.notifyAll ();
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    /**
     * force rollback 
     * @see setAutoCommit(boolean)
     */
    public void rollback () {
        try {
            recman.rollback ();
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    /**
     * close this space - use with care
     */
    public void close () {
        synchronized (JDBMSpace.class) {
            spaceRegistrar.remove (name);
        }
        synchronized (this) {
            try {
                recman.close ();
                recman = null;
            } catch (IOException e) {
                throw new SpaceError (e);
            }
        }
    }
    /**
     * Write a new entry into the Space
     * @param key Entry's key
     * @param value Object value
     */
    public void out (Object key, Object value) {
        out (key, value, -1);
    }
    /**
     * Write a new entry into the Space
     * The entry will timeout after the specified period
     * @param key Entry's key
     * @param value Object value
     * @param timeout entry timeout in millis
     */
    public void out (Object key, Object value, long timeout) {
        try {
            synchronized (this) {
                long recid = recman.insert (value);

                long expiration = timeout == -1 ? Long.MAX_VALUE :
                    (System.currentTimeMillis() + timeout);
                Ref dataRef = new Ref (recid, expiration);
                long dataRefRecId = recman.insert (dataRef, refSerializer);

                Head head = (Head) htree.get (key);
                if (head == null) {
                    head = new Head ();
                    head.first = dataRefRecId;
                    head.last  = dataRefRecId;
                    head.count = 1;
                } else {
                    long previousLast = head.last;
                    Ref lastRef   = 
                        (Ref) recman.fetch (previousLast, refSerializer);
                    lastRef.next      = dataRefRecId;
                    head.last         = dataRefRecId;
                    head.count++;
                    recman.update (previousLast, lastRef, refSerializer);
                }
                htree.put (key, head);
                if (autoCommit) {
                    recman.commit ();
                    this.notifyAll ();
                }
            }
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    /**
     * Read probe reads an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    public synchronized Object rdp (Object key) {
        try {
            Object obj = null;
            Ref ref = getFirst (key, false);
            if (ref != null) 
                obj = recman.fetch (ref.recid);
            if (autoCommit)
                recman.commit ();
            return obj;
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }

    /**
     * In probe takes an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    public synchronized Object inp (Object key) {
        try {
            Object obj = null;
            Ref ref = getFirst (key, true);
            if (ref != null) {
                obj = recman.fetch (ref.recid);
                recman.delete (ref.recid);
            }
            if (autoCommit)
                recman.commit ();
            return obj;
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    public synchronized Object in (Object key) {
        Object obj;
        while ((obj = inp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    /**
     * Take an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    public synchronized Object in (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = inp (key)) == null && 
                ((now = System.currentTimeMillis()) < end))
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return obj;
    }

    /**
     * Read an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    public synchronized Object rd  (Object key) {
        Object obj;
        while ((obj = rdp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return obj;
    }

    /**
     * Read an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     */
    public synchronized Object rd  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = rdp (key)) == null && 
                ((now = System.currentTimeMillis()) < end))
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    /**
     * @return aproximately queue size
     */
    public long size (Object key) {
        try {
            Head head = (Head) htree.get (key);
            return (head != null) ? head.count : 0;
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }

    private void purge (Object key) throws IOException {
        Head head = (Head) htree.get (key);
        Ref ref, previousRef = null;
        if (head != null) {
            for (long recid = head.first; recid >= 0; ) {
                Ref r = (Ref) recman.fetch (recid, refSerializer);
                if (r.isExpired ()) {
                    recman.delete (r.recid);
                    recman.delete (recid);
                    head.count--;
                    if (previousRef == null) {
                        head.first = r.next;
                    } else {
                        previousRef.next = r.next;
                        recman.update (
                            head.last, previousRef, refSerializer
                        );
                    }
                } else {
                    previousRef   = r;
                    head.last     = recid;
                }
                recid = r.next;
            }
            if (head.first == -1)  {
                htree.remove (key);
            }
            else {
                htree.put (key, head);
            }
        }
    }
    /**
     * garbage collector.
     * removes expired entries
     */
    public void gc () {
        final String GCKEY = "GC$" + Integer.toString (hashCode());
        final long TIMEOUT = 24 * 3600 * 1000;
        try {
            FastIterator iter = htree.keys ();
            Object obj = new Boolean (true);

            // avoid ConcurrentModificationException in Head
            out (GCKEY, obj, TIMEOUT);  

            while ( (obj = iter.next()) != null) {
                out (GCKEY, obj, TIMEOUT);
                Thread.yield ();
            }
            while ( (obj = inp (GCKEY)) != null) {
                synchronized (this) {
                    purge (obj);
                    recman.commit ();
                }
                Thread.yield ();
            }    
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }

    private Ref getFirst (Object key, boolean remove) throws IOException {
        Head head = (Head) htree.get (key);
        Ref ref = null;
        if (head != null) {
            long recid;
            for (recid = head.first; recid >= 0; ) {
                Ref r = (Ref) recman.fetch (recid, refSerializer);
                if (r.isExpired ()) {
                    recman.delete (r.recid);
                    recman.delete (recid);
                    recid = r.next;
                    head.count--;
                } else  {
                    ref = r;
                    if (remove) {
                        recman.delete (recid);
                        recid = ref.next;
                        head.count--;
                    }
                    break;
                }
            } 
            if (head.first != recid) {
                if (recid < 0)
                    htree.remove (key);
                else {
                    head.first = recid;
                    htree.put (key, head);
                }
            }
        }
        return ref;
    }
    static class Head implements Externalizable {
        public long first;
        public long last;
        public long count;
        static final long serialVersionUID = 2L;

        public Head () {
            super ();
            first = -1;
            last  = -1;
        }
        public void writeExternal (ObjectOutput out) throws IOException {
            out.writeLong (first);
            out.writeLong (last);
            out.writeLong (count);
        }
        public void readExternal (ObjectInput in) throws IOException {
            first = in.readLong ();
            last  = in.readLong ();
            count = in.readLong ();
        }
        public String toString() {
            return getClass().getName() 
                + "@" + Integer.toHexString(hashCode())
                + ":[first=" + first 
                + ",last=" + last
                + "]";
        }
    }
    static class Ref implements Serializer {
        long recid;
        long expires;
        long next;
        static final long serialVersionUID = 1L;

        public Ref () {
            super();
        }
        public Ref (long recid, long expires) {
            super();
            this.recid   = recid;
            this.expires = expires;
            this.next    = -1;
        }

        public boolean isExpired () {
            return expires < System.currentTimeMillis ();
        }
        public String toString() {
            return getClass().getName() 
                + "@" + Integer.toHexString(hashCode())
                + ":[recid=" + recid
                + ",next=" + next
                + ",expired=" + isExpired ()
                + "]";
        }
        public byte[] serialize (Object obj) 
            throws IOException
        {
            Ref d = (Ref) obj;

            byte[] buf = new byte [24];
            putLong (buf, 0, d.recid);
            putLong (buf, 8, d.next);
            putLong (buf,16, d.expires);
            return buf;
        }
        public Object deserialize (byte[] serialized) 
            throws IOException
        {
            Ref d = new Ref ();
            d.recid   = getLong (serialized,  0);
            d.next    = getLong (serialized,  8);
            d.expires = getLong (serialized, 16);
            return d;
        }
    }
    static void putLong (byte[] b, int off, long val) {
        b[off+7] = (byte) (val >>>  0);
        b[off+6] = (byte) (val >>>  8);
        b[off+5] = (byte) (val >>> 16);
        b[off+4] = (byte) (val >>> 24);
        b[off+3] = (byte) (val >>> 32);
        b[off+2] = (byte) (val >>> 40);
        b[off+1] = (byte) (val >>> 48);
        b[off+0] = (byte) (val >>> 56);
    }
    static long getLong (byte[] b, int off) {
        return ((b[off+7] & 0xFFL) << 0)  +
            ((b[off+6] & 0xFFL) << 8)  +
            ((b[off+5] & 0xFFL) << 16) +
            ((b[off+4] & 0xFFL) << 24) +
            ((b[off+3] & 0xFFL) << 32) +
            ((b[off+2] & 0xFFL) << 40) +
            ((b[off+1] & 0xFFL) << 48) +
            ((b[off+0] & 0xFFL) << 56);
    }
}
