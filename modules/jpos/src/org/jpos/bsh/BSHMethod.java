/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.bsh;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;

import bsh.EvalError;
import bsh.Interpreter;

/** This is a utility class that makes it a bit easier to work with beanshell 
 *  scripts.
 *  Check out the execute methods.
 *
 * @author  AMarques
 */
public class BSHMethod {
    
    private String bshData;
    private boolean source;
    
    
    /** Creates a BSHMethod from a JDom Element.
     *
     *  The element is not requiered to have a specific name. 
     *  
     *  If the given element defines an attribute named 'file' the file 
     *  specified as its value will be sourced by the created BSHMethod.
     *
     *  If the 'file' attribute is specified, a 'cache'
     *  attribute may be specified as well which can take the values true|false
     *  and indicates wether to load the script to memory or to read from the 
     *  file for every script evaluation.
     *
     *  If the 'file' attibute is not specified then the text contained by the 
     *  element is set to be evaluated by the new BSHMethod. 
     *  <pre>
     *  Example 1 : 
     *          &lt;prepare>
     *                  import org.jpos.iso.*;
     *  		import org.jpos.transaction.*;
     *
     *			msg = context.get("txnRequest");
     *			BaseChannel.getChannel("loop-channel").send(msg);
     *			result=TransactionConstants.PREPARED | TransactionConstants.READONLY;
     *		&lt;/prepare>
     *
     *  Example 2 :
     *          &lt;routing file='cfg\files\routing1.bsh' cache='false'/>
     *  </pre>
     */ 
    public static BSHMethod createBshMethod(Element e) throws FileNotFoundException, IOException {
        if (e == null) {
            return null;
        }
        String file = e.getAttributeValue("file");
        String bsh;
        if (file != null) {
            boolean cache = false;
            String cacheAtt = e.getAttributeValue("cache");
            if (cacheAtt != null) {
                cache = cacheAtt.equalsIgnoreCase("true"); 
            }
            if (!cache) {
                return new BSHMethod(file, true);
            } else {
                bsh = "";
                FileReader f = new FileReader(file);
                int c;
                while ( (c = f.read()) != -1) {
                    bsh += (char) c; 
                }
                f.close();
                return new BSHMethod(bsh, false);
            }
        } else {
            bsh = e.getTextTrim();
            if (bsh == null || bsh.equals("")) {
                return null;
            }
            return new BSHMethod(bsh, false);
        }
    }
        
    /** Creates a BSHMethod.
     *  @param bshData - May either be the file to source or the script itself to
     *                  evaluate.
     *  @param source - If true indicates that the bshData passed is a file to 
     *                  source. Otherwise the string itself is evaluated.
     */
    public BSHMethod(String bshData, boolean source) {
        this.bshData = bshData;
        this.source = source; 
    }

    /** Sets the given arguments to the Interpreter, evaluates the script and 
     *  returns the object stored on the variable named resultName.
     *
     *  @param arguments    Parameters to set to the Interpreter. For every 
     *                      Map.Entry (key, value), interpreter.set(key, value)
     *                      is called. All keys must be Strings.
     *  @param returnName   The names of the variables wich`s content is to be 
     *                      returned.
     */
    public Object execute(Map arguments, String resultName) throws EvalError, FileNotFoundException, IOException {
        Interpreter i = initInterpreter(arguments);
        return i.get(resultName);
    }
    
    /** Sets the given arguments to the Interpreter, evaluates the script and 
     *  returns a map that has the Strings of the returnNames collection as keys
     *  and the objects stored in the variables thus named as values.
     *  
     *  @param arguments    Parameters to set to the Interpreter. For every 
     *                      Map.Entry (key, value), interpreter.set(key, value)
     *                      is called. All keys must be Strings.
     *  @param returnNames  Collection of Strings. The names of the variables 
     *                      wich`s contents are to be returned.
     */
    public Map execute(Map arguments, Collection returnNames) throws EvalError, FileNotFoundException, IOException {
        Interpreter i = initInterpreter(arguments);
        Map result = new HashMap();
        String rName;
        for (Iterator it = returnNames.iterator(); it.hasNext(); ) {
            rName = (String) it.next();
            result.put(rName, i.get(rName));
        }
        return result;
    }
    
    protected Interpreter initInterpreter(Map arguments) throws EvalError, FileNotFoundException, IOException {
        Interpreter i = new Interpreter();
        Map.Entry entry;
        for (Iterator it = arguments.entrySet().iterator(); it.hasNext(); ) {
            entry = (Map.Entry) it.next();
            i.set((String) entry.getKey(), entry.getValue());
        }
        if (source) {
            i.source(bshData);
        } else {
            i.eval(bshData);
        }
        return i;
    }

    public String toString() {
        return bshData;
    }
}
