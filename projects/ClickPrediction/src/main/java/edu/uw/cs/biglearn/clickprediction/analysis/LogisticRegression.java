package edu.uw.cs.biglearn.clickprediction.analysis;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
		if (instance.age != DataInstance.MISSING_AGE) {
			dotProduct += weights.wAge * instance.age;
		}
		if (instance.gender != DataInstance.MISSING_GENDER) {
			dotProduct += weights.wGender * instance.gender;
		}
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
				tokenWeight -= Math.pow((1 - step * lambda), now - accessTime
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

			double prediction = computePrediction(weights, instance);

			// Only update our weights if the prediction did not match the
			// outcome
			if (prediction != instance.clicked) {

				if (0 != lambda) {
					// TODO regularize tokens
					performDelayedRegularization(instance.tokens, weights,
							count, step, lambda);
				}

				// TODO getting confused by ascent/descent, is this the correct
				// order for use in the weight update formula?
				// Is the issue log likelihood see Equation from page 29 of
				// http://www.cs.washington.edu/education/courses/cse599c1/13wi/slides/perceptron-kernels-sgd-annotated.pdf
				// versus negative log likelihood in KN p 247 eqn 8.5?
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
				for (int token : instance.tokens) {
					// Can be null if this is this data instance is the first
					// time we've seen this token
					Double tokenWeight = weights.wTokens.get(token);
					if (null == tokenWeight) {
						tokenWeight = 0.0;
					}
					weights.wTokens.put(token, tokenWeight + step * error);
				}
				weightAccumulator.runningAverage(weights, instance.tokens,
						count);
			}
		}
		// Do _not_ return the running average of weights for this HW problem
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
			predictions.add(computePrediction(weights, instance));
		}
		return predictions;
	}

	private static double computePrediction(Weights weights,
			DataInstance instance) {
		double partialResult = Math.exp(computeWeightFeatureProduct(weights,
				instance));
		return partialResult / (1 + partialResult);
	}

	public static void main(String args[]) throws IOException {
		// Fill in your code here
		System.out.println("See unit tests for answers to homework questions");
	}
}