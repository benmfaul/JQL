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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gibello.zql.ZConstant;
import org.gibello.zql.ZExpression;
import org.gibello.zql.ZFromItem;
import org.gibello.zql.data.ZEval;
import org.gibello.zql.data.ZTuple;

import com.google.gson.Gson;

/**
 * Base class for the JQL statement objects.
 * @author Ben Faul
 *
 */
public class JCommon {
	
	void setTableMap(Map<String, Map<String,Object>> dbMap, ZFromItem table) {
		if (table.getSchema() != null)
			tableMap = dbMap.get(table.getSchema());
		else
			tableMap = dbMap.get("default");
	}
	/**
	 * Converts a JSON embedded keystring in the form of 'a,b,c,d' into a_b_c_d_
	 * @param name String. The name of the embedded key.
	 * @return Underscored name of the embedded JSON.
	 */
	public static String compositeName(String name) {
		name=name.replaceAll("'","");
		return compositeName(name.split(","));
		
	}
	
	/**
	 * Given an array int the form args[]{a,b,c,d} convert to its underscored index.
	 * @param args String[]. The array of strings to convert to underscored form.
	 * @return String. The underscored version of the args.
	 */
	public static String  compositeName(String [] args) {
		if (args.length==1)
			return args[0];
		String s = "";
		for (String ss : args) {
			s += ss.trim() + "_";
		}
		return s;
	}
	
	/**
	 * Retrieve and evaluate the value of the JSON at a_b_c_d_ and assign it to the tuple at d_
	 * @param depth int. The current index into the JSON key of jsonArg.
	 * @param jsonArg String. The JSON key to use on retrieval.
	 * @param tuple ZTuple. The row to set
	 * @return String. The embedded JSON at the indicated  location.
	 */
	public static String evaluateEmbeddedJson(Integer depth, String jsonArg, ZTuple tuple) {
		jsonArg=jsonArg.replaceAll("'","");
		String [] args = jsonArg.split(",");
		Object object = tuple.getAttValue(args[0]);
		depth++;
		String cname = compositeName(args);
		object = evaluateRootJson(depth,args,object);

		tuple.setAtt(cname,object);
		return cname;
	}
	
	/**
	 * Evaluate embedded JSON, where the keys are in the form a_b_c_d_. (d of c of b of a).
	 * @param arg String. The keys of the embedded JSON
	 * @param tuple ZTuple. The current row.
	 * @return String. The JSON at d_.
	 * @throws Exception. Throws exceptions on JSON encoding errors.
	 */
	public static String  evaluateEmbeddedJson(String arg, ZTuple tuple) throws Exception  {
		String [] args = arg.split("_");
		Object object = tuple.getAttValue(args[0]);
		Integer depth = 1;
		String cname = compositeName(args);
		object = evaluateRootJson(depth,args,object);

		tuple.setAtt(cname,object);
		return cname;
	}
	
	/**
	 * Evaluates embedded JSON when the argument is a constant, and that constant is in the form a_b_c_.
	 * @param arg ZConstant. The value to be retrieved.
	 * @param tuple ZTuple. The current row being evaluated.
	 * @return String. The JSON string at that value of arg.
	 * @throws Exception
	 */
	public static String  evaluateEmbeddedJson(ZConstant arg, ZTuple tuple) throws Exception {
		if (arg.getType()!=ZConstant.STRING)
			throw new Exception("Expected constant not found");
		return evaluateEmbeddedJson(arg.toString(), tuple);
	}
	
	/**
	 * Recursively evaluate the value of embedded JSON. 
	 * @param depth int. The current index into args[].
	 * @param args String[]. The array of JSON keys. 
	 * @param value Object. The value of the JSON at args[args.length].
	 * @return Object. The value of the field a.b.c.d, if args[] = {a,b,c,d}.
	 */
	public static Object evaluateRootJson(Integer depth, String [] args, Object value) {
		if (value instanceof Map) {
			String attribute = args[depth];
			Map map = (Map)value;
			Object newValue = map.get(attribute); 
			depth++;
			if (depth+1==args.length)
				return newValue;
			else
				return evaluateRootJson(depth,args,newValue);
		}
		if (value instanceof ArrayList) {
			String attribute = args[depth];
			List list = (List)value;
			Object newValue = list.get(Integer.parseInt(attribute));
			depth++;
			if (depth+1==args.length)
				return newValue;
			else
				return evaluateRootJson(depth,args,newValue);
		}
		return value;
	}
	
