package org.deflaux.facebook2;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DataInstance {

	public static final int FREE_LINK = 1;
	public static final int PAID_LINK = 0;

	static Logger logger = Logger.getLogger("DataInstance");

	int featuredim;

	String tail;
	String head;
	int cost;
	int exists;
	int epoch;

	// TODO if we continue to hash only one feature into this, we can just hold
	// the key and value, no need for the Map
	Map<Integer, Integer> hashedTextFeature; // map hashed feature key to its
												// value;

	static {
		logger.setLevel(Level.INFO);
	}

	public DataInstance(int dim) {
		hashedTextFeature = new HashMap<Integer, Integer>();
	}

	public DataInstance(String line, int epoch, int dim, boolean validate) {
		hashedTextFeature = new HashMap<Integer, Integer>();
		setValues(line, epoch, dim, validate);
	}

	public static DataInstance reuse(DataInstance instance, String line,
			int epoch, int dim, boolean validate) {
		instance.hashedTextFeature.clear();
		instance.setValues(line, epoch, dim, validate);
		return instance;
	}

	void setValues(String line, int epoch, int dim, boolean validate) {
		String[] fields = line.split("\\|");
		tail = fields[0];
		head = fields[1];
		cost = Integer.valueOf(fields[2]);

		this.epoch = epoch;
		this.exists = 1; // All links in training data "exist" so this is
							// hardcoded to 1

		featuredim = dim;

		updateFeature(tail + "|" + head, 1);

		if (validate) {
			if (!isValid()) {
				logger.error("Invalid data instance: " + line);
			}
		}

		// Remap cost so that "free" is true in our logistic regression model
		cost = (0 == cost) ? FREE_LINK : PAID_LINK;

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

		if (!(FREE_LINK == cost || PAID_LINK == cost)) {
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
