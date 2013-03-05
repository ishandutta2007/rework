package org.deflaux.facebook2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogisticRegression {
	static Logger logger = Logger.getLogger("LogisticRegressionWithHashing");

	static {
		logger.setLevel(Level.INFO);
	}

	/**
	 * Helper function to compute inner product w^Tx.
	 * 
	 * @param weights
	 * @param instance
	 * @return
	 */
	private static double computeWeightFeatureProduct(Weights weights,
			Set<Integer> featureids, DataInstance instance) {
		double dotProduct = 0.0;
		// dotProduct += weights.w0; // x0 = 1, so not bothering with w0*1
		// dotProduct += weights.wCost * instance.cost;
		for (int featureid : featureids) {
			dotProduct += weights.wHashedFeature[featureid]
					* instance.hashedTextFeature.get(featureid);
		}
		return dotProduct;
	}

	/**
	 * Apply delayed regularization to the weights corresponding to the given
	 * tokens.
	 * 
	 * @param featureids
	 * @param weights
	 * @param now
	 * @param step
	 * @param lambda
	 */
	private static void performDelayedRegularization(Set<Integer> featureids,
			Weights weights, int now, double step, double lambda) {
		for (int featureid : featureids) {
			Integer accessTime = weights.accessTime.get(featureid);
			if (null != accessTime) {
				Double featureWeight = weights.wHashedFeature[featureid];
				featureWeight = Math.pow((1 - step * lambda), now - accessTime
						- 1)
						* featureWeight;
				weights.wHashedFeature[featureid] = featureWeight;
			}
			weights.accessTime.put(featureid, now);
		}

	}

	/**
	 * Train the logistic regression model using the training data and the
	 * hyperparameters. Return the weights, and record the cumulative loss.
	 * 
	 * @param dataStream
	 * @param lambda
	 * @param step
	 * @return the weights for the model.
	 */
	public static Weights train(DataStream dataStream, int dim, double lambda,
			double step, ArrayList<Double> AvgLossPerEpoch) {
		// Fill in your code here
		Weights weights = new Weights(dim);
		int count = 0;
		int lossAccumulator = 0;
		int currentEpoch = 0;
		logger.info("Training on data in " + dataStream.pathPattern
				+ " with lambda " + lambda + " and step size " + step);

		DataInstance instance = null;
		while (dataStream.hasNext()) {
			instance = dataStream.nextInstance(instance, dim);
			if (!instance.isValid()) {
				continue;
			}

			if (currentEpoch != instance.epoch) {
				double avgLoss = (count == 0) ? 1.0 : (double) lossAccumulator / count;
				AvgLossPerEpoch.add(currentEpoch, avgLoss);
				logger.info("Trained (lambda:" + lambda + ", step:" + step
						+ ", dim:" + dim + ") with " + count
						+ " cases with avg loss for epoch "
						+ currentEpoch + ": " + avgLoss);
				currentEpoch = instance.epoch;
				lossAccumulator = 0;
				count = 0;
			}
			count++;

			Set<Integer> featureids = instance.hashedTextFeature.keySet();

			if (0 != lambda) {
				performDelayedRegularization(featureids, weights, count, step,
						lambda);
			}

			double exp = Math.exp(computeWeightFeatureProduct(weights,
					featureids, instance));
			exp = Double.isInfinite(exp) ? (Double.MAX_VALUE - 1) : exp;

			// Predict the label, record the loss
			int click_hat = (exp / (1 + exp)) > 0.5 ? 1 : 0;
			if (click_hat != instance.exists) {
				lossAccumulator += 1;
			}
			
			// Compute the gradient
			// TODO all examples are positive
			double gradient = (instance.exists == 1) ? (-1 / (1 + exp))
					: (exp / (1 + exp));

			// weights.w0 += -step * gradient; // no reg and assumed x0 = 1
			// weights.wCost += -step
			// * (lambda * weights.wCost + instance.cost * gradient);
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

		// Average loss for final epoch
		double avgLoss = (double) lossAccumulator / count;
		AvgLossPerEpoch.add(currentEpoch, avgLoss);
		logger.info("Trained (lambda:" + lambda + ", step:" + step
				+ ", dim:" + dim + ") with " + count
				+ " cases with avg loss for epoch "
				+ currentEpoch + ": " + avgLoss);

		// Final sweep of delayed regularization
		Set<Integer> allFeatures = new HashSet<Integer>();
		for (int i = 0; i < weights.wHashedFeature.length; i++) {
			allFeatures.add(i);
		}
		performDelayedRegularization(allFeatures, weights, count, step, lambda);

		return weights;
	}

	/**
	 * Using the weights to predict CTR in for the test paths.
	 * 
	 * @param weights
	 * @param dataStream
	 * @return An array storing the CTR for each datapoint in the test data.
	 */
	public static ArrayList<Double> predict(Weights weights, List<String> path) {
		ArrayList<Double> predictions = new ArrayList<Double>();

		for (int tailIdx = 0; tailIdx < path.size(); tailIdx++) {
			String head = null;
			if (tailIdx == path.size() - 1) {
				// We're at the end of this list
				if (1 < path.size()) {
					// Its not a single node path
					break;
				}
				head = path.get(tailIdx);
			} else {
				head = path.get(tailIdx + 1);
			}
			String tail = path.get(tailIdx);

			// TODO set epoch correctly
			// TODO add all vertices to data instance?
			DataInstance instance = new DataInstance(tail + "|" + head + "|0",
					16, weights.featuredim, false);
			double exp = Math.exp(computeWeightFeatureProduct(weights,
					instance.hashedTextFeature.keySet(), instance));
			predictions.add(exp / (1 + exp));
		}
		return predictions;
	}
}