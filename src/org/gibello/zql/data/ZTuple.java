/*
 * This file is part of Zql.
 *
 * Zql is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Zql is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Zql.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gibello.zql.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.StringTokenizer;

import org.faul.jql.core.Jql;

public class ZTuple {

	/**
	 * the names of the attributes
	 */
	protected ArrayList<String> attributes_;
	/**
	 * the values of the attributes
	 */
	protected HashMap values_;
	/**
	 * hashtable to locate attribute names more easily
	 */

	HashMap<String, HashMap<String, Object>> functions = new HashMap<String, HashMap<String, Object>>();
	List<String> funcNames = new ArrayList<String>();

	// //////////////////////////
	public ZTuple(Map map) {
		Set set = map.keySet();
		Iterator keys = set.iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			setAtt(key, null);
		}
	}

	public List<String> getOrderedFields() {
		return attributes_;
	}

	public boolean isFunctionArg(String name) {
		for (String n : funcNames) {
			String[] test = n.split(":");
			test[1].replaceAll("'","");
			if (name.equalsIgnoreCase(test[1]))
				return true;
		}
		return false;
	}

	public void addFuncArgs(Map hmap) {
		for (String n : funcNames) {
			String[] test = n.split(":");
			hmap.put(test[1], getAttValue(test[1]));
		}
	}

	public boolean usedFunctions() {
		if (functions.size() > 0)
			return true;
		return false;
	}

	public void setRow(Map map) {
		Set set = map.keySet();
		Iterator keys = set.iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object value = map.get(key);
			setAtt(key, value);
		}
	}

	public void clearValues() {
		attributes_.clear();
		values_.clear();
	}

	public Map getAllElements() {
		Map map = new HashMap();
		for (int i = 0; i < attributes_.size(); i++) {
			map.put(attributes_.get(i), values_.get(attributes_.get(i)));
		}
		return map;
	}

	public List returnFunctionMapping() throws Exception {
		List list = new ArrayList();
		if (functions.size() == 0)
			return list;
		for (String pair : funcNames) {
			String[] p = pair.split(":");
			HashMap<String, Object> entry = functions.get(p[0]);
			if (entry == null)
				throw new Exception("Internal error, function data for " + p[0]
						+ " not found");
			Double[] d = (Double[]) entry.get(p[1]);
			list.add(d[0].doubleValue());
		}

		return list;
	}

	public void handleFunction(String func, String funcArg,  String alias) throws Exception {
		Double d[] = null;

		/**
		 * Regular sql function
		 */
		HashMap<String, Object> entry = functions.get(func);
		if (entry == null) {
			entry = new HashMap<String, Object>();
			functions.put(func, entry);
		}
		Object obj = entry.get(funcArg);
		if (obj == null) {
			d = new Double[2];
			d[0] = (double) 0;
			d[1] = (double) 0;
			obj = d;
			if (alias == null)
				funcNames.add(func + ":" + funcArg);
			else
				funcNames.add(func + ":" + funcArg + ":" + alias);
			entry.put(funcArg, obj);
		} else {
			d = (Double[]) obj;
		}
		
		// Strip the ' from column names/
		funcArg = funcArg.replaceAll("'","");

		if (func.equalsIgnoreCase("sum"))
			sum(d, funcArg);
		else if (func.equalsIgnoreCase("min"))
			min(d, funcArg);
		else if (func.equalsIgnoreCase("max"))
			max(d, funcArg);
		else if (func.equalsIgnoreCase("count"))
			count(d);
		else if (func.equalsIgnoreCase("avg"))
			avg(d, funcArg);
	}

	public List<String> getUsedFunctions() {
		return funcNames;
	}

	public void count(Double d[]) {
		d[0]++;
	}

	public void min(Double min[], String column_) {
		double obj = getDouble(column_);
		if (min[0] < obj)
			min[0] = obj;
	}

	public void max(Double[] max, String column_) {
		double obj = getDouble(column_);
		if (obj > max[0])
			max[0] = obj;
	}

	public void sum(Double[] sum, String column_) {
		sum[0] += getDouble(column_);
	}

	double getDouble(String column) {
		Object o = values_.get(column);
		Double d = null;
		if (o instanceof Integer)
			return (double) (Integer) o;
		if (o instanceof Long)
			return (double) (Long) o;
		if (o instanceof String) {
			return Double.parseDouble((String)o);
		}
		
		return walkAttribute(o);
	}
	
	/**
	 * Walk an attribute, adding all the embedded attributes with numbers.
	 * @param o Object. This can be a double, a Map, List<Map>, or List<
	 * @return double. The value of this attribute.
	 */
	private double walkAttribute(Object o) {
		double total = 0;
		if (o instanceof Map) {
			Map x = (Map)o;
			Set set = x.keySet();
			Iterator it = set.iterator();
			while(it.hasNext()) {
				String key = (String)it.next();
				Object y = x.get(key);
				if (y instanceof Double) {
					total += (Double)y;
				}
				else {
					total += walkAttribute(y);
				}
			}
			return total;
		}
		if (o instanceof List) {
			List list = (List)o;
			for (Object lx : list) {
				total += walkAttribute(lx);
			}
			return total;
		}
		try {
			total += Double.parseDouble(o.toString());
		} catch (Exception error) {
			
		}
		return total;
	}

	public void avg(Double[] avg, String column) {
		double obj = getDouble(column);
		avg[0] += obj;
		avg[1]++;
	}

	// //////////////////////////
	/**
	 * The simplest constructor
	 */
	public ZTuple() {
		attributes_ = new ArrayList();
		values_ = new HashMap();;
	}

	/**
	 * Create a new tuple, given it's column names
	 * 
	 * @param colnames
	 *            Column names separated by commas (,).
	 */
	public ZTuple(String colnames) {
		this();
		StringTokenizer st = new StringTokenizer(colnames, ",");
		while (st.hasMoreTokens()) {
			setAtt(st.nextToken().trim(), null);
		}
	}

	/**
	 * Set the current tuple's column values.
	 * 
	 * @param row
	 *            Column values separated by commas (,).
	 */
	public void setRow(String row) {
		StringTokenizer st = new StringTokenizer(row, ",");
		for (int i = 0; st.hasMoreTokens(); i++) {
			String val = st.nextToken().trim();
			try {
				Double d = new Double(val);
				setAtt(getAttName(i), d);
			} catch (Exception e) {
				setAtt(getAttName(i), val);
			}
		}
	}

	/**
	 * Set the current tuple's column values.
	 * 
	 * @param row
	 *            A vector of column values.
	 */
	public void setRow(List row) {
		for (int i = 0; i < row.size(); i++) {
			setAtt(getAttName(i), row.get(i));
		}
	}

	/**
	 * Set the value of the given attribute name
	 * 
	 * @param name
	 *            the string representing the attribute name
	 * @param value
	 *            the Object representing the attribute value
	 */
	public void setAtt(String name, Object value) {
		if (name == null) 
			return;
	
		if (getAttValue(name)==null) 
			attributes_.add(name);
		values_.put(name,value);
	}

	/**
	 * Return the name of the attribute corresponding to the index
	 * 
	 * @param index
	 *            integer giving the index of the attribute
	 * @return a String
	 */
	public String getAttName(int index) {
		try {
			return (String) attributes_.get(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Return the index of the attribute corresponding to the name
	 * 
	 * @param name - String. The name of the attribute to get the index of.
	 * @return the index as an int, -1 if name is not an attribute
	 */
	public int getAttIndex(String name) {
		if (name == null)
			return -1;
		
		for (int i=0;i<attributes_.size();i++) {
			if (attributes_.get(0).equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}

	/**
	 * Return the value of the attribute corresponding to the index
	 * 
	 * @param index
	 *            integer giving the index of the attribute
	 * @return an Object (null if index is out of bound)
	 */
	public Object getAttValue(int index) {
		return values_.get(attributes_.get(index));
	}

	
	public Object getAttValue(String name) {
		if (name == null)
			return null;
		
		Object rets = values_.get(name);
		return rets;
	}

	/**
	 * To know if an attributes is already defined
	 * 
	 * @param attrName
	 *            the name of the attribute
	 * @return true if there, else false
	 */
	public boolean isAttribute(String attrName) {
		if (attrName != null) {
			if (values_.get(attrName)==null)
				return false;
			else return true;
		}
		return false;
	}

	/**
	 * Return the number of attributes in the tupple
	 * 
	 * @return int the number of attributes
	 */
	public int getNumAtt() {
		return values_.size();
	}

	/**
	 * Returns a string representation of the object
	 * 
	 * @return a string representation of the object
	 */
	public String toString() {
		Object att;
		Object value;
		String attS;
		String valueS;

		StringBuffer resp = new StringBuffer();
		resp.append("[");
		if (attributes_.size() > 0) {
			att = attributes_.get(0);
			if (att == null)
				attS = "(null)";
			else
				attS = att.toString();

			value = values_.get(0);
			if (value == null)
				valueS = "(null)";
			else
				valueS = value.toString();
			resp.append(attS + " = " + valueS);
		}

		for (int i = 1; i < attributes_.size(); i++) {
			att = attributes_.get(i);
			if (att == null)
				attS = "(null)";
			else
				attS = att.toString();

			value = values_.get(i);
			if (value == null)
				valueS = "(null)";
			else
				valueS = value.toString();
			resp.append(", " + attS + " = " + valueS);
		}
		resp.append("]");
		return resp.toString();
	}

};
