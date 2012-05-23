package org.jpos.space;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jpos.iso.ISOUtil;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 *
 * @author Robert Demski
 */
public class TSpacePerformanceTest  {

    LocalSpace<String,Object> sp1;
    LocalSpace<String,Object> sp2;
    List<Long> t1 = new ArrayList();
    List<Long> t2 = new ArrayList();
//    List t1 = Collections.synchronizedCollection(new ArrayList());
    public static final int COUNT = 100000;

    class WriteSpaceTask implements Runnable {
        String key;
        
        WriteSpaceTask(String key){
           this.key = key;
        }
        public void run (){
          long stamp = System.nanoTime();
          for (int i=0; i<COUNT; i++)
             sp1.out(key, Boolean.TRUE);
          long stamp2 = System.nanoTime();
          t1.add(stamp2-stamp);
          System.err.println("Write "+key+" out: "+(stamp2-stamp)/1000000);
        }

    }

    class ReadSpaceTask implements Runnable {
        String key;
        
        ReadSpaceTask(String key){
           this.key = key;
        }
        public void run (){
          long stamp = System.nanoTime();
          for (int i=0; i<COUNT; i++)
             sp1.in(key);
          long stamp2 = System.nanoTime();
          t2.add(stamp2-stamp);
          System.err.println("Read  "+key+" out: "+(stamp2-stamp)/1000000);
        }  
    }

    class WriteSpaceWithNotifyTask implements Runnable, SpaceListener {
        String key;
        LocalSpace sp1;
        LocalSpace sp2;
        int count = 0;
        
        WriteSpaceWithNotifyTask(String key, LocalSpace sp1, LocalSpace sp2){
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

        public void notify(Object key, Object value) {
          if ( (++count % 100) == 0) {
            sp2.out((String)key, value);
          }
        }
    }

    class WriteSpaceWithNotifyReadTask implements Runnable, SpaceListener {
        String key;
       
        WriteSpaceWithNotifyReadTask(String key){
          this.key = key;
        }
        public void run (){
          sp1.addListener(key, this);
          for (int i=0; i<COUNT; i++)
             sp1.out(key, Boolean.TRUE);
        }

        public void notify(Object key, Object value) {
          if ( sp1.rdp(key) == null)
            sp2.out("lost-entry", value);
        }
    }

    @Before
    public void setUp () {
        sp1 = new TSpace<String,Object>();
        sp2 = new TSpace<String,Object>();
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
    public void testReadPerformance() throws Throwable {
        int size = 10;
        ExecutorService es = new ThreadPoolExecutor(size, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue());
        ((ThreadPoolExecutor)es).prestartAllCoreThreads();

        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceTask("PerformTask-"+i));
        ISOUtil.sleep(500);
        printAvg(t1, "Avg. write: ");

        for (int i=0; i<size; i++)
          es.execute(new ReadSpaceTask("PerformTask-"+i));
        ISOUtil.sleep(500);
        printAvg(t2, "Avg. read : ");

        es.shutdown();
    }

    @Test
    public void testDeadLockWithNotify() throws Throwable {
        int size = 10;
        final ExecutorService es = new ThreadPoolExecutor(size*2, Integer.MAX_VALUE,
                              30, TimeUnit.SECONDS, new SynchronousQueue());
        ((ThreadPoolExecutor)es).prestartAllCoreThreads();
        
        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceWithNotifyTask("WriteTask1-"+i,sp1,sp2));
        for (int i=0; i<size; i++)
          es.execute(new WriteSpaceWithNotifyTask("WriteTask2-"+i,sp2,sp1));

        long stamp = System.currentTimeMillis();
        while (((ThreadPoolExecutor)es).getActiveCount() > 0) {
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

    @Ignore("Remove it when TSpace can pass it")
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

        assertNull("Detected stolen entry at notify",sp2.in("lost-entry", 200));

        es.shutdownNow();
//        es.awaitTermination(5, TimeUnit.SECONDS);
    }

}
