package edu.uw.cs.biglearn.hadoop.kmeans;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class representing a cluster center with its mean and id. 
 * @author haijieg
 */
public class Cluster {
	public int id;
	public Map<Integer, Double> tfidf;
	
	/**
	 * Creates an empty cluster with id=-1.
	 */
	public Cluster() {
		id = -1;
		tfidf = new HashMap<Integer, Double>();
	}
	
	/**
	 * Parse the string and fill in the cluster.
	 * @param line
	 */
	public void read(String line) {
		String[] splits = line.split("\\|");
		id = Integer.parseInt(splits[0]);
		tfidf = new HashMap<Integer, Double>();
		String[] tokens = (splits[1].split(","));
		for (String token: tokens) {
			String[] pairs = token.split(":");
			tfidf.put(Integer.parseInt(pairs[0]), Double.parseDouble(pairs[1]));
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(id + "|");
		boolean addcomma = false;
		for (Entry<Integer, Double> pair : tfidf.entrySet()) {
			if (addcomma) {
				builder.append(",");
			} else {
				addcomma = true;
			}
			builder.append(pair.getKey()+":"+pair.getValue());
		}
		return builder.toString();
	}
}