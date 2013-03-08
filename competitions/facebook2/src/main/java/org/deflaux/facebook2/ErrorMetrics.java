package org.deflaux.facebook2;

public class ErrorMetrics {
	double truePositive = 0;
	double falsePositive = 0;
	double trueNegative = 0;
	double falseNegative = 0;
	
	void update(int prediction, int label) {
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
	
	double getPrecision() {
		return truePositive / (truePositive + falsePositive);
	}
	
	double getRecall() {
		return truePositive / (truePositive + falseNegative);
	}
	
	double getLoss() {
		return falsePositive + falseNegative;
	}
	
	double getCount() {
		return truePositive + trueNegative + falsePositive + falseNegative;
	}
	
	double getAverageLoss() {
		return getLoss()/getCount();
	}
	
	double getFScore() {
		return 2 * ((getPrecision()*getRecall())/(getPrecision()+getRecall()));
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
