package edu.uw.cs.biglearn.clickprediction.analysis;

import edu.uw.cs.biglearn.clickprediction.util.StringUtil;

/**
 * This class represents an instance of the data.
 * 
 * @author haijieg
 * 
 */
public class DataInstance {
	// Label
	int clicked; // 0 or 1

	// Feature of the page and ad
	int depth; // depth of the session.
	int position; // position of the ad.
	int[] tokens; // list of token ids.
	

	// Feature of the user
	int userid;
	int gender; // user gender indicator -1 for male, 1 for female
	int age;		// user age indicator '1' for (0, 12], '2' for (12, 18], '3' for
							// (18, 24], '4' for (24, 30],
							// 	'5' for (30, 40], and '6' for greater than 40.

	/**
	 * Create a DataInstance from input string.
	 * 
	 * @param line
	 * @param hasLabel
	 *            True if the input string is from training data. False
	 *            otherwise.
	 * @throws Exception 
	 */
	public DataInstance(String line, boolean hasLabel) throws Exception {
		String[] fields = line.split("\\|");
		int offset = 0;
		if (hasLabel) {
			clicked = Integer.valueOf(fields[0]);
			if(!(0 == clicked || 1 == clicked)) {
				throw new Exception("Malformed click value: " + line);
			}
			offset = 1;
		} else {
			clicked = -1;
		}
		depth = Integer.valueOf(fields[offset + 0]);
		if(!(1 <= depth)) {
			throw new Exception("Malformed depth value: " + line);
		}
		position = Integer.valueOf(fields[offset + 1]);
		if(!(0 < position && depth >= position)) {
			throw new Exception("Malformed position value: " + line);
		}
		userid = Integer.valueOf(fields[offset + 2]);
		// There is a zero userid
		if(!(0 <= userid)) {
			throw new Exception("Malformed userid value: " + line);
		}
		gender = Integer.valueOf(fields[offset + 3]);
		// There are some zero values for gender
		if(!(0 == gender || 1 == gender || 2 == gender)) {
			throw new Exception("Malformed gender value: " + line);
		}
		gender = (int)((gender - 1.5) * 2.0); // map gender from {1,2} to {-1, 1}
		age = Integer.valueOf(fields[offset + 4]);
		// There are some zero values for age
		if(!(0 <= age && 6 >= age)) {
			throw new Exception("Malformed age value: " + line);
		}
		tokens = StringUtil.mapArrayStrToInt(fields[offset+5].split(","));
		if(!(0 < tokens.length)) {
			throw new Exception("Malformed token value: " + line);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (clicked >= 0) {
			builder.append(clicked + "|");
		}
		builder.append(depth + "|" + position + "|");
		builder.append(userid + "|" + gender + "|" + age + "|");
		builder.append(StringUtil.implode(tokens, ","));
		return builder.toString();
	}
}
