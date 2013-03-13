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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faul.jql.utils.JavaScriptShell;

/**
 * A class the implements stored procedures, which is JavaScript code
 * stored in a table called "procs" in the currently USEd database.
 * @author Ben Faul
 *
 */
public class JStoredProcedure {

	List<Map> procs = new ArrayList();
	Jql parent;
	
	/**
	 * Constructor for the stored procedures. If the table 'procs' exists
	 * then it reads the table. Otherwise, it creates an empty tableMap.
	 * @param parent
	 */
	public JStoredProcedure(Jql parent) {
		this.parent = parent;
		List xprocs =(List)parent.tableMap.get("procs");
		if (xprocs == null) {
			parent.addTable("procs");
			parent.tableMap.put("procs",procs);
		}
		parent.sp = this;
	}
	
	/**
	 * Execute a stored procedure.
	 * @param name String. The name of the procedure to execute.
	 * @param objects Object[]. The varargs of the procedure.
	 * @return Object. The return value of the procedure, can be void.
	 * @throws Exception. Throws exceptions on JavaScript and JSON coding errors.
	 */
	public Object exec(String name, Object ...objects ) throws Exception {
		if (procs.size()==0)
			return null;
		
		int i;
		for (i = 0; i < procs.size(); i++) {
			Map proc = procs.get(i);
			String test = (String)proc.get("name");
			if (test.equalsIgnoreCase(name)) {
				return execute(proc,objects);
			}
		}
		return null;
	}

	/**
	 * Executes a stored procedure using the entries "shell" object. If the shell
	 * is null, a new JavaScript shell is created and stored in the map. 
	 * @param proc Map. The current Map of the stored procedure being executed.
	 * @param objects Object[]. The varargs of the procedure.
	 * @return Object. The return value of the procedure, can also be void.
	 * @throws Exception. Throws exceptions on JavaScript and JSON coding errors.
	 */
	Object execute(Map proc, Object [] objects) throws Exception {
		Object temp = parent.intermediateResult;
		Object returns = null;
		
		Object[] args = new Object[objects.length+1]; 
		args[0] = parent.tableMap;
		for (int i=0;i<objects.length;i++) {
			args[i+1] = objects[i];
		}
		
		JavaScriptShell shell = (JavaScriptShell)proc.get("shell");
		if (shell == null) {
			shell = new JavaScriptShell(parent);
			proc.put("shell",shell);
		}
		
		shell.setObject("args", args);
		String callSequence = "rc = " + proc.get("name") + "(args);";
		shell.evalScript(callSequence);
		returns = shell.getObject("rc");
			
		parent.intermediateResult = temp;
		return returns;
	}


	/**
	 * List the stored procedures in the database.
	 * @return List<String>. The list of stored procedure names.
	 */
	public List<String> list() {
		List list = new ArrayList();
		if (procs == null) {
			return list;
		}
		for (Map proc : procs ) {
			list.add(proc.get("name"));
		}
		return list;
	}
	
	/**
	 * Removes a stored procedure from the 'procs' table of the currently USEd database.
	 * @param name String. The name of the procedure to remove from 'procs'.
	 */
	public void remove(String name) {
		int i;
		for (i = 0; i < procs.size(); i++) {
			Map proc = procs.get(i);
			String test = (String)proc.get("name");
			if (test.equalsIgnoreCase(name)) {
				procs.remove(i);
				return;
			}
		}
	}
	
	/**
	 * Instantiate a stored procedure from a file reader object.
	 * @param name String. The name of the new stored procedure.
	 * @param fr FileReader. The object to read the procdedure from.
	 * @throws Exception. Throws exceptions on I/O errors.
	 */
	public void store(String name, FileReader fr) throws Exception {
		String proc = "";
		char [] buf = new char[4096];
		int rc = 1;
		while(rc>0) {
			rc = fr.read(buf);		
			if (rc > 0)
				proc += new String(buf,0,rc);
		}

		store(name,proc);
	}
	
	/**
	 * Store a stored procedure in the 'procs' table of the currently USEd database.
	 * @param name String. The name of the stored procedure.
	 * @param procedure String. The JavaScript to ececute.
	 * @throws Exception. Throws exception on JSON errors.
	 */
	public void store(String name, String procedure) throws Exception  {
		int i;
		Map proc = null;
		
		for (i = 0; i < procs.size(); i++) {
			proc = procs.get(i);
			String test = (String)proc.get("name");
			if (test.equalsIgnoreCase(name)) {
				proc.put("value",procedure);			
				break;
			}
		}
		
		if (proc == null || i == procs.size()) 
			proc = new HashMap();
		proc.put("name", name);
		proc.put("value", procedure);
		
		JavaScriptShell shell = (JavaScriptShell)proc.get("shell");
		if (shell==null) {
			shell = new JavaScriptShell(parent);
			proc.put("shell",shell);
		}
		
		String function = "function " + name + "(functionArgs) {\n" +
								procedure + "\n" +
								"}";
		try {
			shell.evalScript(function);
		} catch (Exception error) {
			error.printStackTrace();
		}
		procs.add(proc);					
	}
	
	/**
	 * Remove the JavaScript shells from this stored procedure before the database is 
	 * saved to disk. 
	 */
	public void stripShells() {
		List<Map> procs =(List)parent.tableMap.get("procs");
		for (Map proc : procs) {
			proc.remove("shell");
		}
	}
}
