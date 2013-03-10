package org.deflaux.facebook2;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Weights {
	double w0;
	double[] wHistoryFeature;
	double[] wHashedFeature;
	/**
	 * Keep track of the access timestamp of feature weights. Using this to do
	 * delayed regularization.
	 */
	Map<Integer, Integer> accessTime;

	public Weights(int historyWindowSize, int numDimensions) {
		w0 = 0.0;
		wHistoryFeature = new double[historyWindowSize];
		wHashedFeature = new double[numDimensions];
		accessTime = new HashMap<Integer, Integer>();
	}

	@Override
	public String toString() {
		DecimalFormat formatter = new DecimalFormat("###.##");
		StringBuilder builder = new StringBuilder();
		builder.append("Intercept: " + formatter.format(w0) + "\n");
		builder.append("HistoryFeature: ");
		for (double w : wHistoryFeature)
			builder.append(w + " ");
		builder.append("\n");
		builder.append("HashedFeature: ");
		for (double w : wHashedFeature)
			builder.append(w + " ");
		builder.append("\n");
		return builder.toString();
	}

	/**
	 * @return the l2 norm of this weight vector.
	 */
	public double l2norm() {
		double l2 = w0 * w0;
		for (double w : wHistoryFeature)
			l2 += w * w;
		for (double w : wHashedFeature)
			l2 += w * w;
		return Math.sqrt(l2);
	}
}