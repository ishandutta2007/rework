package edu.uw.cs.biglearn.clickprediction.analysis;

import java.io.IOException;
import java.util.ArrayList;
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
		if (instance.age != DataInstance.MISSING_AGE) {
			dotProduct += weights.wAge * instance.age;
		}
		if (instance.gender != DataInstance.MISSING_GENDER) {
			dotProduct += weights.wGender * instance.gender;
		}
		for (int featureid : instance.hashedTextFeature.keySet()) {
			dotProduct += weights.wHashedFeature[featureid];
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
				featureWeight -= Math.pow((1 - step * lambda), now - accessTime
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
		double lossAccumulator = 0.0;
		AvgLoss.add(lossAccumulator);
		logger.info("Training on data in " + dataset.path + " with lambda "
				+ lambda + " and step size " + step);

		while (dataset.hasNext()) {
			HashedDataInstance instance = dataset.nextHashedInstance(dim,
					personalized);
			count++;
			if (count % LOSS_AVG_INTERVAL == 0) {
				double avgLoss = lossAccumulator / LOSS_AVG_INTERVAL;
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
				lossAccumulator = 0.0;
			}

			double prediction = computePrediction(weights, instance);

			// Only update our weights if the prediction did not match the
			// outcome
			if (prediction != instance.clicked) {

				if (0 != lambda) {
					performDelayedRegularization(
							instance.hashedTextFeature.keySet(), weights,
							count, step, lambda);
				}

				double error = instance.clicked - prediction;

				lossAccumulator += Math.pow((error), 2);

				weights.w0 = weights.w0 + step * error; // no reg and assumed x0
														// = 1
				weights.wDepth = weights.wDepth
						+ step
						* ((-1 * lambda * weights.wDepth) + instance.depth
								* (error));
				weights.wPosition = weights.wPosition
						+ step
						* ((-1 * lambda * weights.wPosition) + instance.position
								* (error));
				if (instance.age != DataInstance.MISSING_AGE) {
					weights.wAge = weights.wAge
							+ step
							* ((-1 * lambda * weights.wAge) + instance.age
									* (error));
				}
				if (instance.gender != DataInstance.MISSING_GENDER) {
					weights.wGender = weights.wGender
							+ step
							* ((-1 * lambda * weights.wGender) + instance.gender
									* (error));
				}
				for (int featureid : instance.hashedTextFeature.keySet()) {
					// Can be null if this is this data instance is the first
					// time we've seen this feature
					double featureWeight = weights.wHashedFeature[featureid];
					weights.wHashedFeature[featureid] = featureWeight + step
							* error;
				}
			}
		}
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
			predictions.add(computePrediction(weights, instance));
		}
		return predictions;
	}

	private static double computePrediction(WeightsWithHashedFeatures weights,
			HashedDataInstance instance) {
		double partialResult = Math.exp(computeWeightFeatureProduct(weights,
				instance));
		return partialResult / (1 + partialResult);
	}

	public static void main(String args[]) throws IOException {
		// Fill in your code here
		System.out.println("See unit tests for answers to homework questions");

	}
}