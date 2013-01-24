package edu.uw.cs.biglearn.clickprediction.analysis;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the weights in the logistic regression model.
 * 
 * @author haijieg, deflaux
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