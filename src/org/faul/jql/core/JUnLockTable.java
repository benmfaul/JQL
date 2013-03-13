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
import java.util.Map;

import org.gibello.zql.ZUnLockTable;


/**
 * Implements the extension to SQL "unlock tableName;"
 * @author Ben Faul
 *
 */
public class JUnLockTable extends JCommon {

/**
 * Unlocks tables held by the currently running thread.
 * @param jql Jql. The Jql database.
 * @param z JUnlockTable. The unlock SQL statement.
 * @throws Exception. Throws exceptions if table doesn't exist, if table not locked, or caller doesn't have the lock.
 */
	public JUnLockTable(Jql jql, ZUnLockTable z) throws Exception {
		List<String> tables = z.getTables();
		
		/////////
		String thread = Thread.currentThread().getName();
		
		List<String> list = jql.locks.get(thread);
		if (list == null || list.size()==0) {
			throw new Exception("Thread holds no table locks");
		}
		
		for (String s : tables) {
			if (!jql.tableLocked.contains(s)) {
				throw new Exception("Table " + s + " not locked.");
			}
			list.remove(s);
			jql.tableLocked.remove(s);
		}
	}
}
