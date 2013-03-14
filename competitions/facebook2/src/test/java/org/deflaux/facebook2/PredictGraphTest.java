package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deflaux.facebook2.io.DataStream;
import org.deflaux.facebook2.io.PredictionPaths;
import org.deflaux.util.Stopwatch;
import org.junit.Before;
import org.junit.Test;

public class PredictGraphTest {
	static final Logger logger = Logger.getLogger("PredictGraphTest");

	static final double EXISTENCE_PREDICTION_THRESHOLD = 0.7;
	static final double COST_PREDICTION_THRESHOLD = 0.6;

	static DataStream training;
	static PredictionPaths testing;

	static final boolean printAssertions = Boolean.parseBoolean(System
			.getProperty("printAssertions"));

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
	public void testPredictGraph() throws IOException {
		int historyWindowSize = 8;
		int numDimensions = (int) Math.pow(2, 16);

		DataInstance.setHistoryWindowSize(historyWindowSize);
		ExistenceModel existenceModel = new ExistenceModel(0.05, 0.1,
				historyWindowSize, numDimensions);
		CostModel costModel = new CostModel(0.05, 0.001, historyWindowSize,
				numDimensions);

		List<FacebookModel> models = new ArrayList<FacebookModel>();
		models.add(existenceModel);
		models.add(costModel);

		Set<String> edges = new HashSet<String>();
		DataInstance instance = null;
		Stopwatch watch = new Stopwatch();
		while (training.hasNext()) {
			instance = training.nextInstance(instance, numDimensions);
			if (15 == training.getEpoch()) {
				// Do not train with data from epoch 15
				break;
			}
			if (!instance.isValid()) {
				continue;
			}
			edges.add(instance.tail + "|" + instance.head);
			for (FacebookModel model : models) {
				model.train(instance);
			}
		}
		logger.info("Time: " + watch.elapsedTime());
		for (FacebookModel model : models) {
			logger.info(model);
		}

		assertEqualsHelper("num instances", 673345, training.getCounter());
		assertEqualsHelper("number of unique training edges", 137065,
				edges.size());

		while (testing.hasNext()) {
			List<String> path = testing.nextInstance();
			if (1 == path.size())
				continue;
			for (int i = 0; i < path.size() - 1; i++) {
				edges.add(path.get(i) + "|" + path.get(i + 1));
			}
		}

		assertEqualsHelper("number of unique training and test edges",
				140160, edges.size());

		// Instead of a n^2 loop to predict all edges, just predict for edges we
		// have seen in the past plus edges in the test set
		int epoch = 15;
		Writer predictedGraph = new BufferedWriter(new FileWriter(
				"/Users/deflaux/rework/competitions/facebook2/data/graph"
						+ epoch + ".txt"));

		int numEdges = 0;
		for (String edge : edges) {
			String[] fields = edge.split("\\|");
			String tail = fields[0];
			String head = fields[1];
			Double existencePrediction = existenceModel.predictEdge(tail, head,
					epoch);
			if (EXISTENCE_PREDICTION_THRESHOLD < existencePrediction) {
				// The edge exists for the graph at this epoch
				// Remember to write out the rawCost -> zero means free
				int cost = (COST_PREDICTION_THRESHOLD < costModel.predictEdge(
						tail, head, epoch)) ? 0 : 1;
				predictedGraph.write(tail + "|" + head + "|" + cost + "\n");
			}
			numEdges++;
			if (0 == numEdges % 1000) {
				logger.info("Progress: " + numEdges / (double) edges.size()
						+ " with " + numEdges + " edges");
			}
		}
		predictedGraph.close();
		logger.info("Time: " + watch.elapsedTime());

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
