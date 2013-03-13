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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gibello.zql.ZLoadFile;

/**
 * Load a file into the map of JQL databases.
 * @author Ben Faul
 *
 */
public class JLoad extends JCommon {
	
	Map data = null;
	String dbName;
	String fileName;
	
	/**
	 * Constructor for the LOAD from file SQL statement
	 * @param dbMap Map<String, Map<String,Object>>dbMap. The database map of this JQL instance.
	 * @param dbName String. The name of the new database to load.
	 * @param fileName String. The file's name containing the JSON.
	 */
	public JLoad (Map<String, Map<String,Object>>dbMap, String dbName,  String fileName) {
		this.dbName = dbName;
		this.fileName = fileName;
	}
	
	/**
	 * Constructor for the LOAD from ZLoadFile object based SQL statement.
	 * @param dbMap Map<String, Map<String,Object>>dbMap. The database map of this JQL instance.
	 * @param z ZLoadFile. The ZQL object that has the filename and database info within it.
	 * @throws Exception. Throws Exceptions on I/O errors.
	 */
	public JLoad (Map<String, Map<String,Object>>dbMap, ZLoadFile z) throws Exception {
		String fileName = z.getFileName();
		if (fileName.startsWith("'")) {
			fileName = fileName.substring(1,fileName.length()-1);
		}
		dbName = z.getDbName();
		load(fileName,dbMap);
	}
		
	/**
	 * Load the file into the database using attributes stored in thos object.
	 * @param dbMap Map<String, Map<String,Object>>dbMap. The database Map of this instance of JQL.
	 * @throws Exception. Throws exceptions on I/O errors.
	 */
	void load(String fileName, Map<String, Map<String,Object>>dbMap) throws Exception {
		FileReader fr = new FileReader(fileName);
		
		char [] buf =new char[64000];
		StringBuilder sbuf = new StringBuilder();
		
		int rc = 1;
		try  {
			while(rc > 0) {
				rc = fr.read(buf);
				String str = new String(buf,0,rc);
				sbuf.append(str);
			}
		} catch (Exception error) {
			
		}
		Map load  = (Map)gson.fromJson(sbuf.toString(),Object.class);
		Set keys = load.keySet();
		Iterator it = keys.iterator();
		String key = (String)it.next();
		data = (Map)load.get(key);
		dbMap.put(dbName, data);
		dbMap.put("default", data);
	}
	
	/**
	 * Returns a list of tables in the newly loaded database.
	 * @return List<String>. The list of tables found in this newly loaded database.
	 */
	public List<String> execute() {
		List<String> list = new ArrayList<String>();
		Set keys = data.keySet();
		Iterator it = keys.iterator();
		while(it.hasNext()) {
			list.add((String)it.next());
		}
		return list;
	}
	
	/**
	 * Returns the name of the newly loaded database.
	 * @return String. The name of the newly loaded database.
	 */
	public String getDbName() {
		return dbName;
	}
}
