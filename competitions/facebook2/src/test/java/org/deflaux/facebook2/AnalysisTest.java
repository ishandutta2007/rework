package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
	public void testLogisticRegression() throws IOException {
		// Turn off data stream shuffling so that our test results are
		// reproducible
		training.setShuffle(false);

		DecimalFormat formatter = new DecimalFormat("###.######");
		int historyWindowSize = 8;
		int numDimensions = (int) Math.pow(2, 16);

		DataInstance.setHistoryWindowSize(historyWindowSize);
		ExistenceModel existenceModel = new ExistenceModel(0.01, 0.2,
				historyWindowSize, numDimensions);
		CostModel costModel = new CostModel(0.01, 0.1, historyWindowSize,
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
				0.00400772772114436, existenceModel.errorMetricsPerEpoch
						.get(14).getAverageLoss());
		assertEqualsHelper("cost average loss for epoch 15",
				0.057402992436698455, costModel.errorMetricsPerEpoch.get(14)
						.getAverageLoss());
		assertEqualsHelper("existence f score for epoch 15",
				0.9979921126064438, existenceModel.errorMetricsPerEpoch.get(14)
						.getFScore());
		assertEqualsHelper("cost f score for epoch 15", 0.8281548021903649,
				costModel.errorMetricsPerEpoch.get(14).getFScore());
		// No false positives since all the training data is positive for this
		// model
		assertEqualsHelper("existence false positive for epoch 15", 0.0,
				existenceModel.errorMetricsPerEpoch.get(14).getFalsePositive());
		assertEqualsHelper("existence false negative for epoch 15", 195.0,
				existenceModel.errorMetricsPerEpoch.get(14).getFalseNegative());
		assertEqualsHelper("cost false positive for epoch 15", 2255.0,
				costModel.errorMetricsPerEpoch.get(14).getFalsePositive());
		assertEqualsHelper("cost false negative for epoch 15", 538.0,
				costModel.errorMetricsPerEpoch.get(14).getFalseNegative());

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

		List<Double> superNodesExistencePrediction = existenceModel.predict(
				superNodesPath, 16);
		assertEqualsHelper("link between two super nodes (.99)",
				0.906439349052297, superNodesExistencePrediction.get(0), DELTA);
		List<Double> missingPathPrediction = existenceModel.predict(
				missingPath, 16);
		assertEqualsHelper("link between two non-existent nodes (0.0)",
				0.5906333116838719, missingPathPrediction.get(0), DELTA);
		List<Double> notFreePathPrediction = existenceModel.predict(
				notFreePath, 16);
		assertEqualsHelper("paid link between two nodes (.99)",
				0.9055536086222568, notFreePathPrediction.get(0), DELTA);
		List<Double> selfEdgePathPrediction = existenceModel.predict(
				selfEdgePath, 16);
		assertEqualsHelper("self edge (1.0)", 1.0,
				selfEdgePathPrediction.get(0), DELTA);

		List<Double> superNodesCostPrediction = costModel.predict(
				superNodesPath, 16);
		assertEqualsHelper("cost of link between two super nodes (free)",
				0.7352650212154823, superNodesCostPrediction.get(0), DELTA);
		List<Double> missingPathCostPrediction = costModel.predict(missingPath,
				16);
		assertEqualsHelper(
				"cost of link between two non-existent nodes (not free?)",
				0.4999998982790982, missingPathCostPrediction.get(0), DELTA);
		List<Double> notFreePathCostPrediction = costModel.predict(notFreePath,
				16);
		assertEqualsHelper("cost of paid link between two nodes (not free)",
				0.46343308328898397, notFreePathCostPrediction.get(0), DELTA);
		List<Double> selfEdgeCostPrediction = costModel.predict(selfEdgePath,
				16);
		assertEqualsHelper("self edge cost (1.0)", 1.0,
				selfEdgeCostPrediction.get(0), DELTA);

		// TODO think more about non-optimal versus non-existent paths
		// The existence model is too strongly positive right now to use it
		// alone
		// The cost model is useful for predicting cost, but how strongly does
		// that relate to optimality?
		// See how often costs actually change
		// See whether more of the test paths which were optimal at some point,
		// are low cost

		// cceeilnorst dehiiln|aabcdginorst aabceknoprsstw
		// ehnorstu|as61321|as60366|akmosss
		List<String> optimalTestPath = new ArrayList<String>();
		optimalTestPath.add("cceeilnorst dehiiln");
		optimalTestPath.add("aabcdginorst aabceknoprsstw ehnorstu");
		optimalTestPath.add("as61321");
		optimalTestPath.add("as60366");
		optimalTestPath.add("akmosss");

		// abcfklnors acceiinnooqrrrtuu|as28233|abcdehiinnrsttt ceeirsv cehint
		// eeinnrtt eilnno|as59900
		List<String> nonOptimalTestPath = new ArrayList<String>();
		nonOptimalTestPath.add("abcfklnors acceiinnooqrrrtuu");
		nonOptimalTestPath.add("as28233");
		nonOptimalTestPath
				.add("abcdehiinnrsttt ceeirsv cehint eeinnrtt eilnno");
		nonOptimalTestPath.add("as59900");

		Writer testPathPredictions = new BufferedWriter(
				new FileWriter(
						"/Users/deflaux/rework/competitions/facebook2/data/testPathPredictions.txt"));

		// Normalization bugs:
		// TODO why is link 'NIC' normalized to the empty string?
		// TODO one path has Ltd normalized to the empty string
		// TODO output something else for empty
		// once all this is fixed (1) check num invalid again (2) take question
		// mark out of predict

		Double pathPrediction = null;
		for (int epoch = 16; epoch <= 20; epoch++) {
			Double optimalPathPrediction = predictPath(optimalTestPath, epoch,
					existenceModel, costModel);
			Double nonOptimalPathPrediction = predictPath(nonOptimalTestPath, epoch,
					existenceModel, costModel);

			switch (epoch) {
			case 16:
				assertEqualsHelper(epoch + " optimalTestPath prediction", 0.6309956698488746,
						optimalPathPrediction);
				assertEqualsHelper(epoch + " nonOptimalTestPath prediction", 0.24506292467034826, nonOptimalPathPrediction);
				break;
			case 17:
				assertEqualsHelper(epoch + " optimalTestPath prediction", 0.5813610509876052,
						optimalPathPrediction);
				assertEqualsHelper(epoch + " nonOptimalTestPath prediction", 0.24506292467034826, nonOptimalPathPrediction);
				break;
			case 18:
				assertEqualsHelper(epoch + " optimalTestPath prediction", 0.5240060489830546,
						optimalPathPrediction);
				assertEqualsHelper(epoch + " nonOptimalTestPath prediction", 0.24506292467034826, nonOptimalPathPrediction);
				break;
			case 19:
				assertEqualsHelper(epoch + " optimalTestPath prediction", 0.4640625041906731,
						optimalPathPrediction);
				assertEqualsHelper(epoch + " nonOptimalTestPath prediction", 0.24506292467034826, nonOptimalPathPrediction);
				break;
			case 20:
				assertEqualsHelper(epoch + " optimalTestPath prediction", 0.4184742229163553,
						optimalPathPrediction);
				assertEqualsHelper(epoch + " nonOptimalTestPath prediction", 0.24506292467034826, nonOptimalPathPrediction);
				break;
			default:
				fail("unexpected epoch " + epoch);
			}

			while (testing.hasNext()) {
				List<String> path = testing.nextInstance();
				pathPrediction = predictPath(path, epoch, existenceModel,
						costModel);
				testPathPredictions.write(formatter.format(pathPrediction)
						+ "\n");
			}
			testing = new PredictionPaths(
					"/Users/deflaux/rework/competitions/facebook2/data/normTestPaths.txt");
		}
		testPathPredictions.close();

	}

	private Double predictPath(List<String> path, int epoch,
			ExistenceModel existenceModel, CostModel costModel) {
		Double pathPrediction = 0.0;
		List<Double> linkPredictions = existenceModel.predict(path, epoch);
		List<Double> costPredictions = costModel.predict(path, epoch);
		// Multiply them together and take the average?
		for (int i = 0; i < linkPredictions.size(); i++) {
			pathPrediction += linkPredictions.get(i) * costPredictions.get(i);
		}
		return pathPrediction / linkPredictions.size();
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
