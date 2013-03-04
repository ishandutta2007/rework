package org.deflaux.facebook2;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DataInstance {
	@Override
	public String toString() {
		return "DataInstance [featuredim=" + featuredim + ", tail=" + tail
				+ ", head=" + head + ", cost=" + cost + ", epoch=" + epoch
				+ ", hashedTextFeature=" + hashedTextFeature + "]";
	}

	public static final int FREE_LINK = -1;
	public static final int PAID_LINK = 1;

	static Logger logger = Logger.getLogger("DataInstance");

	int featuredim;

	String tail;
	String head;
	int cost;
	int exists;
	int epoch;
	Map<Integer, Integer> hashedTextFeature; // map hashed feature key to its
												// value;

	public DataInstance(String line, int epoch, int dim, boolean validate) {
		String[] fields = line.split("\\|");
		tail = fields[0];
		head = fields[1];
		cost = Integer.valueOf(fields[2]);
		if (0 == cost) {
			cost = FREE_LINK;
		}

		this.epoch = epoch;
		this.exists = 1; // All links in training data "exist" so this is hardcoded to 1

		featuredim = dim;

		hashedTextFeature = new HashMap<Integer, Integer>(featuredim);
		updateFeature(tail + "|" + head, cost);

		if (validate) {
			if (!isValid()) {
				logger.error("Invalid data instance: " + line);
			}
		}
	}

	/**
	 * Helper function. Updates the feature hashmap with a given key and value.
	 * You can use HashUtil.hashToRange as h, and HashUtil.hashToSign as \xi.
	 * 
	 * @param key
	 * @param val
	 */
	private void updateFeature(String key, int val) {
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
			logger.warn("Invalid data instance " + toString()
					+ validationMessages);
		}
		return valid;
	}
}
