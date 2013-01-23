package edu.uw.cs.biglearn.clickprediction.analysis;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uw.cs.biglearn.clickprediction.analysis.LogisticRegression.Weights;

public class LogisticRegressionTest {

	static final double DELTA = 1e-15;

	static DataSet training;
	static DataSet testing;

	@BeforeClass
	public static void LoadData() throws FileNotFoundException {
		BasicConfigurator.configure();

		boolean debug = true;
		int debugSize = 100;
		// Creates a dataset from the trainingdata
		training = new DataSet(
				"/Users/deflaux/rework/projects/ClickPrediction/data/train.txt",
				true, debug ? debugSize : DataSet.TRAININGSIZE);

		// Creates a dataset from the testdata
		testing = new DataSet(
				"/Users/deflaux/rework/projects/ClickPrediction/data/test.txt",
				false, debug ? debugSize : DataSet.TESTINGSIZE);
	}

	@Before
	public void resetData() {
		// Important: remember to reset the datasets everytime
		// you are done with processing.
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
		assertEquals(0.27040402016379006, weights.l2norm(), DELTA);
	}
}
