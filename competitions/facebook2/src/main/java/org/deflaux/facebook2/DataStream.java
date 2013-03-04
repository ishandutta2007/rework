package org.deflaux.facebook2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * Simulate a data stream from file-based batch data
 * 
 * @author deflaux
 */
public class DataStream {
	public static final String EPOCH_PLACEHOLDER = "_EPOCH_";
	
	static Logger logger = Logger.getLogger("DataStream");

	String pathPattern;
	boolean validate;
	int epoch;
	int counter;
	int rawDataIndex;
	List<String> rawData;

	/**
	 * Creates a dataset from the given path.
	 * 
	 * @param path
	 *            Path to the data file living on the disk.
	 * @param isTraining
	 *            True if the input is training data.
	 * @param size
	 *            The size of the dataset, can be SMALLER than the size of the
	 *            input.
	 * @throws FileNotFoundException
	 */
	public DataStream(String pathPattern, boolean validate) {
		this.pathPattern = pathPattern;
		this.validate = validate;
		this.epoch = 0;
		this.counter = 0;
		this.rawDataIndex = 0;
		this.rawData = new ArrayList<String>();
	}
	
	boolean nextEpoch() {
		epoch++;
		String path = pathPattern.replaceAll(EPOCH_PLACEHOLDER, Integer.toString(epoch));
		try {
			Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
			while(sc.hasNextLine()) {
				rawData.add(sc.nextLine());
			}
			// Randomize the batch data
			Collections.shuffle(rawData);
		} catch (FileNotFoundException e) {
			logger.info(e);
			return false;
		}
		return true;
	}

	/**
	 * @return True if the data stream has more data.
	 */
	public boolean hasNext() {
		if(rawDataIndex >= rawData.size()) {
			return nextEpoch();
		}
		return true;
	}

	/**
	 * @return the next data instance.
	 */
	public DataInstance nextInstance(int featuredim) {
		DataInstance dataInstance = new DataInstance(rawData.get(rawDataIndex), epoch, featuredim, validate);
		rawDataIndex++;
		counter++;
		return dataInstance;
	}
}