package org.deflaux.util;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A collection of matrix operations.
 * @author haijieg
 *
 */
public class MatUtil {	
	/**
	 * Read a matrix market format dense matrix. Returns a 2-d array representing the TRANSPOSE of A.
	 * For example:
	 * 	float[][] m = readMatrix("SOMEPATH");
	 * 	float[] coli = m[i][] will get the ith column of the matrix;
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 */
	public static float[][] readMatrix(String path) throws FileNotFoundException {
		int n=0, p=0;
		System.err.println("Load matrix from " + path);
		Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
		while (sc.hasNext()) {
			String line = sc.nextLine();
			if (line.startsWith("%")) {
				System.err.println(line);
			} else {
				String[] dim = line.split(" ");
				n = Integer.parseInt(dim[0]);
				p = Integer.parseInt(dim[1]);
				System.err.println("num rows: " + n + "\n" + "num cols: " + p);
				break;
			}
		}
		int count = 0;
		float[][] mat = new float[p][n];
		int row = 0, col = 0;
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.isEmpty()) {
				break;
			}
			Float val = Float.parseFloat(line);
			mat[col][row] = val;
			row++;
			if (row == n) {
				row = 0;
				col++;
			}
			count++;
		}
		if (count != n*p) {
			System.err.println("Parse fail: not enough elements. num rows: " + n + "\n" + "num cols: " + p + "\n nun elements: " + count);
			return null;
		}
		System.err.println("Done.");
		return mat;
	}
	
	/**
	 * Read a matrix market (or plain text) format vector. Returns a float array that representing the vector V.
	 * For example:
	 * 	float[] m = readVector("SOMEPATH");
	 * 	float vi = m[i] will get the ith element of the vector,
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 */
	public static float[] readVector(String path) throws FileNotFoundException {
		ArrayList<Float> arr = new ArrayList<Float>();
		System.err.println("Load vector from " + path);
		Scanner sc = new Scanner(new BufferedReader(new FileReader(path)));
		while (sc.hasNext()) {
			String line = sc.nextLine();
			if (line.isEmpty()) {
				break;
			}			
			if (line.startsWith("%%")) { // detect matrix market format
				System.err.println(line);
				while(sc.nextLine().startsWith("%")); // skip the comment line, and the dimension info.
				continue;
			}
			arr.add(Float.parseFloat(line));
		}
		System.err.println("Length: " + arr.size());
		float[] ret = new float[arr.size()];
		for (int i = 0; i < arr.size(); i++)
			ret[i] = arr.get(i);
		System.err.println("Done.");
		return ret;
	}

	
	/**
	 * @param x
	 * @param y
	 * @return the dot product between two vectors x and y, assuming x, y have equal length.
	 */
	public static float dot(float[] x, float[] y) {
		float ret = 0;
		for (int i = 0; i < x.length; i++) {
			ret += x[i] * y[i];
		}
		return ret;
	}
	
	/**
	 * @param x
	 * @param y
	 * @return the vector element wise sum: x + y, assuming x, y have equal length.
	 */
	public static float[] plus(float[] x, float[] y) {
		float ret[] = new float[x.length];
		for (int i = 0; i < x.length; i++) {
			ret[i] = x[i] + y[i];
		}
		return ret;
	}
	
	/** 
	 * Computes x += y;
	 * @param x
	 * @param y
	 * @return
	 */
	public static void plusequal(float[] x, float[] y) {
		for (int i = 0; i < x.length; i++) {
			x[i] += y[i];
		}
	}
	
	/**
	 * @param x
	 * @param y
	 * @return the vector element wise sum: x - y, assuming x, y have equal length.
	 */
	public static float[] minus(float[] x, float[] y) {
		float ret[] = new float[x.length];
		for (int i = 0; i < x.length; i++) {
			ret[i] = x[i] - y[i];
		}
		return ret;
	}
	
	/**
	 * @param x
	 * @param y
	 * @return the vector x scaled by a constant c.
	 */
	public static float[] scale(float[] x, float c) {
		float ret[] = new float[x.length];
		for (int i = 0; i < x.length; i++) {
			ret[i] = c * x[i];
		}
		return ret;
	}
	
	/**
	 * Takes the transpose of X and a vector w.
	 * Return the Matrix vector product of X * w.
	 * @param XTrans The transpose of X
	 * @param w
	 * @return Xw
	 */
	public static float[] multiply(float[][] XTrans, float[] w) {
		float ret[] = new float[XTrans[0].length];
		for (int i = 0; i < XTrans.length; i++) {
			plusequal(ret, scale(XTrans[i], w[i]));
		}
		return ret;
	}

	/**
	 * @param v
	 * @return the l2 norm of the vector v.
	 */
	public static float l2(float[] v) {
		float ret = 0;
		for (int i = 0; i < v.length; i++) {
			ret += v[i]*v[i];
		}
		return (float)Math.sqrt(ret);
	}
	
	/**
	 * @param v
	 * @return the l2 norm of the vector v.
	 */
	public static float l0(float[] v) {
		int nnz = 0;
		for (int i = 0; i < v.length; i++) {
			if (Math.abs(v[i]) > 1e-8) {
				nnz++;
			}
		}	
		return nnz;
	}
	
	public static void main(String args[]) throws FileNotFoundException {
		// Test loading the matrices
		float[][] Xtrain = readMatrix("data/lasso_synthetic/Xtrain.mtx");
		float[][] Xtest = readMatrix("data/lasso_synthetic/Xtest.mtx");
		float[] Ytrain = readVector("data/lasso_synthetic/Ytrain.mtx");
		float[] Ytest = readVector("data/lasso_synthetic/Ytest.mtx");
		float[][] fmriXtrain = readMatrix("data/fmri/subject1_fmri_std.train.mtx");
		float[][] fmriXtest = readMatrix("data/fmri/subject1_fmri_std.test.mtx");
		float[] widtrain = readVector("data/fmri/subject1_wordid.train.mtx");
		float[][] widtest = readMatrix("data/fmri/subject1_wordid.test.mtx");
	}
}
