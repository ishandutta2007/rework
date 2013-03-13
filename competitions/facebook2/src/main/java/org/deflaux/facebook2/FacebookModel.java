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
	int historyWindowSize;
	int numDimensions;
	Weights weights;
	int trainingCount;
	int epochCount;
	int currentEpoch;
	boolean finalSweepPerformed;
	ErrorMetrics errorMetrics;
	ArrayList<ErrorMetrics> errorMetricsPerEpoch;

	public FacebookModel(double step, double lambda, int historyWindowSize, int numDimensions) {
		this.step = step;
		this.lambda = lambda;
		this.historyWindowSize = historyWindowSize;
		this.numDimensions = numDimensions;
		weights = new Weights(historyWindowSize, numDimensions);
		errorMetrics = new ErrorMetrics();
		errorMetricsPerEpoch = new ArrayList<ErrorMetrics>();

		trainingCount = epochCount = currentEpoch = 0;
		finalSweepPerformed = false;
	}

	abstract int getInstanceLabel(DataInstance instance);

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

	void recordEpochErrorMetrics(int newEpoch) {
		errorMetricsPerEpoch.add(currentEpoch, errorMetrics);
		logger.info(this.getClass().getName() + " trained (lambda:" + lambda
				+ ", step:" + step + ", numDimensions:" + numDimensions
				+ ") with " + errorMetrics.getCount() + " cases with "
				+ errorMetrics.getLoss()
				+ " losses for an average loss for epoch " + currentEpoch
				+ ": " + errorMetrics.getAverageLoss());
		currentEpoch = newEpoch;
		errorMetrics = new ErrorMetrics();
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
			recordEpochErrorMetrics(instance.epoch);
		}
		trainingCount++;

		Set<Integer> featureids = instance.hashedTextFeature.keySet();

		int label = getInstanceLabel(instance);

		if (0 != lambda) {
			performDelayedRegularization(featureids, trainingCount);
		}

		double exp = Math
				.exp(computeWeightFeatureProduct(featureids, instance));
		exp = Double.isInfinite(exp) ? (Double.MAX_VALUE - 1) : exp;

		// Predict the label, record the loss
		int prediction = (exp / (1 + exp)) > 0.5 ? 1 : 0;
		errorMetrics.update(prediction, label);

		// Compute the gradient
		double gradient = (label == 1) ? (-1 / (1 + exp)) : (exp / (1 + exp));

		updateWeights(instance, gradient);
	}

	abstract void updateWeights(DataInstance instance, double gradient);

	void performFinalSweep() {
		// Average loss for final epoch
		recordEpochErrorMetrics(currentEpoch);

		// Final sweep of delayed regularization
		Set<Integer> allFeatures = new HashSet<Integer>();
		for (int i = 0; i < weights.wHashedFeature.length; i++) {
			allFeatures.add(i);
		}
		performDelayedRegularization(allFeatures, trainingCount);
		finalSweepPerformed = true;
	}

	/**
	 * Using the weights to predict CTR in for the test paths.
	 * 
	 * @param weights
	 * @param dataStream
	 * @return An array storing the CTR for each datapoint in the test data.
	 */
	public ArrayList<Double> predictPath(List<String> path, int epoch) {
		ArrayList<Double> predictions = new ArrayList<Double>();

		for (int tailIdx = 0; tailIdx < path.size(); tailIdx++) {
			String head = null;
			if (tailIdx == path.size() - 1) {
				// We're at the end of this list, time to stop
				if (1 == path.size()) {
					// Special case: for single node paths predict 1 for both
					// existence model and cost model because self-edges always
					// exist and they are always free
					predictions.add(1.0);
				}
				break;
			} else {
				head = path.get(tailIdx + 1);
			}
			String tail = path.get(tailIdx);

			// TODO add all vertices to data instance hash and then make one prediction per path as opposed to one prediction per edge?
			predictions.add(predictEdge(tail, head, epoch));
		}
		return predictions;
	}
	
	public Double predictEdge(String tail, String head, int epoch) {
		if (!finalSweepPerformed) {
			performFinalSweep();
		}

		String edgeKey = tail + "|" + head;
		DataInstance instance = new DataInstance(edgeKey,
				epoch, numDimensions, false, false);
		double exp = Math.exp(computeWeightFeatureProduct(
				instance.hashedTextFeature.keySet(), instance));
		return (exp / (1 + exp));
	}

	@Override
	public String toString() {
		int finalEpoch = errorMetricsPerEpoch.size();
		return this.getClass().getName() + " [step=" + step + ", lambda="
				+ lambda + ", numDimensions=" + numDimensions
				+ ", trainingCount=" + trainingCount + ", metrics for epoch " + finalEpoch + "="
				+ errorMetricsPerEpoch.get(finalEpoch-1) + "]";
	}
}