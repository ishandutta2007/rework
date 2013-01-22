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
	public static Set<Integer> uniqTokens(DataSet dataset,
			boolean discardInvalidDataInstances) {
		int count = 0;
		Set<Integer> tokens = new HashSet<Integer>();
		logger.info("Counting unique tokens in " + dataset.path + " ... ");
		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % 100000 == 0) {
				logger.info("Counted " + count + " lines");
			}
			if (discardInvalidDataInstances && !instance.isValid()) {
				continue;
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
		return tokens;
	}

	/**
	 * Return the unique users in the dataset.
	 * 
	 * @param dataset
	 * @return
	 */
	public static Set<Integer> uniqUsers(DataSet dataset,
			boolean discardInvalidDataInstances) {
		int count = 0;
		Set<Integer> users = new HashSet<Integer>();
		logger.info("Counting unique users in " + dataset.path + " ... ");
		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % 100000 == 0) {
				logger.info("Counted " + count + " lines");
			}
			if (discardInvalidDataInstances && !instance.isValid()) {
				continue;
			}
			users.add(instance.userid);
		}
		if (count < dataset.size) {
			logger.error("The real size of the data is less than the input size: "
					+ dataset.size + "<" + count);
		}
		logger.info("Done. Total processed instances: " + count);
		if(users.contains(DataInstance.MISSING_USER_ID)) {
			users.remove(DataInstance.MISSING_USER_ID);
		}
		return users;
	}

	/**
	 * @return the average CTR for the training set.
	 */
	public static double averageCtr(DataSet dataset,
			boolean discardInvalidDataInstances) {
		int count = 0;
		int clicks = 0;
		logger.info("Counting clicks in " + dataset.path + " ... ");
		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % 100000 == 0) {
				logger.info("Counted " + count + " lines");
			}
			if (discardInvalidDataInstances && !instance.isValid()) {
				continue;
			}
			
			switch(instance.clicked) {
			case 0:
				break;
			case 1:
				clicks++;
				break;
			default:
				// note that click values are -1 in the test data instances, but we should not be passing that data to this method
				throw new RuntimeException("invalid click data value: " + instance.clicked);
			}
		}
		if (count < dataset.size) {
			logger.error("The real size of the data is less than the input size: "
					+ dataset.size + "<" + count);
		}
		logger.info("Done. Total processed instances: " + count);
		logger.info("Clicks: " + clicks);
		return (double) clicks / (double) count;
	}

	public static void main(String args[]) {
		System.out.println("See unit tests for answers to warmup questions");
	}
}
