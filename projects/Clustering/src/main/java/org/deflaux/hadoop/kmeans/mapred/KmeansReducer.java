package org.deflaux.hadoop.kmeans.mapred;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import org.deflaux.hadoop.kmeans.Cluster;
import org.deflaux.hadoop.kmeans.Document;
import org.deflaux.hadoop.util.MathUtil;

/**
 * Update the cluster center and compute the with-in class distances
 * 
 * @author haijieg
 */
public class KmeansReducer extends MapReduceBase implements
		Reducer<IntWritable, Text, Text, Text> {

	public void reduce(IntWritable key, Iterator<Text> values,
			OutputCollector<Text, Text> out, Reporter reporter)
			throws IOException {

		/**
		 * Your code goes in here.
		 */
		Cluster c = new Cluster();
		c.id = key.get();

		/*
		 * NOTE: in general for Hadoop it is inadvisable to stash the data so
		 * that we can iterate over the data more than once, but that is what
		 * Jay is doing to get the *exact* distance to the *new* centroid
		 */
		List<Document> documents = new ArrayList<Document>();
		while (values.hasNext()) {
			Text value = values.next();
			Document document = new Document(value.toString());
			documents.add(document);
		}

		// Maximize our new centroid
		for (Document document : documents) {
			for (Entry<Integer, Double> entry : document.tfidf.entrySet()) {
				Double tfidf = c.tfidf.get(entry.getKey());
				if (null == tfidf) {
					tfidf = 0.0;
				}
				tfidf += entry.getValue() / documents.size();
				c.tfidf.put(entry.getKey(), tfidf);
			}
		}

		// Compute the exact distance within the cluster for our new centroid
		double dist = 0.0;
		for (Document document : documents) {
			dist += MathUtil.computeDistance(c.tfidf, document.tfidf);
		}

		// Output the cluster center into file: clusteri
		out.collect(new Text("cluster" + c.id), new Text(c.toString()));

		// Output the within distance into file: distancei
		out.collect(new Text("distance" + c.id),
				new Text(c.id + "|" + String.valueOf(dist)));

		// Output the number of documents within the cluster into file: counti
		out.collect(new Text("count" + c.id),
				new Text(c.id + "|" + String.valueOf(documents.size())));
	}
}
