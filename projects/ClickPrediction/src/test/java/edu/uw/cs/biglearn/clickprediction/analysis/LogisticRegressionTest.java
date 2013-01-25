package edu.uw.cs.biglearn.clickprediction.analysis;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uw.cs.biglearn.clickprediction.util.EvalUtil;

public class LogisticRegressionTest {

	static Logger logger = Logger.getLogger("LogisticRegressionTest");
	static final double DELTA = 1e-15;
	static final String DATA_PATH = "/Users/deflaux/rework/projects/ClickPrediction/data/";

	static boolean debug = false; // true;
	static boolean printAssertions = false; // true;
	static int debugSize = 100;
	static DataSet training;
	static DataSet testing;

	@BeforeClass
	public static void LoadData() throws FileNotFoundException {
		BasicConfigurator.configure();
		training = new DataSet(DATA_PATH + "train.txt", true, debug ? debugSize
				: DataSet.TRAININGSIZE);
		testing = new DataSet(DATA_PATH + "test.txt", false, debug ? debugSize
				: DataSet.TESTINGSIZE);
	}

	@Before
	public void resetData() {
		// Important: remember to reset the datasets every time you are done
		// with processing.
		training.reset();
		testing.reset();
	}

	@Test
	public void test_homework_1_4_2() throws IOException {
		double lambda = 0.0;
		double steps[] = { 0.001, 0.01, 0.05 };

		assertEqualsHelper("1.4.2 (b) RMSE of baseline CTR",
				0.1730837340117073,
				EvalUtil.evalBaseLine(DATA_PATH + "test_label.txt", 0.033655),
				DELTA);

		for (double step : steps) {
			ArrayList<Double> avgLosses = new ArrayList<Double>();
			Weights weights = LogisticRegression.train(training, lambda, step,
					avgLosses);
			
			// Write losses to a file for later plotting
			FileWriter writer = new FileWriter(step + "output.csv"); 
			for(Double avgLoss : avgLosses) {
			  writer.write(avgLoss.toString()+ "\n");
			}
			writer.close();

			double l2norm = weights.l2norm();
			ArrayList<Double> predictions = LogisticRegression.predict(weights,
					testing);
			double rmse = EvalUtil.eval(DATA_PATH + "test_label.txt",
					predictions);

			if (0.001 == step) {
				assertEqualsHelper("1.4.2 (b) l2 norm of weights for step size 0.001",
						3.8018789742819346, l2norm, DELTA);

				assertEqualsHelper("1.4.2 (b) RMSE of predicted CTR for step size 0.001",
						0.17117218852652383, rmse, DELTA);
			}
			else if (0.01 == step) {
				assertEqualsHelper("1.4.2 (b) l2 norm of weights for step size 0.01",
						9.057169765210123, l2norm, DELTA);

				assertEqualsHelper("1.4.2 (b) RMSE of predicted CTR for step size 0.01",
						0.1713282869636794, rmse, DELTA);
				assertEqualsHelper("1.4.2 (c) weight for intercept for step size 0.01",
						-2.429819110781506, weights.w0, DELTA);
				assertEqualsHelper("1.4.2 (c) weight for depth for step size 0.01",
						0.17221938765761013, weights.wDepth, DELTA);
				assertEqualsHelper("1.4.2 (c) weight for position for step size 0.01",
						-0.7735761366536621, weights.wPosition, DELTA);
				assertEqualsHelper("1.4.2 (c) weight for age for step size 0.01",
						-0.040948015124305764, weights.wAge, DELTA);
				assertEqualsHelper("1.4.2 (c) weight for gender for step size 0.01",
						0.10413821477220414, weights.wGender, DELTA);
			}
			else if (0.05 == step) {
				assertEqualsHelper("1.4.2 (b) l2 norm of weights for step size 0.05",
						22.29195102596319, l2norm, DELTA);

				assertEqualsHelper("1.4.2 (b) RMSE of predicted CTR for step size 0.05",
						0.17340495195443922, rmse, DELTA);
			}
			else {
				fail("this step size is not one requested by the homework problem");
			}
			resetData();
		}
	}

	// n 1.4.3 Regularization part, please set \lambda from 0 to 0.014 spaced by
	// 0.002. (0, 0.002, 0.004, ..., 0.014).

	void assertEqualsHelper(String testCase, Double expected, Double actual,
			Double delta) {
		if (true == printAssertions) {
			logger.info("Test case: " + testCase + ", expected: " + expected
					+ ", actual: " + actual);
		} else {
			assertEquals(testCase, expected, actual, delta);
		}

	}
}
