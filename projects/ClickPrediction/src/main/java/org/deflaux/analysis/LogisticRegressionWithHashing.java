package org.deflaux.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogisticRegressionWithHashing {
	static final int LOSS_AVG_INTERVAL = 100;
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
	private static double computeWeightFeatureProduct(
			WeightsWithHashedFeatures weights, HashedDataInstance instance) {
		// Fill in your code here
		double dotProduct = 0.0;
		dotProduct += weights.w0; // x0 = 1, so not bothering with w0*1
		dotProduct += weights.wDepth * instance.depth;
		dotProduct += weights.wPosition * instance.position;
		dotProduct += weights.wAge * instance.age;
		dotProduct += weights.wGender * instance.gender;
		for (int featureid : instance.hashedTextFeature.keySet()) {
			dotProduct += weights.wHashedFeature[featureid] * instance.hashedTextFeature.get(featureid);
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
			WeightsWithHashedFeatures weights, int now, double step,
			double lambda) {
		// Fill in your code here
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
	 * @param dataset
	 * @param lambda
	 * @param step
	 * @return the weights for the model.
	 */
	public static WeightsWithHashedFeatures train(DataSet dataset, int dim,
			double lambda, double step, ArrayList<Double> AvgLoss,
			boolean personalized) {
		// Fill in your code here
		WeightsWithHashedFeatures weights = new WeightsWithHashedFeatures(dim);
		int count = 0;
		int lossAccumulator = 0;
		AvgLoss.add(0.0);
		logger.info("Training on data in " + dataset.path + " with lambda "
				+ lambda + " and step size " + step);

		while (dataset.hasNext()) {
			HashedDataInstance instance = dataset.nextHashedInstance(dim,
					personalized);
			count++;
			if (count % LOSS_AVG_INTERVAL == 0) {

				double avgLoss = (double) lossAccumulator / LOSS_AVG_INTERVAL;
				AvgLoss.add(avgLoss);
				logger.debug("Trained (lambda:"
						+ lambda
						+ ", step:"
						+ step
						+ ", dim:"
						+ dim
						+ ") with "
						+ count
						+ " cases so far with avg loss for this 100 case interval: "
						+ avgLoss);
				lossAccumulator = 0;
			}

			if (0 != lambda) {
				performDelayedRegularization(
						instance.hashedTextFeature.keySet(), weights, count,
						step, lambda);
			}

			double exp = Math
					.exp(computeWeightFeatureProduct(weights, instance));
			exp = Double.isInfinite(exp) ? (Double.MAX_VALUE - 1) : exp;

			// Compute the gradient
			double gradient = (instance.clicked == 1) ? (-1 / (1 + exp))
					: (exp / (1 + exp));

			// Predict the label, record the loss
			int click_hat = (exp / (1 + exp)) > 0.5 ? 1 : 0;
			if (click_hat != instance.clicked) {
				lossAccumulator += 1;
			}

			weights.w0 += -step * gradient; // no reg and assumed x0 = 1
			weights.wDepth += -step
					* (lambda * weights.wDepth + instance.depth * gradient);
			weights.wPosition += -step
					* (lambda * weights.wPosition + instance.position
							* gradient);
			weights.wAge += -step
					* (lambda * weights.wAge + instance.age * gradient);
			weights.wGender += -step
					* (lambda * weights.wGender + instance.gender * gradient);
			for (int featureid : instance.hashedTextFeature.keySet()) {
				// Can be null if this is this data instance is the first
				// time we've seen this feature
				double featureWeight = weights.wHashedFeature[featureid];
				weights.wHashedFeature[featureid] = featureWeight + -step
						* (gradient * instance.hashedTextFeature.get(featureid) + lambda * featureWeight);
			}
		}

		// Final sweep of delayed regularization
		Set<Integer> allFeatures = new HashSet<Integer>();
		for (int i = 0; i < weights.wHashedFeature.length; i++) {
			allFeatures.add(i);
		}
		performDelayedRegularization(allFeatures, weights, count, step, lambda);

		return weights;
	}

	/**
	 * Using the weights to predict CTR in for the test dataset.
	 * 
	 * @param weights
	 * @param dataset
	 * @return An array storing the CTR for each datapoint in the test data.
	 */
	public static ArrayList<Double> predict(WeightsWithHashedFeatures weights,
			DataSet dataset, boolean personalized) {
		// Fill in your code here
		ArrayList<Double> predictions = new ArrayList<Double>();

		while (dataset.hasNext()) {
			HashedDataInstance instance = dataset.nextHashedInstance(
					weights.featuredim, personalized);
            double exp = Math.exp(computeWeightFeatureProduct(weights, instance));
			predictions.add(exp / (1 + exp));
		}
		return predictions;
	}

	public static void main(String args[]) throws IOException {
		// Fill in your code here
		System.out.println("See unit tests for answers to homework questions");

	}
}