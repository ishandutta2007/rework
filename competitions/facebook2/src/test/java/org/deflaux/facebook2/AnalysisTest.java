package org.deflaux.facebook2;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class AnalysisTest {
	static final double DELTA = 1e-6;
	static final Logger logger = Logger.getLogger("BasicAnalysisTest");
	static final boolean printAssertions = false;

	static DataStream training;
	static PredictionPaths testing;

	@BeforeClass
	public static void beforeClass() {
		BasicConfigurator.configure();
	}

	@Before
	public void resetData() throws FileNotFoundException {
		// Creates a data stream from the trainingdata
		training = new DataStream(
				"/Users/deflaux/rework/competitions/facebook2/data/normTrain"
						+ DataStream.EPOCH_PLACEHOLDER + ".txt", false);
		testing = new PredictionPaths(
				"/Users/deflaux/rework/competitions/facebook2/data/normTestPaths.txt");
	}

	@Ignore
	@Test
	public void testDataStream() throws FileNotFoundException {
		int dim = 2;
		DataInstance first = null;
		int numInvalid = 0;

		while (training.hasNext()) {
			DataInstance instance = training.nextInstance(dim);
			if (null == first) {
				first = instance;
			}
			if (!instance.isValid()) {
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
	public void testLogisticRegression() throws IOException {
		DecimalFormat formatter = new DecimalFormat("###.######");
		int dim = 32; // (int) Math.pow(2,16);
		double lambda = 0.01;
		double step = 0.01;
		Weights weights = LogisticRegression.train(training, dim, lambda, step);
		logger.info(weights);

		List<String> superPath = new ArrayList<String>();
		superPath.add("aabcdginorst aabceknoprsstw ehnorstu");
		superPath.add("aachikknosvy ceeeghirsv eegrsy");
		ArrayList<Double> superPrediction = LogisticRegression.predict(weights,
				superPath);
		assertEqualsHelper("link between two super nodes", 0.9998556414961637, superPrediction.get(0), DELTA);
		
		Writer foo = new BufferedWriter(new FileWriter(
				"/Users/deflaux/rework/competitions/facebook2/data/foo.txt"));

		while (testing.hasNext()) {
			ArrayList<Double> linkPredictions = LogisticRegression.predict(
					weights, testing.nextInstance());
			// Multiply them all together
			Double pathPrediction = null;
			for (double linkPrediction : linkPredictions) {
				if (null == pathPrediction) {
					pathPrediction = linkPrediction;
				} else {
					pathPrediction = pathPrediction * linkPrediction;
				}
			}
			foo.write(formatter.format(pathPrediction) + "\n");
		}
		foo.close();
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
