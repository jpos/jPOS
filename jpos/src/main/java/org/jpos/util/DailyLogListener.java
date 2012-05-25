/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.util;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Rotates log daily and compress the previous log.
 * @author <a href="mailto:alcarraz@cs.com.uy">Andr&eacute;s Alcarraz</a>
 * @since jPOS 1.5.1
 */
public class DailyLogListener extends RotateLogListener{
    private static final String DEF_SUFFIX = ".log";
    private static final int DEF_WIN = 24*3600;
    private static final long DEF_MAXSIZE = -1;
    private static final String DEF_DATE_FMT = "-yyyy-MM-dd";
    private static final int NONE = 0;
    private static final int GZIP = 1;
    private static final int ZIP = 2;
    private static final int DEF_COMPRESSION = NONE;
    private static final int DEF_BUFFER_SIZE = 128*1024;//128 KB
    private static final String[] DEF_COMPRESSED_SUFFIX= {"",".gz",".zip"};
    private static final Map<String,Integer> COMPRESSION_FORMATS = new HashMap<String,Integer>(3);
    static {
        COMPRESSION_FORMATS.put("none", NONE);
        COMPRESSION_FORMATS.put("gzip", GZIP);
        COMPRESSION_FORMATS.put("zip", ZIP);
    }
    
    /** Creates a new instance of DailyLogListener */
    public DailyLogListener() {
        setLastDate(getDateFmt().format(new Date()));
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        String suffix = cfg.get("suffix", DEF_SUFFIX), prefix = cfg.get("prefix");
        setSuffix(suffix);
        setPrefix(prefix);
        Integer formatObj =
                COMPRESSION_FORMATS
                .get(cfg.get("compression-format","none").toLowerCase());
        int compressionFormat = (formatObj == null) ? 0 : formatObj;
        setCompressionFormat(compressionFormat);
        setCompressedSuffix(cfg.get("compressed-suffix", 
                DEF_COMPRESSED_SUFFIX[compressionFormat]));
        setCompressionBufferSize(cfg.getInt("compression-buffer-size", 
                DEF_BUFFER_SIZE));
        logName = prefix + suffix;
        maxSize = cfg.getLong("maxsize",DEF_MAXSIZE);
        try {
            openLogFile();
        } catch (IOException e) {
            throw new ConfigurationException ("error opening file: " + logName, 
                    e);
        }
        sleepTime = cfg.getInt("window", DEF_WIN);
        if (sleepTime <= 0)
            sleepTime = DEF_WIN;
        sleepTime*=1000;
        DateFormat fmt = new SimpleDateFormat(cfg.get("date-format",DEF_DATE_FMT));
        
        setDateFmt(fmt);
        setLastDate(fmt.format(new Date()));
        Date time;
        try {
            time = new SimpleDateFormat("HH:mm:ss").parse(cfg.get("first-rotate-time", "00:00:00"));
        } catch (ParseException ex) {
            throw new ConfigurationException("Bad 'first-rotate-time' format " +
                    "expected HH(0-23):mm:ss ",ex);
        }
        String strDate = cfg.get("first-rotate-date",null);
        //calculate the first execution time
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND,0);
        Calendar calTemp = Calendar.getInstance();
        calTemp.setTime(time);
        cal.set(Calendar.SECOND,calTemp.get(Calendar.SECOND));
        cal.set(Calendar.MINUTE,calTemp.get(Calendar.MINUTE));
        cal.set(Calendar.HOUR_OF_DAY,calTemp.get(Calendar.HOUR_OF_DAY));
        
