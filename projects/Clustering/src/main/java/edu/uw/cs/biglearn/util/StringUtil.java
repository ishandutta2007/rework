package edu.uw.cs.biglearn.util;

import java.util.Iterator;

public final class StringUtil {

	/* Helper function imploding a list of strings into a single string. */
	public static String implode(Iterator iter, String delim) {
		StringBuilder builder = new StringBuilder();
		builder.append(iter.next());
		while (iter.hasNext()) {
			builder.append(delim);
			builder.append(iter.next());
		}
		return builder.toString();
	}

	/* Helper function imploding an int array into a single string. */
	public static String implode(double[] arr, String delim) {
		StringBuilder builder = new StringBuilder();
		builder.append(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			builder.append(delim);
			builder.append(arr[i]);
		}
		return builder.toString();
	}

	/* Helper function mapping a string array to int array. */
	public static int[] mapArrayStrToInt(String[] in) {
		int[] out = new int[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = Integer.valueOf(in[i]);
		}
		return out;
	}
	
	/* Helper function mapping a string array to double array. */
	public static double[] mapArrayStrToDouble(String[] in) {
		double[] out = new double[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = Double.valueOf(in[i]);
		}
		return out;
	}
}
