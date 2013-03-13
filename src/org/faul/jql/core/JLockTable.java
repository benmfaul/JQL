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

import java.util.ArrayList;
import java.util.List;

import org.gibello.zql.ZLockTable;

/**
 * Implements the extension to SQL "SAVE tableName fileName;"
 * @author Ben Faul
 *
 */
public class JLockTable extends JCommon {

	/**
	 * Saves a database table to disk.
	 * @param dbMap Map. The database mapping object for JAVA map.
	 * @param z ZSaveFile object.
	 * @throws Exception. Throws exceptions if table doesn't exist and File IO errors.
	 */
	public JLockTable(Jql jql, ZLockTable z) throws Exception {
		String thread = Thread.currentThread().getName();
		List<String>tables = z.getTables();
		String mode = z.getLockMode();
		
		List<String> list = jql.locks.get(thread);
		if (list == null) {
			list = new ArrayList<String>();
			jql.locks.put(thread,list);
		}
		
		for (String s : tables) {
			if (jql.locked(s))
				throw new Exception("Another thread already has the lock");
			if (jql.tableMap.get(s)==null) {
				throw new Exception("No such table " + s);
			}
			
			list.add(s);
			jql.tableLocked.add(s);
		}
	
	}
}