	Gson gson = Jql.gson;		// use the same Gson object
	
	Map<String,Object> tableMap = null;		// the tableMap, maps Java objects to table names.
	
	/**
	 * Loads the source data and converts to an ArrayList of Maps (The 'database table').
	 * @param source Object. The source of data for the database.
	 * @return Object. The returned database table.
	 * @throws Exception. throws exception on File IO or Gson encoding errors.
	 */
	public Object loadData(Object source) throws Exception {
		if (source instanceof String) {
			String data = (String) source;
			if (data.startsWith("file://")) {
				data = data.substring(7);
				FileReader fr = new FileReader(data);
				source = loadFile(fr);
				return source;
			}
		}
		if (source instanceof ArrayList) {
			return source;
		}
		return  gson.fromJson((String) source, Object.class);
	}
	
	/**
	 * Load data from a file into a List of HashMaps.
	 * @param fr FileReader. The filereader to load the data from.
	 * @return List. The database table, a List of Maps.
	 * @throws Exception. throws exceptions on File IO and Gson encoding errors.
	 */
	List loadFile(FileReader fr) throws Exception {
		char[] buf = new char[4096];
		String data = null;
		int rc = fr.read(buf);
		fr.close();
		data = new String(buf, 0, rc);
		Object rets = gson.fromJson(data, Object.class);
		if (rets instanceof ArrayList == false) {
			throw new Exception("List class not returned!");
		}
		return (List) rets;
	}
	
	/**
	 * Given an SQL constant object, return its value. The value is encoded as string
	 * and needs to be converted to the proper object (String or Number) of the indicated type.
	 * @param zcon ZConstant. The SQL constant object.
	 * @return Object. The value represented by the object. 
	 */
	public Object returnValueFromConstant(ZConstant zcon) {
		Object oval = null;
		String sval = zcon.getValue();
		switch(zcon.getType()) {
		case ZConstant.NUMBER:
			if (sval.contains(".")) {
				Double d = Double.parseDouble(sval);
				oval = d;
			} else {
				Long l = Long.parseLong(sval);
				oval = l;
			}
			break;
		case ZConstant.STRING:
			oval = sval;
			sval=sval.toLowerCase();
			if (sval.startsWith("json::")) {
				sval = sval.substring(6);
				oval = gson.fromJson(sval, Object.class);
			}
			break;
		case ZConstant.NULL:
			oval = null;
			break;
		default:
			oval = sval;
		}
		return oval;
	}
	
	/**
	 * Sets the value in the indicated column (value into Map at key).
	 * @param map Map. The table 'row'
	 * @param column String. The column name to set in the row (the Map key).
	 * @param evaluator ZEval. Evaluates the right hand side of an assignment (e.g.30/2);
	 * @param tuple ZTuple. The current row to be evaluated.
	 * @param value Object. The value to set at the indicated column.
	 * @throws Exception
	 */
	public void setValueFromObject(Map map, String column, ZEval evaluator, ZTuple tuple, Object value)  throws Exception {
		if (evaluator == null)
			evaluator = new ZEval();
		if (value instanceof ZConstant) {
			ZConstant zcon = (ZConstant)value;
			map.put(column, returnValueFromConstant(zcon));
		} else if (value instanceof ZExpression) {
			ZExpression zval = (ZExpression)value;
			Object rets = evaluator.evalExpValue(tuple, zval);
			map.put(column, rets);
		}
	}
	
	/**
	 * Return the next index of the table.
	 * @param indexes Map<String,Double>. The index map.
	 * @param name String. The name of the table.
	 * @return double. Returns the next index to use for this table, or -1 if no index is on this table.
	 */
	public static double getNextIndex(Map<String,Double> indexes, String name) {  
		if (indexes == null)
			return -1;
		
		Double d  = indexes.get(name);
		if (d == null)
			return -1;
		double newD = d+1;
		indexes.put(name, newD);
		return d;
	}
}
