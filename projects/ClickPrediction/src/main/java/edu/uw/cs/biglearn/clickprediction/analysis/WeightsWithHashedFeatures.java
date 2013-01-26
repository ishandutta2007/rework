package edu.uw.cs.biglearn.clickprediction.analysis;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class WeightsWithHashedFeatures {
	double w0;
	double wPosition;
	double wDepth;
	double wAge;
	double wGender;
	double[] wHashedFeature;
	Map<Integer, Integer> accessTime; // keep track of the access timestamp of feature weights.
																		// Using this to do delayed regularization.
	int featuredim;
	
	public WeightsWithHashedFeatures(int featuredim) {
		this.featuredim = featuredim;
		w0 = wAge = wGender = wDepth = wPosition = 0.0;
		wHashedFeature = new double[featuredim];
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
		builder.append("HashedFeature: " );
		for (double w: wHashedFeature)
			builder.append(w + " ");
		builder.append("\n");				
		return builder.toString();
	}

	/**
	 * @return the l2 norm of this weight vector.
	 */
	public double l2norm() {
		double l2 = w0 * w0 + wAge * wAge + wGender * wGender
				 				+ wDepth*wDepth + wPosition*wPosition;
		for (double w : wHashedFeature)
			l2 += w * w;
		return Math.sqrt(l2);
	}
} // end of weight class