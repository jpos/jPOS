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


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.opentest4j.AssertionFailedError;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Extend me in order to test a class's functional compliance with the
 * <code>equals</code> and <code>hashCode</code> contract.
 * <p>
 * Override my {@link #createInstance() createInstance} and
 * {@link #createNotEqualInstance() createNotEqualInstance} methods to provide
 * me with objects to test against. Both methods should return objects that are
 * of the same class.
 * <p>
 * <b>WARNING</b>: Extend me only if your class overrides <code>equals</code> to
 * test for equivalence. If your class's <code>equals</code> tests for identity
 * or preserves the behavior from <code>Object</code>, I'm not interested,
 * because I expect <code>createInstance</code> to return equivalent but
 * distinct objects.
 * 
 * @see java.lang.Object#equals(Object)
 * @see java.lang.Object#hashCode()
 */
@SuppressWarnings("javadoc")
public abstract class EqualsHashCodeTestCase {
	private Object eq1;
	private Object eq2;
	private Object eq3;
	private Object neq;
	private static final int NUM_ITERATIONS = 20;

	/**
	 * Creates and returns an instance of the class under test.
	 * 
	 * @return a new instance of the class under test; each object returned from
	 *         this method should compare equal to each other.
	 * @throws Exception
	 */
	protected abstract Object createInstance() throws Exception;

	/**
	 * Creates and returns an instance of the class under test.
	 * 
	 * @return a new instance of the class under test; each object returned from
	 *         this method should compare equal to each other, but not to the
	 *         objects returned from {@link #createInstance() createInstance}.
	 * @throws Exception
	 */
	protected abstract Object createNotEqualInstance() throws Exception;

	/**
	 * Sets up the test fixture.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		eq1 = createInstance();
		eq2 = createInstance();
		eq3 = createInstance();
		neq = createNotEqualInstance();
		// We want these assertions to yield errors, not failures.
		try {
			assertNotNull(eq1, "createInstance() returned null");
			assertNotNull(eq2, "2nd createInstance() returned null");
			assertNotNull(eq3, "3rd createInstance() returned null");
			assertNotNull(neq, "createNotEqualInstance() returned null");
			Assertions.assertNotSame(eq1, eq2);
			Assertions.assertNotSame(eq1, eq3);
			Assertions.assertNotSame(eq1, neq);
			Assertions.assertNotSame(eq2, eq3);
			Assertions.assertNotSame(eq2, neq);
			Assertions.assertNotSame(eq3, neq);
			assertEquals(eq1.getClass(), eq2.getClass(),
					"1st and 2nd equal instances of different classes");
			assertEquals(eq1.getClass(), eq3.getClass(),
					"1st and 3rd equal instances of different classes");
			assertEquals(eq1.getClass(), neq.getClass(),
					"1st equal instance and not-equal instance of different classes");
		} catch (AssertionFailedError ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	/**
	 * Tests whether <code>equals</code> holds up against a new
	 * <code>Object</code> (should always be <code>false</code>).
	 */
	@Test
	public final void testEqualsAgainstNewObject() {
		Object o = new Object();
		assertNotEquals(o, eq1);
		assertNotEquals(o, eq2);
		assertNotEquals(o, eq3);
		assertNotEquals(o, neq);
	}

	/**
	 * Asserts that two objects are not equal. Throws an
	 * <tt>AssertionFailedError</tt> if they are equal.
	 */
	public void assertNotEquals(Object expected, Object actual) {
		if (expected == null && actual == null
				|| expected != null && expected.equals(actual)) {
			fail("expected not equals to: <" + expected + ">");
		}
	}

	/**
	 * Tests whether <code>equals</code> holds up against <code>null</code>.
	 */
	@Test
	public final void testEqualsAgainstNull() {
		assertThat("null vs. 1st", null, not(equalTo(eq1)));
		assertThat("null vs. 2nd", null, not(equalTo(eq2)));
		assertThat("null vs. 3rd", null, not(equalTo(eq3)));
		assertThat("null vs. not-equal", null, not(equalTo(neq)));
	}

	/**
	 * Tests whether <code>equals</code> holds up against objects that should
	 * not compare equal.
	 */
	@Test
	public final void testEqualsAgainstUnequalObjects() {
		assertThat("1st vs. not-equal", eq1, not(equalTo(neq)));
		assertThat("2nd vs. not-equal", eq2, not(equalTo(neq)));
		assertThat("3rd vs. not-equal", eq3, not(equalTo(neq)));
		assertThat("not-equal vs. 1st", neq, not(equalTo(eq1)));
		assertThat("not-equal vs. 2nd", neq, not(equalTo(eq2)));
		assertThat("not-equal vs. 3rd", neq, not(equalTo(eq3)));
	}

	/**
	 * Tests whether <code>equals</code> is <em>consistent</em>.
	 */
	@Test
	public final void testEqualsIsConsistentAcrossInvocations() {
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			testEqualsAgainstNewObject();
			testEqualsAgainstNull();
			testEqualsAgainstUnequalObjects();
			testEqualsIsReflexive();
			testEqualsIsSymmetricAndTransitive();
		}
	}

	/**
	 * Tests whether <code>equals</code> is <em>reflexive</em>.
	 */
	@Test
	public final void testEqualsIsReflexive() {
		assertEquals(eq1, eq1, "1st equal instance");
		assertEquals(eq2, eq2, "2nd equal instance");
		assertEquals(eq3, eq3, "3rd equal instance");
		assertEquals(neq, neq, "not-equal instance");
	}

	/**
	 * Tests whether <code>equals</code> is <em>symmetric</em> and
	 * <em>transitive</em>.
	 */
	@Test
	public final void testEqualsIsSymmetricAndTransitive() {
		assertEquals(eq1, eq2, "1st vs. 2nd");
		assertEquals(eq2, eq1, "2nd vs. 1st");
		assertEquals(eq1, eq3, "1st vs. 3rd");
		assertEquals(eq3, eq1, "3rd vs. 1st");
		assertEquals(eq2, eq3, "2nd vs. 3rd");
		assertEquals(eq3, eq2, "3rd vs. 2nd");
	}

	/**
	 * Tests the <code>hashCode</code> contract.
	 */
	@Test
	public final void testHashCodeContract() {
		assertEquals(eq1.hashCode(), eq2.hashCode(), "1st vs. 2nd");
		assertEquals(eq1.hashCode(), eq3.hashCode(), "1st vs. 3rd");
		assertEquals(eq2.hashCode(), eq3.hashCode(), "2nd vs. 3rd");
	}

	/**
	 * Tests the consistency of <code>hashCode</code>.
	 */
	@Test
	public final void testHashCodeIsConsistentAcrossInvocations() {
		int eq1Hash = eq1.hashCode();
		int eq2Hash = eq2.hashCode();
		int eq3Hash = eq3.hashCode();
		int neqHash = neq.hashCode();
		for (int i = 0; i < NUM_ITERATIONS; ++i) {
			assertEquals(eq1Hash, eq1.hashCode(), "1st equal instance");
			assertEquals(eq2Hash, eq2.hashCode(), "2nd equal instance");
			assertEquals(eq3Hash, eq3.hashCode(), "3rd equal instance");
			assertEquals(neqHash, neq.hashCode(), "not-equal instance");
		}
	}
}
