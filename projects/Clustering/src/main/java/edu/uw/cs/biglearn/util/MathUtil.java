package edu.uw.cs.biglearn.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MathUtil {
	public static double computeDistance(Map<Integer, Double> map1, Map<Integer, Double> map2) {
		double dist = 0.0;
		Set<Integer> union = new HashSet<Integer>(map1.keySet()); union.addAll(map2.keySet());
		for (int key : union) {
			Double map1val = map1.get(key); if (map1val == null) map1val = 0.0;
			Double map2val = map2.get(key); if (map2val == null) map2val = 0.0;
			dist += Math.pow((map1val-map2val),2);
		}
		return Math.sqrt(dist);
	}
}
