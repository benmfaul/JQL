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

import java.io.ByteArrayInputStream;

import java.io.FileReader;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faul.jql.utils.RemoteObjectHandler;
import org.gibello.zql.ZFromItem;

import org.gibello.zql.ZDelete;
import org.gibello.zql.ZDropTable;
import org.gibello.zql.ZInsert;
import org.gibello.zql.ZListTables;
import org.gibello.zql.ZLoadFile;
import org.gibello.zql.ZLockTable;
import org.gibello.zql.ZQuery;
import org.gibello.zql.ZSaveFile;
import org.gibello.zql.ZStatement;
import org.gibello.zql.ZTransactStmt;
import org.gibello.zql.ZUnLockTable;
import org.gibello.zql.ZUpdate;
import org.gibello.zql.ZUse;
import org.gibello.zql.ZqlParser;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Implements the JSON Query Language for JAVA. This object is heart of the system
 * all entry points into the JSON database are made through this object.
 * 
 * @author Ben Faul
 * 
 */
public class Jql {

	static transient Gson gson = null;
	static transient Gson gsonPretty = null;
	static transient GsonBuilder gx = new GsonBuilder();// The static json

	static Gson nopretty = new Gson();
	static String sepCSV = ",";

	// transient used to
	// Encoder/Decoder for Map<String,Object>
	static {
		gx.registerTypeAdapter(Object.class, new RemoteObjectHandler());
		gsonPretty = gx.setPrettyPrinting().create();
		gson = gx.create();
	}

	/**
	 * Return a linked hashmap object from the JSON formatted data string. The JSON data is stored as a List of Map objects.
	 * 
	 * @param data
	 *            String. The JSON data to convert.
	 * @return Object Map. The list of hashmaps that represents the object defined in the data parameter.
	 * @throws Exception. Throws
	 *             exception on badly formed JSON data.
	 */
	public static Map<String, Object> fromJson(String data) throws Exception {
		return (Map<String, Object>) gson.fromJson(data, Object.class);
	}

	/**
	 * Given an object File::/filename, ArrayList or JSON source, convert to a
	 * JSON database or a FileReader. When given a file name, the contents of the file is converted to JSON.
	 * If the object parameter is a list of objects, then this is converted into a JSOn database.
	 * If the object parameter is a string in JSON format, it will be used to create a JSON database.
	 * 
	 * @param source
	 *            . The source of the database.
	 * @return Object. The List of Hashmaps that represent the data formed by
	 * @throws Exception. Throws exceptions on i/o errors and badly formed JSON.
	 */
	public static Object jsqlConvert(Object source) throws Exception {
		if (source instanceof String) {
			String data = (String) source;
			if (data.startsWith("file://")) {
				data = data.substring(7);
				FileReader fr = new FileReader(data);
				return fr;
			} else {
				return gson.fromJson((String) source, Object.class);
			}
		}
		if (source instanceof ArrayList) {
			return source;
		}

		/**
		 * Convert to arraylist;
		 */
		String data = gson.toJson(source);
		return gson.fromJson(data, Object.class);
	}

	/**
	 * Convert an object of a linked hashmap into a JSON string. Pass the object to
	 * this method and will return a  the JSON formatted compressed string representation of that object.
	 * 
	 * @param convert
	 *            Object. The object to convert to a JSON string.
	 * @return String. The string representation of the Object.
	 * @throws Exception. Throws
	 *             exceptions if the Gson converter could not parse the input
	 *             string.
	 */
	public static String toJson(Object convert) throws Exception {
		return gson.toJson(convert);
	}

	/**
	 * Convert an object into a pretty printed JSON string. Pass the object to
	 * this method and will return a JSON formatted string representation of that object.
	 * 
	 * @param convert
	 *            Object. The object to convert to JSON string.
	 * @param pretty
	 *            boolean. Set to true to get pretty output.
	 * @return String. The returned object.
	 * @throws Exception. Throws exception on bad JSON.
	 *             exceptions on JSON errors.
	 */
	public static String toJson(Object convert, boolean pretty)
			throws Exception {
		if (pretty)
			return gson.toJson(convert);
		else {
			return nopretty.toJson(convert);
		}
	}

	transient Map<String, Map<String, Object>> dbMap = new HashMap<String, Map<String, Object>>();

	/**
	 * Add an empty table to the database.
	 * 
	 * @param name
	 *            String. The name of the empty table
	 */
	Map<String, Double> indexes;

