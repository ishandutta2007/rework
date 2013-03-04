package org.deflaux.facebook2;

import static org.junit.Assert.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnalysisTest {
	static final double DELTA = 1e-15;
	static final Logger logger = Logger.getLogger("BasicAnalysisTest");
	static final boolean printAssertions = false;

	static DataStream training;
	static DataStream testing;

	@BeforeClass
	public static void beforeClass() {
		BasicConfigurator.configure();
	}

	@Before
	public void resetData() {
		// Creates a data stream from the trainingdata
		training = new DataStream(
				"/Users/deflaux/rework/competitions/facebook2/data/normTrain"
						+ DataStream.EPOCH_PLACEHOLDER + ".txt", false);
	}

	@Test
	public void testDataStream() {
		int dim = 2;
		DataInstance first = null;
		int numInvalid = 0;
		
		while (training.hasNext()) {
			DataInstance instance = training.nextInstance(dim);
			if (null == first) {
				first = instance;
			}
			if(!instance.isValid()) {
				numInvalid++;
			}
		}
		assertEqualsHelper("num instances", 722588, training.counter);
		assertEqualsHelper("num invalid", 378, numInvalid);

		resetData();
		training.hasNext();
		DataInstance nextFirst = training.nextInstance(dim);
		assertFalse(first.head.equals(nextFirst.head));
		assertFalse(first.tail.equals(nextFirst.tail));
	}
	
	@Test 
	public void testLogisticRegression() {
		int dim = 32; //(int) Math.pow(2,16);
		double lambda = 0.01;
		double step = 0.01;
		Weights weights = LogisticRegression.train(training, dim, lambda, step);
		logger.info(weights);
		
	}
	
	void assertEqualsHelper(String testCase, Double expected, Double actual,
			Double delta) {
		if (true == printAssertions) {
			logger.info("Test case: " + testCase + ", expected: " + expected
					+ ", actual: " + actual);
		} else {
			assertEquals(testCase, expected, actual, delta);
		}
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
