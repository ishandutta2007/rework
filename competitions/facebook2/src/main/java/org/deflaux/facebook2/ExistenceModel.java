package org.deflaux.facebook2;

import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Note: not thread safe
 * 
 * @author deflaux
 *
 */
public class ExistenceModel extends FacebookModel {
	static Logger logger = Logger.getLogger("ExistenceModel");

	public ExistenceModel(double step, double lambda, int historyWindowSize, int numDimensions) {
		super(step, lambda, historyWindowSize, numDimensions);
	}

	int getInstanceLabel(DataInstance instance) {
		return instance.exists;
	}

	/**
	 * Helper function to compute inner product w^Tx.
	 * 
	 * @param weights
	 * @param instance
	 * @return
	 */
	double computeWeightFeatureProduct(Set<Integer> featureids,
			DataInstance instance) {
		double dotProduct = 0.0;
		// dotProduct += weights.w0; // x0 = 1, so not bothering with w0*1
		for (int featureid : featureids) {
			dotProduct += weights.wHashedFeature[featureid]
					* instance.hashedTextFeature.get(featureid);
		}
		return dotProduct;
	}

	
	void updateWeights(DataInstance instance, double gradient) {
		// weights.w0 += -step * gradient; // no reg and assumed x0 = 1
		for (int featureid : instance.hashedTextFeature.keySet()) {
			// Can be null if this is this data instance is the first
			// time we've seen this feature
			double featureWeight = weights.wHashedFeature[featureid];
			weights.wHashedFeature[featureid] = featureWeight
					+ -step
					* (gradient * instance.hashedTextFeature.get(featureid) + lambda
							* featureWeight);
		}
	}
}