	transient Object intermediateResult = null;

	transient String lastName = null;

	/**
	 * Lock information
	 */
	Map<String, List<String>> locks = new HashMap<String, List<String>>();

	transient ZqlParser parser = null;

	/** Reference to stored procedures in database */
	public JStoredProcedure sp = null; 

	List<String> tableLocked = new ArrayList<String>();

	transient Map<String, Object> tableMap = null;

	/**
	 * Empty constructor. An empty collection is created with name
	 * 'defaultTable'.
	 */
	public Jql() {

	}

	/**
	 * A FileReader constructor for the JQL. Pass it a database name, and the file reader
	 * for a data file of JSON data, the constructor instantiates the Jql object with the
	 * contents of that file.
	 * 
	 * @param dbName. String - the name to associiate with this database,
	 * @param source. FileReader - The filereader object to instantiate JQL from.
	 * @throws Exception. Throws exception on bad JSON.
	 */
	public Jql(String dbName, FileReader source) throws Exception {
		tableMap = (Map<String, Object>) jsqlConvert(source);
		dbMap.put(dbName, tableMap);
	}
	
	/**
	 * Constructor that creates a JQL parser from a Map. Pass a Map object to the constructor
	 * and a database is generated from it by the named database.
	 * 
	 * @param source
	 *            HashMap. The hashmap you wish to treat as a database.
	 * @param dbName. String - the name to associiate with this database,
	 */
	public Jql(String dbName, Map<String, Object> source) {
		tableMap = source;
		dbMap.put(dbName, tableMap);
	}

	/**
	 * Create a table from the JSON string. This constructor generates a Jql object from
	 * the JSON formatted string.
	 * 
	 * @param source
	 *            String. The JSON string to convert to a database.
	 * @throws Exception. Throws
	 *             an exception if the source could not be converted to a Map
	 *             object.
	 */
	public Jql(String dbName, String source) throws Exception {
		tableMap = (Map<String, Object>) jsqlConvert(source);
		dbMap.put(dbName, tableMap);
	}

	/**
	 * Add an empty database to the database mapping.
	 * 
	 * Creates a new hashmap to represent the new database, stored in the
	 * database map.
	 * 
	 * @param key String. The name of the empty database in memory.
	 */
	public void addDatabase(String key) {
		Map value = new HashMap();
		dbMap.put(key, value);
	}

	/**
	 * Add a record (Map) to the list. Using key/value pairs/.
	 * 
	 * @param list
	 *            List<Map>. A list of Map objects.
	 * @param args
	 *            Object[]. The varargs list of arguments in the form name,value
	 *            ...
	 */
	public void addRecord(List<Map> list, Object... args) {
		Map map = new HashMap();
		for (int i = 0; i < args.length; i += 2) {
			map.put(args[i], args[i + 1]);
		}
		list.add(map);
	}

	/**
	 * Add an object to a table. If it is a string it is converted to a Map.
	 * 
	 * @param tableName
	 *            String. The name of the table.
	 * @param obj
	 *            Object, A list object, or a JSON formatted List.
	 * @throws Exception. Throws
	 *             exceptions if obj is wrong type, or JSON decode error occurs.
	 */
	public void addRecord(String tableName, Object obj) throws Exception {
		List list = null;
		if (obj instanceof String) {
			list = gson.fromJson((String) obj, List.class);
		} else
			list = (List) obj;
		List table = (List) tableMap.get(tableName);
		table.add(list);
	}

	/**
	 * Adds a record to the table. of the named table using key/value pairs.
	 * 
	 * @param name
	 *            String. The name of the table.
	 * @param objects
	 *            Object... The name, value pairs of the Map to add to the
	 *            table.
	 * @throws Exception. Throws
	 *             exceptions if the number of objects is odd.
	 */
	public void addRecord(String name, Object... objects) throws Exception {
		Map row = new HashMap();

		if (objects.length % 2 != 0) {
			throw new Exception("Odd number of field:value pairs specified...");
		}
		for (int i = 0; i < objects.length; i += 2) {
			row.put((String) objects[i], objects[i + 1]);
		}
		if (tableMap.get(name) == null) {
			tableMap.put(name, new HashMap());
		}
		List table = (List) tableMap.get(name);
		table.add(row);
	}

