package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.deflaux.facebook2.io.DataStream;
import org.deflaux.facebook2.io.PredictionPaths;
import org.deflaux.ml.ErrorMetrics;
import org.deflaux.util.Stopwatch;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class PredictGraphITCase {
	static final Logger logger = Logger.getLogger("PredictGraphTest");

	static final double DELTA = 1e-2;
	static final double EXISTENCE_PREDICTION_THRESHOLD = 0.75;
	static final double COST_PREDICTION_THRESHOLD = 0.5;

	static DataStream training;
	static PredictionPaths testing;
	static Set<String> edges;
	static int historyWindowSize = 10;
	static int numDimensions = (int) Math.pow(2, 16);

	static final boolean printAssertions = Boolean.parseBoolean(System
			.getProperty("printAssertions"));

	static void resetData() throws FileNotFoundException {
		// Creates a data stream from the training data
		training = new DataStream(
				"/Users/deflaux/rework/competitions/facebook2/data/normTrain"
						+ DataStream.EPOCH_PLACEHOLDER + ".txt", false);
		testing = new PredictionPaths(
				"/Users/deflaux/rework/competitions/facebook2/data/normTestPaths.txt");

		DataInstance.clearEdgeHistory();
		DataInstance.setHistoryWindowSize(historyWindowSize);
	}

	@BeforeClass
	public static void determineAllPossibleEdges() throws FileNotFoundException {
		logger.setLevel(Level.INFO);
		resetData();
		edges = new HashSet<String>();
		DataInstance instance = null;
		while (training.hasNext()) {
			instance = training.nextInstance(instance, numDimensions);
			if (!instance.isValid()) {
				continue;
			}
			edges.add(instance.tail + "|" + instance.head);
		}
		assertEqualsHelper("num instances", 722588, training.getCounter());
		assertEqualsHelper("number of unique training edges", 67612,
				edges.size());

		while (testing.hasNext()) {
			List<String> path = testing.nextInstance();
			if (1 == path.size())
				continue;
			for (int i = 0; i < path.size() - 1; i++) {
				edges.add(path.get(i) + "|" + path.get(i + 1));
			}
		}

		assertEqualsHelper("number of unique training and test edges", 67848,
				edges.size());
	}

	@Before
	public void setup() throws FileNotFoundException {
		resetData();
	}

	@Test
	public void testPredictTrainingGraphs() throws IOException {
		final double steps[] = { 0.05 };
		final double lambdas[] = { 0.1 };

		CostModel costModel = new CostModel(0.05, 0.001, historyWindowSize,
				numDimensions);
		boolean costModelTrained = false;

		Stopwatch watch = new Stopwatch();
		for (double lambda : lambdas) {
			for (double step : steps) {

				ExistenceModel existenceModel = new ExistenceModel(step,
						lambda, historyWindowSize, numDimensions);

				DataInstance instance = null;
				while (training.hasNext()) {
					instance = training.nextInstance(instance, numDimensions);
					if (11 == training.getEpoch()) {
						// Do not train with data from epoch 11 onward
						break;
					}
					if (!instance.isValid()) {
						continue;
					}
					existenceModel.train(instance);
					if (!costModelTrained) {
						costModel.train(instance);
					}
				}
				logger.info("Time: " + watch.elapsedTime());
				costModelTrained = true;

				// Instead of a n^2 loop to predict all edges (which takes too
				// long), just predict for edges we have seen in the past plus
				// edges in the test set
				for (int epoch = 11; epoch <= 15; epoch++) {
					String filePathPredictedGraph = "/Users/deflaux/rework/competitions/facebook2/data/graph"
							+ epoch + ".txt";
					predictGraph(existenceModel, costModel, epoch,
							filePathPredictedGraph);

					String filePathActualGraph = "/Users/deflaux/rework/competitions/facebook2/data/normTrain"
							+ epoch + ".txt";
					ErrorMetrics existenceMetrics = new ErrorMetrics();
					ErrorMetrics costMetrics = new ErrorMetrics();

					CompareGraphsTest.compareGraphs(filePathActualGraph,
							filePathPredictedGraph, existenceMetrics,
							costMetrics);

					System.out.println(epoch + " Existence f-score: "
							+ existenceMetrics.getFScore() + ", "
							+ existenceMetrics + ", " + existenceModel);
					System.out.println(epoch + " Cost f-score: "
							+ costMetrics.getFScore() + ", " + costMetrics
							+ ", " + costModel);
					
					switch (epoch) {
					case 11:
						assertEqualsHelper(epoch + " Existence f-score",
								0.9074856200474639, existenceMetrics.getFScore(), DELTA);
						assertEqualsHelper(epoch + " Cost f-score",
								0.9957277039550153, costMetrics.getFScore(), DELTA);
						break;
					case 12:
						assertEqualsHelper(epoch + " Existence f-score",
								0.9025366886882848, existenceMetrics.getFScore(), DELTA);
						assertEqualsHelper(epoch + " Cost f-score",
								0.99496265971567, costMetrics.getFScore(), DELTA);
						break;
					case 13:
						assertEqualsHelper(epoch + " Existence f-score",
								0.8939245195178479, existenceMetrics.getFScore(), DELTA);
						assertEqualsHelper(epoch + " Cost f-score",
								0.9947428725483588, costMetrics.getFScore(), DELTA);
						break;
					case 14:
						assertEqualsHelper(epoch + " Existence f-score",
								0.8780144048914452, existenceMetrics.getFScore(), DELTA);
						assertEqualsHelper(epoch + " Cost f-score",
								0.9936175451222514, costMetrics.getFScore(), DELTA);
						break;
					case 15:
						assertEqualsHelper(epoch + " Existence f-score",
								0.8555241995374397, existenceMetrics.getFScore(), DELTA);
						assertEqualsHelper(epoch + " Cost f-score",
								0.9922961753096502, costMetrics.getFScore(), DELTA);
						break;
					default:
						fail("unexpected epoch " + epoch);
					}

				}
				logger.info("Time: " + watch.elapsedTime());
				resetData();
			}
		}
	}

	@Ignore
	@Test
	public void testPredictTestGraphs() throws IOException {
		ExistenceModel existenceModel = new ExistenceModel(0.05, 0.1,
				historyWindowSize, numDimensions);
		CostModel costModel = new CostModel(0.05, 0.001, historyWindowSize,
				numDimensions);

		Stopwatch watch = new Stopwatch();
		DataInstance instance = null;
		while (training.hasNext()) {
			instance = training.nextInstance(instance, numDimensions);
			if (!instance.isValid()) {
				continue;
			}
			existenceModel.train(instance);
			costModel.train(instance);
		}
		logger.info("Time: " + watch.elapsedTime());

		// Instead of a n^2 loop to predict all edges, just predict for
		// edges we
		// have seen in the past plus edges in the test set
		for (int epoch = 16; epoch <= 20; epoch++) {
			String filePathPredictedGraph = "/Users/deflaux/rework/competitions/facebook2/data/graph"
					+ epoch + ".txt";
			predictGraph(existenceModel, costModel, epoch,
					filePathPredictedGraph);
		}
		logger.info("Time: " + watch.elapsedTime());
	}

	void predictGraph(ExistenceModel existenceModel, CostModel costModel,
			int epoch, String filePathPredictedGraph) throws IOException {
		Writer predictedGraph = new BufferedWriter(new FileWriter(
				filePathPredictedGraph));

		int numEdges = 0;
		for (String edge : edges) {
			String[] fields = edge.split("\\|");
			String tail = fields[0];
			String head = fields[1];
			Double existencePrediction = existenceModel.predictEdge(tail, head,
					epoch);
			if (EXISTENCE_PREDICTION_THRESHOLD < existencePrediction) {
				// The edge exists for the graph at this epoch
				// Remember to write out the rawCost -> zero means
				// free
				int cost = (COST_PREDICTION_THRESHOLD < costModel.predictEdge(
						tail, head, epoch)) ? 0 : 1;
				predictedGraph.write(tail + "|" + head + "|" + cost + "\n");
			}
			numEdges++;
			if (0 == numEdges % 1000) {
				logger.debug("Progress: " + numEdges / (double) edges.size()
						+ " with " + numEdges + " edges");
			}
		}
		predictedGraph.close();
	}

	static void assertEqualsHelper(String testCase, Object expected,
			Object actual) {
		if (true == printAssertions) {
			logger.info("Test case: " + testCase + ", expected: " + expected
					+ ", actual: " + actual);
		} else {
			assertEquals(testCase, expected, actual);
		}
	}
	
	static void assertEqualsHelper(String testCase, Double expected, Double actual,
			Double delta) {
		if (true == printAssertions) {
			logger.info("Test case: " + testCase + ", expected: " + expected
					+ ", actual: " + actual);
		} else {
			assertEquals(testCase, expected, actual, delta);
		}
	}
	
}
