package edu.uw.cs.biglearn.clickprediction.analysis;

import java.util.Arrays;

import org.apache.log4j.Logger;

import edu.uw.cs.biglearn.clickprediction.util.StringUtil;

/**
 * This class represents an instance of the data.
 * 
 * @author haijieg, deflaux
 * 
 */
public class DataInstance {
	static Logger logger = Logger.getLogger("DataInstance");
	
	public static final int MISSING_CLICK = -1;
	public static final int MISSING_USER_ID = 0;
	public static final int MISSING_GENDER = -3;
	public static final int MISSING_AGE = 0;

	// Label
	int clicked; // 0 or 1; -1 if unavailable

	// Feature of the page and ad
	int depth; // depth of the session.
	int position; // position of the ad.
	int[] tokens; // list of token ids.

	// Feature of the user
	int userid; // zero if unavailable 
	int gender; // user gender indicator -1 for male, 1 for female; -3 if unavailable
	int age; // user age indicator '1' for (0, 12], '2' for (12, 18], '3' for
				// (18, 24], '4' for (24, 30],
				// '5' for (30, 40], and '6' for greater than 40.
				// '0' if unavailable

	/**
	 * Create a DataInstance from input string.
	 * 
	 * @param line
	 * @param hasLabel
	 *            True if the input string is from training data. False
	 *            otherwise.
	 */
	public DataInstance(String line, boolean hasLabel) {
		String[] fields = line.split("\\|");
		int offset = 0;
		if (hasLabel) {
			clicked = Integer.valueOf(fields[0]);
			offset = 1;
		} else {
			clicked = -1;
		}
		depth = Integer.valueOf(fields[offset + 0]);
		position = Integer.valueOf(fields[offset + 1]);
		userid = Integer.valueOf(fields[offset + 2]);
		gender = Integer.valueOf(fields[offset + 3]);
		gender = (int) ((gender - 1.5) * 2.0); // map gender from {1,2} to
												// {-1,
												// 1}
		age = Integer.valueOf(fields[offset + 4]);
		tokens = StringUtil.mapArrayStrToInt(fields[offset + 5].split(","));
	}

	/**
	 * Determine whether a DataInstance is valid.
	 */
	public boolean isValid() {
		boolean valid = true;
		String validationMessages = "";

		if (!(MISSING_CLICK == clicked || 0 == clicked || 1 == clicked)) {
			valid = false;
			validationMessages += ", Malformed click value: " + clicked;
		}

		if (!(1 <= depth)) {
			valid = false;
			validationMessages += ", Malformed depth value: " + depth;
		}

		if (!(1 <= position && depth >= position)) {
			valid = false;
			validationMessages += ", Malformed position value: " + position;
		}

		// Some data points do not have user information. In these cases, the
		// userid, age, and gender are set to zero.
		if (MISSING_USER_ID == userid) {
			if (!(MISSING_GENDER == gender)) { // normalized gender value
				valid = false;
				validationMessages += ", Malformed gender value (zero userid): "
						+ gender;
			}
			if (!(MISSING_AGE == age)) {
				valid = false;
				validationMessages += ", Malformed age value (zero userid): "
						+ age;
			}
		} else if (0 < userid) { // We have user data
			if (!(MISSING_GENDER == gender || -1 == gender || 1 == gender)) {
				valid = false;
				validationMessages += ", Malformed gender value: " + gender;
			}

			if (!(MISSING_AGE == age || (1 <= age && 6 >= age))) {
				valid = false;
				validationMessages += ", Malformed age value: " + age;
			}
		} else {
			valid = false;
			validationMessages += ", Malformed userid value: " + userid;
		}

		if (!(0 < tokens.length)) {
			valid = false;
			validationMessages += ", Malformed token value: " + tokens;
		}

		if (!valid) {
			logger.warn("Invalid data instance " + toString()
					+ validationMessages);
		}

		return valid;
	}

	@Override
	public String toString() {
		return "DataInstance [clicked=" + clicked + ", depth=" + depth
				+ ", position=" + position + ", tokens="
				+ Arrays.toString(tokens) + ", userid=" + userid + ", gender="
				+ gender + ", age=" + age + "]";
	}

}
