package org.deflaux.facebook2.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.deflaux.facebook2.DataInstance;

/**
 * Simulate a data stream from file-based batch data
 * 
 * @author deflaux
 */
public class DataStream {
	public static final String EPOCH_PLACEHOLDER = "_EPOCH_";

	static Logger logger = Logger.getLogger("DataStream");

	String pathPattern;
	boolean shuffle;
	boolean validate;
	int epoch;
	int counter;
	int rawDataIndex;
	List<String> rawData;

	/**
	 * Creates a data stream from matching files in the given path.
	 * 
	 * @param pathPattern
	 *            Pattern of the file path to the data on disk.
	 * @throws FileNotFoundException
	 */
	public DataStream(String pathPattern, boolean validate) {
		this.pathPattern = pathPattern;
		this.shuffle = true;
		this.validate = validate;
		this.epoch = 0;
		this.counter = 0;
		this.rawDataIndex = 0;
		this.rawData = new ArrayList<String>();
	}

	public int getEpoch() {
		return epoch;
	}

	public int getCounter() {
		return counter;
	}

	public boolean isShuffled() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	boolean nextEpoch() {
		epoch++;
		this.rawData = new ArrayList<String>();
		this.rawDataIndex = 0;
		String path = pathPattern.replaceAll(EPOCH_PLACEHOLDER,
				Integer.toString(epoch));
		try {
			Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
			while (sc.hasNextLine()) {
				rawData.add(sc.nextLine());
			}
			// Randomize the batch data so that we are truly simulating a stream of data
			if (shuffle) {
				Collections.shuffle(rawData);
			}
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
		if (rawDataIndex >= rawData.size()) {
			return nextEpoch();
		}
		return true;
	}

	/**
	 * @return the next data instance.
	 */
	public DataInstance nextInstance(DataInstance instanceToReuse,
			int featuredim) {
		DataInstance dataInstance;

		if (null == instanceToReuse) {
			dataInstance = new DataInstance(rawData.get(rawDataIndex), epoch,
					featuredim, true, validate);
		} else {
			dataInstance = DataInstance.reuse(instanceToReuse,
					rawData.get(rawDataIndex), epoch, featuredim, true, validate);
		}
		rawDataIndex++;
		counter++;
		return dataInstance;
	}
}