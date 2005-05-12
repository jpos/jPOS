/*
 * This class was contributed by Andres Marques from Cabal Uruguay.
 * This class is very similar to our existing DailyTaskAdaptor,
 * the only difference in terms of functionality is that we don't
 * deal with periods other than 24hours in the DailyTaskAdaptor.
 *
 * We can either add that functionality to the existing 
 * DailyTaskAdaptor or replace it by this stuff. We need to evaluate
 * pros and cons.
 *
 * There might be a minor bug there, the 'task' is not getting
 * unregistered at stop time.
 *
 *  Sample Configuration:
 *  -------------------------------------------------------------

   <task-adaptor class="org.jpos.q2.iso.SchedulingTaskAdaptor" 
       logger="Q2" realm="xxx" name="cierreLoteMovicom">
    <class>uy.com.cabal.transactionality.movicom.MovicomCierreLote</class>
    
    <property name="delay" value="0"/>
    <!--property name="firstTime" value="01:00:00"/-->
    <property name="period" value="86400000"/>   
                                   
   </task-adaptor>

 *  -------------------------------------------------------------
 *
 */

package org.jpos.q2.iso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.jpos.core.ConfigurationException;
import org.jpos.util.NameRegistrar;

/**
 *
 * @author amarqueslin
 */
public class SchedulingTaskAdaptor extends TaskAdaptor {
    
    private Timer timer = new Timer();
    private Long delay;
    private Date firstTime;
    private long period;
    
    
    /** Creates a new instance of SchedulingTaskAdaptor */
    public SchedulingTaskAdaptor() {
    }
    
    public class RunnableWrapper extends TimerTask {
        
        private Runnable innerRunnable;
        
        public RunnableWrapper(Runnable innerRunnable) {
            this.innerRunnable = innerRunnable;
        }
        
        public void run() {
            innerRunnable.run();
        }
    }

    public void setConfiguration(org.jpos.core.Configuration cfg) throws org.jpos.core.ConfigurationException {

        super.setConfiguration(cfg);
        boolean daemon = cfg.getBoolean("daemon", true);
        if (daemon) {
            timer = new Timer(true);
        }
        period = cfg.getLong("period", 24 * 60 * 60 * 1000); //By default execute task every 24hs
        
        String initTimeInfo = "";
        
        long delayLong = cfg.getLong("delay", -1);
        if (delayLong != -1) {
            delay = new Long(delayLong);
            initTimeInfo = "delay = " + delay;
        } else {
            String strFirstTime = cfg.get("firstTime", "00:00:00"); //By default start at 12 AM
            Date now = new Date();
            if (strFirstTime.equals("now")) {
                firstTime = now;
            } else {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    String today = dateFormat.format(now);
                    Date time = dateTimeFormat.parse(today + " " + strFirstTime);
                    if (time.after(now)) {
                        firstTime = time;
                    } else {
                        firstTime = new Date(time.getTime() + 1000 * 60 * 60 * 24);
                    }
                } catch (ParseException ex) {
                    throw new ConfigurationException("The format in which to specify the first execution time is HH:mm:ss");
                }
                initTimeInfo = "firstTime = " + dateTimeFormat.format(firstTime);
            }
        }
        
        String strConfig = "daemon = " + daemon + "\n" +
                           initTimeInfo  + "\n"
                            + "period = " + period;
                            
        log.info("Configuration", strConfig);
    }
    
    protected void startService() throws Exception {
        getServer().getFactory().setConfiguration(task, getPersist());
        NameRegistrar.register (getName (), task);
        if (task instanceof TimerTask) {
            schedule((TimerTask) task);
            return;
        }
        if (task instanceof Runnable) {
            schedule(new RunnableWrapper((Runnable) task));
        }
    }
    
    protected void schedule(TimerTask task) {
        if (delay != null) {
            timer.schedule(task, delay.longValue(), period);
        } else {
            timer.schedule(task, firstTime, period);
        }
    }

    protected void stopService() throws Exception {

        super.stopService();
        timer.cancel();
    }
    
    
    
}
