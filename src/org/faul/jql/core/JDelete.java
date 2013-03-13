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
import java.util.List;
import java.util.Map;

import org.gibello.zql.ZDelete;
import org.gibello.zql.ZExp;
import org.gibello.zql.data.ZEval;
import org.gibello.zql.data.ZTuple;

/**
 * A class implementing the DELETE SQL statement.
 * @author Ben Faul
 *
 */
public class JDelete extends JCommon {
	ZDelete z = null;
	Object result = null;
	
	/**
	 * Sets the indicated table to delete rows from.
	 * @param dbMap Map. The table mapping object.
	 * @param z ZDelete. The SQL delete attributes.
	 * @param data List<Map>. Not used, just added to keep base class happy.
	 * @throws Exception. Throws exception if the table does in exist in the table map.
	 */
	public JDelete(Map<String, Map<String,Object>>dbMap, ZDelete z, List<Map> data) throws Exception {
		this.z = z;
		String name = z.getTable();
		
		tableMap = (Map)dbMap.get("default");
		
		Object test = tableMap.get(name);
		result = loadData(test);
		tableMap.put(name, result);
	}
	
	/**
	 * Execute the where clause, delete as needed, return results.
	 * @return Object. A List<Map>, the new table values after the delete.
	 * @throws Exception. Throws Exceptions on database errors.
	 */
	public Object execute() throws Exception {
		executeQuery();
		return result;
	}
	
	/**
	 * Execute the query that does the deletes, based on the wwhere clause.
	 * @throws Exception. Throws exceptions on SQL errors.
	 */
	void executeQuery() throws Exception {
		ZExp where = z.getWhere();
		List<Integer> removeIndex = new ArrayList<Integer>();
		ZTuple tuple = new ZTuple();
		ZEval evaluator = new ZEval();
		
		List<Map> list = (List)result;
		
		/**
		 * Evaluate the where clause on each row, save the index of the object to be deleted
		 * in a List.
		 */
		for (int i=0;i<list.size();i++) {
			Map e = list.get(i);
			tuple.clearValues();
			tuple.setRow(e);
			if (where == null || evaluator.eval(tuple, where)) {
				removeIndex.add(i);
			}
		}
		
		/**
		 * Use the deletions indexes, and delete them from the table, 
		 * in reverse order.
		 */
		for (int i=removeIndex.size()-1;i>=0;i--) {
			int index = removeIndex.get(i);
			list.remove(index);
		}
	}
	
}
