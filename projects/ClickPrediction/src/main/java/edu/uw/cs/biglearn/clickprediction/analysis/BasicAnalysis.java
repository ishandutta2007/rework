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
	 */
	public Set<Integer> uniqTokens(DataSet dataset) {
		// Fill in your code here
		return null;
	}

	/**
	 * Return the unique users in the dataset.
	 * 
	 * @param dataset
	 * @return
	 */
	public static Set<Integer> uniqUsers(DataSet dataset) {
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
	 */
	public double averageCtr(DataSet dataset) {
		// Fill in your code here
		return 0.0;
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
