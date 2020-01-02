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

package org.jpos.bsh;

import bsh.Interpreter;
import bsh.NameSpace;
import java.io.BufferedReader;
import org.jpos.core.Configuration;
import org.jpos.util.LogEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** This is a log listener that reads its actions from Bean Shell scripts.
 * You can define many scripts, and the order in wich they are called, also you
 * can define scripts whose name depends on the event being processed, and the
 * realm of the object that generated it.
 * This way you can set a configuration like this:
 * <log-listener class="org.jpos.bsh.BSHLoglistener">
 *    <property name="source" value="general.bsh"/>
 *    <property name="source" value="tag_$tag.bsh"/> <!--this is to handle
 * specific tags-->
 *    <property name="source" value="realm_$realm.bsh"/> <!-- to handle specific
 * realms-->
 *    <property name="source" value="tag_$tag_realm_$realm.bsh"/> <!-- to handle
 * specific tags from specific realms-->
 * </log-listener>
 * <!-- the following lines are for html doc-->
 * <br>
 * <pre>
 * <font color="#008080">&lt;</font><font color="#008080">log-listener</font><font color="#008080"> </font><font color="#2e8b57"><b>class</b></font>=<font color="#ff00ff">&quot;org.jpos.bsh.BSHLoglistener&quot;</font><font color="#008080">&gt;</font>
 *       <font color="#008080">&lt;</font><font color="#008080">property</font><font color="#008080"> </font><font color="#2e8b57"><b>name</b></font>=<font color="#ff00ff">&quot;source&quot;</font><font color="#008080"> </font><font color="#2e8b57"><b>value</b></font>=<font color="#ff00ff">&quot;general.bsh&quot;</font><font color="#008080">/&gt;</font>
 *       <font color="#008080">&lt;</font><font color="#008080">property</font><font color="#008080"> </font><font color="#2e8b57"><b>name</b></font>=<font color="#ff00ff">&quot;source&quot;</font><font color="#008080"> </font><font color="#2e8b57"><b>value</b></font>=<font color="#ff00ff">&quot;tag_$tag.bsh&quot;</font><font color="#008080">/&gt;</font> <font color="#0000ff">&lt;!</font><font color="#0000ff">--this is to handle specific tags--</font><font color="#0000ff">&gt;</font>
 *       <font color="#008080">&lt;</font><font color="#008080">property</font><font color="#008080"> </font><font color="#2e8b57"><b>name</b></font>=<font color="#ff00ff">&quot;source&quot;</font><font color="#008080"> </font><font color="#2e8b57"><b>value</b></font>=<font color="#ff00ff">&quot;realm_$realm.bsh&quot;</font><font color="#008080">/&gt;</font> <font color="#0000ff">&lt;!</font><font color="#0000ff">-- to handle specific realms--</font><font color="#0000ff">&gt;</font>
 *       <font color="#008080">&lt;</font><font color="#008080">property</font><font color="#008080"> </font><font color="#2e8b57"><b>name</b></font>=<font color="#ff00ff">&quot;source&quot;</font><font color="#008080"> </font><font color="#2e8b57"><b>value</b></font>=<font color="#ff00ff">&quot;tag_$tag_realm_$realm.bsh&quot;</font><font color="#008080">/&gt;</font> <font color="#0000ff">&lt;!</font><font color="#0000ff">-- to handle specific tags from specific realms--</font><font color="#0000ff">&gt;</font>
 * <font color="#008080">&lt;/log-listener&gt;</font>
 * </pre>
 * <!-- end of lines for html doc-->
 * If a source with the given name is not found, or it canbe read, it is not processed, so this lets
 * you change what is processed in real time, if you put a file called
 * tag_SystemMonitor_realm_monitor.bsh it will be executed whenever the system
 * monitor is run.<BR>
 * If you want to filter an event so that the remaining log listeners don't see
 * it, you have to set event = null in your script.<br>
 * <table border=1 color="black">
 * <caption>Other Configuration Options: </caption>
 * <th><td> Name                </td><td>Type       </td><td>Description</td></th>
 * <tr><td> filter-by-default   </td><td>boolean    </td><td>
 *  If true, filter the events if no script is processed, this way you can put a
 * Log Listener that filters every thing unless you put some script file, even if
 * it is empty, you also can dynamically change what kind of message are filtered
 * by placing empty files with names like tag_SystemMonitor_realm_monitor.bsh in
 * the previous example, if you "touch" a file with this name these events will
 * begin to be processed.</TD></TR>
 * <TR><TD> preload-scripts     </TD><TD>boolean    </TD><TD>If true scripts a
 * loaded once, and kept in memory, being realoaded only if they are touched. This
 * is good when you have lots of RAM memory but ou have troubles with
 * speed</TD></TR>
 * <TR><TD> save-name-space     </TD><TD>boolean    </TD><TD>If true the namespace
 * of the script instance will be saved so that in the next event you can access
 * them from the script, by default it's off, this property is overriden if the
 * script exposes a boolean variable named saveNameSpace</TD></TR>
 * <TR><TD> reload              </TD><TD>long       </TD><TD>this property is used
 * if the preload-script property is true, is the time in milliseconds between
 * updates in the script, during this time BSHLogListener will not check if the
 * script source was modified or deleted on disk </TD></TR>
 * </TABLE>
 */
