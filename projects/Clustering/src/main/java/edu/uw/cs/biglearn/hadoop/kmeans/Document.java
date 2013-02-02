package edu.uw.cs.biglearn.hadoop.kmeans;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class representing a document with with its mean and id.
 * @author haijieg
 */
public class Document {
	public String id;
	public Map<Integer, Double> tfidf;
	
	public Document(String id, Map<Integer, Double> tfidf) {
		this.id = id;
		this.tfidf = new HashMap<Integer, Double>(tfidf);
	}
	
	public Document(String line) {
		String[] splits = line.split("\\|");
		id = splits[0];
		tfidf = new HashMap<Integer, Double>();
		String[] tokens = (splits[1].split(","));
		for (String token: tokens) {
			String[] pairs = token.split(":");
			tfidf.put(Integer.parseInt(pairs[0]), Double.parseDouble(pairs[1]));
		}
	}
	
	public Document(Document other) {
		this.id = other.id;
		this.tfidf = new HashMap<Integer, Double>(other.tfidf);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(id + "\\|");
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