	/**
	 * Programatically add am object to the named dataabase and table. 
	 * 
	 * @param dbName
	 *            String. The name of the database being edited.
	 * @param table
	 *            String. The name of the table to add the record to.
	 * @param obj
	 *            Object. A list or JSON representation of a list.
	 * @throws Exception. Throws
	 *             exceptions of Object is not String or List, or there are JSON
	 *             errors.
	 */
	public void addRecord(String dbName, String table, Object obj)
			throws Exception {

		Map<String, Object> db = dbMap.get(dbName);
		List list = null;
		if (obj instanceof String) {
			list = gson.fromJson((String) obj, List.class);
		}
		List records = (List) tableMap.get(table);
		records.add(list);
	}

	/**
	 * Adds a new table to the currently used database.
	 * @param name Strting. The name of the new table.
	 */
	public void addTable(String name) {
		List<Map> table = new ArrayList<Map>();
		tableMap.put(name, table);

		List<Map> schema = (List) tableMap.get("indexes");
		Map m = null;
		if (schema == null) {
			schema = new ArrayList<Map>();
			tableMap.put("indexes", schema);
			indexes = new HashMap();
			schema.add(indexes);
		}
		indexes.put(name, new Double(0));
	}

	/**
	 * Add an object List<Map> for use as a database table.
	 * 
	 * @param key
	 *            String. The name of the table.
	 * @param value
	 *            Object. The object to use as the database. 
	 */
	public void addTableMap(String key, Object value) throws Exception {
		String[] parts = key.split("\\.");
		String table = key;
		if (parts.length == 2) {
			tableMap = dbMap.get(parts[0]);
			if (tableMap == null) {
				tableMap = new HashMap();
				dbMap.put(parts[0], tableMap);
			}
			table = parts[1];
		}
		if (tableMap == null) {
			throw new Exception("No database was specified first.");
		}
		tableMap.put(table, value);
		dbMap.put("default", tableMap);
	}

	/**
	 * Set the stored procedure map to null. This will set the stored procedures reference to null.
	 * If you want to save the stored procedures to disk, don't call closeProcedure(), keep
	 * the stored procedures open, then write the database to disk.
	 */
	public void closeStoredProcedures() {
		sp = null;
	}

	/**
	 * Compile SQL statements into their JQL Statement primitives.
	 * 
	 * @param sql
	 *            String. The SQL statements to compile.
	 * @return List<ZStatement>. The executable statements.
	 * @throws Exception. Throws
	 *             exceptions on SQL errors.
	 */
	public List<ZStatement> compile(String sql) throws Exception {
		List<ZStatement> list = new ArrayList();

		if (sql.trim().endsWith(";") == false) {
			sql += ";";
			System.err
					.println("Warning, sql statement did not end in ';', terminator added automatically.");
		}
		byte[] bytes = sql.getBytes("UTF-8");
		InputStream input = new ByteArrayInputStream(bytes);
		parser = new ZqlParser(input);

		ZStatement st = null;
		Object arg = null;

		intermediateResult = null;
		try {
			while ((st = parser.readStatement()) != null) {
				list.add(st);
			}
		} catch (Exception error) {

		}
		return list;
	}

