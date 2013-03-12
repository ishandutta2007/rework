package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class DataStreamTest {
	static final Logger logger = Logger.getLogger("DataStreamTest");

	static DataStream training;
	boolean printAssertions = false;
	
	@Before
	public void resetData() throws FileNotFoundException {
		// Creates a data stream from the trainingdata
		training = new DataStream(
				"/Users/deflaux/rework/competitions/facebook2/data/normTrain"
						+ DataStream.EPOCH_PLACEHOLDER + ".txt", false);
		DataInstance.clearEdgeHistory();
	}

	@Test
	public void testShuffledDataStream() throws FileNotFoundException {
		int dim = 2;
		DataInstance first = null;
		int numInvalid = 0;
		Set<String> nodes = new HashSet<String>();
		Set<String> freeEdges = new HashSet<String>();
		Set<String> paidEdges = new HashSet<String>();

		while (training.hasNext()) {
			DataInstance instance = training.nextInstance(null, dim);
			if (null == first) {
				first = instance;
			}
			if (!instance.isValid()) {
				numInvalid++;
			}
			nodes.add(instance.head);
			nodes.add(instance.tail);
			if(DataInstance.FREE_EDGE_COST == instance.cost) {
				freeEdges.add(instance.edgeKey);
			}
			else if(DataInstance.PAID_EDGE_COST == instance.cost) {
				paidEdges.add(instance.edgeKey);
			}
			else {
				fail("unexpected value for edge cost: " + instance.cost);
			}
		}
		assertEquals("num instances", 722588, training.counter);
		assertEquals("num invalid", 376, numInvalid);

		resetData();
		training.hasNext();
		DataInstance nextFirst = training.nextInstance(null, dim);
		assertFalse(first.head.equals(nextFirst.head));
		assertFalse(first.tail.equals(nextFirst.tail));
		
		assertEqualsHelper("number of unique nodes", 44015, nodes.size());
		assertEqualsHelper("number of unique FREE edges", 24716, freeEdges.size());
		assertEqualsHelper("number of unique PAID edges", 119418, paidEdges.size());

		Set<String> allEdges = new HashSet<String>(freeEdges);
		allEdges.addAll(paidEdges);

		Set<String> edgesWithACostChange = new HashSet<String>(freeEdges);
		edgesWithACostChange.retainAll(paidEdges);

		assertEqualsHelper("number of unique nodes", 44015, nodes.size());
		assertEqualsHelper("number of unique edges", 142191, allEdges.size());
		assertEqualsHelper("number of unique edges whose cost changed", 1943, edgesWithACostChange.size());
	}

	void assertEqualsHelper(String testCase, Object expected, Object actual) {
		if (true == printAssertions) {
			logger.info("Test case: " + testCase + ", expected: " + expected
					+ ", actual: " + actual);
		} else {
			assertEquals(testCase, expected, actual);
		}
	}
}
