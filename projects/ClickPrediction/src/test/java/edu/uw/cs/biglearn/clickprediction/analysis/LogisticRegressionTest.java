package edu.uw.cs.biglearn.clickprediction.analysis;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uw.cs.biglearn.clickprediction.analysis.LogisticRegression.Weights;
import edu.uw.cs.biglearn.clickprediction.util.EvalUtil;

public class LogisticRegressionTest {

	static final double DELTA = 1e-15;
	static final String DATA_PATH = "/Users/deflaux/rework/projects/ClickPrediction/data/";

	static boolean debug = true;
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
	public void test() {
		LogisticRegression lr = new LogisticRegression();
		double lambda = 0.0;
		double step = 0.001;
		ArrayList<Double> avgLoss = new ArrayList<Double>();
		Weights weights = lr.train(training, lambda, step, avgLoss);
		System.out.println("Weights: " + weights);
		System.out.println("Average Loss: " + avgLoss);
		assertEquals("1.4.2 (b) l2 norm of weights", 0.13261375920128926,
				weights.l2norm(), DELTA);

		ArrayList<Double> predictions = lr.predict(weights, testing);
		assertEquals("1.4.2 (b) RMSE of predicted CTR", 0.3022793809306685,
				EvalUtil.eval(DATA_PATH + "test_label.txt", predictions), DELTA);
	}
}
