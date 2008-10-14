package org.jpos.q2.qbean;

/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;

public class QBeanFactory extends QBeanSupport implements QBeanFactoryMBean {
  
    private static Map beanMap = new WeakHashMap();
    private Map privateList; // list of beans in the config file

    public void initService() throws Exception {
        Element e = getPersist();
        privateList = new HashMap();
        List beans = e.getChildren("bean");
        for (Iterator i = beans.iterator(); i.hasNext();) {
            Element bean = (Element) i.next();
            String id = bean.getAttributeValue("id");
            privateList.put(id, bean);
        }

    }

    public void start() {
        super.start();
        NameRegistrar.register("QBeanFactory." + getName(), this);
    }

    public void stop() {
        super.stop();
        NameRegistrar.unregister("QBeanFactory." + getName());
    }
    // Support methods for bean life cycle
    public void startService() {
        Iterator keys = privateList.keySet().iterator();
        while (keys.hasNext()) {
            Element bean = (Element) privateList.get(keys.next());
            String id = bean.getAttributeValue("id");
            String lazy = bean.getAttributeValue("lazy");
            // Load the bean only when lazy="false". default value true
            if (lazy != null && lazy.equalsIgnoreCase("false")) {
                getBean(id);
            }
        }
    }

    public void stopService() {
        Iterator keys = privateList.keySet().iterator();
        while (keys.hasNext()) {
            Element bean = (Element) privateList.get(keys.next());
            String id = bean.getAttributeValue("id");// id and key are same
            beanMap.remove(id);
        }
    }

    public Object getBean(String id) {

        // Hack to support a global ref bean across multiple
        // deployment files. We may need to support lazy loading here but now
        // not supported
        Element bean = (Element) privateList.get(id);
        if (bean == null) {
            return beanMap.get(id);
        }
        // Need to create a new instance when singleton="false"
        // It is true by default
        String singleton = bean.getAttributeValue("singleton");
       try{ 
        if(singleton != null && singleton.equals("false")){// create new instance every time
            Object beanInstance=newBean(bean,false);
            return beanInstance;
        }else{
            Object beanInstance=newBean(bean,true);
            return beanInstance; 
        }
        }catch(Exception e){
            log.fatal(e);
            return null;
        }
       
    }
    private Object newBean(Element bean,boolean useCache) throws ConfigurationException{
        String id = bean.getAttributeValue("id");
        
        if(useCache && beanMap.containsKey(id)){
            return beanMap.get(id);
        }
        
        String className = bean.getAttributeValue("class");
        Object beanInstance = getFactory().newInstance(className);
        List propertyList = bean.getChildren("property");
        for (Iterator props = propertyList.iterator(); props.hasNext();) {
            Element propertyElement = (Element) props.next();
            String pName = propertyElement.getAttributeValue("name");
            String pValue = propertyElement.getAttributeValue("value");
            String pRef =  propertyElement.getAttributeValue("ref");
            String methodName = "set"
                    + Character.toUpperCase(pName.charAt(0))
                    + pName.substring(1);
            if (pValue == null) {
                getFactory()
                        .invoke(beanInstance, methodName, getBean(pRef));
            } else {
                getFactory().invoke(beanInstance, methodName, pValue);
            }
        }
        if(useCache)//indication for a singleton 
          beanMap.put(id, beanInstance);   
        // Return the bean instance
        return beanInstance;
    }

}
