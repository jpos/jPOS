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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import org.opentest4j.AssertionFailedError;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Extend me in order to test a class's functional compliance with the
 * {@link java.lang.Comparable Comparable} interface.
 * <p>
 * Override my {@link #createLessInstance() createLessInstance},
 * {@link #createEqualInstance() createEqualInstance}, and
 * {@link #createGreaterInstance() createGreaterInstance} methods to provide me
 * with objects to test against. These methods should return objects that are of
 * the same class.
 * 
 * @see java.lang.Comparable
 */
@SuppressWarnings("javadoc")
public abstract class ComparabilityTestCase<T extends Comparable<T>> {
	private T less;
	private T equal1;
	private T equal2;
	private T greater;

	/**
	 * Creates and returns an instance of the class under test.
	 * 
	 * @return a new instance of the class under test; each object returned from
	 *         this method should compare less than the objects returned from
	 *         {@link #createEqualInstance() createEqualInstance()}
	 * @throws Exception
	 */
	protected abstract T createLessInstance() throws Exception;

	/**
	 * Creates and returns an instance of the class under test.
	 * 
	 * @return a new instance of the class under test; each object returned from
	 *         this method should compare equal to each other
	 * @throws Exception
	 */
	protected abstract T createEqualInstance() throws Exception;

	/**
	 * Creates and returns an instance of the class under test.
	 * 
	 * @return a new instance of the class under test; each object returned from
	 *         this method should compare greater than the objects returned from
	 *         {@link #createEqualInstance() createEqualInstance()}
	 * @throws Exception
	 */
	protected abstract T createGreaterInstance() throws Exception;

	/**
	 * Sets up the test fixture.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		less = createLessInstance();
		equal1 = createEqualInstance();
		equal2 = createEqualInstance();
		greater = createGreaterInstance();
		// We want these assertions to yield errors, not failures.
		try {
			assertNotNull(less, "createLessInstance() returned null");
			assertNotNull(equal1, "createEqualInstance() returned null");
			assertNotNull(equal2, "2nd createEqualInstance() returned null");
			assertNotNull(greater, "createGreaterInstance() returned null");
			assertEquals(less.getClass(), equal1.getClass(),
					"less and equal1 of different classes");
			assertEquals(less.getClass(), equal2.getClass(),
					"less and equal2 of different classes");
			assertEquals(less.getClass(), greater.getClass(),
					"less and greater of different classes");
			checkForEquality(equal1, equal2);
		} catch (AssertionFailedError ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	/**
	 * Override as a no-op if you do not require that
	 * {@link #createEqualInstance() createEqualInstance()} return distinct but
	 * equivalent objects.
	 */
	protected void checkForEquality(T c1, T c2) {
		assertNotSame(c1, c2, "1st equal instance same as 2nd");
		assertEquals(equal1, equal2, "1st equal not equal to 2nd");
	}

	/**
	 * Tests whether <code>sgn(x.compareTo(y)) == -sgn(y.compareTo(x))</code>
	 * for all <code>x</code> and <code>y</code> given to this test.
	 */
	@Test
	public final void testForReverseSigns() {
		assertEquals(sgn(less.compareTo(equal1)),
				-sgn(equal1.compareTo(less)), "less vs. equal1");
		assertEquals(sgn(less.compareTo(equal2)),
				-sgn(equal2.compareTo(less)), "less vs. equal2");
		assertEquals(sgn(less.compareTo(greater)),
				-sgn(greater.compareTo(less)), "less vs. greater");
		assertEquals(sgn(equal1.compareTo(equal2)),
				-sgn(equal2.compareTo(equal1)), "equal1 vs. equal2");
		assertEquals(sgn(equal1.compareTo(greater)),
				-sgn(greater.compareTo(equal1)), "equal1 vs. greater");
		assertEquals(sgn(equal2.compareTo(greater)),
				-sgn(greater.compareTo(equal2)), "equal2 vs. greater");
	}

	/**
	 * Tests whether <code>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</code> for
	 * all <code>z</code> when <code>x.compareTo(y) == 0</code>.
	 */
	@Test
	public final void testForSameSigns() {
		assertEquals(sgn(equal1.compareTo(less)),
				sgn(equal2.compareTo(less)), "equal vs. less");
		assertEquals(sgn(equal1.compareTo(greater)),
				sgn(equal2.compareTo(greater)), "equal vs. greater");
	}

	/**
	 * Tests for sensible return values from the class under test's
	 * <code>compareTo</code> method. Doing so effectively tests the
	 * transitivity of <code>compareTo</code> also--
	 * <code>(x.compareTo(y)>0 && y.compareTo(z)>0)</code> implies
	 * <code>x.compareTo(z)>0</code>.
	 */
	@Test
	public final void testReturnValues() {
		ComparableAssert.assertLesser(equal1, less);
		ComparableAssert.assertLesser(equal2, less);
		ComparableAssert.assertGreater(less, greater);
		ComparableAssert.assertEquals(equal1, equal2);
		ComparableAssert.assertGreater(equal1, greater);
		ComparableAssert.assertGreater(equal2, greater);
	}

	// /**
	// * Tests whether <code>compareTo</code> throws a ClassCastException when
	// appropriate.
	// */
	// @Test
	// public final void testForClassCastException() throws Exception {
	// try {
	// less.compareTo(new Object());
	// } catch (ClassCastException success) {
	// return;
	// }
	// fail("should have thrown ClassCastException");
	// }

	private int sgn(int x) {
		return x == 0 ? 0 : x / Math.abs(x);
	}
}
