package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class DataStreamTest {
	static final Logger logger = Logger.getLogger("DataStreamTest");

	static DataStream training;

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

		while (training.hasNext()) {
			DataInstance instance = training.nextInstance(null, dim);
			if (null == first) {
				first = instance;
			}
			if (!instance.isValid()) {
				numInvalid++;
			}
		}
		assertEquals("num instances", 722588, training.counter);
		assertEquals("num invalid", 376, numInvalid);

		resetData();
		training.hasNext();
		DataInstance nextFirst = training.nextInstance(null, dim);
		assertFalse(first.head.equals(nextFirst.head));
		assertFalse(first.tail.equals(nextFirst.tail));
	}
}
