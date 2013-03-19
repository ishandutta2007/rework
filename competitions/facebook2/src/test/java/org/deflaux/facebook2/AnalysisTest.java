package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deflaux.facebook2.io.DataStream;
import org.deflaux.facebook2.io.PredictionPaths;
import org.deflaux.util.Stopwatch;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnalysisTest {
	static final double DELTA = 1e-2;
	static final Logger logger = Logger.getLogger("AnalysisTest");

	// mvn test -DargLine="-DprintAssertions=true"
	static final boolean printAssertions = Boolean.parseBoolean(System
			.getProperty("printAssertions"));

	static DataStream training;
	static PredictionPaths testing;
	static int historyWindowSize = 10;
	static int numDimensions = (int) Math.pow(2, 16);

	@BeforeClass
	public static void beforeClass() {
		// This is no longer needed since apparently Hadoop has already
		// configured log4j
		// BasicConfigurator.configure();
		DataInstance.setHistoryWindowSize(historyWindowSize);
	}

	@Before
	public void resetData() throws FileNotFoundException {
		// Creates a data stream from the trainingdata
		training = new DataStream(
				"/Users/deflaux/rework/competitions/facebook2/data/normTrain"
						+ DataStream.EPOCH_PLACEHOLDER + ".txt", false);
		testing = new PredictionPaths(
				"/Users/deflaux/rework/competitions/facebook2/data/normTestPaths.txt");
		DataInstance.setHistoryWindowSize(historyWindowSize);
		DataInstance.clearEdgeHistory();
	}

	@Test
	public void testLogisticRegression() throws IOException {
		// Turn off data stream shuffling so that our test results are
		// reproducible
		training.setShuffle(false);

		ExistenceModel existenceModel = new ExistenceModel(0.05, 0.1,
				historyWindowSize, numDimensions);
		CostModel costModel = new CostModel(0.05, 0.001, historyWindowSize,
				numDimensions);

		List<FacebookModel> models = new ArrayList<FacebookModel>();
		models.add(existenceModel);
		models.add(costModel);

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
		assertEqualsHelper("num invalid", 0, numInvalid);

		assertEqualsHelper("existence average loss for epoch 15",
				0.0027732697877935046,
				existenceModel.errorMetricsPerEpoch.get(14).getAverageLoss(), DELTA);
		assertEqualsHelper("cost average loss for epoch 15",
				0.016619075987592186, costModel.errorMetricsPerEpoch.get(14)
						.getAverageLoss(), DELTA);
		assertEqualsHelper("existence f score for epoch 15",
				0.9986114396799111, existenceModel.errorMetricsPerEpoch.get(14)
						.getFScore(), DELTA);
		assertEqualsHelper("cost f score for epoch 15", 0.9430642550496164,
				costModel.errorMetricsPerEpoch.get(14).getFScore(), DELTA);
		// No false positives since all the training data is positive for this
		// model
		assertEqualsHelper("existence false positive for epoch 15", 0.0,
				existenceModel.errorMetricsPerEpoch.get(14).getFalsePositive());
		assertEqualsHelper("existence false negative for epoch 15", 129.0,
				existenceModel.errorMetricsPerEpoch.get(14).getFalseNegative());
		assertEqualsHelper("cost false positive for epoch 15", 241.0,
				costModel.errorMetricsPerEpoch.get(14).getFalsePositive());
		assertEqualsHelper("cost false negative for epoch 15", 526.0,
				costModel.errorMetricsPerEpoch.get(14).getFalseNegative());

		// Test case for a path between two supernodes that is free and goes in
		// both directions
		String superNode1 = "aachikknosvy acnns ceeeghirsv eegrsy fop";
		String superNode2 = "aabcdginorst aabceknoprsstw corss ehnorstu";
		List<String> superNodesPath = new ArrayList<String>();
		superNodesPath.add(superNode2);
		superNodesPath.add(superNode1);
		List<String> missingPath = new ArrayList<String>();
		missingPath.add("missing");
		missingPath.add("does not exist");
		// Test case for a link that is not free and only goes in one direction
		String normalNode = "aabclnopt aadt abclot ceenrst";
		List<String> notFreePath = new ArrayList<String>();
		notFreePath.add(normalNode);
		notFreePath.add(superNode1);
		// Test case for self edge
		List<String> selfEdgePath = new ArrayList<String>();
		selfEdgePath.add(normalNode);

		List<Double> superNodesExistencePrediction = existenceModel
				.predictPath(superNodesPath, 16);
		assertEqualsHelper("link between two super nodes (.99)",
				0.9699367295194332, superNodesExistencePrediction.get(0), DELTA);
		List<Double> missingPathPrediction = existenceModel.predictPath(
				missingPath, 16);
		assertEqualsHelper("link between two non-existent nodes (0.0)",
				0.5, missingPathPrediction.get(0), DELTA);
		List<Double> notFreePathPrediction = existenceModel.predictPath(
				notFreePath, 16);
		assertEqualsHelper("paid link between two nodes (.99)",
				0.9699317075808634, notFreePathPrediction.get(0), DELTA);
		List<Double> selfEdgePathPrediction = existenceModel.predictPath(
				selfEdgePath, 16);
		assertEqualsHelper("self edge (1.0)", 1.0,
				selfEdgePathPrediction.get(0), DELTA);
		assertEqualsHelper(
				"missing edge between existing nodes (0.0)",
				0.5000000004769575,
				existenceModel.predictEdge(notFreePath.get(1),
						notFreePath.get(0), 16), DELTA);

		List<Double> superNodesCostPrediction = costModel.predictPath(
				superNodesPath, 16);
		assertEqualsHelper("cost of link between two super nodes (free)",
				0.9490541549330433, superNodesCostPrediction.get(0), DELTA);
		List<Double> missingPathCostPrediction = costModel.predictPath(
				missingPath, 16);
		assertEqualsHelper(
				"cost of link between two non-existent nodes (not free?)",
				0.021307255565861747, missingPathCostPrediction.get(0), DELTA);
		List<Double> notFreePathCostPrediction = costModel.predictPath(
				notFreePath, 16);
		assertEqualsHelper("cost of paid link between two nodes (not free)",
				0.011264026671426293, notFreePathCostPrediction.get(0), DELTA);
		List<Double> selfEdgeCostPrediction = costModel.predictPath(
				selfEdgePath, 16);
		assertEqualsHelper("self edge cost (1.0)", 1.0,
				selfEdgeCostPrediction.get(0), DELTA);
		assertEqualsHelper(
				"cost of missing edge between existing nodes (0.0)",
				0.021601273174569655,
				costModel.predictEdge(notFreePath.get(1),
						notFreePath.get(0), 16), DELTA);
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
