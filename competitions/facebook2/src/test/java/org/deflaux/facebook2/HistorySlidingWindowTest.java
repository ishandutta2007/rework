package org.deflaux.facebook2;

import static org.junit.Assert.*;

import org.deflaux.ml.HistorySlidingWindow;
import org.junit.BeforeClass;
import org.junit.Test;

public class HistorySlidingWindowTest {
	static HistorySlidingWindow history;
	
	@BeforeClass
	public static void setup() {
		history = new HistorySlidingWindow(5,1024,3);		
	}
	
	@Test
	public void testHappyCase() {
		
		history.recordHistory("one", 1);

		assertFalse(history.viewHistory("neg one", -1));
		assertFalse(history.viewHistory("zero", 0));
		
		history.recordHistory("two", 2);
		history.recordHistory("twoB", 2);
		history.recordHistory("three", 3);
		history.recordHistory("threeB", 3);
		history.recordHistory("four", 4);
		history.recordHistory("five", 5);
		
		assertTrue(history.viewHistory("one", 1));
		assertFalse(history.viewHistory("one", 2));
		assertFalse(history.viewHistory("one", 3));
		assertFalse(history.viewHistory("one", 4));
		assertFalse(history.viewHistory("one", 5));

		assertFalse(history.viewHistory("two", 1));
		assertTrue(history.viewHistory("two", 2));
		assertTrue(history.viewHistory("twoB", 2));
		assertFalse(history.viewHistory("two", 3));
		assertFalse(history.viewHistory("two", 4));
		assertFalse(history.viewHistory("two", 5));
		
		assertFalse(history.viewHistory("three", 1));
		assertFalse(history.viewHistory("three", 2));
		assertTrue(history.viewHistory("three", 3));
		assertTrue(history.viewHistory("threeB", 3));
		assertFalse(history.viewHistory("three", 4));
		assertFalse(history.viewHistory("three", 5));

		assertFalse(history.viewHistory("four", 1));
		assertFalse(history.viewHistory("four", 2));
		assertFalse(history.viewHistory("four", 3));
		assertTrue(history.viewHistory("four", 4));
		assertFalse(history.viewHistory("four", 5));

		assertFalse(history.viewHistory("five", 1));
		assertFalse(history.viewHistory("five", 2));
		assertFalse(history.viewHistory("five", 3));
		assertFalse(history.viewHistory("five", 4));
		assertTrue(history.viewHistory("five", 5));

		history.recordHistory("six", 6);

		assertTrue(history.viewHistory("two", 2));
		assertTrue(history.viewHistory("twoB", 2));
		assertFalse(history.viewHistory("two", 3));
		assertFalse(history.viewHistory("two", 4));
		assertFalse(history.viewHistory("two", 5));
		assertFalse(history.viewHistory("two", 6));
		
		assertFalse(history.viewHistory("three", 2));
		assertTrue(history.viewHistory("three", 3));
		assertTrue(history.viewHistory("threeB", 3));
		assertFalse(history.viewHistory("three", 4));
		assertFalse(history.viewHistory("three", 5));
		assertFalse(history.viewHistory("three", 6));

		assertFalse(history.viewHistory("four", 2));
		assertFalse(history.viewHistory("four", 3));
		assertTrue(history.viewHistory("four", 4));
		assertFalse(history.viewHistory("four", 5));
		assertFalse(history.viewHistory("four", 6));

		assertFalse(history.viewHistory("five", 2));
		assertFalse(history.viewHistory("five", 3));
		assertFalse(history.viewHistory("five", 4));
		assertTrue(history.viewHistory("five", 5));
		assertFalse(history.viewHistory("five", 6));

		assertFalse(history.viewHistory("six", 2));
		assertFalse(history.viewHistory("six", 3));
		assertFalse(history.viewHistory("six", 4));
		assertFalse(history.viewHistory("six", 5));
		assertTrue(history.viewHistory("six", 6));

		history.recordHistory("seven", 7);

		assertTrue(history.viewHistory("three", 3));
		assertTrue(history.viewHistory("threeB", 3));
		assertFalse(history.viewHistory("three", 4));
		assertFalse(history.viewHistory("three", 5));
		assertFalse(history.viewHistory("three", 6));
		assertFalse(history.viewHistory("three", 7));

		assertFalse(history.viewHistory("four", 3));
		assertTrue(history.viewHistory("four", 4));
		assertFalse(history.viewHistory("four", 5));
		assertFalse(history.viewHistory("four", 6));
		assertFalse(history.viewHistory("four", 7));

		assertFalse(history.viewHistory("five", 3));
		assertFalse(history.viewHistory("five", 4));
		assertTrue(history.viewHistory("five", 5));
		assertFalse(history.viewHistory("five", 6));
		assertFalse(history.viewHistory("five", 7));

		assertFalse(history.viewHistory("six", 3));
		assertFalse(history.viewHistory("six", 4));
		assertFalse(history.viewHistory("six", 5));
		assertTrue(history.viewHistory("six", 6));
		assertFalse(history.viewHistory("six", 7));

		assertFalse(history.viewHistory("seven", 3));
		assertFalse(history.viewHistory("seven", 4));
		assertFalse(history.viewHistory("seven", 5));
		assertFalse(history.viewHistory("seven", 6));
		assertTrue(history.viewHistory("seven", 7));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRecordOlderThanWindow() {
		history.recordHistory("twoC", 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRecordMuchOlderThanWindow() {
		history.recordHistory("oneB", 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRecordNewerThanWindow() {
		history.recordHistory("nine", 9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRecordMuchNewerThanWindow() {
		history.recordHistory("ten", 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testViewOlderThanWindow() {
		history.viewHistory("twoC", 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testViewMuchOlderThanWindow() {
		history.viewHistory("oneB", 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testViewNewerThanWindow() {
		history.viewHistory("nine", 9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testViewMuchNewerThanWindow() {
		history.viewHistory("ten", 10);
	}
}
