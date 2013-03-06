package org.deflaux.facebook2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Note: not thread safe
 * 
 * @author deflaux
 * 
 */
abstract public class FacebookModel {
	static Logger logger = Logger.getLogger("FacebookModel");

	double step;
	double lambda;
	int numDimensions;
	ArrayList<Double> avgLossPerEpoch;
	Weights weights;
	int trainingCount;
	int lossAccumulator;
	int epochCount;
	int currentEpoch;
	boolean finalSweepPerformed;

	public FacebookModel(double step, double lambda, int numDimensions) {
		this.step = step;
		this.lambda = lambda;
		this.numDimensions = numDimensions;
		weights = new Weights(numDimensions);
		avgLossPerEpoch = new ArrayList<Double>();
		trainingCount = epochCount = lossAccumulator = currentEpoch = 0;
		finalSweepPerformed = false;
	}

	abstract int getInstanceOutcome(DataInstance instance);

	/**
	 * Helper function to compute inner product w^Tx.
	 * 
	 * @param weights
	 * @param instance
	 * @return
	 */
	abstract double computeWeightFeatureProduct(Set<Integer> featureids,
			DataInstance instance);

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
	void performDelayedRegularization(Set<Integer> featureids, int now) {
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

	void recordEpochAverageLoss(int newEpoch) {
		int currentEpochCount = trainingCount - epochCount;
		epochCount += currentEpochCount;
		double avgLoss = (currentEpochCount == 0) ? 1.0
				: (double) lossAccumulator / currentEpochCount;
		avgLossPerEpoch.add(currentEpoch, avgLoss);
		logger.info(this.getClass().getName() + " trained (lambda:" + lambda
				+ ", step:" + step + ", numDimensions:" + numDimensions
				+ ") with " + currentEpochCount + " cases with "
				+ lossAccumulator + " losses for an average loss for epoch "
				+ currentEpoch + ": " + avgLoss);
		currentEpoch = newEpoch;
		lossAccumulator = 0;
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
	public void train(DataInstance instance) {
		if (currentEpoch != instance.epoch) {
			recordEpochAverageLoss(instance.epoch);
		}
		trainingCount++;

		Set<Integer> featureids = instance.hashedTextFeature.keySet();
		int outcome = getInstanceOutcome(instance);

		if (0 != lambda) {
			performDelayedRegularization(featureids, trainingCount);
		}

		double exp = Math
				.exp(computeWeightFeatureProduct(featureids, instance));
		exp = Double.isInfinite(exp) ? (Double.MAX_VALUE - 1) : exp;

		// Predict the label, record the loss
		int click_hat = (exp / (1 + exp)) > 0.5 ? 1 : 0;
		if (click_hat != outcome) {
			lossAccumulator += 1;
		}

		// sanity check
		if (15 == currentEpoch) {
			logger.info(this.getClass().getName() + "pred: " + click_hat
					+ " outcome: " + outcome);
		}

		// Compute the gradient
		double gradient = (outcome == 1) ? (-1 / (1 + exp)) : (exp / (1 + exp));

		updateWeights(instance, gradient);
	}

	abstract void updateWeights(DataInstance instance, double gradient);

	void performFinalSweep() {
		// Average loss for final epoch
		recordEpochAverageLoss(currentEpoch);

		// Final sweep of delayed regularization
		Set<Integer> allFeatures = new HashSet<Integer>();
		for (int i = 0; i < weights.wHashedFeature.length; i++) {
			allFeatures.add(i);
		}
		performDelayedRegularization(allFeatures, trainingCount);
	}

	/**
	 * Using the weights to predict CTR in for the test paths.
	 * 
	 * @param weights
	 * @param dataStream
	 * @return An array storing the CTR for each datapoint in the test data.
	 */
	public ArrayList<Double> predict(List<String> path) {

		if (!finalSweepPerformed) {
			performFinalSweep();
			finalSweepPerformed = true;
		}

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
			double exp = Math.exp(computeWeightFeatureProduct(
					instance.hashedTextFeature.keySet(), instance));
			predictions.add(exp / (1 + exp));
		}
		return predictions;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " [step=" + step + ", lambda="
				+ lambda + ", numDimensions=" + numDimensions
				+ ", trainingCount=" + trainingCount + ", avgLossPerEpoch="
				+ avgLossPerEpoch + "]";
	}
}