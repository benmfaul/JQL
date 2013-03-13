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
package org.faul.jql.test;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.faul.jql.utils.RemoteObjectHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GenerateJson1 {
	static transient Gson gson = null;
	static transient GsonBuilder gx = new GsonBuilder();// The static json transient used to build envelopes
	static {
		gx.registerTypeAdapter(Object.class, new RemoteObjectHandler());
		gson = gx.setPrettyPrinting().create();
	}
	
	transient List nums = new Vector();
	transient String data = null;
	transient HashMap dataHash = null;
	
	public GenerateJson1() {
		
	}
	
	public static void main(String args[]) throws Exception {
		GenerateJson1 test = new GenerateJson1();
		
		test.createFile("jsonTest1.json");
		test.loadFile("jsonTest1.json");
		System.out.println(test.nums);
	}
	
	public String createFile(String fileName) throws Exception {
		
		for (int i=0;i<10;i++) {
			int value = (i * 1000);
			Map entry = new HashMap();
			entry.put("id", i);
			entry.put("data",value);
			nums.add(entry);
		}
		data = gson.toJson(nums);
		
		FileWriter fw = new FileWriter(fileName);
		fw.write(data,0,data.length());
		fw.close();
		
		return data;
		// select * from nums;
		// select * from nums where id=9;
		// select * from nums where id >5 AND id < 8;
		// insert 
	}
	
	public String loadFile(String fileName) throws Exception {
		char [] buf = new char[4096];
		FileReader fr = new FileReader(fileName);
		int rc = fr.read(buf);
		fr.close();
		data = new String(buf,0,rc);
		Object rets = gson.fromJson(data, Object.class);
		if (rets instanceof ArrayList == false) {
			throw new Exception("Vector class not returned!");
		}
		nums = (List)rets;
		return data;
	}
}
