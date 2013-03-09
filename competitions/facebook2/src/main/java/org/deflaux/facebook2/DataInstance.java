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
	public static final int UNKNOWN_EDGE_COST = Integer.MAX_VALUE;
	
	public static final int EDGE_EXISTS = 1;
	public static final int EDGE_EXISTENCE_UNKNOWN = 0;

	public static final String KEY_SEPARATOR = "|";
	
	// See bloomFilterParameters.R for code to determine parameters to
	// BloomFilter
	static final int NUM_BITS_EDGE_HISTORY = 720000;
	static final int NUM_HASHES_EDGE_HISTORY = 10;
	public static final int HISTORY_SLIDING_WINDOW_SIZE = 5;
	
	static Logger logger = Logger.getLogger("DataInstance");
	static HistorySlidingWindow edgesHistory;

	int featuredim;
	int epoch;
	String tail;
	String head;
	String edgeKey;

	int cost; // Cost model label
	int exists; // Existence model label

	// TODO if we continue to hash only one feature into this, we can just hold
	// the key and value, no need for the Map
	Map<Integer, Integer> hashedTextFeature; // map hashed feature key to its
												// value;

	static {
		logger.setLevel(Level.INFO);
		clearEdgeHistory();
	}
	
	public static void clearEdgeHistory() {
		edgesHistory = new HistorySlidingWindow(HISTORY_SLIDING_WINDOW_SIZE,
				NUM_BITS_EDGE_HISTORY, NUM_HASHES_EDGE_HISTORY);		
	}

	public DataInstance(int dim) {
		hashedTextFeature = new HashMap<Integer, Integer>();
	}

	public DataInstance(String line, int epoch, int dim, boolean hasLabel,
			boolean validate) {
		hashedTextFeature = new HashMap<Integer, Integer>();
		setValues(line, epoch, dim, hasLabel, validate);
	}

	public static DataInstance reuse(DataInstance instance, String line,
			int epoch, int dim, boolean hasLabel, boolean validate) {
		instance.hashedTextFeature.clear();
		instance.setValues(line, epoch, dim, hasLabel, validate);
		return instance;
	}

	void setValues(String line, int epoch, int dim, boolean hasLabel,
			boolean validate) {
		this.featuredim = dim;
		this.epoch = epoch;

		String[] fields = line.split("\\|");
		this.tail = fields[0];
		this.head = fields[1];
		if (hasLabel) {
			this.cost = Integer.valueOf(fields[2]);
			this.exists = EDGE_EXISTS; // All links in training data "exist" 								// hardcoded to 1
		} else {
			this.cost = UNKNOWN_EDGE_COST;
			this.exists = EDGE_EXISTENCE_UNKNOWN;
		}

		edgeKey = tail + KEY_SEPARATOR + head;
		if (EDGE_EXISTS == this.exists) {
			edgesHistory.recordHistory(edgeKey, this.epoch);
		}
		updateFeature(edgeKey, 1);

		if (validate) {
			if (!isValid()) {
				logger.error("Invalid data instance: " + line);
			}
		}

		// Remap cost so that "free" is true in our logistic regression model
		cost = (0 == cost) ? FREE_EDGE_COST: PAID_EDGE_COST;

	}

	public int[] getEdgeHistory() {
		// Values default to zero
		int edgeHistory[] = new int[HISTORY_SLIDING_WINDOW_SIZE];
		for (int i = 0; i < HISTORY_SLIDING_WINDOW_SIZE; i++) {
			int historyEpoch = epoch - i;
			if (edgesHistory.viewHistory(edgeKey, historyEpoch)) {
				edgeHistory[i] = EDGE_EXISTS;
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
	void updateFeature(String key, int val) {
		int hashKey = HashUtil.hashToRange(key, featuredim);
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

		if (!(1 == hashedTextFeature.size())) {
			valid = false;
			validationMessages += ", Malformed hash feature: "
					+ hashedTextFeature.size();
		}

		if (!valid) {
			logger.debug("Invalid data instance " + toString()
					+ validationMessages);
		}
		return valid;
	}

	@Override
	public String toString() {
		return "DataInstance [featuredim=" + featuredim + ", tail=" + tail
				+ ", head=" + head + ", cost=" + cost + ", epoch=" + epoch
				+ ", hashedTextFeature=" + hashedTextFeature + "]";
	}
}
