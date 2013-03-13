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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.faul.jql.utils.RemoteObjectHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class GenerateJson2 {
	static transient Gson gson = null;
	static transient GsonBuilder gx = new GsonBuilder();// The static json transient used to build envelopes
	static {
		gx.registerTypeAdapter(Object.class, new RemoteObjectHandler());
		gson = gx.setPrettyPrinting().create();
	}
	
	public static void main(String args[]) {
		Vector nums = new Vector();
		
		for (int i=0;i<10;i++) {
			Vector in = new Vector();
			for (int j=0;j<5;j++) {
				in.add(j*(i+1));
			}
			Map entry = new HashMap();
			entry.put("id", i);
			entry.put("data",in);
			nums.add(entry);
		}
		String data = gson.toJson(nums);
		System.out.println(data);
		
		nums = (Vector)gson.fromJson(data, Vector.class);
	}
}
