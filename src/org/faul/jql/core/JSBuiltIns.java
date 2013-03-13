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
package org.faul.jql.core;

import java.io.FileNotFoundException;


import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.tools.shell.Global;


/**
 * A class that supports handy extensions to JavaScript.
 * @author Ben Faul
 *
 */
public class JSBuiltIns extends ScriptableObject {

	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Return the class name of this object.
	 * @return String. The name of the class.
	 */
	public String getClassName() {
		return "JSBuiltIns";
	}
	
	/**
	 * Load and execute a set of JavaScript source files.
	 * 
	 * This method is defined as a JavaScript function.
	 * @param cx Context. The current execution context.
	 * @param thisObj Scriptable. The scriptable obkect, not used.
	 * @param args Object[]. The arguments to the load.
	 * @param funObj Function. The function argument, not used.
	 * 
	 */
	public static void require(Context cx, Scriptable thisObj, Object[] args,
							Function funObj) {
		String arg = Context.toString(args[0]);
		String cmd = arg;
		if (arg.endsWith(".js")==false) 
			arg += ".js";
		processSource(cx, arg);
	}

	/**
	 * Print the string values of its arguments.
	 * 
	 * This method is defined as a JavaScript function. Note that its arguments
	 * are of the "varargs" form, which allows it to handle an arbitrary number
	 * of arguments supplied to the JavaScript function.
	 * 
	 * @param cx Context. The current execution context.
	 * @param thisObj Scriptable. The scriptable obkect, not used.
	 * @param args Object[]. The arguments to the load.
	 * @param funObj Function. The function argument, not used.
	 * 
	 */
	public static void printConsole(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				System.out.print(" ");

			// Convert the arbitrary JavaScript value into a string form.
			String s = Context.toString(args[i]);

			System.out.print(s);
		}
	}

	/**
	 * Load and execute a set of JavaScript source files.
	 * 
	 * This method is defined as a JavaScript function.
	 * @param cx Context. The current execution context.
	 * @param thisObj Scriptable. The scriptable obkect, not used.
	 * @param args Object[]. The arguments to the load.
	 * @param funObj Function. The function argument, not used.
	 * 
	 */
	public static void load(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		for (int i = 0; i < args.length; i++) {
			processSource(cx, Context.toString(args[i]));
		}
	} 
	
	/**
	 * Sleep indicated number of milliseconsa.
	 * @param cx Context. The current execution context.
	 * @param thisObj Scriptable. Not used.
	 * @param args Object[]. The arguments array.
	 * @param funObj Function. Not used.
	 * @throws Exception
	 */
	public static void sleep(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		int ms = (int)Context.toNumber(args[0]);
		Thread.sleep(ms);
	}
	
	/**
	 * Change the current working directory.
	 * @param cx Context. The current execution context.
	 * @param thisObj Scriptable. Not used.
	 * @param args Object[]. The arguments array, args[0] is the new directory name
	 * @param funObj Function. Not used.
	 * @throws Exception. Throws exception on nullw.
	 */
	public void cwd(Context cx, Scriptable thisObj, Object[] args, Function funObj) 
			throws Exception  {
		String newDir  = (String)Context.toString(args[0]);
		String prop = System.getProperty("user.dir");
		System.setProperty(prop, newDir);
	}


	/**
	 * Evaluate JavaScript source.
	 * 
	 * @param cx
	 *            the current context
	 * @param filename
	 *            the name of the file to compile, or null for interactive mode.
	 */
	private static void processSource(Context cx, String filename) {
		FileReader in = null;
		Global globalScript = new Global(cx);
		try {
			in = new FileReader(filename);
		} catch (FileNotFoundException ex) {
			Context.reportError("Couldn't open file \"" + filename + "\".");
			return;
		}

		try {
			// Here we evalute the entire contents of the file as
			// a script. Text is printed only if the print() function
			// is called.
			cx.evaluateReader(globalScript, in, filename, 1, null);
		} catch (WrappedException we) {
			System.err.println(we.getWrappedException().toString());
			we.printStackTrace();
		} catch (EvaluatorException ee) {
			System.err.println("js: " + ee.getMessage());
		} catch (JavaScriptException jse) {
			System.err.println("js: " + jse.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.toString());
		} finally {
			try {
				in.close();
			} catch (IOException ioe) {
				System.err.println(ioe.toString());
			}
		}
	}

}
