package org.deflaux.ml;

public class ErrorMetrics {
	private double truePositive = 0;
	private double falsePositive = 0;
	private double trueNegative = 0;
	private double falseNegative = 0;
	
	public void update(int prediction, int label) {
		if(1 == label) {
			if(1 == prediction) {
				truePositive++;
			}
			else {
				falseNegative++;
			}
		}
		else {
			if(1 == prediction) {
				falsePositive++;
			}
			else {
				trueNegative++;
			}
		}
	}
	
	public double getTruePositive() {
		return truePositive;
	}

	public double getFalsePositive() {
		return falsePositive;
	}

	public double getTrueNegative() {
		return trueNegative;
	}

	public double getFalseNegative() {
		return falseNegative;
	}

	public double getPrecision() {
		return truePositive / (truePositive + falsePositive);
	}
	
	public double getRecall() {
		return truePositive / (truePositive + falseNegative);
	}
	
	public double getLoss() {
		return falsePositive + falseNegative;
	}
	
	public double getCount() {
		return truePositive + trueNegative + falsePositive + falseNegative;
	}
	
	public double getAverageLoss() {
		return getLoss()/getCount();
	}
	
	public double getFScore() {
		return 2.0 * ((getPrecision()*getRecall())/(getPrecision()+getRecall()));
	}

	@Override
	public String toString() {
		
		if(truePositive == 0 && falsePositive == 0 && trueNegative == 0 && falseNegative == 0) {
			return "ErrorMetrics [uninitialized]";
		}
		
		return "ErrorMetrics [truePositive=" + truePositive
				+ ", falsePositive=" + falsePositive + ", trueNegative="
				+ trueNegative + ", falseNegative=" + falseNegative
				+ ", getPrecision()=" + getPrecision() + ", getRecall()="
				+ getRecall() + ", getLoss()=" + getLoss() + ", getFScore()="
				+ getFScore() + "]";
	}
}
