package edu.uw.cs.biglearn.clickprediction.analysis;

import static org.junit.Assert.*;
import org.apache.log4j.BasicConfigurator;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicAnalysisTest {
	static final double DELTA = 1e-15;

	static DataSet training;
	static DataSet testing;

	@BeforeClass
	public static void LoadData() throws FileNotFoundException {
		BasicConfigurator.configure();

		boolean debug = false;
		int debugSize = 10;
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
	public void testUniqTokens() {
		Set<Integer> trainingTokens = BasicAnalysis.uniqTokens(training, true);
		assertEquals("Number of unique tokens in training dataset", 141063,
				trainingTokens.size());

		Set<Integer> testingTokens = BasicAnalysis.uniqTokens(testing, true);
		assertEquals("Number of unique tokens in testing dataset", 109459,
				testingTokens.size());

		Set<Integer> userIntersection = new HashSet<Integer>(trainingTokens);
		userIntersection.retainAll(testingTokens);
		assertEquals(
				"Number of unique tokens residing in both the testing and training dataset (intersection)",
				79261, userIntersection.size());
	}

	@Test
	public void testUniqUsers() {
		Set<Integer> trainingUsers = BasicAnalysis.uniqUsers(training, true);
		assertEquals("Number of unique users in training dataset", 982431,
				trainingUsers.size());

		Set<Integer> testingUsers = BasicAnalysis.uniqUsers(testing, true);
		assertEquals("Number of unique users in testing dataset", 574906,
				testingUsers.size());

		Set<Integer> userIntersection = new HashSet<Integer>(trainingUsers);
		userIntersection.retainAll(testingUsers);
		assertEquals(
				"Number of unique users residing in both the testing and training dataset (intersection)",
				56075, userIntersection.size());
	}

	@Test
	public void testAverageCtr() {
		double clickThruRate = BasicAnalysis.averageCtr(training, true);
		assertEquals("Click through rate in training dataset", 0.03365528484381977, clickThruRate, DELTA);
	}

}
