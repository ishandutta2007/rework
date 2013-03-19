package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deflaux.ml.ErrorMetrics;
import org.junit.Ignore;
import org.junit.Test;

public class CompareGraphsTest {
	static final Logger logger = Logger.getLogger("CompareGraphsTest");
	
	static final boolean printAssertions = Boolean.parseBoolean(System.getProperty("printAssertions"));
	
	@Test
	public void testSanityCheckCompareGraphs() throws IOException {
		String filePathActualGraph = "/Users/deflaux/rework/competitions/facebook2/data/normTrain15.txt";
		ErrorMetrics existenceMetrics = new ErrorMetrics();
		ErrorMetrics costMetrics = new ErrorMetrics();
		
		compareGraphs(filePathActualGraph, filePathActualGraph, existenceMetrics, costMetrics);
		
		// Dev Note: this is the number of unique edges in the file, not the total number of edges
		// cut -d '|' -f "1-2" normTrain15.txt | sort | uniq | wc -l
		// Not 
		// wc -l normTrain15.txt
		assertEqualsHelper("num edges in 15 actual graph", 48825.0, existenceMetrics.getTruePositive());
		
		assertEqualsHelper("edges from 15 not in 15", 0.0, existenceMetrics.getFalseNegative());
		assertEqualsHelper("Existence f-score: " + existenceMetrics, 1.0, existenceMetrics.getFScore());
		assertEqualsHelper("Cost f-score: " + costMetrics, 1.0, costMetrics.getFScore());
	}

	@Ignore
	@Test
	public void testCompareGraphs15And15() throws IOException {
		String filePathActualGraph = "/Users/deflaux/rework/competitions/facebook2/data/normTrain15.txt";
		String filePathPredictedGraph = "/Users/deflaux/rework/competitions/facebook2/data/graph15.txt";
		ErrorMetrics existenceMetrics = new ErrorMetrics();
		ErrorMetrics costMetrics = new ErrorMetrics();
		
		compareGraphs(filePathActualGraph, filePathPredictedGraph, existenceMetrics, costMetrics);
		
		assertEqualsHelper("num edges in 15 actual graph", 48741.0, existenceMetrics.getTruePositive() + existenceMetrics.getFalseNegative());
		assertEqualsHelper("num edges in 15 predicted graph", 56748.0, existenceMetrics.getTruePositive() + existenceMetrics.getFalsePositive());
		
		assertEqualsHelper("edges from actual 15 not in predicted 15", 9093.0, existenceMetrics.getFalseNegative());
		assertEqualsHelper("Existence f-score: " + existenceMetrics, 0.7516992293035293, existenceMetrics.getFScore());
		assertEqualsHelper("Cost f-score: " + costMetrics, 0.9933842391875064, costMetrics.getFScore());				
	}
	
	public static void compareGraphs(String filePathActualGraph, String filePathPredictedGraph, ErrorMetrics existenceMetrics, ErrorMetrics costMetrics) throws FileNotFoundException {
		Map<String,Integer> actualEdges = readEdgesFromFile(filePathActualGraph);
		Map<String,Integer> predictedEdges = readEdgesFromFile(filePathPredictedGraph);
		
		Set<String> actualEdgesMissingFromPredicted = new HashSet<String>(actualEdges.keySet());
		actualEdgesMissingFromPredicted.removeAll(predictedEdges.keySet());
		
		// Record existence true and false positives
		for(String edge : predictedEdges.keySet()) {
			if(actualEdges.containsKey(edge)) {
				existenceMetrics.update(1, 1);
				costMetrics.update(predictedEdges.get(edge), actualEdges.get(edge));
			}
			else {
				existenceMetrics.update(1, 0);
			}
		}
		
		// Record existence false negatives
		for(int i = 0; i < actualEdgesMissingFromPredicted.size(); i++) {
			existenceMetrics.update(0, 1);
		}
		
		// TODO no good way to know existence true negatives		
	}

	public static Map<String,Integer> readEdgesFromFile(String filePath) throws FileNotFoundException {
		Map<String,Integer> edges = new HashMap<String, Integer>();
		Scanner sc = new Scanner(new BufferedReader(new FileReader(filePath)));
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] fields = line.split("\\|");
			String tail = fields[0];
			String head = fields[1];
			int cost = Integer.valueOf(fields[2]);
			edges.put(tail + "|" + head, cost);
		}
		return edges;
	}

	public static void assertEqualsHelper(String testCase, Object expected, Object actual) {
		if (true == printAssertions) {
			logger.info("Test case: " + testCase + ", expected: " + expected
					+ ", actual: " + actual);
		} else {
			assertEquals(testCase, expected, actual);
		}
	}
}
