package org.deflaux.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.ArrayUtils;

public class LogisticRegression {
	static final int LOSS_AVG_INTERVAL = 100;
	static Logger logger = Logger.getLogger("LogisticRegression");

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
			DataInstance instance) {
		// Fill in your code here
		double dotProduct = 0.0;
		dotProduct += weights.w0; // x0 = 1, so not bothering with w0*1
		dotProduct += weights.wDepth * instance.depth;
		dotProduct += weights.wPosition * instance.position;
		dotProduct += weights.wAge * instance.age;
		dotProduct += weights.wGender * instance.gender;
		for (int token : instance.tokens) {
			Double tokenWeight = weights.wTokens.get(token);
			if (null != tokenWeight) {
				dotProduct += tokenWeight;
				// if token is present, its "value" is 1, otherwise zero
				// since x_token = 1, not bothering with w_token*1
			}
		}
		return dotProduct;
	}

	/**
	 * Apply delayed regularization to the weights corresponding to the given
	 * tokens.
	 * 
	 * @param tokens
	 * @param weights
	 * @param now
	 * @param step
	 * @param lambda
	 */
	private static void performDelayedRegularization(int[] tokens,
			Weights weights, int now, double step, double lambda) {
		// Fill in your code here.
		for (int token : tokens) {
			Integer accessTime = weights.accessTime.get(token);
			if (null != accessTime) {
				Double tokenWeight = weights.wTokens.get(token);
				tokenWeight = Math.pow((1 - step * lambda), now - accessTime
						- 1)
						* tokenWeight;
				weights.wTokens.put(token, tokenWeight);
			}
			weights.accessTime.put(token, now);
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
	public static Weights train(DataSet dataset, double lambda, double step,
			ArrayList<Double> AvgLoss) {

		// Fill in your code here. The structure should look like:
		// For each data point: {
		// Your code: perform delayed regularization
		// Your code: predict the label, record the loss
		// Your code: compute w0 + <w, x>, and gradient
		// Your code: update weights along the negative gradient
		// }

		Weights weights = new Weights();
		Weights weightAccumulator = new Weights();
		int count = 0;
		double lossAccumulator = 0.0;
		AvgLoss.add(lossAccumulator);
		logger.info("Training on data in " + dataset.path + " with lambda "
				+ lambda + " and step size " + step);

		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % LOSS_AVG_INTERVAL == 0) {
				double avgLoss = lossAccumulator / LOSS_AVG_INTERVAL;
				AvgLoss.add(avgLoss);
				logger.debug("Trained (lambda:"
						+ lambda
						+ ", step:"
						+ step
						+ ") with "
						+ count
						+ " cases so far with avg loss for this 100 case interval: "
						+ avgLoss);
				lossAccumulator = 0.0;
			}

			if (0 != lambda) {
				performDelayedRegularization(instance.tokens, weights, count,
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

			// Update weights along the negative gradient
			weights.w0 += -step * gradient; // no reg and assumed x0
											// = 1
			weights.wDepth += -step
					* (lambda * weights.wDepth + instance.depth * gradient);
			weights.wPosition += -step
					* (lambda * weights.wPosition + instance.position
							* gradient);
			weights.wAge += -step
					* (lambda * weights.wAge + instance.age * gradient);
			weights.wGender += -step
					* (lambda * weights.wGender + instance.gender * gradient);
			for (int token : instance.tokens) {
				// Can be null if this is this data instance is the first
				// time we've seen this token
				Double tokenWeight = weights.wTokens.get(token);
				if (null == tokenWeight) {
					tokenWeight = 0.0;
				}
				weights.wTokens.put(token, tokenWeight + -step
						* (gradient + lambda * tokenWeight));
			}
			weightAccumulator.runningAverage(weights, instance.tokens, count);
		}

		// Final sweep of delayed regularization
		Set<Integer> allFeatures = weights.wTokens.keySet();
		performDelayedRegularization(
				ArrayUtils.toPrimitive((Integer[]) allFeatures
						.toArray(new Integer[0])), weights, count, step, lambda);

		// Do _not_ return the running average of weights for this HW problem
		// even though in real life, the averaged weights are better
		return weights; // weightAccumulator;
	}

	/**
	 * Using the weights to predict CTR in for the test dataset.
	 * 
	 * @param weights
	 * @param dataset
	 * @return An array storing the CTR for each datapoint in the test data.
	 */
	public static ArrayList<Double> predict(Weights weights, DataSet dataset) {
		// Fill in your code here
		ArrayList<Double> predictions = new ArrayList<Double>();

		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			double exp = Math
					.exp(computeWeightFeatureProduct(weights, instance));
			predictions.add(exp / (1 + exp));
		}
		return predictions;
	}

	public static void main(String args[]) throws IOException {
		// Fill in your code here
		System.out.println("See unit tests for answers to homework questions");
	}
}