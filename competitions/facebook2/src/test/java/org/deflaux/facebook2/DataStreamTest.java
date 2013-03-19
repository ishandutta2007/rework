package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deflaux.facebook2.io.DataStream;
import org.deflaux.facebook2.io.PredictionPaths;
import org.junit.Before;
import org.junit.Test;

public class DataStreamTest {
	static final Logger logger = Logger.getLogger("DataStreamTest");

	static DataStream training;
	static PredictionPaths testing;

	static final boolean printAssertions = Boolean.parseBoolean(System.getProperty("printAssertions"));

	@Before
	public void resetData() throws FileNotFoundException {
		// Creates a data stream from the trainingdata
		training = new DataStream(
				"/Users/deflaux/rework/competitions/facebook2/data/normTrain"
						+ DataStream.EPOCH_PLACEHOLDER + ".txt", false);
		testing = new PredictionPaths(
				"/Users/deflaux/rework/competitions/facebook2/data/normTestPaths.txt");

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
			if (DataInstance.FREE_EDGE_COST == instance.cost) {
				freeEdges.add(instance.edgeKey);
			} else if (DataInstance.PAID_EDGE_COST == instance.cost) {
				paidEdges.add(instance.edgeKey);
			} else {
				fail("unexpected value for edge cost: " + instance.cost);
			}
		}
		assertEquals("num instances", 722588, training.getCounter());
		assertEqualsHelper("num invalid", 0, numInvalid);

		resetData();
		training.hasNext();
		DataInstance nextFirst = training.nextInstance(null, dim);
		assertFalse(first.head.equals(nextFirst.head));
		assertFalse(first.tail.equals(nextFirst.tail));

		assertEqualsHelper("number of unique training nodes", 22568,
				nodes.size());
		assertEqualsHelper("number of unique FREE training edges", 13937,
				freeEdges.size());
		assertEqualsHelper("number of unique PAID training edges", 55439,
				paidEdges.size());

		Set<String> trainingEdges = new HashSet<String>(freeEdges);
		trainingEdges.addAll(paidEdges);

		Set<String> edgesWithACostChange = new HashSet<String>(freeEdges);
		edgesWithACostChange.retainAll(paidEdges);

		assertEqualsHelper("number of unique training edges", 67612,
				trainingEdges.size());
		assertEqualsHelper(
				"number of unique training edges whose cost changed", 1764,
				edgesWithACostChange.size());

		Set<String> testNodes = new HashSet<String>();
		Set<String> testEdges = new HashSet<String>();
		int numTestPathsKnown = 0;
		while (testing.hasNext()) {
			List<String> path = testing.nextInstance();
			boolean pathKnown = true;
			testNodes.addAll(path);
			for (int tailIdx = 0; tailIdx < path.size(); tailIdx++) {
				String head = null;
				if (tailIdx == path.size() - 1) {
					// We're at the end of this list, time to stop
					break;
				} else {
					head = path.get(tailIdx + 1);
				}
				String tail = path.get(tailIdx);
				String line = tail + "|" + head;
				DataInstance instance = new DataInstance(line, 16, 2, false,
						false);
				testEdges.add(instance.edgeKey);
				if (!trainingEdges.contains(instance.edgeKey)) {
					pathKnown = false;
				}
			}
			if (pathKnown) {
				numTestPathsKnown++;
			}
		}
		
		assertEqualsHelper("number of unique test nodes", 9454,
				testNodes.size());
		assertEqualsHelper("number of unique test edges", 15491,
				testEdges.size());
		testNodes.removeAll(nodes);
		testEdges.removeAll(trainingEdges);
		assertEqualsHelper("number of unique test nodes not in training", 12,
				testNodes.size());
		assertEqualsHelper("number of unique test edges not in training",
				236, testEdges.size());
		assertEqualsHelper(
				"Out of 10,000 test paths, we have training data for",
				9672, numTestPathsKnown);
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
