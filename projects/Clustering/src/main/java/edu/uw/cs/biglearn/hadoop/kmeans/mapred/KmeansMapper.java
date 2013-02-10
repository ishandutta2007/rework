package edu.uw.cs.biglearn.hadoop.kmeans.mapred;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.StringUtils;

import edu.uw.cs.biglearn.hadoop.kmeans.Cluster;
import edu.uw.cs.biglearn.hadoop.kmeans.Document;
import edu.uw.cs.biglearn.util.MathUtil;

/**
 * Mapper computes the cluster assignment of each document.
 * @author haijieg
 */
public class KmeansMapper  extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text>{
	ArrayList<Cluster> clusters;
	
	@Override
	public void configure(JobConf job) {
		super.configure(job);
		int K = job.getInt("numClusters", 0);
		clusters = new ArrayList<Cluster>();
		try {
			Path[] clusterFiles = DistributedCache.getLocalCacheFiles(job);
			for (Path clusterFile : clusterFiles) {
				loadCluster(clusterFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Caught exception while getting cached files: " + StringUtils.stringifyException(e));
		}
	}
	
	public void map(LongWritable id, Text line,
			OutputCollector<IntWritable, Text> out, Reporter reporter)
			throws IOException {
		
		/**
		 * Your code goes in here.
		 */
		
	}
	
	/**
	 * Load the current cluster centers from path.
	 * @param path
	 */
	private void loadCluster(Path path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path.toString()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				Cluster c = new Cluster();
				c.read(line);
				clusters.add(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Caught exception while parsing the cached file '" + path + "' : " + StringUtils.stringifyException(e));
		}
	}
}
