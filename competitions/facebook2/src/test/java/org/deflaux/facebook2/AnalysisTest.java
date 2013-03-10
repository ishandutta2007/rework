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

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnalysisTest {
	static final double DELTA = 1e-6;
	static final Logger logger = Logger.getLogger("AnalysisTest");
	static final boolean printAssertions = false;

	static DataStream training;
	static PredictionPaths testing;

	@BeforeClass
	public static void beforeClass() {
		// This is no longer needed since apparently Hadoop has already
		// configured log4j
		// BasicConfigurator.configure();
	}

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
		assertEqualsHelper("num invalid", 376, numInvalid);

		resetData();
		training.hasNext();
		DataInstance nextFirst = training.nextInstance(null, dim);
		assertFalse(first.head.equals(nextFirst.head));
		assertFalse(first.tail.equals(nextFirst.tail));
	}

	@Test
	public void testLogisticRegression() throws IOException {
		// Turn off data stream shuffling so that our test results are
		// reproducible
		training.setShuffle(false);

		DecimalFormat formatter = new DecimalFormat("###.######");
		int historyWindowSize = 8;
		int numDimensions = (int) Math.pow(2, 16);

		DataInstance.setHistoryWindowSize(historyWindowSize);
		ExistenceModel existenceModel = new ExistenceModel(0.1, 0,
				historyWindowSize, numDimensions);
		CostModel costModel = new CostModel(0.1, 0, historyWindowSize,
				numDimensions);

		List<FacebookModel> models = new ArrayList<FacebookModel>();
		models.add(existenceModel);
		models.add(costModel);

		/*
		 * final double steps[] = { 0.001, 0.01, 0.05 }; final double lambdas[]
		 * = { 0.0, 0.002, 0.004, 0.006, 0.008, 0.010, 0.012, 0.014 }; for
		 * (double lambda : lambdas) { for (double step : steps) {
		 * models.add(new ExistenceModel(step, lambda, numDimensions));
		 * models.add(new CostModel(step, lambda, numDimensions)); } }
		 */

		int numInvalid = 0;
		DataInstance instance = null;
		Stopwatch watch = new Stopwatch();
		while (training.hasNext()) {
			instance = training.nextInstance(instance, numDimensions);
			if (!instance.isValid()) {
				numInvalid++;
				continue;
			}
			for (FacebookModel model : models) {
				model.train(instance);
			}
		}
		logger.info("Time: " + watch.elapsedTime());
		for (FacebookModel model : models) {
			logger.info(model);
		}
		assertEqualsHelper("num invalid", 376, numInvalid);

		assertEqualsHelper("existence average loss for epoch 15",
				0.01409898059848734, existenceModel.errorMetricsPerEpoch
						.get(14).getAverageLoss());
		assertEqualsHelper("cost average loss for epoch 15",
				0.04013893456099967, costModel.errorMetricsPerEpoch.get(14)
						.getAverageLoss());
		assertEqualsHelper("existence f score for epoch 15",
				0.9929004615734895, existenceModel.errorMetricsPerEpoch.get(14)
						.getFScore());
		assertEqualsHelper("cost f score for epoch 15", 0.8655421686746988,
				costModel.errorMetricsPerEpoch.get(14).getFScore());

		Writer testPathPredictions = new BufferedWriter(
				new FileWriter(
						"/Users/deflaux/rework/competitions/facebook2/data/testPathPredictions.txt"));

		// Normalization bugs:
		// TODO why is link 'NIC' normalized to the empty string?
		// TODO one path has Ltd normalized to the empty string
		// TODO output something else for empty
		// once all this is fixed (1) check num invalid again (2) take question
		// mark out of predict

		// TODO predictions for epochs 16 - 20
		while (testing.hasNext()) {
			List<String> path = testing.nextInstance();
			ArrayList<Double> linkPredictions = existenceModel
					.predict(path, 16);
			ArrayList<Double> costPredictions = costModel.predict(path, 16);
			// Multiply them all together
			Double pathPrediction = null;
			for (int i = 0; i < linkPredictions.size(); i++) {
				if (null == pathPrediction) {
					pathPrediction = linkPredictions.get(i)
							* costPredictions.get(i);
				} else {
					pathPrediction = pathPrediction
							* (linkPredictions.get(i) * costPredictions.get(i));
				}
			}
			testPathPredictions.write(formatter.format(pathPrediction) + "\n");
		}
		testPathPredictions.close();

		// Test case for a path between two supernodes that is free
		// 'aabcdginorst aabceknoprsstw ehnorstu|aachikknosvy ceeeghirsv
		// eegrsy|0'
		List<String> superNodesPath = new ArrayList<String>();
		superNodesPath.add("aabcdginorst aabceknoprsstw ehnorstu");
		superNodesPath.add("aachikknosvy ceeeghirsv eegrsy");
		List<String> missingPath = new ArrayList<String>();
		missingPath.add("missing");
		missingPath.add("does not exist");
		// Test case for a link that is not free 'aabclnopt aadt
		// ceenrst|aachikknosvy ceeeghirsv eegrsy|1'
		List<String> notFreePath = new ArrayList<String>();
		notFreePath.add("aabclnopt aadt ceenrst");
		notFreePath.add("aachikknosvy ceeeghirsv eegrsy");
		// Test case for self edge
		List<String> selfEdgePath = new ArrayList<String>();
		selfEdgePath.add("aabclnopt aadt ceenrst");

		ArrayList<Double> superNodesExistencePrediction = existenceModel
				.predict(superNodesPath, 16);
		assertEqualsHelper("link between two super nodes (.99)",
				0.6759955252618445, superNodesExistencePrediction.get(0), DELTA);
		ArrayList<Double> missingPathPrediction = existenceModel.predict(
				missingPath, 16);
		assertEqualsHelper("link between two non-existent nodes (0.0)",
				0.5124973964842103, missingPathPrediction.get(0), DELTA);
		ArrayList<Double> notFreePathPrediction = existenceModel.predict(
				notFreePath, 16);
		assertEqualsHelper("paid link between two nodes (.99)",
				0.6964685022527203, notFreePathPrediction.get(0), DELTA);
		ArrayList<Double> selfEdgePathPrediction = existenceModel.predict(
				selfEdgePath, 16);
		assertEqualsHelper("self edge (1.0)", 1.0,
				selfEdgePathPrediction.get(0), DELTA);

		ArrayList<Double> superNodesCostPrediction = costModel.predict(
				superNodesPath, 16);
		assertEqualsHelper("cost of link between two super nodes (free)",
				0.9894274398534469, superNodesCostPrediction.get(0), DELTA);
		ArrayList<Double> missingPathCostPrediction = costModel.predict(
				missingPath, 16);
		assertEqualsHelper(
				"cost of link between two non-existent nodes (not free?)",
				0.48750260351578967, missingPathCostPrediction.get(0), DELTA);
		ArrayList<Double> notFreePathCostPrediction = costModel.predict(
				notFreePath, 16);
		assertEqualsHelper("cost of paid link between two nodes (not free)",
				0.3035314977472798, notFreePathCostPrediction.get(0), DELTA);
		ArrayList<Double> selfEdgeCostPrediction = costModel.predict(
				selfEdgePath, 16);
		assertEqualsHelper("self edge cost (1.0)", 1.0,
				selfEdgeCostPrediction.get(0), DELTA);
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
