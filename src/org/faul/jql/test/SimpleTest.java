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
import java.util.List;

import org.faul.jql.core.Jql;

public class SimpleTest {
	
	public static void main(String [] args) {
		try {
			Jql jql = new Jql();
			List tables = (List)jql.execute("load db './jstests/jsonTest1.json';");
			System.out.println("Tables = " + tables);
			jql.execute("use db;");

			List t = (List)jql.execute("select * from xxx;");
			System.out.println(jql.prettyPrint(t));
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
}
