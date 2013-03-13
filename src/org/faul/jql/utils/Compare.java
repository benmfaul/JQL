
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
package org.faul.jql.utils;

import java.util.Comparator;
import java.util.Map;

/**
 * A class that works with the JAVA comparator for hashmap entries.
 * @author Ben M. Faul
 *
 */
public class Compare implements Comparator {
	String indexName = null;
	boolean ascending;

	/**
	 * Constructor for the compator
	 * @param indexName String. The key to the hashmap entries.
	 * @param ascending boolean. Set to true to set Comparable test a == b, otherwise, 
	 * to compare b == a set to false. No effect when a and b are both Maps
	 */
	public Compare(String indexName, boolean ascending) {
		this.indexName = indexName;
		this.ascending = ascending;
	}

	/**
	 * Compare object a with object b.
	 * @param a Object. The left side of the comparison for equality. 
	 * @param b Object. The right sode of the comaprison for equality.
	 * @return int. Returns -1 if a < b, 0 if a == b, and 1 if a > b.
	 */
	public int compare(Object a, Object b) {
		Object o1 = null, o2 = null;
		
		/**
		 * If a is a Mao, retrieve the value at indexName, else just use the object
		 */
		if (a instanceof Map) {
			o1 = ((Map)a).get(indexName);
		} else
			o1 = a;
		
		/**
		 * If b is a Mao, retrieve the value at indexName, else just use the object
		 */
		if (b instanceof Map) {
			o2 = ((Map)b).get(indexName);
		} else
			o2 = b;

		// Treat empty strains like nulls
		if (o1 instanceof String && ((String) o1).length() == 0) {
			o1 = null;
		}
		if (o2 instanceof String && ((String) o2).length() == 0) {
			o2 = null;
		}

		/**
		 * Process nulls.
		 */
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) {
			return 1;
		} else if (o2 == null) {
			return -1;
		} else if (o1 instanceof Comparable) {
			if (ascending) {
				return ((Comparable) o1).compareTo(o2);
			} else {
				return ((Comparable) o2).compareTo(o1);
			}
		} 
		return 0;
	}
}