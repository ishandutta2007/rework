package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
			DataInstance instance = training.nextInstance(null, dim);
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
		DataInstance nextFirst = training.nextInstance(null, dim);
		assertFalse(first.head.equals(nextFirst.head));
		assertFalse(first.tail.equals(nextFirst.tail));
	}

	@Test
	public void testLogisticRegression() throws IOException {
		DecimalFormat formatter = new DecimalFormat("###.######");
		int dim = (int) Math.pow(2,16); 
		ExistenceModel existenceModel = new ExistenceModel(0.1, 0, dim);
		CostModel costModel = new CostModel(0.1, 0.01, dim);
		Stopwatch watch = new Stopwatch();
		List<FacebookModel> models = new ArrayList<FacebookModel>();
		models.add(existenceModel);
		models.add(costModel);

		/*
		final double steps[] = { 0.001, 0.01, 0.05 };
		final double lambdas[] = { 0.0, 0.002, 0.004, 0.006, 0.008, 0.010, 0.012, 0.014 };
		for (double lambda : lambdas) {
			for (double step : steps) {
				models.add(new ExistenceModel(step, lambda, dim));
				models.add(new CostModel(step, lambda, dim));
			}
		}
		*/

		DataInstance instance = null;
		while (training.hasNext()) {
			instance = training.nextInstance(instance, dim);
			if (!instance.isValid()) {
				continue;
			}
			for(FacebookModel model : models) {
				model.train(instance);
			}
		}
		logger.info("Time: " + watch.elapsedTime());
		for(FacebookModel model : models) {
			logger.info(model);
		}

		List<String> superNodesPath = new ArrayList<String>();
		superNodesPath.add("aabcdginorst aabceknoprsstw ehnorstu");
		superNodesPath.add("aachikknosvy ceeeghirsv eegrsy");
		ArrayList<Double> superNodesExistencePrediction = existenceModel.predict(superNodesPath);
		assertEqualsHelper("link between two super nodes (.99)", 0.6759955252618445, superNodesExistencePrediction.get(0), DELTA);
		ArrayList<Double> superNodesCostPrediction = costModel.predict(superNodesPath);
		assertEqualsHelper("cost of link between two super nodes (free)", 0.5000000000000003, superNodesCostPrediction.get(0), DELTA);

		List<String> missingPath = new ArrayList<String>();
		missingPath.add("missing");
		missingPath.add("does not exist");
		ArrayList<Double> missingPathPrediction = existenceModel.predict(missingPath);
		assertEqualsHelper("link between two non-existent nodes (0.0)", 0.5124973964842103, missingPathPrediction.get(0), DELTA);
		ArrayList<Double> missingPathCostPrediction = costModel.predict(missingPath);
		assertEqualsHelper("cost of link between two non-existent nodes (not free)", 0.5, missingPathCostPrediction.get(0), DELTA);

		// TODO test case for a link that is not free
		
		
		Writer testPathPredictions = new BufferedWriter(new FileWriter(
				"/Users/deflaux/rework/competitions/facebook2/data/testPathPredictions.txt"));

		while (testing.hasNext()) {
			ArrayList<Double> linkPredictions = existenceModel.predict(testing.nextInstance());
			// Multiply them all together
			Double pathPrediction = null;
			for (double linkPrediction : linkPredictions) {
				if (null == pathPrediction) {
					pathPrediction = linkPrediction;
				} else {
					pathPrediction = pathPrediction * linkPrediction;
				}
			}
			testPathPredictions.write(formatter.format(pathPrediction) + "\n");
		}
		testPathPredictions.close();
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
