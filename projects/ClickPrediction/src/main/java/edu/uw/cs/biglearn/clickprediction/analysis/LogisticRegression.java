package edu.uw.cs.biglearn.clickprediction.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.print.attribute.standard.DateTimeAtCompleted;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import edu.uw.cs.biglearn.clickprediction.util.EvalUtil;

public class LogisticRegression {
	static final int LOSS_AVG_INTERVAL = 100;
	static Logger logger = Logger.getLogger("LogisticRegression");

	/**
	 * This class represents the weights in the logistic regression model.
	 * 
	 * @author haijieg
	 * 
	 */
	public class Weights {
		double w0;
		/*
		 * query.get("123") will return the weight for the feature:
		 * "token 123 in the query field".
		 */
		Map<Integer, Double> wTokens;
		double wPosition;
		double wDepth;
		double wAge;
		double wGender;

		Map<Integer, Integer> accessTime; // keep track of the access timestamp
											// of feature weights.
											// Using this to do delayed
											// regularization.

		public Weights() {
			w0 = wAge = wGender = wDepth = wPosition = 0.0;
			wTokens = new HashMap<Integer, Double>();
			accessTime = new HashMap<Integer, Integer>();
		}

		@Override
		public String toString() {
			DecimalFormat myFormatter = new DecimalFormat("###.##");
			StringBuilder builder = new StringBuilder();
			builder.append("Intercept: " + myFormatter.format(w0) + "\n");
			builder.append("Depth: " + myFormatter.format(wDepth) + "\n");
			builder.append("Position: " + myFormatter.format(wPosition) + "\n");
			builder.append("Gender: " + myFormatter.format(wGender) + "\n");
			builder.append("Age: " + myFormatter.format(wAge) + "\n");
			builder.append("Tokens: " + wTokens.toString() + "\n");
			return builder.toString();
		}

		/**
		 * @return the l2 norm of this weight vector.
		 */
		public double l2norm() {
			double l2 = w0 * w0 + wAge * wAge + wGender * wGender + wDepth
					* wDepth + wPosition * wPosition;
			for (double w : wTokens.values())
				l2 += w * w;
			return Math.sqrt(l2);
		}

		/**
		 * @return the l0 norm of this weight vector.
		 */
		public int l0norm() {
			return 4 + wTokens.size();
		}

		public void runningAverage(Weights currentWeight, int[] tokens,
				int count) {
			double impact = 1 / count;
			this.w0 = this.w0 - impact * (this.w0 - currentWeight.w0);
			this.wDepth = this.wDepth - impact
					* (this.wDepth - currentWeight.wDepth);
			this.wPosition = this.wPosition - impact
					* (this.wPosition - currentWeight.wPosition);
			// Note: we may not have changed the weights for age and gender so
			// these two updates could be skipped in those cases
			this.wAge = this.wAge - impact * (this.wAge - currentWeight.wAge);
			this.wGender = this.wGender - impact
					* (this.wGender - currentWeight.wGender);

			// Only update averages for tokens in this instance
			for (int token : tokens) {
				Double wTokenPrevious = this.wTokens.get(token); // can be null
				double wTokenCurrent = currentWeight.wTokens.get(token);
				if (null == wTokenPrevious) {
					wTokenPrevious = 0.0;
				}
				this.wTokens.put(token, wTokenPrevious - impact
						* (wTokenPrevious - wTokenCurrent));
			}
		}
	}

	/**
	 * Helper function to compute inner product w^Tx.
	 * 
	 * @param weights
	 * @param instance
	 * @return
	 */
	private double computeWeightFeatureProduct(Weights weights,
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
			if (weights.wTokens.containsKey(token)) {
				dotProduct += weights.wTokens.get(token);
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
	private void performDelayedRegularization(int[] tokens, Weights weights,
			int now, double step, double lambda) {
		// Fill in your code here.
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
	public Weights train(DataSet dataset, double lambda, double step,
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

		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			count++;
			if (count % LOSS_AVG_INTERVAL == 0) {
				double avgLoss = lossAccumulator / LOSS_AVG_INTERVAL;
				AvgLoss.add(avgLoss);
				logger.info("Trained (lambda:"
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
				// TODO regularize tokens
				performDelayedRegularization(instance.tokens, weights, count,
						step, lambda);
			}

			double prediction = computePrediction(weights, instance);

			// Only update our weights if the prediction did not match the
			// outcome
			if (prediction != instance.clicked) {

				// TODO getting confused by ascent/descent, is this the correct
				// order for use in the weight update formula?
				// Is the issue log likelihood see Equation from page 29 of
				// http://www.cs.washington.edu/education/courses/cse599c1/13wi/slides/perceptron-kernels-sgd-annotated.pdf
				// versus negative log likelihood in KN p 247 eqn 8.5
				double error = instance.clicked - prediction;

				lossAccumulator += Math.pow((error), 2);

				weights.w0 = weights.w0 + step * error; // no reg and assumed x0 = 1 
				weights.wDepth = weights.wDepth
						+ step
						* ((-1 * lambda * weights.wDepth) + instance.depth
								* (error));
				weights.wPosition = weights.wPosition
						+ step
						* ((-1 * lambda * weights.wPosition) + instance.position
								* (error));
				if (instance.age != DataInstance.MISSING_AGE) { // TODO correct?
					weights.wAge = weights.wAge
							+ step
							* ((-1 * lambda * weights.wAge) + instance.age
									* (error));
				}
				if (instance.gender != DataInstance.MISSING_GENDER) { // TODO correct?
					weights.wGender = weights.wGender
							+ step
							* ((-1 * lambda * weights.wGender) + instance.gender
									* (error));
				}
				for (int token : instance.tokens) {
					Double tokenWeight = weights.wTokens.get(token); // can be null
					if (null == tokenWeight) {
						tokenWeight = 0.0;
					}
					weights.wTokens.put(token, tokenWeight + step
							* error);
				}
				weightAccumulator.runningAverage(weights, instance.tokens, count);
			}
		}
		// Do not return the running average of weights for this HW problem
		return weights; // weightAccumulator; 
	}

	/**
	 * Using the weights to predict CTR in for the test dataset.
	 * 
	 * @param weights
	 * @param dataset
	 * @return An array storing the CTR for each datapoint in the test data.
	 */
	public ArrayList<Double> predict(Weights weights, DataSet dataset) {
		ArrayList<Double> predictions = new ArrayList<Double>();

		while (dataset.hasNext()) {
			DataInstance instance = dataset.nextInstance();
			predictions.add(computePrediction(weights, instance));
		}
		return predictions;
	}

	private double computePrediction(Weights weights, DataInstance instance) {
		double partialResult = Math.exp(computeWeightFeatureProduct(weights,
				instance));
		// TODO is this correct or should it be 1 / (1 + partialResult)
		// see page 246 in KM versus formula in homework question 1.2 (a)
		// see page 21 in KM
		return partialResult / (1 + partialResult);
	}

	public static void main(String args[]) throws IOException {
		// Fill in your code here
		System.out.println("See unit tests for answers to homework questions");

	}
}