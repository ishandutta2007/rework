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
		fail("Not yet implemented");
	}

	@Test
	public void testUniqUsers() {
		Set<Integer> trainingUsers = BasicAnalysis.uniqUsers(training);
		assertEquals("Number of unique users in training dataset", 982432,
				trainingUsers.size());

		Set<Integer> testingUsers = BasicAnalysis.uniqUsers(testing);
		assertEquals("Number of unique users in testing dataset", 574907,
				testingUsers.size());

		// Note that the question "How many users appear in both datasets?" is
		// ambiguous so I've reported answers for both interpretations. But if I
		// had to pick the most likely interpretation, I think what is wanted is the intersection.
		
		Set<Integer> userUnion = new HashSet<Integer>(trainingUsers);
		userUnion.addAll(testingUsers);
		assertEquals(
				"Number of unique users in the COMBINED testing and training dataset (union)",
				1501263, userUnion.size());

		Set<Integer> userIntersection = new HashSet<Integer>(trainingUsers);
		userIntersection.retainAll(testingUsers);
		assertEquals(
				"Number of unique users residing in both the testing and training dataset (intersection)",
				56076, userIntersection.size());
	}

	@Test
	public void testAverageCtr() {
		fail("Not yet implemented");
	}

}
