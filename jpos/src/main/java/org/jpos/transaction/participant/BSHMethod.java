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

package org.jpos.transaction.participant;

import bsh.EvalError;
import bsh.Interpreter;
import org.jdom2.Element;
import org.jpos.q2.QFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** This is a utility class that makes it a bit easier to work with beanshell 
 *  scripts.
 *  Check out the execute methods.
 *
 * @author  AMarques
 */
@SuppressWarnings("unchecked")
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
    public static BSHMethod createBshMethod(Element e) throws IOException {
        if (e == null) {
            return null;
        }
        String file = QFactory.getAttributeValue(e, "file");
        String bsh;
        if (file != null) {
            boolean cache = false;
            String cacheAtt = QFactory.getAttributeValue(e, "cache");
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
     */
    public Object execute(Map arguments, String resultName) throws EvalError, IOException {
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
    public Map execute(Map arguments, Collection returnNames) throws EvalError, IOException {
        Interpreter i = initInterpreter(arguments);
        Map result = new HashMap();
        String rName;
        for (Object returnName : returnNames) {
            rName = (String) returnName;
            result.put(rName, i.get(rName));
        }
        return result;
    }
    
    protected Interpreter initInterpreter(Map arguments) throws EvalError, IOException {
        Interpreter i = new Interpreter();
        Map.Entry entry;
        for (Object o : arguments.entrySet()) {
            entry = (Map.Entry) o;
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
