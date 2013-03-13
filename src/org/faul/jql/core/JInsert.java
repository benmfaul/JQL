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

import java.util.HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gibello.zql.ZInsert;
import org.gibello.zql.ZQuery;
import org.gibello.zql.data.ZEval;
import org.gibello.zql.data.ZTuple;

/**
 * The implemention of the SQL insert statement.
 * @author Ben Faul
 *
 */
public class JInsert extends JCommon {
	Object execute = null;
	ZInsert z = null;
	List<Map> result = null;
	
	/**
	 * Load the elements (fields and values) of the ZInsert into the database table.
	 * @param tableMap Map. The database object to name map.
	 * @param z ZInsert. The insert fields and values object
	 * @throws Exception. Throws exceptions on JSON encoding errors.
	 */
	public JInsert(Map<String,Object>tableMap, ZInsert z, Map<String,Double> index) throws Exception {
		this.z = z;
		this.tableMap = tableMap;
		
                                                
		ZQuery zq = z.getQuery();
		String table = z.getTable();
			
		List<Object> vals = z.getValues();
		List<String> fields = z.getColumns();

		/**
		 * Create a new Map, which will be the new row in the table (data).
		 */
		ZTuple tuple = new ZTuple();
		Map newMap = new HashMap();
		ZEval evaluator = new ZEval();
		
		/**
		 * Evaluate the values from the VALUES() of the SQL statement and
		 * set them into the newMap. (Creates the new row).
		 */
		int i=0;
		for (Object e : vals) {
			setValueFromObject(newMap,fields.get(i),evaluator, tuple, e);
			i++;
		}
		
		double idx = this.getNextIndex(index, table);
		newMap.put("index",idx);
		
		List data = (List)tableMap.get(table);
		if (data == null) {
			data = new ArrayList();
		}
		data.add(newMap);
		result = data;
	}
	
	/**
	 * The constructor does the insert, just return the resulting table.
	 * @return Object. The new table
	 */
	Object execute() {
		return result;
	}
}
