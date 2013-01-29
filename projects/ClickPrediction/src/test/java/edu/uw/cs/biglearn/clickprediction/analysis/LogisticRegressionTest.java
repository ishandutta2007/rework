package edu.uw.cs.biglearn.clickprediction.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
		final double lambda = 0.0;
		final double steps[] = { 0.001, 0.01, 0.05 };

		assertEqualsHelper("1.4.2 (b) RMSE of baseline CTR",
				0.1730837340117073,
				EvalUtil.evalBaseLine(DATA_PATH + "test_label.txt", 0.033655),
				DELTA);

		for (double step : steps) {
			ArrayList<Double> avgLosses = new ArrayList<Double>();
			Weights weights = LogisticRegression.train(training, lambda, step,
					avgLosses);

			// Write losses to a file for later plotting
			FileWriter writer = new FileWriter(step + "Output.csv");
			for (Double avgLoss : avgLosses) {
				writer.write(avgLoss.toString() + "\n");
			}
			writer.close();

			double l2norm = weights.l2norm();
			ArrayList<Double> predictions = LogisticRegression.predict(weights,
					testing);
			double rmse = EvalUtil.eval(DATA_PATH + "test_label.txt",
					predictions);

			if (0.001 == step) {
				assertEqualsHelper(
						"1.4.2 (b) l2 norm of weights for step size 0.001",
						3.8018789742819346, l2norm, DELTA);

				assertEqualsHelper(
						"1.4.2 (b) RMSE of predicted CTR for step size 0.001",
						0.17117218852652383, rmse, DELTA);
			} else if (0.01 == step) {
				assertEqualsHelper(
						"1.4.2 (b) l2 norm of weights for step size 0.01",
						9.057169765210123, l2norm, DELTA);

				assertEqualsHelper(
						"1.4.2 (b) RMSE of predicted CTR for step size 0.01",
						0.1713282869636794, rmse, DELTA);
				assertEqualsHelper(
						"1.4.2 (c) weight for intercept for step size 0.01",
						-2.429819110781506, weights.w0, DELTA);
				assertEqualsHelper(
						"1.4.2 (c) weight for depth for step size 0.01",
						0.17221938765761013, weights.wDepth, DELTA);
				assertEqualsHelper(
						"1.4.2 (c) weight for position for step size 0.01",
						-0.7735761366536621, weights.wPosition, DELTA);
				assertEqualsHelper(
						"1.4.2 (c) weight for age for step size 0.01",
						-0.040948015124305764, weights.wAge, DELTA);
				assertEqualsHelper(
						"1.4.2 (c) weight for gender for step size 0.01",
						0.10413821477220414, weights.wGender, DELTA);
			} else if (0.05 == step) {
				assertEqualsHelper(
						"1.4.2 (b) l2 norm of weights for step size 0.05",
						22.29195102596319, l2norm, DELTA);

				assertEqualsHelper(
						"1.4.2 (b) RMSE of predicted CTR for step size 0.05",
						0.17340495195443922, rmse, DELTA);
			} else {
				fail("this step size is not one requested by the homework problem");
			}
			resetData();
		}
	}

	@Test
	public void test_homework_1_4_3() throws IOException {
		// n 1.4.3 Regularization part, please set \lambda from 0 to 0.014
		// spaced by
		// 0.002. (0, 0.002, 0.004, ..., 0.014).
		final double lambdas[] = { 0.0, 0.002, 0.004, 0.006, 0.008, 0.010,
				0.012, 0.014 };
		final double step = 0.05;

		// Write lambda and RMSE to a file for later plotting
		FileWriter writer = new FileWriter("regularizationOutput.csv");

		for (double lambda : lambdas) {
			ArrayList<Double> avgLosses = new ArrayList<Double>();
			Weights weights = LogisticRegression.train(training, lambda, step,
					avgLosses);

			ArrayList<Double> predictions = LogisticRegression.predict(weights,
					testing);
			double rmse = EvalUtil.eval(DATA_PATH + "test_label.txt",
					predictions);

			writer.write(lambda + ", " + rmse + "\n");

			if (0.0 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.0 ",
						0.17340495195443922, rmse, DELTA);
			} else if (0.002 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.002 ",
						0.17305476141147214, rmse, DELTA);
			} else if (0.004 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.004 ",
						0.17308221992056902, rmse, DELTA);
			} else if (0.006 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.006 ",
						0.17312376937144372, rmse, DELTA);
			} else if (0.008 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.008 ",
						0.17316907146122948, rmse, DELTA);
			} else if (0.010 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.010 ",
						0.1732147133812869, rmse, DELTA);
			} else if (0.012 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.012 ",
						0.17325938238861147, rmse, DELTA);
			} else if (0.014 == lambda) {
				assertEqualsHelper(
						"1.4.3 RMSE of predicted CTR for step size 0.05 and lambda 0.014 ",
						0.17330253247633165, rmse, DELTA);
			} else {
				fail("this lambda is not one requested by the homework problem");
			}
			resetData();
		}
		writer.close();
	}

	@Test
	public void test_homework_1_4_4() throws IOException {
		final int dims[] = { 97, 12289, 1572869 };
		final double lambda = 0.001;
		final double step = 0.01;

		for (int dim : dims) {
			ArrayList<Double> avgLosses = new ArrayList<Double>();
			WeightsWithHashedFeatures weights = LogisticRegressionWithHashing
					.train(training, dim, lambda, step, avgLosses, false);

			ArrayList<Double> predictions = LogisticRegressionWithHashing
					.predict(weights, testing, false);
			double rmse = EvalUtil.eval(DATA_PATH + "test_label.txt",
					predictions);

			switch (dim) {
			case 97:
				assertEqualsHelper(
						"1.4.4 RMSE of predicted CTR for step size 0.01, lambda 0.001, feature dimensions 97 ",
						0.17283788911636036, rmse, DELTA);
				break;
			case 12289:
				assertEqualsHelper(
						"1.4.4 RMSE of predicted CTR for step size 0.01, lambda 0.001, feature dimensions 12289 ",
						0.17283291750600113, rmse, DELTA);
				break;
			case 1572869:
				assertEqualsHelper(
						"1.4.4 RMSE of predicted CTR for step size 0.01, lambda 0.001, feature dimensions 1572869 ",
						0.17283146600005928, rmse, DELTA);
				break;
			default:
				fail("this lambda is not one requested by the homework problem");
			}
			resetData();
		}
	}

	@Test
	public void test_homework_Personalization() throws IOException {
		final int dim = 12289;
		final double lambda = 0.001;
		final double step = 0.01;

		ArrayList<Double> predictions = null;		
		{ // nest this block so that we free unnecessary objects 
			ArrayList<Double> avgLosses = new ArrayList<Double>();
			WeightsWithHashedFeatures weights = LogisticRegressionWithHashing
					.train(training, dim, lambda, step, avgLosses, true);

			predictions = LogisticRegressionWithHashing.predict(weights,
					testing, false);
			double rmse = EvalUtil.eval(DATA_PATH + "test_label.txt",
					predictions);

			assertEqualsHelper(
					"Extra Credit RMSE of predicted CTR for step size 0.01, lambda 0.001, feature dimensions 12289 and PERSONALIZATION ",
					0.1728366811415177, rmse, DELTA);
		}
		Set<Integer> userIntersection = null;
		{ // nest this block so that we free unnecessary objects 
			training.reset();
			testing.reset();
			Set<Integer> trainingUsers = BasicAnalysis
					.uniqUsers(training, true);
			Set<Integer> testingUsers = BasicAnalysis.uniqUsers(testing, true);
			userIntersection = new HashSet<Integer>(trainingUsers);
			userIntersection.retainAll(testingUsers);
			assertEquals(
					"Number of unique users residing in both the testing and training dataset (intersection)",
					56075, userIntersection.size());
		}

		ArrayList<Boolean> includingList = new ArrayList<Boolean>(
				predictions.size());
		testing.reset();
		while (testing.hasNext()) {
			DataInstance instance = testing.nextInstance();
			if (userIntersection.contains(instance.userid)) {
				includingList.add(true);
			} else {
				includingList.add(false);
			}
		}

		double subsetUsersRMSE = EvalUtil.evalWithIncludingList(DATA_PATH
				+ "test_label.txt", predictions, includingList);
		assertEqualsHelper(
				"Extra Credit RMSE of predicted CTR for step size 0.01, lambda 0.001, feature dimensions 12289 and PERSONALIZATION over user intersection ",
				0.14187369257443835, subsetUsersRMSE, DELTA);

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
}