public class BSHLogListener implements org.jpos.util.LogListener, org.jpos.core.Configurable {
    /**Holds the configuration for this object*/
    protected Configuration cfg;
    protected static final String[] patterns = {"tag", "realm"};
    protected Map<String, ScriptInfo> scripts = new HashMap<>();
    /** Creates a new instance of BSHLogListener */
    public BSHLogListener() {
    }

    @Override
    public void setConfiguration(org.jpos.core.Configuration cfg) {
        this.cfg = cfg;
    }
    protected static String[] replace(String[] src, String[] patterns, String[] to){
        String[] ret = new String[src.length];
        for(int i=0; i<src.length; i++){
            StringBuilder buff = new StringBuilder(2*src[i].length());
            int begin, end=0;
            //begin is the position of the next pattern, end is the end of the last pattern
            while ((begin = src[i].indexOf('$',end))>=0 && begin<src[i].length()){
                buff.append(src[i].substring(end, begin));
                boolean patternFound = false;
                for(int k=0; k<patterns.length && !patternFound ; k++){
                    if(patternFound = src[i].indexOf(patterns[k], begin) == begin+1){
                        buff.append(to[k]);
                        end = begin + patterns[k].length() + 1;
                    }
                }
                if(!patternFound) end = begin + 1;
            }
            buff.append(src[i].substring(end));
            //if(buff.length()==0) ret[i] = src[i];
            ret[i] = buff.toString();
        }
        return ret;
    }

