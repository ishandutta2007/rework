package org.deflaux.facebook2;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.hash.Hash;
import org.apache.hadoop.util.bloom.Key;

/**
 * See bloomFilterParameters.R for code to determine parameters to BloomFilter.
 * TODO implement that code in Java
 * 
 * Assumptions: epochs are monotonically increasing and we record at least one
 * entry for each epoch
 * 
 * @author deflaux
 * 
 */
public class HistorySlidingWindow {
	static final int NOT_INITIALIZED = -1;
	int numEpochsInWindow;
	int filterSize;
	int numHashFuncToUse;
	List<BloomFilter> windowPanes;
	int currentEpoch;
	int currentSlot;

	public HistorySlidingWindow(int numEpochsInWindow, int filterSize,
			int numHashFuncToUse) {
		this.numEpochsInWindow = numEpochsInWindow;
		this.filterSize = filterSize;
		this.numHashFuncToUse = numHashFuncToUse;
		currentEpoch = currentSlot = NOT_INITIALIZED;

		windowPanes = new ArrayList<BloomFilter>(this.numEpochsInWindow);
		for (int i = 0; i < this.numEpochsInWindow; i++) {
			BloomFilter filter = new BloomFilter(filterSize, numHashFuncToUse,
					Hash.MURMUR_HASH);
			windowPanes.add(filter);
		}
	}

	public void recordHistory(String entry, int epoch) {
		int paneIndex = NOT_INITIALIZED;
		if (NOT_INITIALIZED == currentEpoch) {
			currentEpoch = epoch;
			currentSlot = 0;
			paneIndex = currentSlot;
		} else if (currentEpoch >= epoch) {
			// Add an item to current or past history
			if (numEpochsInWindow <= (currentEpoch - epoch)) {
				// This is outside of our window
				throw new IllegalArgumentException(
						"Attempting to record history for an epoch outside of the window: " + epoch);
			}
			paneIndex = (currentSlot + (currentEpoch - epoch))
					% numEpochsInWindow;
		} else if (currentEpoch + 1 == epoch) {
			// Slide our window forward by one epoch
			currentEpoch = epoch;
			currentSlot = (currentSlot + 1) % numEpochsInWindow;
			windowPanes.set(currentSlot, new BloomFilter(filterSize,
					numHashFuncToUse, Hash.MURMUR_HASH));
			paneIndex = currentSlot;
		} else {
			throw new IllegalArgumentException(
					"Attempting to record history for a non-monotonically increasing epoch");
		}
		BloomFilter filter = windowPanes.get(paneIndex);
		Key key = new Key(entry.getBytes());
		filter.add(key);
	}

	public boolean viewHistory(String entry, int epoch) {
		if (!((currentEpoch >= epoch) && (currentEpoch - numEpochsInWindow < epoch))) {
			throw new IllegalArgumentException(
					"Attempting to retrieve history for an epoch outside of the window");
		}
		int paneIndex = (currentSlot - (currentEpoch - epoch))
				% numEpochsInWindow;
		paneIndex = (0 > paneIndex) ? paneIndex + numEpochsInWindow : paneIndex;
		BloomFilter filter = windowPanes.get(paneIndex);
		Key key = new Key(entry.getBytes());
		return filter.membershipTest(key);
	}

}
