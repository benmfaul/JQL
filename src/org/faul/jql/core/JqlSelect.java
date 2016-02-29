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
 * along with Zql.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.faul.jql.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faul.jql.utils.Compare;
import org.faul.jql.utils.CompareScalar;
import org.gibello.zql.ZConstant;
import org.gibello.zql.ZExp;
import org.gibello.zql.ZExpression;
import org.gibello.zql.ZFromItem;
import org.gibello.zql.ZOrderBy;
import org.gibello.zql.ZQuery;
import org.gibello.zql.ZSelectItem;
import org.gibello.zql.data.ZEval;
import org.gibello.zql.data.ZTuple;

/**
 * Executes the SQL select statement.
 * 
 * @author Ben Faul
 * 
 */
public class JqlSelect extends JCommon {

	Map tableMap = null;
	List execute = null;
	ZQuery z = null;
	JStoredProcedure sp = null;
	int limit = -1;
	int offset = -1;

	/**
	 * Constructor that Instantiates the Select paramters.
	 * 
	 * @param tableMap
	 *            Map<String,Object>. The tables of the currently used database.
	 * @param z
	 *            ZQuery. The semantic elements of the query.
	 * @throws Exception. Throws
	 *             exceptions on JSON errors, or the table does not exist.
	 */
	public JqlSelect(Map<String, Object> tableMap, ZQuery z) throws Exception {
		this.z = z;

		List from = z.getFrom(); // FROM part of the query
		ZFromItem table = (ZFromItem) from.get(0);

		if (z.getLimit() != null) {
			ZConstant lim = (ZConstant) z.getLimit();
			limit = Integer.parseInt(lim.getValue());
		}

		if (z.getOffset() != null) {
			ZConstant off = (ZConstant) z.getOffset();
			offset = Integer.parseInt(off.getValue());
		}

		this.tableMap = tableMap;
		String name = table.getTable();
		Object test = tableMap.get(name);
		execute = (List) loadData(test);

	}

	/**
	 * Returns the results of the query. This is called after the constructor.
	 * 
	 * @param arg
	 *            Object. A table (List<Map> or, a file reader object. This is
	 *            the table to sececute the query against.
	 * @return List. The List of rows (map objects) that satisfied the select
	 *         clause.
	 * @throws Exception. Throws
	 *             exceptions if the arg is not a List<Map> or a filereader,
	 *             also throws IO exceptions if file doesn't exist or contains
	 *             bad JSON.
	 */
	public List results(Object arg) throws Exception {
		List mapping = new ArrayList();
		// List argList = null;
		List sel = z.getSelect(); // SELECT part of the query
		HashMap<String, HashMap> distincts = null;
		List<String> distKeys = null;

		if (z.isDistinct()) {
			distincts = new HashMap<String, HashMap>();
			distKeys = new ArrayList<String>();
			for (int i = 0; i < sel.size(); i++) {
				ZSelectItem item = (ZSelectItem) sel.get(i);
				String distKey = compositeName(item.getColumn());
				distKeys.add(distKey);
			}
		}

		/*
		 * List larg = (List)arg; arg = larg.get(0); if (arg instanceof
		 * FileReader) { argList = loadFile((FileReader) arg); } else { if (arg
		 * instanceof String) argList = (List)tableMap.get((String)arg);
		 * //(List) arg; else { argList = new ArrayList(); argList.add(arg); } }
		 */

		Object tail = z.getWhere();
		ZExpression where = null;
		List<ZOrderBy> order = z.getOrderBy();
		List<ZConstant> group = null;
		if (z.getGroupBy() != null)
			group = z.getGroupBy().getGroupBy();

		if (tail instanceof ZConstant) {

		} else
			where = (ZExpression) tail;

		/**
		 * Group by causes a sort by first, then uses the summarized info
		 */
		if (group != null) {
			if (order == null)
				order = new ArrayList();

			for (ZConstant groupName : group) {
				ZOrderBy newOrder = new ZOrderBy(groupName);
				order.add(newOrder);
				// Get sort based on the new order
				List pred = executeQuery(execute, where, sel, distincts,
						distKeys, order, true);

				String last = "";
				List temp = new ArrayList();
				for (int i = 0; i < pred.size(); i++) {
					Map inner = (Map) pred.get(i);
					String now = (String) inner.get(groupName.getValue());
					if (now.equals(last)) {
						temp.add(inner);
					} else {
						if (temp.size() != 0)
							mapping.add(executeQuery(temp, where, sel,
									distincts, distKeys, order, false));
						last = now;
						temp.clear();
						temp.add(inner);
					}
				}
			}
			return mapping;
		}
		return executeQuery(execute, where, sel, distincts, distKeys, order,
				false);
	}

