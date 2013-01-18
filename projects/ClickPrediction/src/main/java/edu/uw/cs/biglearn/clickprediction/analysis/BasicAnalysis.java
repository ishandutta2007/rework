package edu.uw.cs.biglearn.clickprediction.analysis;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

public class BasicAnalysis {
	static Logger logger = Logger.getLogger("BasicAnalysis");

	/**
	 * Return the unique tokens in the dataset.
	 * 
	 * @param dataset
	 * @return
	 * @throws Exception 
	 */
	public static Set<Integer> uniqTokens(DataSet dataset) throws Exception {
		int count = 0;
		Set<Integer> tokens = new HashSet<Integer>();
		logger.info("Counting unique tokens in " + dataset.path + " ... ");
		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % 100000 == 0) {
				logger.info("Counted " + count + " lines");
			}
			for (int token : instance.tokens) {
				tokens.add(token);
			}
		}
		if (count < dataset.size) {
			logger.error("The real size of the data is less than the input size: "
							+ dataset.size + "<" + count);
		}
		logger.info("Done. Total processed instances: " + count);
		return tokens;	}

	/**
	 * Return the unique users in the dataset.
	 * 
	 * @param dataset
	 * @return
	 * @throws Exception 
	 */
	public static Set<Integer> uniqUsers(DataSet dataset) throws Exception {
		int count = 0;
		Set<Integer> users = new HashSet<Integer>();
		logger.info("Counting unique users in " + dataset.path + " ... ");
		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % 100000 == 0) {
				logger.info("Counted " + count + " lines");
			}
			users.add(instance.userid);
		}
		if (count < dataset.size) {
			logger.error("The real size of the data is less than the input size: "
							+ dataset.size + "<" + count);
		}
		logger.info("Done. Total processed instances: " + count);
		return users;
	}

	/**
	 * @return the average CTR for the training set.
	 * @throws Exception 
	 */
	public static double averageCtr(DataSet dataset) throws Exception {
		int count = 0;
		int clicks = 0;
		logger.info("Counting clicks in " + dataset.path + " ... ");
		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % 100000 == 0) {
				logger.info("Counted " + count + " lines");
			}
			if(1 == instance.clicked) {
				clicks++;
			}
		}
		if (count < dataset.size) {
			logger.error("The real size of the data is less than the input size: "
							+ dataset.size + "<" + count);
		}
		logger.info("Done. Total processed instances: " + count);
		return (double)clicks/(double)count;
	}

	public static void main(String args[]) throws Exception {
		// Fill in your code here
		DummyLoader loader = new DummyLoader();
		int size = 10;

		// Creates a dataset from the trainingdata with size = 10;
		DataSet training = new DataSet(
				"/Users/deflaux/rework/projects/ClickPrediction/data/train.txt",
				true, size); // DataSet.TRAININGSIZE);
		loader.scanAndPrint(training);

		// Creates a dataset from the testdata with size = 10;
		DataSet testing = new DataSet(
				"/Users/deflaux/rework/projects/ClickPrediction/data/test.txt",
				false, size);  // DataSet.TESTINGSIZE);
		loader.scanAndPrint(testing);
	}
}
