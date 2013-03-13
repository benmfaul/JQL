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

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gibello.zql.ZSaveFile;

/**
 * Implements the extension to SQL "SAVE tableName fileName;"
 * @author Ben Faul
 *
 */
public class JSaveFile extends JCommon {

	/**
	 * Saves a database table to disk.
	 * @param dbMap Map. The database mapping object for JAVA map.
	 * @param z ZSaveFile object.
	 * @throws Exception. Throws exceptions if table doesn't exist and File IO errors.
	 */
	public JSaveFile(Map<String, Map<String,Object>> dbMap, ZSaveFile z) throws Exception {
		
		String fileName = z.getFileName();
		if (fileName.startsWith("'")) {
			fileName = fileName.substring(1,fileName.length()-1);
		}
		
		FileWriter fw = new FileWriter(fileName);

		/**
		 * Remove default and intermediate tables before saving, then restore
		 */
		Set set = dbMap.keySet();
		Iterator it = set.iterator();
		List<String> keys = new ArrayList();
		Map<String,Map> tuples = new HashMap<String,Map>();
		while(it.hasNext()) {
			String key = (String)it.next();
			keys.add(key);
			Map db = dbMap.get(key);
			
			Map x = new HashMap();
			x.put("default",db.get("default"));
			x.put("intermediate",db.get("intermediate"));
			tuples.put(key, x);
			
			db.remove("default");
			db.remove("intermediate"); 
		}
		
		String data = this.gson.toJson(dbMap);
		
		for (String key : keys) {
			Map db = dbMap.get(key);
			Map tuple = tuples.get(key);
			db.put("intermediate",tuple.get("intermeidate"));
			db.put("default",tuple.get("default"));
			
		}
		fw.write(data);
		fw.close();
	}
	
	/**
	 * Write the JSON data to the filename.
	 * @param data String. The JSON data.
	 * @param fileName String. The filename.
	 * @throws Exception. Throws exceptions on I/O errors.
	 */
	public JSaveFile(String data, String fileName) throws Exception {
		FileWriter fw = new FileWriter(fileName);
		
		fw.write(data);
		fw.close();
	}
}
