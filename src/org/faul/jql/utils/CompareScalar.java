
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

/**
 * A class that compares two scalars
 * @author Ben Faul. 
 *
 */
public class CompareScalar implements Comparator {

	/**
	 * Empty constructor.
	 */
	public CompareScalar() {
		
	}
	
	/**
	 * Compare two scalar objects (Use Compare.java to compare Maps).
	 * @param o1 Object. The left hand side of the equality test.
	 * @param o2 Object. The right hand side of the equality test.
	 * @return int. Returns -1 if a < b, 0 if a == n and 1 if a > 1
	 */
	public int compare(Object o1, Object o2) {

		// Treat empty strains like nulls
		if (o1 instanceof String && ((String) o1).length() == 0) {
			o1 = null;
		}
		if (o2 instanceof String && ((String) o2).length() == 0) {
			o2 = null;
		}

		// Sort nulls so they appear last, regardless
		// of sort order
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) {
			return 1;
		} else if (o2 == null) {
			return -1;
		} else if (o1 instanceof Comparable) {
			return ((Comparable) o1).compareTo(o2);
		} 
		return 0;
	}
}