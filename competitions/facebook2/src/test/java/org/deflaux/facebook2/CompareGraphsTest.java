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
import org.junit.Test;

public class CompareGraphsTest {
	static final Logger logger = Logger.getLogger("CompareGraphsTest");
	
	static final boolean printAssertions = Boolean.parseBoolean(System.getProperty("printAssertions"));
	
	Map<String,Integer> readEdgesFromFile(String filePath) throws FileNotFoundException {
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

	@Test
	public void testCompareGraphs15And16() throws IOException {
		
		Map<String,Integer> epoch15Edges = readEdgesFromFile("/Users/deflaux/rework/competitions/facebook2/data/normTrain15.txt");
		Map<String,Integer> epoch16Edges = readEdgesFromFile("/Users/deflaux/rework/competitions/facebook2/data/graph16.txt");
		
		assertEqualsHelper("num edges in 15", 48741, epoch15Edges.size());
		assertEqualsHelper("num edges in 16", 90248, epoch16Edges.size());
		
		Set<String> epoch15KeysMissingFrom16 = new HashSet<String>(epoch15Edges.keySet());
		epoch15KeysMissingFrom16.removeAll(epoch16Edges.keySet());
		assertEqualsHelper("edges from 15 not in 16 " + ((double)epoch15KeysMissingFrom16.size())/((double)epoch15Edges.size()), 14482, epoch15KeysMissingFrom16.size());
		assertEqualsHelper("existence error rate", 0.297121519870335, ((double)epoch15KeysMissingFrom16.size())/((double)epoch15Edges.size()));
	}

	@Test
	public void testCompareGraphs15And15() throws IOException {
		
		Map<String,Integer> epoch15ActualEdges = readEdgesFromFile("/Users/deflaux/rework/competitions/facebook2/data/normTrain15.txt");
		Map<String,Integer> epoch15PredictedEdges = readEdgesFromFile("/Users/deflaux/rework/competitions/facebook2/data/graph15.txt");
		
		assertEqualsHelper("num edges in 15Actual", 48741, epoch15ActualEdges.size());
		assertEqualsHelper("num edges in 15Predicted", 57392, epoch15PredictedEdges.size());
		
		Set<String> epoch15ActualKeysMissingFrom15Predicted = new HashSet<String>(epoch15ActualEdges.keySet());
		epoch15ActualKeysMissingFrom15Predicted.removeAll(epoch15PredictedEdges.keySet());
		assertEqualsHelper("edges from 15Actual not in 15Predicted " + ((double)epoch15ActualKeysMissingFrom15Predicted.size())/((double)epoch15ActualEdges.size()), 9039, epoch15ActualKeysMissingFrom15Predicted.size());
		assertEqualsHelper("existence error rate", 0.18544962146857882, ((double)epoch15ActualKeysMissingFrom15Predicted.size())/((double)epoch15ActualEdges.size()));
		
		ErrorMetrics existenceMetrics = new ErrorMetrics();
		ErrorMetrics costMetrics = new ErrorMetrics();
		
		// Record existence true and false positives
		for(String edge : epoch15PredictedEdges.keySet()) {
			if(epoch15ActualEdges.containsKey(edge)) {
				existenceMetrics.update(1, 1);
				costMetrics.update(epoch15PredictedEdges.get(edge), epoch15ActualEdges.get(edge));
			}
			else {
				existenceMetrics.update(1, 0);
			}
		}
		
		// Record existence false negatives
		for(int i = 0; i < epoch15ActualKeysMissingFrom15Predicted.size(); i++) {
			existenceMetrics.update(0, 1);
		}
		
		// TODO no good way to know existence true negatives
		
		assertEqualsHelper("Existence f-score: " + existenceMetrics, 0.7481556160666334, existenceMetrics.getFScore());
		assertEqualsHelper("Cost f-score: " + costMetrics, 0.9937642403165847, costMetrics.getFScore());
	}

	void assertEqualsHelper(String testCase, Object expected, Object actual) {
		if (true == printAssertions) {
			logger.info("Test case: " + testCase + ", expected: " + expected
					+ ", actual: " + actual);
		} else {
			assertEquals(testCase, expected, actual);
		}
	}
}