	/**
	 * Executes the query called by results() method.
	 * 
	 * @param argList
	 *            List.
	 * @param where
	 *            ZExpression.
	 * @param sel
	 *            Vector<ZSelect>.
	 * @param distincts
	 *            HashMap<String, HashMap>.
	 * @param distKeys
	 *            Vector<String>
	 * @param order
	 *            Vector<ZOrderBy>
	 * @param suppressFuncs
	 *            boolean.
	 * @return List. The results of the query, fully executed.
	 * @throws Exception. Throws
	 *             exceptions on badly formed JSON and references to undefined
	 *             tables and columns.
	 */
	List executeQuery(List argList, ZExpression where, List<ZSelectItem> sel,
			HashMap<String, HashMap> distincts, List<String> distKeys,
			List<ZOrderBy> order, boolean suppressFuncs) throws Exception {

		if (offset > -1) {
			int last = argList.size();
			argList = argList.subList(offset, last);
		}

		List mapping = new ArrayList();
		ZTuple tuple = new ZTuple();
		ZEval evaluator = new ZEval();
		Map vals = null;
		if (argList != null) {
			for (Object e : argList) {
				tuple.clearValues();
				tuple.setRow((Map) e);

				try {
					if (where == null || evaluator.eval(tuple, where)) {
						vals = processTuple(tuple, sel, distincts, distKeys);
						if (vals.size() != 0)
							mapping.add(vals);
					}
				} catch (Exception error) {
					/*
					 * If it's just a missing column, just skip it.
					 */
					if ((error.toString().contains("unknown column name") || error
							.toString().contains("Operands had a null")) == false) {
						throw error;
					}
				}
			}
		}

		if (order != null) {
			Integer depth = 0;
			subsequentSort(depth, order, mapping);
		}
		/**
		 * if the tuple has function mappings, we have to remap each of the map
		 * entries to use the function value.
		 */
		if (suppressFuncs == false && tuple.usedFunctions()) {
			List list = tuple.returnFunctionMapping();
			HashMap e = new HashMap();
			List<String> funcNames = tuple.getUsedFunctions();

			// Extract values from the aggregate functions, they match the
			// function names
			List<String> funcArgs = new ArrayList<String>();
			for (int i = 0; i < funcNames.size(); i++) {
				String[] entry = funcNames.get(i).split(":");
				if (entry.length == 2)
					e.put(entry[0], list.get(i));
				else
					e.put(entry[2], list.get(i));
				funcArgs.add(entry[1]);
			}

			/**
			 * Now iterate through the map entries using the column names.
			 */
			List<String> selections = new ArrayList<String>();
			for (ZSelectItem select : sel) {
				String column = select.getColumn();
				if (funcArgs.contains(column) == false)
					selections.add(column);
			}
			List converted = new ArrayList();
			;

			for (Object m : mapping) {
				Map entry = (Map) m;
				for (String s : selections) {
					e.put(s, entry.get(s));
				}
			}

			converted.add(e);
			mapping = converted; // now that the mapped da
		}
		if (limit > -1) {
			return mapping.subList(0, limit);
		} else
			return mapping;
	}

	/**
	 * Process a tuple
	 * 
	 * @param tuple
	 *            ZTuple.
	 * @param map
	 *            Vector.
	 * @param distincts
	 *            HashMap<String, HashMap>.
	 * @param distKeys
	 *            Vector<String>
	 * @return Map.
	 * @throws Exception
	 */
	Map processTuple(ZTuple tuple, List map,
			HashMap<String, HashMap> distincts, List<String> distKeys)
			throws Exception {

		String func = null;
		String funcArg = null;

		// If it is a "select *", display the whole tuple
		if (((ZSelectItem) map.get(0)).isWildcard()) {
			return tuple.getAllElements();
		}

		Map hmap = new HashMap();
		ZEval evaluator = new ZEval();

		// Evaluate the value of each select item
		boolean innerDistinct = false;
		List distinctAdd = new ArrayList();
		for (int i = 0; i < map.size(); i++) {

			ZSelectItem item = (ZSelectItem) map.get(i);
			// Function?
			if (item.getAggregate() != null) {
				func = item.getAggregate();
				funcArg = item.getColumn();
				if (func.equalsIgnoreCase("jql") == false) {
					tuple.handleFunction(func, funcArg, item.getAlias());
					hmap.put(funcArg, tuple.getAttValue(funcArg)); // keep the
																	// function
																	// argument
																	// around
																	// for group
																	// by
				} else {
					Integer depth = 0;
					String cname = evaluateEmbeddedJson(depth, funcArg, tuple);

					ZExp e = new ZConstant(cname, 0);
					// ZExp e = item.getExpression();

					if (processTuple(evaluator, cname, tuple, e, hmap,
							distincts, distKeys, distinctAdd, item.getAlias()) == true)
						innerDistinct = true;
				}
			} else {
				String column = item.getColumn();
				ZExp e = item.getExpression();
				if (column.endsWith("_")) {
					column = column.substring(0, column.length() - 1);
					column = column.replaceAll("_", ",");
					Integer depth = 0;
					column = evaluateEmbeddedJson(depth, column, tuple);
					e = new ZConstant(column, 0);
				}

				if (processTuple(evaluator, column, tuple, e, hmap, distincts,
						distKeys, distinctAdd, item.getAlias()) == true)
					innerDistinct = true;
			}
		}

		if (distincts != null && innerDistinct) {
			for (int i = 0; i < map.size(); i++) {
				ZSelectItem item = (ZSelectItem) map.get(i);
				String column = compositeName(item.getColumn());
				Object v = distinctAdd.get(i);
				hmap.put(column, v);
			}
		}

		return hmap;
	}

