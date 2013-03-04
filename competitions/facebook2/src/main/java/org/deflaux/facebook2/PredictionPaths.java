package org.deflaux.facebook2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author deflaux
 * 
 */
public class PredictionPaths {
	public String filePath;
	public int counter;
	private Scanner sc;

	/**
	 * Provides access to paths for prediction from the given filePath.
	 * 
	 * @param filePath
	 *            FilePath to the prediction paths file on disk.
	 * @throws FileNotFoundException
	 */
	public PredictionPaths(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		sc = new Scanner(new BufferedReader(new FileReader(filePath)));
	}

	/**
	 * @return True if there are more prediction paths
	 */
	public boolean hasNext() {
		return sc.hasNextLine();
	}

	/**
	 * @return the next prediction path instance.
	 */
	public List<String> nextInstance() {
		counter++;
		String line = sc.nextLine();
		String[] fields = line.split("\\|");
		return Arrays.asList(fields);
	}
}