    @Override
    public LogEvent log(LogEvent ev) {
        LogEvent ret = ev;
        boolean processed = false;
        try{
            String[] sources = replace(cfg.getAll("source"), patterns, new String[] {ev.getTag(), ev.getRealm()});
            for(int i=0; i<sources.length && ret != null; i++){
                try{
                    Interpreter bsh = new Interpreter();
                    BSHLogListener.ScriptInfo info = getScriptInfo(sources[i]);
                    NameSpace ns = info!=null ?info.getNameSpace():null;
                    if(ns!=null) bsh.setNameSpace(ns);
                    bsh.set("event", ret);
                    bsh.set("cfg", cfg);
                    File f = new File(sources[i]);
                    if(!cfg.getBoolean("preload-scripts")){
                        if(f.exists() && f.canRead() && f.isFile()){
                            //if(f.lastModified())
                            processed = true;
                            bsh.eval(new java.io.FileReader(f));
                        }
                    }else{
                        if(info == null) scripts.put(sources[i], info=new ScriptInfo());
                        if(System.currentTimeMillis() > info.getLastCheck() + cfg.getLong("reload")){
                            info.setLastCheck(System.currentTimeMillis());
                            if(f.exists() && f.canRead() && f.isFile()){
                                if(info.getLastModified() != f.lastModified()) {
                                    info.setLastModified(f.lastModified());
                                    info.setCode(loadCode(f));
                                }
                            }else{
                                info.setCode(null);
                            }
                        }
                        if(info.getCode() != null){
                            processed = true;
                            bsh.eval(new StringReader(info.getCode()));
                        }else scripts.remove(sources[i]);
                    }
                    ret = (LogEvent)bsh.get("event");
                    Object saveNS = bsh.get("saveNameSpace");
                    boolean saveNameSpace = 
                        saveNS instanceof Boolean ? (Boolean) saveNS :cfg.getBoolean("save-name-space");
                    if(saveNameSpace) {
                        if(info!=null) info.setNameSpace(bsh.getNameSpace());
                        else scripts.put(sources[i], new ScriptInfo(bsh.getNameSpace()));
                    }else if (info!=null) info.setNameSpace(null);
                }catch(Exception e){
                    ret.addMessage(e);
                }
            }
            return !processed && cfg.getBoolean("filter-by-default") ? null : ret;
        }catch(Exception e){
            if (ret != null) {
                ret.addMessage(e);
            }
            return ret;
        }
    }
    protected String loadCode(File f) throws IOException{
        StringBuilder buf = new StringBuilder((int)f.length());
        char[] content = new char[(int)f.length()];
        int l;
        Reader r = new BufferedReader(new FileReader(f));
        try {
            while((l=r.read(content))!=-1) buf.append(content,0,l);
        } finally {
            r.close();
        }
        return buf.toString();
    }

    protected ScriptInfo getScriptInfo(String filename){
        Objects.requireNonNull(filename, "The script file name cannot be null");
        return scripts.get(filename);
    }

    protected void addScriptInfo(String filename, String code, long lastModified){
        Objects.requireNonNull(filename, "The script file name cannot be null");
        scripts.put(filename, new ScriptInfo(code, lastModified));
    }
    protected static class ScriptInfo{
        String code;
        long lastModified;
        long lastCheck;
        NameSpace nameSpace;
        
        public ScriptInfo(){
        }
        public ScriptInfo(NameSpace ns){
            nameSpace = ns;
        }
        
        public ScriptInfo(String code, long lastModified){
            setCode(code);
            setLastModified(lastModified);
        }
        
        /** Getter for property code.
         * @return Value of property code.
         *
         */
        public java.lang.String getCode() {
            return code;
        }
        
        /** Setter for property code.
         * @param code New value of property code.
         *
         */
        public void setCode(java.lang.String code) {
            this.code = code;
        }
        
        /** Getter for property lastModified.
         * @return Value of property lastModified.
         *
         */
        public long getLastModified() {
            return lastModified;
        }
        
        /** Setter for property lastModified.
         * @param lastModified New value of property lastModified.
         *
         */
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
        
        /** Getter for property lastCheck.
         * @return Value of property lastCheck.
         *
         */
        public long getLastCheck() {
            return lastCheck;
        }
        
        /** Setter for property lastCheck.
         * @param lastCheck New value of property lastCheck.
         *
         */
        public void setLastCheck(long lastCheck) {
            this.lastCheck = lastCheck;
        }
        
        /** Getter for property nameSpace.
         * @return Value of property nameSpace.
         *
         */
        public NameSpace getNameSpace() {
            return nameSpace;
        }
        
        /** Setter for property nameSpace.
         * @param nameSpace New value of property nameSpace.
         *
         */
        public void setNameSpace(NameSpace nameSpace) {
            this.nameSpace = nameSpace;
        }
        
    }
}
