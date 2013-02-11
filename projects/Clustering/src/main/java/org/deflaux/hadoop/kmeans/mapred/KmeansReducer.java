package org.deflaux.hadoop.kmeans.mapred;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
 * @author haijieg
 */
public class KmeansReducer extends MapReduceBase implements Reducer<IntWritable, Text, Text, Text>{
	
	public void reduce(IntWritable key, Iterator<Text> values,
			OutputCollector<Text, Text> out, Reporter reporter) throws IOException {
		
		Cluster c = new Cluster();
		c.id = key.get();
		double dist = 0.0;
		int count = 0;
	
		/**
		 * Your code goes in here.
		 */
		while (values.hasNext()) {
			Text value = values.next();
			Document document = new Document(value.toString());
			
			for(Entry<Integer,Double> entry : document.tfidf.entrySet()) {
				Double tfidfAccumulator = c.tfidf.get(entry.getKey());
				if(null == tfidfAccumulator) {
					tfidfAccumulator = 0.0;
				}
				double tfidfSum = tfidfAccumulator + entry.getValue();
				if(tfidfSum < tfidfAccumulator) {
					throw new Error("overflowed accumulator, implement a running average instead");
				}
				c.tfidf.put(entry.getKey(), tfidfSum);
				count++;
			}

			dist += MathUtil.computeDistance(c.tfidf, document.tfidf);
		}

		for(Entry<Integer,Double> entry : c.tfidf.entrySet()) {
			double tfidfSum = c.tfidf.get(entry.getKey());
			double tfidf = tfidfSum / count;
			c.tfidf.put(entry.getKey(), tfidf);
		}
		
		// Output the cluster center into file: clusteri
		out.collect(new Text("cluster" + c.id), new Text(c.toString()));
		
		// Output the within distance into file: distancei
		out.collect(new Text("distance" + c.id), new Text(c.id + "|" + String.valueOf(dist)));
	}
	

}
