package org.deflaux.facebook2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DataInstanceTest {
	String anotherEdgeLine = "hello|world|0";
	String edgeLine = "foo|bar|";
	String freeEdgeLine = edgeLine + "0"; // its zero in the raw data
	String paidEdgeLine = edgeLine + "1"; // its one in the raw data

	@Before
	public void setup() {
		DataInstance.clearEdgeHistory();
	}

	@Test
	public void testEdgeHistory() {
		// Establish some history
		DataInstance instance = new DataInstance(freeEdgeLine, 1, 2, true, true);
		assertArrayEquals(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 },
				instance.getEdgeExistenceHistory());
		instance = new DataInstance(anotherEdgeLine, 2, 2, true, true);
		instance = new DataInstance(freeEdgeLine, 3, 2, true, true);
		assertArrayEquals(new int[] { 0, 0, 1, 0, 0, 0, 0, 0 },
				instance.getEdgeExistenceHistory());
		instance = new DataInstance(freeEdgeLine, 4, 2, true, true);
		assertArrayEquals(new int[] { 0, 1, 0, 1, 0, 0, 0, 0 },
				instance.getEdgeExistenceHistory());
		instance = new DataInstance(anotherEdgeLine, 5, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 6, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 7, 2, true, true);
		instance = new DataInstance(freeEdgeLine, 8, 2, true, true);
		assertArrayEquals(new int[] { 0, 0, 0, 0, 1, 1, 0, 1 },
				instance.getEdgeExistenceHistory());
		instance = new DataInstance(freeEdgeLine, 9, 2, true, true);
		assertArrayEquals(new int[] { 0, 1, 0, 0, 0, 1, 1, 0 },
				instance.getEdgeExistenceHistory());
	}

	@Test
	public void testCostHistoryBackfill() {
		// Establish some history
		DataInstance instance = new DataInstance(freeEdgeLine, 1, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 2, 2, true, true);
		instance = new DataInstance(freeEdgeLine, 3, 2, true, true);
		instance = new DataInstance(freeEdgeLine, 4, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 5, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 6, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 7, 2, true, true);
		instance = new DataInstance(freeEdgeLine, 8, 2, true, true);
		instance = new DataInstance(freeEdgeLine, 9, 2, true, true);

		assertArrayEquals(new int[] { 0, 1, 0, 0, 0, 1, 1, 0 },
				instance.getEdgeExistenceHistory());
		// Raw cost history
		assertArrayEquals(new int[] { 0, 1, 0, 0, 0, 1, 1, 0 },
				instance.getEdgeHistory(DataInstance.edgeCostHistory, DataInstance.FREE_EDGE_COST));
		// Filled in cost history
		assertArrayEquals(new int[] { 0, 1, 1, 1, 1, 1, 1, 0 }, instance.getEdgeCostHistory());
	}

	@Test
	public void testCostHistoryBackfillFreeToPaid() {
		// Establish some history
		DataInstance instance = new DataInstance(freeEdgeLine, 1, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 2, 2, true, true);
		instance = new DataInstance(freeEdgeLine, 3, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 4, 2, true, true);
		instance = new DataInstance(paidEdgeLine, 4, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 5, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 6, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 7, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 8, 2, true, true);
		instance = new DataInstance(paidEdgeLine, 8, 2, true, true);
		instance = new DataInstance(anotherEdgeLine, 9, 2, true, true);
		instance = new DataInstance(paidEdgeLine, 9, 2, true, true);

		assertArrayEquals(new int[] { 0, 1, 0, 0, 0, 1, 1, 0 },
				instance.getEdgeExistenceHistory());
		// Raw cost history
		assertArrayEquals(new int[] { 0, 0, 0, 0, 0, 0, 1, 0 },
				instance.getEdgeHistory(DataInstance.edgeCostHistory, DataInstance.FREE_EDGE_COST));
		// Filled in cost history
		assertArrayEquals(new int[] { 0, 0, 0, 0, 0, 0, 1, 0 }, instance.getEdgeCostHistory());
	}

}
