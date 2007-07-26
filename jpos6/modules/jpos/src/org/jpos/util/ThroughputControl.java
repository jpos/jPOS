/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.util;

/**
 * ThroughputControl can be used to limit the throughput 
 * of a system to a maximum number of transactions in 
 * a given period of time.
 */
public class ThroughputControl {
    private int[] period;
    private int[] max;
    private int[] cnt;
    private long[] start;
    private long[] sleep;

    /**
     * @param maxTransactions ditto
     * @param periodInMillis ditto
     */
    public ThroughputControl (int maxTransactions, int periodInMillis) {
        this (new int[] { maxTransactions },
              new int[] { periodInMillis });
    }
    /**
     * @param maxTransactions ditto
     * @param periodInMillis ditto
     */
    public ThroughputControl (int[] maxTransactions, int[] periodInMillis) {
        super();
        int l = maxTransactions.length;
        period = new int[l];
        max = new int[l];
        cnt = new int[l];
        start = new long[l];
        sleep = new long[l];
        for (int i=0; i<l; i++) {
            this.max[i]    = maxTransactions[i];
            this.period[i] = periodInMillis[i];
            this.sleep[i]  = Math.min(Math.max (periodInMillis[i]/10, 500L),50L);;
            this.start[i]  = System.currentTimeMillis();
        }
    }

    /**
     * control should be called on every transaction.
     * it may sleep for a while in order to control the system throughput
     * 
     * @return aprox sleep time or zero if no sleep
     */
    public long control() {
        boolean delayed = false;
        long init = System.currentTimeMillis();
        for (int i=0; i<cnt.length; i++) {
            synchronized (this) {
                cnt[i]++;
            }
            do {
                if (cnt[i] > max[i]) {
                    delayed = true;
                    try { 
                        Thread.sleep (sleep[i]); 
                    } catch (InterruptedException e) { }
                }
                synchronized (this) {
                    long now = System.currentTimeMillis();
                    if (now - start[i] > period[i]) {
                        long elapsed = now - start[i];
                        int  allowed = (int) ((elapsed * max[i]) / period[i]);
                        start[i] = now;
                        cnt[i] = Math.max (cnt[i] - allowed, 0);
                    }
                }
            } while (cnt[i] > max[i]);
        }
        return delayed ? (System.currentTimeMillis() - init) : 0L;
    }
}

