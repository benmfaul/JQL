

/*
 * This file is part of JQL.
 *
 * JQL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JQL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jql.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.faul.jql.utils;



import org.faul.jql.core.JSBuiltIns;
import org.faul.jql.core.Jql;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Implements a JavaScript Shell.
 * @author Ben M. Faul
 *
 */
public class JavaScriptShell  {
	
    // create a JavaScript engine
	transient Context cx = Context.enter();
    transient  ScriptableObject global = null;
	
	transient Jql parent = null;
	
	/**
	 * Constructor
	 */
	public JavaScriptShell(Jql parent) {
		this.parent = parent;
		cx.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
		
		global = (ScriptableObject) cx.initStandardObjects(new JSBuiltIns());
		
		try {
			setObject("jql",parent);
			String[] names = { "printConsole", "load", "sleep", };
			global.defineFunctionProperties(names,
				            JSBuiltIns.class,
				            ScriptableObject.DONTENUM);
			
			evalScript("function print(data) { printConsole(data); }");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Evaluates a script, and replace the name with Xname when found in src.
	 * @param src String. The source to evaluate.
	 * @exception Exception. Throws exceptions on evaluation errors.
	 */
	public  void evalScript(String src) throws Exception {
		cx.evaluateString(global, src, "TestScript", 1, null);
	}

	
	/**
	 * Return an object from the shell
	 * @param name String. The name of the object.
	 * @return Object. The object, could be null.
	 * @exception Exception. Throws exception when jshell is null
	 */
	public Object getObject(String name) throws Exception {
		Object x = global.get(name, global);
		if (x == Scriptable.NOT_FOUND) {
			throw new Exception("Name not found");
		}
		return x;
	}

	/**
	 * Set an object.
	 * @param name String. The name of the object
	 * @param o Object. The object to set in the shell
	 * @exception Exception. Throws exception if shell is null
	 */
	public void setObject(String name, Object o) throws Exception {
		
		Object[] args = new Object[1];
		args[0] = o;
		
		cx.enter();
		Scriptable obj = Context.toObject(o, global);
		global.put(name, global, obj);
	}
}
