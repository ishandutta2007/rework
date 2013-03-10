package org.deflaux.facebook2;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author deflaux
 * 
 */
public class DataInstance {
	public static final int FREE_EDGE_COST = 1;
	public static final int PAID_EDGE_COST = 0;
	public static final int UNKNOWN_EDGE_COST = Integer.MIN_VALUE;

	public static final int EDGE_EXISTS = 1;
	public static final int EDGE_EXISTENCE_UNKNOWN = 0;

	public static final String KEY_SEPARATOR = "|";

	// See bloomFilterParameters.R for code to determine parameters to
	// BloomFilter
	static final int NUM_BITS_EDGE_HISTORY = 720000;
	static final int NUM_HASHES_EDGE_HISTORY = 10;
	public static final int DEFAULT_HISTORY_SLIDING_WINDOW_SIZE = 8;

	static Logger logger = Logger.getLogger("DataInstance");
	static int historyWindowSize;
	static HistorySlidingWindow edgeExistenceHistory;
	static HistorySlidingWindow edgeCostHistory;

	int numDimensions;
	int epoch;
	String tail;
	String head;
	String edgeKey;

	int cost; // Cost model label
	int exists; // Existence model label

	// TODO add head and tail into hash too
	// TODO if we continue to hash only one feature into this, we can just hold
	// the key and value, no need for the Map
	Map<Integer, Integer> hashedTextFeature; // map hashed feature key to its
												// value;

	static {
		logger.setLevel(Level.INFO);
		historyWindowSize = DEFAULT_HISTORY_SLIDING_WINDOW_SIZE;
		clearEdgeHistory();
	}

	public static int getHistoryWindowSize() {
		return historyWindowSize;
	}

	public static void setHistoryWindowSize(int historyWindowSize) {
		DataInstance.historyWindowSize = historyWindowSize;
	}

	public static void clearEdgeHistory() {
		edgeExistenceHistory = new HistorySlidingWindow(historyWindowSize,
				NUM_BITS_EDGE_HISTORY, NUM_HASHES_EDGE_HISTORY);
		edgeCostHistory = new HistorySlidingWindow(historyWindowSize,
				NUM_BITS_EDGE_HISTORY, NUM_HASHES_EDGE_HISTORY);
	}

	public DataInstance(int numDimensions) {
		hashedTextFeature = new HashMap<Integer, Integer>();
	}

	public DataInstance(String line, int epoch, int numDimensions,
			boolean hasLabel, boolean validate) {
		hashedTextFeature = new HashMap<Integer, Integer>();
		setValues(line, epoch, numDimensions, hasLabel, validate);
	}

	public static DataInstance reuse(DataInstance instance, String line,
			int epoch, int numDimensions, boolean hasLabel, boolean validate) {
		instance.hashedTextFeature.clear();
		instance.setValues(line, epoch, numDimensions, hasLabel, validate);
		return instance;
	}

	void setValues(String line, int epoch, int numDimensions, boolean hasLabel,
			boolean validate) {
		this.numDimensions = numDimensions;
		this.epoch = epoch;

		String[] fields = line.split("\\|");
		tail = fields[0];
		head = fields[1];
		if (hasLabel) {
			cost = Integer.valueOf(fields[2]);
			// Remap cost so that "free" is true in our logistic regression
			// model
			if (0 == cost) {
				cost = FREE_EDGE_COST;
			} else if (1 == cost) {
				cost = PAID_EDGE_COST;
			}
			exists = EDGE_EXISTS; // All links in training data "exist"
		} else {
			cost = UNKNOWN_EDGE_COST;
			exists = EDGE_EXISTENCE_UNKNOWN;
		}
		if (validate) {
			if (!isValid()) {
				logger.error("Invalid data instance: " + line);
			}
		}

		edgeKey = tail + KEY_SEPARATOR + head;
		if (EDGE_EXISTS == exists) {
			edgeExistenceHistory.recordHistory(edgeKey, this.epoch);
		}
		if (FREE_EDGE_COST == cost) {
			edgeCostHistory.recordHistory(edgeKey, this.epoch);
		}
		updateHashedTextFeature(edgeKey, 1);
		updateHashedTextFeature("head|" + head, 1);
		updateHashedTextFeature("tail|" + tail, 1);
	}

	public int[] getEdgeExistenceHistory() {
		return getEdgeHistory(edgeExistenceHistory, EDGE_EXISTS);
	}

	public int[] getEdgeCostHistory() {
		return getEdgeHistory(edgeCostHistory, FREE_EDGE_COST);
	}

	/**
	 * Helper function to walk history datastructures
	 * 
	 * @param edgeHistoryWindow
	 * @param value
	 * @return
	 */
	int[] getEdgeHistory(HistorySlidingWindow edgeHistoryWindow, int value) {
		// Values default to zero
		int edgeHistory[] = new int[historyWindowSize];
		// Do not use value for current epoch or else we are using the label AS
		// a feature ;-)
		for (int i = 1; i < historyWindowSize; i++) {
			int historyEpoch = epoch - i;
			if (edgeHistoryWindow.viewHistory(edgeKey, historyEpoch)) {
				edgeHistory[i] = value;
			}
		}
		return edgeHistory;
	}

	/**
	 * Helper function. Updates the feature hashmap with a given key and value.
	 * You can use HashUtil.hashToRange as h, and HashUtil.hashToSign as \xi.
	 * 
	 * @param key
	 * @param val
	 */
	void updateHashedTextFeature(String key, int val) {
		int hashKey = HashUtil.hashToRange(key, numDimensions);
		int sign = HashUtil.hashToSign(key);
		Integer featureValue = hashedTextFeature.get(hashKey);
		if (null == featureValue) {
			featureValue = 0;
		}
		featureValue += sign * val;
		hashedTextFeature.put(hashKey, featureValue);
	}

	/**
	 * Determine whether a DataInstance is valid.
	 */
	public boolean isValid() {
		boolean valid = true;
		String validationMessages = "";

		if (!(0 < tail.length())) {
			valid = false;
			validationMessages += ", Malformed tail value: " + tail;
		}

		if (!(0 < head.length())) {
			valid = false;
			validationMessages += ", Malformed head value: " + head;
		}

		if (!(FREE_EDGE_COST == cost || PAID_EDGE_COST == cost)) {
			valid = false;
			validationMessages += ", Malformed cost value: " + cost;
		}

		if (!(1 <= epoch && 15 >= epoch)) {
			valid = false;
			validationMessages += ", Malformed epoch value: " + epoch;
		}

		if (!((1 <= hashedTextFeature.size()) && (3 >= hashedTextFeature.size()))) {
			valid = false;
			validationMessages += ", Malformed hash feature: "
					+ hashedTextFeature.size();
		}

		if (!valid && logger.isDebugEnabled()) {
			logger.debug("Invalid data instance " + toString()
					+ validationMessages);
		}
		return valid;
	}

	@Override
	public String toString() {
		return "DataInstance [numDimensions=" + numDimensions + ", epoch="
				+ epoch + ", tail=" + tail + ", head=" + head + ", edgeKey="
				+ edgeKey + ", cost=" + cost + ", exists=" + exists
				+ ", hashedTextFeature=" + hashedTextFeature + "]";
	}
}
