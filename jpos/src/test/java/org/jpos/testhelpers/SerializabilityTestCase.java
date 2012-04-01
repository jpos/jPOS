package org.jpos.testhelpers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

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
	@Before
	public void setUp() throws Exception {
		obj = createInstance();
		// We want these assertions to yield errors, not failures.
		try {
			assertNotNull("createInstance() returned null", obj);
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
		assertEquals("thawed object comparison", expected, actual);
	}
}
