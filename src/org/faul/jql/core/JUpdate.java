/*
 * This file is part of JQL.
 *
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

import java.util.ArrayList;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.gibello.zql.ZExp;
import org.gibello.zql.ZUpdate;
import org.gibello.zql.data.ZEval;
import org.gibello.zql.data.ZTuple;

/**
 * Implementation of the SQL UPDATE command.
 * @author Ben Faul
 *
 */
public class JUpdate extends JCommon {

	ZUpdate z = null;
	Object result = null;
	
	/**
	 * Constructor that sets up the query into the database.
	 * @param dbMap Map. The database mapping object.
	 * @param z ZUpdate. Attributes of the update (SET and WHERE clauses).
	 * @param data List. The table contents being operated on.
	 * @throws Exception. Throws exceptions if table doesn't exist, file I/O and Gson encoding errors.
	 */
	public JUpdate(Map<String, Map<String,Object>> dbMap, ZUpdate z, List<Map> data) throws Exception {
		this.tableMap = tableMap;
		this.z = z;
		String name = z.getTable();
		tableMap = dbMap.get("default");
		Object test = tableMap.get(name);
		result = loadData(test);
		tableMap.put(name,result);
	}
	
	/**
	 * Executes the query and updates the table accordingly.
	 * @return Object. The table with the updates made to it.
	 * @throws Exception. Throws exceptions on Gson encoding errors.
	 */
	public Object execute() throws Exception {
		executeQuery();
		return result;
	}
	
	/**
	 * Evaluates the WHERE clause on each row, and updates the indicated fields 
	 * if the WHERE clause evaluates to true.
	 * @throws Exception. Throws exceptions if the database table does not exist.
	 */
	void executeQuery() throws Exception {
		ZExp where = z.getWhere();
		List<Map> mapping = new ArrayList();
		ZTuple tuple = new ZTuple();
		ZEval evaluator = new ZEval();
		Map vals = null;
		
		/**
		 * Evaluate the where clause on each row, then, if it evaluates to
		 * true, save that row in a List for later modification.
		 */
		for (Object e : (List)result) {
			tuple.clearValues();
			tuple.setRow((Map) e);
			if (where == null || evaluator.eval(tuple, where)) {
				mapping.add((Map)e);
			}
		}

		/**
		 * Create the setter map. This is two arrays where column[i] is the fieldname @i
		 * and values[i] is the value @i.
		 */
		Hashtable items = z.getSet();
		Enumeration it = items.keys();
		List<String> columns = new ArrayList<String>();
		List values = new ArrayList();
		while(it.hasMoreElements()) {
			String column = (String)it.nextElement();
			columns.add(column);
			values.add(items.get(column));
		}
		
		/**
		 * Iterate through the Maps in the mapping array, as each Map in this array
		 * needs updating. The values in each map is set to the value at
		 * each column name's index.
		 */
		for (Map map : mapping) {
			for (int i=0;i<columns.size();i++) {
				String column = columns.get(i);
				Object value = values.get(i);
				setValueFromObject(map,column, evaluator, tuple, value);
			}
		}
	}
	
}
