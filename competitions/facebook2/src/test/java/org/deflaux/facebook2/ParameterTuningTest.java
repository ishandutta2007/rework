package org.deflaux.facebook2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deflaux.facebook2.io.DataStream;
import org.deflaux.util.Stopwatch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * export MAVEN_OPTS=-Xmx2048 ; mvn test -DargLine="-DprintAssertions=true"
 * -Dtest=ParameterTuningTest
 * 
 * @author deflaux
 * 
 */

public class ParameterTuningTest {
	static final Logger logger = Logger.getLogger("ParameterTuningTest");
	static DataStream training;
	List<String> missingPath;

	@Before
	public void resetData() throws FileNotFoundException {
		// Creates a data stream from the trainingdata
		training = new DataStream(
				"/Users/deflaux/rework/competitions/facebook2/data/normTrain"
						+ DataStream.EPOCH_PLACEHOLDER + ".txt", false);
		DataInstance.clearEdgeHistory();
		missingPath = new ArrayList<String>();
		missingPath.add("missing");
		missingPath.add("does not exist");
	}

	@Ignore
	@Test
	public void testParameters() throws IOException {
		int historyWindowSize = 8;
		int numDimensions = (int) Math.pow(2, 16);
		DataInstance.setHistoryWindowSize(historyWindowSize);

		List<FacebookModel> models = new ArrayList<FacebookModel>();

		// We can only build a few of these at a time without blowing out heap
		int numModelsToBuildConcurrently = 4;
		final double steps[] = { 0.001, 0.005, 0.01, 0.05 };
		final double lambdas[] = { 0.0, 0.001, 0.002, 0.004, 0.006, 0.008,
				0.010, 0.012, 0.1 };
		for (double lambda : lambdas) {
			for (double step : steps) {
				models.add(new ExistenceModel(step, lambda, historyWindowSize,
						numDimensions));
				models.add(new CostModel(step, lambda, historyWindowSize,
						numDimensions));
				if (numModelsToBuildConcurrently <= models.size()) {
					buildModels(models, numDimensions);
					models.clear();
				}
			}
		}
	}

	void buildModels(List<FacebookModel> models, int numDimensions) throws FileNotFoundException {
		resetData();
		DataInstance instance = null;
		Stopwatch watch = new Stopwatch();
		while (training.hasNext()) {
			instance = training.nextInstance(instance, numDimensions);
			if (!instance.isValid()) {
				continue;
			}
			for (FacebookModel model : models) {
				model.train(instance);
			}
		}
		logger.info("Time: " + watch.elapsedTime());
		for (FacebookModel model : models) {
			List<Double> missingPathCostPrediction = model.predictPath(missingPath,
					16);
			logger.info(model + ", missing path pred=" + missingPathCostPrediction.get(0));
		}
	}
}
