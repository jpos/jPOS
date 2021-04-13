/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

package org.jpos.space;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jpos.iso.ISOUtil;

import org.jpos.util.TPS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Robert Demski
 */
public class TSpacePerformanceTest  {

    LocalSpace<String,Object> sp1;
    LocalSpace<String,Object> sp2;
    final List<Long> t1 = new ArrayList<>();
    final List<Long> t2 = new ArrayList<>();
//    List t1 = Collections.synchronizedCollection(new ArrayList());
    public static final int COUNT = 100000;
    final TPS tpsOut = new TPS(100L, false);
    final TPS tpsIn = new TPS(100L, false);

    class WriteSpaceTask implements Runnable {
        final String key;
        
        WriteSpaceTask(String key){
           this.key = key;
        }
        public void run (){
          long stamp = System.nanoTime();
          for (int i=0; i<COUNT; i++) {
             sp1.out(key, Boolean.TRUE);
             tpsOut.tick();
          }
          long stamp2 = System.nanoTime();
          t1.add(stamp2-stamp);
          System.err.println("Write "+key+" out: "+(stamp2-stamp)/1000000 + " " + tpsOut);
        }
    }

    class ReadSpaceTask implements Runnable {
        final String key;
        
        ReadSpaceTask(String key){
           this.key = key;
        }
        public void run (){
          long stamp = System.nanoTime();
          for (int i=0; i<COUNT; i++) {
             sp1.in(key);
             tpsIn.tick();
          }
          long stamp2 = System.nanoTime();
          t2.add(stamp2-stamp);
          System.err.println("Read  "+key+"  in: "+(stamp2-stamp)/1000000 + " " + tpsIn);
        }  
    }

    class WriteSpaceWithNotifyTask implements Runnable, SpaceListener<String,Object> {
        final String key;
        final LocalSpace<String,Object> sp1;
        final LocalSpace<String,Object> sp2;
        int count = 0;
        
        WriteSpaceWithNotifyTask(String key, LocalSpace<String,Object> sp1, LocalSpace<String,Object> sp2){
          this.key = key;
          this.sp1 = sp1;
          this.sp2 = sp2;
        }
        public void run (){
          sp1.addListener(key, this);
          long stamp = System.nanoTime();
          for (int i=0; i<COUNT; i++)
             sp1.out(key, Boolean.TRUE);
          long stamp2 = System.nanoTime();
          t1.add(stamp2-stamp);
          System.err.println("Perform. "+key+" out: "+(stamp2-stamp)/1000000);
        }

        public void notify(String key, Object value) {
          if ( ++count % 100 == 0) {
            sp2.out(key, value);
          }
        }
    }

    class WriteSpaceWithNotifyReadTask implements Runnable, SpaceListener<String,Object> {
        final String key;
       
        WriteSpaceWithNotifyReadTask(String key){
          this.key = key;
        }
        public void run (){
          sp1.addListener(key, this);
          for (int i=0; i<COUNT; i++)
             sp1.out(key, Boolean.TRUE);
        }
        public void notify(String key, Object value) {
          if ( sp1.rdp(key) == null)
            sp2.out("lost-entry", value);
        }
    }

    @BeforeEach
    public void setUp () {
        sp1 = new TSpace<>();
        sp2 = new TSpace<>();
        t1.clear();
        t2.clear();
    }

    private void printAvg(List<Long> times, String prefix){
        long avg = 0;
        for (Long t :times)
          avg += t;
        if (avg != 0) {
            avg /= times.size();
            avg /= 1000000;
        }
        System.out.println(prefix + avg);
    }

    @Test
    public void testReadPerformance() throws InterruptedException {
        int size = 10;
        ThreadPoolExecutor es = new ThreadPoolExecutor(size, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue<>());
        es.prestartAllCoreThreads();

        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceTask("PerformTask-"+i));
        ISOUtil.sleep(500);
        printAvg(t1, "Avg. write: ");

        for (int i=0; i<size; i++)
          es.execute(new ReadSpaceTask("PerformTask-"+i));
        ISOUtil.sleep(500);
        es.shutdown();
        printAvg(t2, "Avg. read : ");
        if (!es.awaitTermination(5, TimeUnit.SECONDS))
            fail("Failed to shutdown ThreadPoolExecutor.");
    }

    @Test
    public void testDeadLockWithNotify() throws InterruptedException {
        int size = 10;
        final ThreadPoolExecutor es = new ThreadPoolExecutor(size*2, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue<>());
        es.prestartAllCoreThreads();
        
        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceWithNotifyTask("WriteTask1-"+i,sp1,sp2));
        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceWithNotifyTask("WriteTask2-"+i,sp2,sp1));

        Instant stamp = Instant.now();
        while (es.getActiveCount() > 0) {
          if (Duration.between(stamp, Instant.now()).toMillis() < 10000){
            ISOUtil.sleep(100);
            continue;
          }
          es.shutdownNow();
          fail("Probably death-lock detected");
          return;
        } 

        printAvg(t1, "Avg. write: ");

//        es.shutdown();
        es.shutdownNow();
        if (!es.awaitTermination(5, TimeUnit.SECONDS))
            fail("Failed to shutdown ThreadPoolExecutor.");
    }

    @Disabled("Remove it when TSpace can pass it")
    @Test
    public void testStolenEntryAtNotify() throws InterruptedException {
        int size = 10;
        final ThreadPoolExecutor es = new ThreadPoolExecutor(size*2, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue<>());
        es.prestartAllCoreThreads();
        
        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceWithNotifyReadTask("WriteTask-"+i));

        //Threads which may stole entries
        for (int i=0; i<size; i++)
          es.execute(new ReadSpaceTask("WriteTask-"+i));

        assertNull(sp2.in("lost-entry", 200), "Detected stolen entry at notify");

        es.shutdownNow();
        if (!es.awaitTermination(5, TimeUnit.SECONDS))
            fail("Failed to shutdown ThreadPoolExecutor.");
    }

}