	/**
	 * Process a tuple, executes a query on the Tuple, and returns true if the
	 * row meets the WHERE clause.
	 * 
	 * @param evaluator
	 *            ZEval.
	 * @param cname
	 *            String.
	 * @param tuple
	 *            ZTuple.
	 * @param e
	 *            ZExp.
	 * @param hmap
	 *            Map<String,Object>.
	 * @param distincts
	 *            HashMap<String,HashMap>.
	 * @param distKeys
	 *            Vector<String>
	 * @param distinctAdd
	 *            Vector.
	 * @param alias
	 *            String.
	 * @return boolean.
	 * @throws Exception
	 */
	boolean processTuple(ZEval evaluator, String cname, ZTuple tuple, ZExp e,
			Map<String, Object> hmap, HashMap<String, HashMap> distincts,
			List<String> distKeys, List distinctAdd, String alias)
			throws Exception {
		boolean innerDistinct = false;
		Object o = evaluator.evalExpValue(tuple, e);
		if (alias != null)
			cname = alias;
		if (distincts == null) {

			if (alias != null && alias.equals("empty")) {
				Map m = (Map) o;
				Set set = m.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					Object value = m.get(key);
					hmap.put(key, value);
				}
			} else {
				hmap.put(cname, o);
			}

		} else {
			if (!distinctAlreadyContains(distincts, distKeys, tuple)) {
				innerDistinct = true;
			}
			distinctAdd.add(o);
		}
		return innerDistinct;
	}

	/**
	 * Sort the results
	 * 
	 * @param order
	 *            Vextor<ZOrderBy>
	 * @param mapping
	 *            List.
	 */
	void sortMapping(List<ZOrderBy> order, List mapping) {

		int i = 0;
		ZOrderBy ord = order.get(0);
		List index = new ArrayList();
		String[] comps = ord.toString().split(" ");
		Compare c = new Compare(comps[0], ord.getAscOrder());
		Collections.sort(mapping, c);
	}

	/**
	 * Recursively sort based on multiple columns.
	 * 
	 * @param depth
	 *            Integer.
	 * @param order
	 *            Vector<ZOrderBy>
	 * @param mapping
	 *            List.
	 */
	void subsequentSort(Integer depth, List<ZOrderBy> order, List mapping) {
		List index = new ArrayList();
		ZOrderBy ord = order.get(depth);
		String[] comps = ord.toString().split(" ");
		String key = comps[0];
		Compare c = new Compare(key, ord.getAscOrder());
		Collections.sort(mapping, c);

		depth++;
		if (depth == order.size())
			return;

		List buckets = new ArrayList();
		List bucket = null;
		Object last = "";
		CompareScalar cs = new CompareScalar();
		for (int i = 0; i < mapping.size(); i++) {
			Map tuple = (Map) mapping.get(i);
			Object value = tuple.get(key);
			if (cs.compare(value, last) == 0) {
				bucket.add(tuple);
			} else {
				bucket = new ArrayList();
				bucket.add(tuple);
				buckets.add(bucket);
				last = value;
			}
		}
		mapping.clear();
		for (int i = 0; i < buckets.size(); i++) {
			List b = (List) buckets.get(i);
			subsequentSort(depth, order, b);
			mapping.addAll(b);
		}
	}

	/**
	 * Determine if a column's has already been seen in the distincts list.
	 * 
	 * @param distincts
	 *            HashMap<String, HashMap>.
	 * @param keys
	 *            Vector<String>.
	 * @param candidateTuple
	 *            ZTuple.
	 * @return
	 */
	boolean distinctAlreadyContains(HashMap<String, HashMap> distincts,
			List<String> keys, ZTuple candidateTuple) throws Exception {
		if (distincts == null)
			return false;

		Integer depth = 0;
		return true & recurseKeyValues(depth, keys, candidateTuple, distincts);

	}

	/**
	 * Recurse through the embedded column names to find out if the lowest level
	 * is distinct.
	 * 
	 * @param depth
	 *            Integer.
	 * @param keys
	 *            Vector<String>
	 * @param tuple
	 *            ZTuole.
	 * @param distincts
	 *            HashMap<String, HashMap>.
	 * @return
	 */
	boolean recurseKeyValues(Integer depth, List<String> keys, ZTuple tuple,
			HashMap<String, HashMap> distincts) throws Exception {
		String key = keys.get(depth);
		depth++;

		Object x = tuple.getAttValue(key);

		String hashKey = mapper
				.writer()
				.writeValueAsString(x);
		HashMap map = distincts.get(hashKey);
		boolean seenBefore = true;
		if (map == null) {
			seenBefore = false;
			map = new HashMap();
			map.put(hashKey, new HashMap());
			distincts.put(hashKey, map);

		}
		if (depth == keys.size())
			return seenBefore;
		return seenBefore & recurseKeyValues(depth, keys, tuple, distincts);
	}

}