	/**
	 * Return a list of databases loaded in this instance of JQL.
	 * 
	 * @return List<String>. The list of databases loaded.
	 */
	public List<String> databasesLoaded() {
		List<String> list = new ArrayList<String>();
		Set keys = dbMap.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			list.add((String) it.next());
		}
		return list;
	}

	/**
	 * Perform a delete statement.
	 * 
	 * @param z
	 *            ZDelete. The delete statement compiled from the textual input
	 * @throws Exception
	 */
	void doDelete(ZDelete z) throws Exception {
		intermediateResult = new JDelete(dbMap, z,
				(ArrayList) intermediateResult).execute();
		tableMap.put("intermediate", intermediateResult);
	}

	/**
	 * Perform a SQL insert statement.
	 * 
	 * @param z
	 *            ZInsert. The insert compiled from a textual insert statement.
	 * @throws Exception. Throw
	 *             errors on JSON errors.
	 */
	void doInsert(ZInsert z) throws Exception {
		intermediateResult = new JInsert(tableMap, z, indexes).execute();
		tableMap.put("intermediate", intermediateResult);
	}

	/**
	 * Given an SQL statement, execute it.
	 * 
	 * @param st
	 *            ZStatement. The statement to execute, compiled from the
	 *            textual.
	 * @throws Exception. Throws
	 *             exceptions on JSON errors.
	 */
	void doJqlStatement(ZStatement st) throws Exception {
		if (st instanceof ZInsert) { // An SQL insert
			doInsert((ZInsert) st);
			return;
		} else if (st instanceof ZUpdate) {
			doUpdate((ZUpdate) st);
			return;
		} else if (st instanceof ZDelete) {
			doDelete((ZDelete) st);
			return;
		} else if (st instanceof ZQuery) { // An SQL query: query the DB
			doQuery((ZQuery) st);
			return;
		} else if (st instanceof ZDropTable) {
			dropTables((ZDropTable) st);
			return;
		} else if (st instanceof ZSaveFile) {
			if (sp != null)
				sp.stripShells(); // Don't save the js objects themselves.
			new JSaveFile(dbMap, (ZSaveFile) st);
			return;
		} else if (st instanceof ZUse) {
			doUse((ZUse) st);
			return;
		} else if (st instanceof ZLoadFile) {
			doLoad((ZLoadFile) st);
			return;
		}
		if (st instanceof ZTransactStmt) {
			doTransaction((ZTransactStmt) st);
			return;
		} else if (st instanceof ZLockTable) {
			new JLockTable(this, (ZLockTable) st);
			return;
		} else if (st instanceof ZUnLockTable) {
			new JUnLockTable(this, (ZUnLockTable) st);
			return;
		} else if (st instanceof ZListTables) {
			doListTables((ZListTables)st);
			return;
		}
	}

	/**
	 * Load the file into the Jql database.
	 * 
	 * @param z
	 *            ZLoadFile. The SQL load construct.
	 * @throws Exception. Throws
	 *             exceptions on I/O and JSON errors.
	 */
	void doLoad(ZLoadFile z) throws Exception {
		JLoad jl = new JLoad(dbMap, z);
		intermediateResult = jl.execute();
		String db = jl.getDbName();
		tableMap = dbMap.get(db);
	}

	/**
	 * Perform a select statement.
	 * 
	 * @param z
	 *            ZQuery. The query compiled from a textual select statement.
	 * @throws Exception. Throws
	 *             exceptions on JSON errors.
	 */
	void doQuery(ZQuery z) throws Exception {
		List list = (List)intermediateResult;
		if (list != null && list.size() > 0) {
			if (list.get(0) instanceof String) 
				lastName = (String)list.get(0);

		}
		if (intermediateResult == null) {

			List from = z.getFrom(); // FROM part of the query
			ZFromItem table = (ZFromItem) from.get(0);

			// Remember the last name used as it used for commit!
			lastName = table.getTable();

			if (table.getSchema() != null)
				tableMap = dbMap.get(table.getSchema());

			Object x = tableMap.get(lastName);
			intermediateResult = jsqlConvert(x);
		}

		if (intermediateResult == null) {
			throw new Exception("No table mapping found for: " + lastName);
		}

		intermediateResult = new JqlSelect(tableMap, z)
				.results(intermediateResult);
		// intermediateResult = queryJSON(z, intermediateResult);
		tableMap.put("intermediate", intermediateResult);
	}

	/**
	 * Perform a COMMIT, saves intermediate table over the last table name used.
	 * 
	 * @param z
	 */
	void doTransaction(ZTransactStmt z) {
		String str = z.getStatement();
		if (str.equalsIgnoreCase("COMMIT")) {
			tableMap.put(lastName, intermediateResult);
		}
	}

	/**
	 * Perform an SQL update statement.
	 * 
	 * @param z
	 *            ZUpdate. The compiled update statement from the textual update
	 *            statement.
	 * @throws Exception. Throws
	 *             exceptions on JSON errors.
	 */
	void doUpdate(ZUpdate z) throws Exception {
		intermediateResult = new JUpdate(dbMap, z,
				(ArrayList) intermediateResult).execute();
		tableMap.put("intermediate", intermediateResult);
	}

	/**
	 * Execute a USE statement.
	 * 
	 * @param z
	 *            ZUse. The USE construct.
	 * @return List<String>. The names of all the tables in the USEd database.
	 * @throws Exception. Throws
	 *             Exceptions if the database does not exist.
	 */
	List<String> doUse(ZUse z) throws Exception {
		String db = z.getDbName();
		tableMap = dbMap.get(db);
		if (tableMap == null) {
			throw new Exception("No such database named: " + db);
		}
		dbMap.put(db, tableMap);
		return tableList();
	}
	
	/**
	 * Links the parser production to listTables() method.
	 * @param z ZListTables. The object that represents the return from the listTables() method to the SQLproduction.
	 * @throws Exception
	 */
	void doListTables(ZListTables z) throws Exception {
		List theList = listTables();
		z.addTablesInfo(theList);
		intermediateResult = theList;
	}

	/**
	 * Drop a table.
	 * 
	 * @param drop
	 *            ZDropTable. The SQL construct of drop.
	 */
	public void dropTables(ZDropTable drop) {
		List<String> tables = drop.getTables();

		if (tables == null)
			return;

		for (String table : tables) {
			tableMap.remove(table);

			if (indexes != null)
				indexes.remove(table);
		}
	}

	/**
	 * Execute a list of precompiled statements, one after another.
	 * 
	 * @param list
	 *            List<ZStatement>. The list of precompiled statements to
	 *            execute.
	 * @return Object. The table resulting from executing the list.
	 * @throws Exception
	 */
	public Object execute(List<ZStatement> list) throws Exception {
		for (ZStatement st : list) {
			doJqlStatement(st);
		}
		return intermediateResult;
	}

	/**
	 * Execute the SQL statements in the string argument. This is where the classical
	 * SQL statements are executed, such as selct, delete, insert, etc.
	 * 
	 * @param sql
	 *            String. The SQL to execute, can be more than one. Each is
	 *            terminated with ';'.
	 * @return Object. The results of the SQL statements.
	 * @throws Exception, Throws errors on bad JSON format and SQL parse errors.
	 */
	public Object execute(String sql) throws Exception {
		return execute(sql, null);
	}

	/**
	 * Given a textual SQL statement, compile it ad then execute it.
	 * 
	 * @param sql
	 *            String. One or more SQL statements, each terminated with ';'.
	 * @param tableInfo
	 *            Map<String,Object>. The table name to JAVA object map.
	 * @return Object. The results of the statements as an intermediate table.
	 * @throws Exception. Throws
	 *             exception on SQL and JSON errors.
	 */

	public Object execute(String sql, Object tableInfo) throws Exception {

		byte[] bytes = sql.getBytes("UTF-8");
		InputStream input = new ByteArrayInputStream(bytes);
		if (parser == null)
			parser = new ZqlParser(input);
		else
			parser.initParser(input);

		ZStatement st = null;
		try {
			while ((st = parser.readStatement()) != null) {
				doJqlStatement(st);

			}
		} catch (Exception error) {
			if (error.toString().contains("<EOF") == false) {
				error.printStackTrace();
				throw new Exception(error.toString());
			}
		}

		return intermediateResult;
	}

	/**
	 * Extend the parser with these built-in functions: sum, min. max, count, and avg.
	 * 
	 * @param z
	 *            ZqlParser. The parser to extend.
	 */
	public void extendParser(ZqlParser z) {
		z.addCustomFunction("sum", 1);
		z.addCustomFunction("min", 1);
		z.addCustomFunction("max", 1);
		z.addCustomFunction("count", 1);
		z.addCustomFunction("avg", 1);

		z.addCustomFunction("jql", 1);
	}

	/**
	 * Format an object into a pretty printed JSON.
	 * 
	 * @param test
	 *            Object. The object to pretty print.
	 * @return String. The pretty printed format of the data to return.
	 */

	public String format(Object test) {
		String rets = "";

		if (test instanceof Map == false) {
			rets = gsonPretty.toJson(test);
			return rets;
		}
		List<Map> o = (List) test;
		int i = 0;
		for (Map map : o) {
			i++;
			String data = gsonPretty.toJson(map, HashMap.class);
			rets += data;
			if (i != o.size())
				rets += ",\n";
			else
				rets += "\n";
		}
		return rets;

	}

	/**
	 * Given a JSON string. pretty print it.
	 * 
	 * @param data
	 *            String. The unformatted JSON to pretty print.
	 * @return String. The formatted JSON.
	 */
	public String format(String data) {
		String rets = null;
		Object obj = tableMap.get(data);
		if (obj == null) {
			obj = gson.fromJson(data, Object.class);
		}
		rets = gsonPretty.toJson(obj);
		return rets;
	}

	/**
	 * Retrieve a CSV file, convert to Maps and append them to the given List.
	 * 
	 * @param list
	 *            List<Map>. The list to append the CSV file onto.
	 * @param fr
	 *            FileReader. The file reader of the CSV file.
	 * @throws Exception. Throws
	 *             exceptions on I/O errors
	 */
	public void fromCSV(List<Map> list, FileReader fr) throws Exception {
		String data = "";
		char buf[] = new char[4096];
		int rc = 1;
		while (rc > 0) {
			rc = fr.read(buf);
			data += new String(buf, 0, rc);
		}
		fromCSV(list, data);
	}

	/**
	 * Read CSV data from a string converts it to Maps and appends them  to the provided list.
	 * 
	 * @param list
	 *            List<Map>. The table to append the CSV data to.
	 * @param data
	 *            . String. The CSV formatted data.
	 */
	public void fromCSV(List<Map> list, String data) {
		String[] tokens = null;
		Map newmap = null;
		String[] lines = data.split("\n");
		tokens = lines[0].split(sepCSV + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		String[] keys = new String[tokens.length];
		String[] types = new String[tokens.length];
		int j = 0;
		for (String t : tokens) {
			setType(t, j, keys, types);
			j++;
		}

		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			newmap = new HashMap();
			tokens = line.split(sepCSV + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			j = 0;
			for (String t : tokens) {
				t = t.trim();
				if (types[j].equals("c"))
					newmap.put(keys[j++], t);
				else if (types[j].equals("n"))
					newmap.put(keys[j++], new Double(t));
				else {
					while (t.startsWith("\"")) {
						t = t.substring(1);
						t = t.substring(0, t.length() - 1);
					}
					Object test = gson.fromJson(t, Object.class);
					newmap.put(keys[j++], test);
				}
			}
			list.add(newmap);
		}
	}

	/**
	 * Retrieve a CSV file and create a table (List of Maps) from it.
	 * 
	 * @param table
	 *            String. The name of the table.
	 * @param fr
	 *            FileReader. The CSV file to convert.
	 * @throws Exception. Throws
	 *             exceptions on I/O errors.
	 */
	public void fromCSV(String table, FileReader fr) throws Exception {
		List<Map> list = (List) tableMap.get(table);
		fromCSV(list, fr);
	}

	/**
	 * Take CSV data from file and append it to the named table.
	 * 
	 * @param table
	 *            String. The table to append to.
	 * @param data
	 *            String. CSV formatted data.
	 */
	public void fromCSV(String table, String data) {
		List list = (List) tableMap.get(table);
		fromCSV(list, data);
	}

	/**
	 * return the List of tables of the given database name.
	 * 
	 * @param name
	 *            String. The name of the database.
	 * @return List<List>. The List of tables in the database.
	 */
	public List getDataBase(String name) {
		return (List) tableMap.get(name);
	}

	/**
	 * Return a List from the table map of the currently USEd database. This is
	 * equivalent to "select * from table;"
	 * 
	 * @param table
	 *            - String. The name of the table.
	 * @return List<Map>. The list that is this table, or null if table does not
	 *         exist.
	 */
	public List<Map> getTable(String table) {
		return (List) tableMap.get(table);
	}

	/**
	 * List the tables and their row counts.
	 * 
	 * @return List<Map>. A list of the tables and their record counts.
	 * @throws Exception. Will throw an exception on null Lists.
	 */
	public List<Map> listTables() throws Exception {
		List<Map> list = new ArrayList();
		Set set = tableMap.keySet();
		Iterator it = set.iterator();
		ArrayList<String> keys = new ArrayList<String>();
		while(it.hasNext()) {
			String key = (String)it.next();
			List records = (List)tableMap.get(key);
			Map nr = new HashMap();
			nr.put("name",key);
			nr.put("count",records.size());
			if (locked(key)) 
				nr.put("locked",true);
			else
				nr.put("locked",false);
			list.add(nr);
		}
		return list;
	}

	/**
	 * Load a previously saved database from disk.
	 * 
	 * @param dbname
	 *            String, The name of the database after loading it from file.
	 * @param filename
	 *            String. The name of the file to load.
	 * @return List<String>. The list of table namesin the database.
	 * @throws Exception. Throws
	 *             exceptions fileI/O and JSON parsing errors.
	 */
	public List<String> load(String dbname, String filename) throws Exception {
		JLoad jl = new JLoad(dbMap, dbname, filename);
		intermediateResult = jl.execute();
		String db = jl.getDbName();
		tableMap = dbMap.get(db);
		return jl.execute();
	}

	/**
	 * Determine if the named table is locked, and if so, is it owned by the current
	 * thread.
	 * 
	 * @param table
	 *            String. The name of the current table.
	 * @return boolean. Returns true if table is locked and current thread is
	 *         not owner, else returns false.
	 */
	public boolean locked(String table) {

		if (tableLocked.size() == 0)
			return false;

		String current = Thread.currentThread().getName();

		/**
		 * Is table even locked?
		 */
		if (tableLocked.contains(table) == false)
			return false;

		/**
		 * If its in the current threads list its not locked to caller.
		 */
		List<String> list = locks.get(current);
		if (list.contains(table))
			return false;

		// Lock exists and current thread doesn't have it.
		return true;
	}

	/**
	 * Creates a new stored procedure object for this JQL instance to use.
	 */
	public JStoredProcedure openStoredProcedures() {
		sp = new JStoredProcedure(this);
		return sp;
	}

	/**
	 * Alternate form of the format method.
	 * 
	 * @param test
	 *            - Object. The object to pretty format as JSON.
	 * @return String. The pretty formatted JSON, that is easy to read.
	 */
	public String prettyFormat(Object test) {
		String rets = "";

		if (test instanceof List == false) {
			rets = gsonPretty.toJson(test);
			return rets;
		}

		List<Map> o = (List) test;
		int i = 0;
		for (Map map : o) {
			i++;
			String data = gsonPretty.toJson(map, HashMap.class);
			rets += data;
			if (i != o.size())
				rets += ",\n";
			else
				rets += "\n";
		}
		return rets;

	}

	/**
	 * Alternate form of the pretty print JSON data.
	 * 
	 * @param data
	 *            String. The JSON string to format.
	 * @return String. The formatted JSON data.
	 */
	public String prettyFormat(String data) {
		String rets = null;
		Object obj = tableMap.get(data);
		if (obj == null) {
			obj = gson.fromJson(data, Object.class);
		}
		rets = gsonPretty.toJson(obj);
		return rets;
	}

	/**
	 * Alternate form of the pretty print JSON data.
	 * 
	 * @param data
	 *            Object. The object to format.
	 * @return String. The formatted JSON data.
	 */
	public String prettyPrint(Object data) {
		return prettyFormat(data);
	}

	/**
	 * Alternate form of the pretty print JSON data.
	 * 
	 * @param data
	 *            String. The JSON string to format.
	 * @return String. The formatted JSON data.
	 */
	public String prettyPrint(String data) {
		return prettyFormat(data);
	}

	/**
	 * Given a filename, read it into a string buffer.
	 * @param fileName. String. The name of the file to load.
	 * @return String. The contents of the file that was read.
	 * @throws Exception. Throws exception on I/O errors.
	 */
	public String readFile(String fileName) throws Exception {
		String data = "";
		FileReader fr = new FileReader(fileName);
		char[] buf = new char[4096];
		int i = 1;
		while (i > 0) {
			i = fr.read(buf);
			if (i > 0)
				data += new String(buf, 0, i);
		}
		return data;
	}

	/**
	 * Removes the index for the named table.
	 * 
	 * @param name
	 *            String. The name of the table to remove the indexes from.
	 */
	public void removeIndex(String name) {
		indexes.remove(name);
	}

	/**
	 * Removes a table from the database.
	 * 
	 * @param key
	 *            String. The name of the table to remove from the database..
	 */
	public void removeTable(String key) {
		tableMap.remove(key);
	}

	/**
	 * Sets the CSV separator string.
	 * 
	 * @param sep
	 *            String. The new CSV separator to use.
	 */
	public void setCSVSeparator(String sep) {
		sepCSV = sep;
	}

	/**
	 * TBD, not used yet.
	 * @param t
	 *            String. The token to decode to the
	 * @param j
	 *            int. The index of the ket/types.
	 * @param keys
	 *            String[]. The key/column name.
	 * @param types
	 *            String[]. The type of data.
	 */
	void setType(String t, int j, String[] keys, String[] types) {
		if (t.startsWith("N::")) {
			types[j] = "n";
			t = t.replace("N::", "");
		} else if (t.startsWith("JSON::")) {
			types[j] = "j";
			t = t.replace("JSON::", "");
		} else
			types[j] = "c";
		keys[j] = t;
	}

	/**
	 * String format the output of the arguments. Uses JAVA formatted output.
	 * 
	 * @param fmt
	 *            String. The format of the output.
	 * @param args
	 *            Object[]. The arguments to print.
	 * @return String. The formatted output.
	 */
	public String sformat(String fmt, Object... args) {
		String s = String.format(fmt, args);
		return s;
	}

	/**
	 * Determine if a table exists in the database.
	 * 
	 * @param name
	 *            String. The name of the table.
	 * @return boolean. Returns true if table exists, else returns false.
	 */
	public boolean tableExists(String name) {
		if (tableMap == null)
			return false;
		if (tableMap.get(name) == null)
			return false;
		else
			return true;
	}

	/**
	 * List the tables of the currently USEd database.
	 * 
	 * @return List<String>. The names of the tables of the currently used
	 *         database.
	 */
	public List<String> tableList() {
		List<String> list = new ArrayList<String>();
		Set keys = tableMap.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			list.add((String) it.next());
		}
		return list;
	}

	/**
	 * Convert the given list to a CSV file. Note, all columns are printed, included embedded ones.
	 * 
	 * @param list
	 *            List<Map>. The map table to turn into CSV formatted string.
	 * @return String. The CSV version of the table.
	 */
	public String toCSV(List<Map> list) {
		String buf = "";
		Map m = list.get(0);
		Set set = m.keySet();
		Iterator it = set.iterator();
		String[] keys = new String[m.size()];
		int j = 0;
		while (it.hasNext()) {
			keys[j++] = (String) it.next();
		}
		return toCSV(list, keys);
	}

	/**
	 * Convert the given list into a CSV file. Use this form to print only those
	 * columns you want, and in the order you want.
	 * 
	 * @param list
	 *            List<Map>. The table map to convert to CSV.
	 * @param tokens
	 *            String []. the column names to output.
	 * @return String. The formatted CSV of the given table.
	 */
	public String toCSV(List<Map> list, String... tokens) {
		String output = "";
		for (String token : tokens) {
			output += token + ",";
		}
		output = output.substring(0, output.length() - 1) + "\n";

		for (Map m : list) {
			String line = "";
			for (String arg : tokens) {
				Object o = m.get(arg);
				if (o instanceof String)
					line += "\"" + m.get(arg) + "\",";
				else if (o instanceof Double)
					line += m.get(arg) + ",";
				else {
					String conv = gson.toJson(o);
					conv = conv.replaceAll("\n", " ");
					line += "\"" + conv + "\",";
				}
			}
			line = line.substring(0, line.length() - 1) + "\n";
			output += line;
		}
		return output;
	}

	/**
	 * Convert the named table to CSV string, using the keys of the map as column
	 * names.
	 * 
	 * @param table
	 *            String. The name of the table to convert.
	 * @return String. The table's CSV representation.
	 */
	public String toCSV(String table) {
		List<Map> list = (List) tableMap.get(table);
		return toCSV(list);
	}

	/**
	 * Convert a table into CSV, using the column arguments provided.
	 * 
	 * @param table
	 *            String. The name of the table to convert.
	 * @param args
	 *            String[] args. The columns to convert. Unmentioned keys are ignored.
	 * @return String. The table converted to CSV as a string.
	 */
	public String toCSV(String table, String... args) {
		List<Map> list = (List) tableMap.get(table);
		return toCSV(list, args);
	}
	
	/**
	 *  Add a record to the table named in select.
	 * 
	 * @param select
	 *            String. The table to add to.
	 * @param args
	 *            Object... The name/value pairs to add to the table.
	 * @throws Exception. Throws
	 *             exceptions if table doesn't exist or JSON errors.
	 */
	public void updateRecord(String select, Object... args) throws Exception {
		int length = args.length;
		if (length % 2 != 0) {
			throw new Exception("Odd number of update predicates");
		}

		this.execute("select * from " + select + ";");

		List list = (List) intermediateResult;
		List results = (List) tableMap.get(lastName);

		for (Object o : list) {
			Map map = (Map) o;
			results.remove(map);

			for (int i = 0; i < length; i += 2) {
				map.put(args[i], args[i + 1]);
			}
			results.add(map);
		}
	}
	
	/**
	 * Method to USE a database previously loaded
	 * 
	 * @param dbName
	 *            String. The database name to use.
	 * @return List<String>. A list of tables found in the USEd database.
	 * @throws Exception. Throws
	 *             exceptions if the db name doesn't exist.
	 */
	public List<String> use(String dbName) throws Exception {
		tableMap = dbMap.get(dbName);

		List<Map> list = (List<Map>) tableMap.get("indexes");
		indexes = list.get(0);
		return tableList();
	}
}
