/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.testhelpers;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A set of assert methods specially targeted to comparable objects.
 * 
 */
public class ComparableAssert {
	/**
	 * Don't let anyone have access to this constructor.
	 */
	private ComparableAssert() {
	}

	/**
	 * Asserts that the <tt>actual</tt> object is lesser than the <tt>limit</tt>
	 * object. Throws an <tt>AssertionFailedError</tt> if it is greater or
	 * equal.
	 */
	static public <T extends Comparable<T>> void assertLesser(String message,
			T equal1, T actual) {
		assertNotNull(equal1, message);
		assertNotNull(actual, message);
		if (equal1.compareTo(actual) <= 0) {
			failLesser(message, equal1, actual);
		}
	}

	/**
	 * Asserts that the <tt>actual</tt> object is lesser than the <tt>limit</tt>
	 * object. Throws an <tt>AssertionFailedError</tt> if it is greater or
	 * equal.
	 */
	static public <T extends Comparable<T>> void assertLesser(T equal1, T actual) {
		assertLesser(null, equal1, actual);
	}

	/**
	 * Asserts that the <tt>actual</tt> object is not lesser than the
	 * <tt>limit</tt> object. Throws an <tt>AssertionFailedError</tt> if it is
	 * lesser.
	 */
	static public <T extends Comparable<T>> void assertNotLesser(
			String message, T limit, T actual) {
		assertNotNull(limit, message);
		assertNotNull(actual, message);
		if (limit.compareTo(actual) > 0) {
			failNotLesser(message, limit, actual);
		}
	}

	/**
	 * Asserts that the <tt>actual</tt> object is not lesser than the
	 * <tt>limit</tt> object. Throws an <tt>AssertionFailedError</tt> if it is
	 * lesser.
	 */
	static public <T extends Comparable<T>> void assertNotLesser(T limit,
			T actual) {
		assertNotLesser(null, limit, actual);
	}

	/**
	 * Asserts that the <tt>expected</tt> and <tt>actual</tt> are equals
	 * (comparables). Throws an <tt>AssertionFailedError</tt> if it is lesser or
	 * equal.
	 */
	static public <T extends Comparable<T>> void assertEquals(String message,
			T equal1, T actual) {
		assertNotNull(equal1, message);
		assertNotNull(actual, message);
		if (equal1.compareTo(actual) != 0) {
			failNotEquals(message, equal1, actual);
		}
	}

	/**
	 * Asserts that the <tt>expected</tt> and <tt>actual</tt> are equals
	 * (comparables). Throws an <tt>AssertionFailedError</tt> if it is lesser or
	 * equal.
	 */
	static public <T extends Comparable<T>> void assertEquals(T equal1, T actual) {
		assertEquals(null, equal1, actual);
	}

	/**
	 * Asserts that the <tt>expected</tt> and <tt>actual</tt> are not equals
	 * (comparables). Throws an <tt>AssertionFailedError</tt> if it is lesser or
	 * equal.
	 */
	static public <T extends Comparable<T>> void assertNotEquals(
			String message, T expected, T actual) {
		assertNotNull(expected, message);
		assertNotNull(actual, message);
		if (expected.compareTo(actual) == 0) {
			failEquals(message, expected);
		}
	}

	/**
	 * Asserts that the <tt>expected</tt> and <tt>actual</tt> are not equals
	 * (comparables). Throws an <tt>AssertionFailedError</tt> if it is lesser or
	 * equal.
	 */
	static public <T extends Comparable<T>> void assertNotEquals(T expected,
			T actual) {
		assertNotEquals(null, expected, actual);
	}

	/**
	 * Asserts that the <tt>actual</tt> object is greater than the
	 * <tt>limit</tt> object. Throws an <tt>AssertionFailedError</tt> if it is
	 * lesser or equal.
	 */
	static public <T extends Comparable<T>> void assertGreater(String message,
			T less, T actual) {
		assertNotNull(less, message);
		assertNotNull(actual, message);
		if (less.compareTo(actual) >= 0) {
			failGreater(message, less, actual);
		}
	}

	/**
	 * Asserts that the <tt>actual</tt> object is greater than the
	 * <tt>limit</tt> object. Throws an <tt>AssertionFailedError</tt> if it is
	 * lesser or equal.
	 */
	static public <T extends Comparable<T>> void assertGreater(T less, T actual) {
		assertGreater(null, less, actual);
	}

	/**
	 * Asserts that the <tt>actual</tt> object is not greater than the
	 * <tt>limit</tt> object. Throws an <tt>AssertionFailedError</tt> if it is
	 * greater.
	 */
	static public <T extends Comparable<T>> void assertNotGreater(
			String message, T limit, T actual) {
		assertNotNull(limit, message);
		assertNotNull(actual, message);
		if (limit.compareTo(actual) < 0) {
			failNotGreater(message, limit, actual);
		}
	}

	/**
	 * Asserts that the <tt>actual</tt> object is not greater than the
	 * <tt>limit</tt> object. Throws an <tt>AssertionFailedError</tt> if it is
	 * greater.
	 */
	static public <T extends Comparable<T>> void assertNotGreater(T limit,
			T actual) {
		assertNotGreater(null, limit, actual);
	}

	static private void failGreater(String message, Object limit, Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected greater than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failNotGreater(String message, Object limit,
			Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected not greater than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failLesser(String message, Object limit, Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected lesser than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failNotLesser(String message, Object limit,
			Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected not lesser than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failNotEquals(String message, Object expected,
			Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected equals to:<" + expected
				+ "> but was:<" + actual + ">");
	}

	static private void failEquals(String message, Object expected) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected not equals to:<" + expected + ">");
	}
}