        if (strDate != null) {
            Date date;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
            } catch (ParseException ex) {
                throw new ConfigurationException("Bad 'first-rotate-date' " +
                        "format, expected (yyyy-MM-dd)", ex);
            }
            calTemp.setTime(date);
            cal.set(calTemp.get(Calendar.YEAR), calTemp.get(Calendar.MONTH), 
                    calTemp.get(Calendar.DATE));
        }
        //here cal contains the first execution, let/s calculate the next one
        calTemp.setTime(new Date());
        //if first executiontime already happened
        if (cal.before(calTemp)){
            //how many windows between cal and now
            long n = (calTemp.getTimeInMillis() - cal.getTimeInMillis()) / 
                    sleepTime;
            cal.setTimeInMillis(cal.getTimeInMillis() + sleepTime*(n+1));
        }
        DefaultTimer.getTimer().scheduleAtFixedRate(
                rotate=new DailyRotate(), cal.getTime(), sleepTime);
    }

    public synchronized  void logRotate() throws IOException {
        closeLogFile ();
        super.close ();
        setPrintStream (null);
        String suffix = getSuffix() + getCompressedSuffix();
        String newName = getPrefix()+getLastDate();
        int i=0;
        File dest = new File (newName+suffix), source = new File(logName);
        while (dest.exists()) 
            dest  = new File (newName + "." + ++i + suffix);
        source.renameTo(dest);
        setLastDate(getDateFmt().format(new Date()));
        openLogFile();
        compress(dest);
    }
    /**
     * Holds value of property suffix.
     */
    private String suffix = DEF_SUFFIX;

    /**
     * Getter for property suffix.
     * @return Value of property suffix.
     */
    public String getSuffix() {
        return this.suffix;
    }

    /**
     * Setter for property suffix.
     * @param suffix New value of property suffix.
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Holds value of property prefix.
     */
    private String prefix;

    /**
     * Getter for property prefix.
     * @return Value of property prefix.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Setter for property prefix.
     * @param prefix New value of property prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Holds value of property rotateCount.
     */
    private int rotateCount;

    /**
     * Getter for property rotateCount.
     * @return Value of property rotateCount.
     */
    public int getRotateCount() {
        return this.rotateCount;
    }

    /**
     * Setter for property rotateCount.
     * @param rotateCount New value of property rotateCount.
     */
    public void setRotateCount(int rotateCount) {
        this.rotateCount = rotateCount;
    }

    /**
     * Holds value of property dateFmt.
     */
    private DateFormat dateFmt = new SimpleDateFormat(DEF_DATE_FMT);

    /**
     * Getter for property dateFmt.
     * @return Value of property dateFmt.
     */
    public DateFormat getDateFmt() {
        return this.dateFmt;
    }

    /**
     * Setter for property dateFmt.
     * @param dateFmt New value of property dateFmt.
     */
    public void setDateFmt(DateFormat dateFmt) {
        this.dateFmt = dateFmt;
    }

    /**
     * Holds value of property lastDate.
     */
    private String lastDate ;

    /**
     * Getter for property lastDate.
     * @return Value of property lastDate.
     */
    public String getLastDate() {
        return this.lastDate;
    }

    /**
     * Setter for property lastDate.
     * @param lastDate New value of property lastDate.
     */
    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    /**
     * Holds value of property compressedSuffix.
     */
    private String compressedSuffix = DEF_COMPRESSED_SUFFIX[DEF_COMPRESSION];

    /**
     * Getter for property compressedExt.
     * @return Value of property compressedExt.
     */
    public String getCompressedSuffix() {
        return this.compressedSuffix;
    }

    /**
     * Setter for property compressedExt.
     * @param compressedSuffix New value of property compressedExt.
     */
    public void setCompressedSuffix(String compressedSuffix) {
        this.compressedSuffix = compressedSuffix;
    }

    /**
     * Hook method that creates a thread to compress the file f.
     * @param f the file name
     * @return a thread to compress the file and null if it is not necesary
     */
    protected Thread getCompressorThread(File f){
        return new Thread(new Compressor(f),"DailyLogListener-Compressor");
    }
    
    /**
     *  Hook method that creates an output stream that will compress the data.
     * @param f the file name
     * @return ZIP/GZip OutputStream
     * @throws java.io.IOException on error
     */
    protected OutputStream getCompressedOutputStream(File f) throws IOException{
        OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        if (getCompressionFormat() == ZIP) {
            ZipOutputStream ret = new ZipOutputStream(os);
            ret.putNextEntry(new ZipEntry(logName));
            return ret;
        } else { 
            return new GZIPOutputStream(os);
        }
    }
    protected void closeCompressedOutputStream(OutputStream os) throws IOException{
        if (os instanceof DeflaterOutputStream)
            ((DeflaterOutputStream)os).finish();
        os.close();
    }
    
    protected void logDebugEx(String msg, Throwable e){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.println(msg);
        e.printStackTrace(ps);
        ps.close();
        logDebug(os.toString());
        
    }
    
    protected class Compressor implements Runnable{
        
        
        File f;
        public Compressor(File f) {
            this.f = f;
        }

        public void run() {
            OutputStream os = null;
            InputStream is = null;
            File tmp = null;
            try {
                tmp = File.createTempFile(f.getName(), ".tmp", f.getParentFile());
                os = getCompressedOutputStream(tmp);
                is = new BufferedInputStream(new FileInputStream(f));
                byte[] buff = new byte[getCompressionBufferSize()];
                int read;
                do {
                    read = is.read(buff);
                    if ( read > 0 )
                        os.write(buff,0,read);
                } while (read > 0);
                
            } catch (Throwable ex) {
                logDebugEx("error compressing file " + f, ex);
            } finally {
                try {
                    if (is!=null)
                        is.close();
                    if (os != null)
                        closeCompressedOutputStream(os);
                    if (f != null){
                        f.delete();
                        if (tmp!=null)
                            tmp.renameTo(f);
                    }
                } catch (Throwable ex) {
                    logDebugEx("error closing files", ex);
                }
                
            }
            
        }
        
    }

    /**
     * Holds value of property compressionFormat.
     */
    private int compressionFormat = DEF_COMPRESSION;

    /**
     * Getter for property compressionFormat.
     * @return Value of property compressionFormat.
     */
    public int getCompressionFormat() {
        return this.compressionFormat;
    }

    /**
     * Setter for property compressionFormat.
     * @param compressionFormat New value of property compressionFormat.
     */
    public void setCompressionFormat(int compressionFormat) {
        this.compressionFormat = compressionFormat;
    }

    /**
     * Holds value of property compressionBufferSize.
     */
    private int compressionBufferSize = DEF_BUFFER_SIZE;

    /**
     * Getter for property compressionBufferSize.
     * @return Value of property compressionBufferSize.
     */
    public int getCompressionBufferSize() {
        return this.compressionBufferSize;
    }

    /**
     * Setter for property compressionBufferSize.
     * @param compressionBufferSize New value of property compressionBufferSize.
     */
    public void setCompressionBufferSize(int compressionBufferSize) {
        this.compressionBufferSize = (compressionBufferSize >= 0) ? 
            compressionBufferSize : DEF_BUFFER_SIZE;
    }

    protected void checkSize() {
        if (maxSize>0 )
            super.checkSize();
    }

    /** 
     * Hook method to optionally compress the file
     * @param logFile the file name
     */
    protected void compress(File logFile) {
        if (getCompressionFormat() != NONE){
            Thread t = getCompressorThread(logFile);
            if (t != null)
                t.start();
        }
    }

    final class DailyRotate extends Rotate {
        public void run() {
            super.run();
            setLastDate(getDateFmt().format(new Date(scheduledExecutionTime())));
        }
        
    }
}
