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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.opentest4j.AssertionFailedError;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Extend me in order to test the serializability of a class. Override my
 * {@link #createInstance() createInstance} methods to provide me with an object
 * to test against. The object's class must implement
 * {@link java.io.Serializable Serializable}.
 * 
 */
@SuppressWarnings("javadoc")
public abstract class SerializabilityTestCase {
	private Serializable obj;

	/**
	 * Creates and returns an instance of the class under test.
	 * 
	 * @return a new instance of the class under test
	 * @throws Exception
	 */
	protected abstract Serializable createInstance() throws Exception;

	/**
	 * Sets up the test fixture.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		obj = createInstance();
		// We want these assertions to yield errors, not failures.
		try {
			assertNotNull(obj, "createInstance() returned null");
		} catch (AssertionFailedError ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	/**
	 * Verifies that an instance of the class under test can be serialized and
	 * deserialized without error.
	 */
	@Test
	public final void testSerializability() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
		byte[] frozenChunk = baos.toByteArray();
		baos.close();
		ByteArrayInputStream bais = new ByteArrayInputStream(frozenChunk);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Serializable thawed = (Serializable) ois.readObject();
		checkThawedObject(obj, thawed);
	}

	/**
	 * Template method--override this to perform checks on the deserialized form
	 * of the object serialized in {@link #testSerializability}. If not
	 * overridden, this asserts that the pre-serialization and deserialized
	 * forms of the object compare equal via
	 * {@link java.lang.Object#equals(Object) equals}.
	 * 
	 * @param expected
	 *            the pre-serialization form of the object
	 * @param actual
	 *            the deserialized form of the object
	 */
	protected void checkThawedObject(Serializable expected, Serializable actual)
			throws Exception {
		assertEquals(expected, actual, "thawed object comparison");
	}
}
