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


/**
 * <p><b>Space</b> uses concepts described in the Linda Coordination Language 
 * that eases the implementation of other jPOS components (such as 
 * Channels, Muxes, etc.), but it is not by any means an attempt to provide
 * a full implementation.</p>
 *
 * <p>jPOS's Space is basically a Map where each entry is a LinkedList 
 * of values that can be used as a BlockingQueue</p>
 *
 * <p>One can place entries on a queue by calling Space.out, take them
 * by calling Space.in and read (without taking) by calling Space.rd</p>
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 * @see LeasedReference
 * @see TransientSpace
 * @see SpaceError
 * @see <a href="http://www.cs.yale.edu/Linda/linda-lang.html">The Linda Coordination Language</a>
 */

public interface Space {
    /**
     * Write a new entry into the Space
     * @param key Entry's key
     * @param value Object value
     */
    public void out (Object key, Object value);

    /**
     * Write a new entry into the Space, with an timeout value
     * @param key Entry's key
     * @param value Object value
     * @param timeout timeout value
     */
    public void out (Object key, Object value, long timeout);

    /**
     * Take an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    public Object in  (Object key);

    /**
     * Read an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    public Object rd  (Object key);

    /**
     * Take an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     */
    public Object in  (Object key, long timeout);


    /**
     * Read an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     */
    public Object rd  (Object key, long timeout);


    /**
     * In probe takes an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    public Object inp (Object key);


    /**
     * Read probe reads an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    public Object rdp (Object key);
}

