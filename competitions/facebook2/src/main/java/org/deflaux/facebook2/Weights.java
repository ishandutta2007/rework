package org.deflaux.facebook2;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Weights {
//	double w0;
//	double wCost;
	double[] wHashedFeature;
	Map<Integer, Integer> accessTime; // keep track of the access timestamp of
										// feature weights.
										// Using this to do delayed
										// regularization.
	int featuredim;

	public Weights(int featuredim) {
		this.featuredim = featuredim;
//		w0 = wCost = 0.0;
		wHashedFeature = new double[featuredim];
		accessTime = new HashMap<Integer, Integer>();
	}

	@Override
	public String toString() {
		DecimalFormat formatter = new DecimalFormat("###.##");
		StringBuilder builder = new StringBuilder();
//		builder.append("Intercept: " + formatter.format(w0) + "\n");
//		builder.append("Cost: " + formatter.format(wCost) + "\n");
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
		double l2 = 0; //w0 * w0 + wCost * wCost;
		for (double w : wHashedFeature)
			l2 += w * w;
		return Math.sqrt(l2);
	}
} 