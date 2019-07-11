/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import org.jpos.iso.ISOUtil;

import org.jpos.util.TPS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Robert Demski
 */
@SuppressWarnings("unchecked")
public class TSpacePerformanceTest  {

    TSpace<String,Object> sp1;
    TSpace<String,Object> sp2;
    List<Long> t1 = new ArrayList();
    List<Long> t2 = new ArrayList();
//    List t1 = Collections.synchronizedCollection(new ArrayList());
    public static final int COUNT = 100000;
    TPS tpsOut = new TPS(100L, false);
    TPS tpsIn = new TPS(100L, false);

    class WriteSpaceTask implements Runnable {
        String key;
        
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
          System.err.println("Write "+key+" out: "+(stamp2-stamp)/1000000 + " " + tpsOut.toString());
        }
    }

    class ReadSpaceTask implements Runnable {
        String key;
        
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
          System.err.println("Read  "+key+"  in: "+(stamp2-stamp)/1000000 + " " + tpsIn.toString());
        }  
    }

    @SuppressWarnings("unchecked")
    class WriteSpaceWithNotifyTask implements Runnable, SpaceListener<String,Object> {
        String key;
        TSpace sp1;
        TSpace sp2;
        int count = 0;
        
        WriteSpaceWithNotifyTask(String key, TSpace sp1, TSpace sp2){
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
        String key;
       
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
        sp1 = new TSpace<String,Object>();
        sp2 = new TSpace<String,Object>();
        t1.clear();
        t2.clear();
    }

    @AfterEach
    public void tearDown() {
        Set keySet1 = new HashSet(sp1.getKeySet());
        Set keySet2 = new HashSet(sp2.getKeySet());
        for (Object key : keySet1) {
            sp1.inp(key);
        }
        for (Object key : keySet2) {
            sp2.inp(key);
        }
        sp1.gc();
        sp2.gc();
        sp1 = null;
        sp2 = null;
        System.gc();
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

    private boolean isFutureListDone(List<Future<Object>> futures){
        for (Future<?> fut : futures){
            if (!fut.isDone()){
                return false;
            }
        }
        return true;
    }

    @Test
    public void testReadPerformance() throws Throwable {
        int size = 10;
        ExecutorService es = new ThreadPoolExecutor(size, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue());
        ((ThreadPoolExecutor)es).prestartAllCoreThreads();

        List<Callable<Object>> writers = new ArrayList<>();
        for (int i=0; i<size; i++)
          writers.add(Executors.callable(new WriteSpaceTask("PerformTask-"+i)));
        List<Future<Object>> writerfuters = es.invokeAll(writers);
        for (Future<?> writefut : writerfuters)
          writefut.get();
        printAvg(t1, "Avg. write: ");

        List<Callable<Object>> readers = new ArrayList<>();
        for (int i=0; i<size; i++)
          readers.add(Executors.callable(new ReadSpaceTask("PerformTask-"+i)));
        List<Future<Object>> readerfuters = es.invokeAll(readers);
        for (Future<?> readerfut : readerfuters)
          readerfut.get();
        es.shutdown();
        printAvg(t2, "Avg. read : ");
    }

    @Test
    public void testDeadLockWithNotify() throws Throwable {
        int size = 10;
        final ExecutorService es = new ThreadPoolExecutor(size*2, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue());
        ((ThreadPoolExecutor)es).prestartAllCoreThreads();

        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i=0; i<size; i++)
          tasks.add(Executors.callable(new WriteSpaceWithNotifyTask("WriteTask1-"+i,sp1,sp2)));
        for (int i=0; i<size; i++)
          tasks.add(Executors.callable(new WriteSpaceWithNotifyTask("WriteTask2-"+i,sp2,sp1)));

        List<Future<Object>> tasksfut = es.invokeAll(tasks, 10, TimeUnit.SECONDS);
        long stamp = System.currentTimeMillis();
        while (!isFutureListDone(tasksfut)) {
          if (System.currentTimeMillis() - stamp < 10000){
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
        es.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Disabled("Remove it when TSpace can pass it")
    @Test
    public void testStolenEntryAtNotify() throws Throwable {
        int size = 10;
        final ExecutorService es = new ThreadPoolExecutor(size*2, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue());
        ((ThreadPoolExecutor)es).prestartAllCoreThreads();
        
        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceWithNotifyReadTask("WriteTask-"+i));

        //Threads which may stole entries
        for (int i=0; i<size; i++)
          es.execute(new ReadSpaceTask("WriteTask-"+i));

        assertNull(sp2.in("lost-entry", 200), "Detected stolen entry at notify");

        es.shutdownNow();
//        es.awaitTermination(5, TimeUnit.SECONDS);
    }

}
