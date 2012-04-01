package org.jpos.testhelpers;


import static org.junit.Assert.assertNotNull;

import org.junit.Assert;

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
		assertNotNull(message, equal1);
		assertNotNull(message, actual);
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
		assertNotNull(message, limit);
		assertNotNull(message, actual);
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
		assertNotNull(message, equal1);
		assertNotNull(message, actual);
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
		assertNotNull(message, expected);
		assertNotNull(message, actual);
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
		assertNotNull(message, less);
		assertNotNull(message, actual);
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
		assertNotNull(message, limit);
		assertNotNull(message, actual);
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
		Assert.fail(formatted + "expected greater than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failNotGreater(String message, Object limit,
			Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		Assert.fail(formatted + "expected not greater than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failLesser(String message, Object limit, Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		Assert.fail(formatted + "expected lesser than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failNotLesser(String message, Object limit,
			Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		Assert.fail(formatted + "expected not lesser than:<" + limit
				+ "> but was:<" + actual + ">");
	}

	static private void failNotEquals(String message, Object expected,
			Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		Assert.fail(formatted + "expected equals to:<" + expected
				+ "> but was:<" + actual + ">");
	}

	static private void failEquals(String message, Object expected) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		Assert.fail(formatted + "expected not equals to:<" + expected + ">");
	}
}
