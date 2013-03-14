package org.deflaux.facebook2;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CompareGraphsTest {
	static final Logger logger = Logger.getLogger("CompareGraphsTest");
	
	static final boolean printAssertions = true; //Boolean.parseBoolean(System.getProperty("printAssertions"));
	
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
	public void testCompareGraphs() throws IOException {
		
		Map<String,Integer> epoch15Edges = readEdgesFromFile("/Users/deflaux/rework/competitions/facebook2/data/normTrain15.txt");
		Map<String,Integer> epoch16Edges = readEdgesFromFile("/Users/deflaux/rework/competitions/facebook2/data/graph16.txt");
		
		Set<String> epoch15EdgeKeys = epoch15Edges.keySet();
		Set<String> epoch16EdgeKeys = epoch16Edges.keySet();
		
		assertEqualsHelper("num edges in 15", 48741, epoch15Edges.size());
		assertEqualsHelper("num edges in 16", 90248, epoch16Edges.size());
		
		Set<String> epoch15KeysMissingFrom16 = new HashSet<String>(epoch15EdgeKeys);
		epoch15KeysMissingFrom16.removeAll(epoch16EdgeKeys);
		assertEqualsHelper("edges from 15 not in 16 " + ((double)epoch15KeysMissingFrom16.size()) +" "+((double)epoch15Edges.size()) +" "+ ((double)epoch15KeysMissingFrom16.size())/((double)epoch15Edges.size()), 14482, epoch15KeysMissingFrom16.size());

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
