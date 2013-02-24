package org.deflaux.shotgun;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.apache.log4j.Logger;

import org.deflaux.util.MatUtil;

public class Shooting {

	static Logger logger = Logger.getLogger("Shooting");
	static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
	static final double DELTA = 1e-5;
	static final boolean DEBUG = true;

	/* Dimension of X */
	int n, p;

	/* Regularization parameter */
	double lambda;

	/* Stores the positive part of w */
	float[] wplus;

	/* Stores the negative part of w */
	float[] wminus;

	/* Stores the product of Xw */
	float[] xw;

	/* The transpose of the design matrix X */
	final float[][] XTrans;

	/* The response vector Y. */
	final float[] Y;

	/* Stores the current parameter. */
	float[] w;

	/* Stores the stale parameter. */
	float[] oldw;

	/**
	 * Constructor
	 * 
	 * @param XTrans
	 * @param Y
	 * @param lambda
	 */
	public Shooting(float[][] XTrans, float[] Y, double lambda) {

		// TODO scale and center data

		this.XTrans = XTrans;
		this.Y = Y;
		p = XTrans.length;
		n = XTrans[0].length;
		this.lambda = lambda;

		/**
		 * Initialize the parameter
		 */
		wplus = new float[p];
		wminus = new float[p];
		xw = new float[n];
		w = new float[p];
		oldw = new float[p];
	}

	public double soft(double aj, double cj, double lambda) {
		double value;
		if (cj > lambda) {
			value = (cj - lambda) / aj;
		} else if (cj < lambda) {
			value = (cj + lambda) / aj;
		} else {
			value = 0;
		}
		return value;
	}

	/**
	 * Perform coordinate descent at K random chosen coordinates;
	 * 
	 * @param j
	 */
	public void shoot(int K) {

		Random r = new Random();
		for (int i = 0; i < K; i++) {
			int j = r.nextInt(2 * p);

			/**
			 * Your code goes here.
			 */
			int index = (p > j) ? j : j - p;

			float[] xw_minus_j = computeXWMinusJ(j, index);

			double aj = 2 * MatUtil.dot(XTrans[index], XTrans[index]);

			double cj = 2 * MatUtil.dot(XTrans[index],
					MatUtil.minus(Y, xw_minus_j));

			double minWj = soft(aj, cj, lambda);

			float prevWj = (p > j) ? wplus[index] : wminus[index];
			double newWj = prevWj + Math.max(-1 * prevWj, -1 * minWj);

			// Enforce the non-negativity constraint and update xw
			if (0 > newWj) {
				wplus[index] = 0;
				wminus[index] = (float) (-1.0 * newWj);
				// xw = MatUtil.minus(xw_minus_j,
				// MatUtil.scale(XTrans[index], wminus[index]));
			} else if (0 <= newWj) {
				wplus[index] = (float) newWj;
				wminus[index] = 0;
				// xw = MatUtil.plus(xw_minus_j,
				// MatUtil.scale(XTrans[index], wplus[index]));
			} else {
				throw new Error("foo!");
			}

			xw = computeXWPlusJ(j, index, newWj);

			// sanity checks
			if (DEBUG) {
				logger.info(i + ": j=" + j + " prevWj=" + prevWj + " minWj="
						+ minWj + " newWj=" + newWj);
				logger.info(i + ": wplus=" + implode(wplus));
				logger.info(i + ": wminus=" + implode(wminus));
				for (int k = 0; k < p; k++) {
					assertTrue(wplus[k] >= 0);
					assertTrue(wminus[k] >= 0);
				}
				float wSanityCheck[] = MatUtil.minus(wplus, wminus);
				assertEquals(wSanityCheck.length, p);
				float xwSanityCheck[] = MatUtil.multiply(XTrans, wSanityCheck);
				assertEquals(xwSanityCheck.length, n);
				for (int k = 0; k < n; k++) {
					assertEquals(xwSanityCheck[k], xw[k], DELTA);
				}
			}
		}
	}

	float[] computeXWMinusJ(int j, int index) {
		float[] xw_minus_j = (p > j) ? MatUtil.minus(xw,
				MatUtil.scale(XTrans[index], wplus[index])) : MatUtil.plus(xw,
				MatUtil.scale(XTrans[index], wminus[index]));

		// sanity checks
		if (DEBUG) {
			float wSanityCheck[] = MatUtil.minus(wplus, wminus);
			wSanityCheck[index] = 0;
			assertEquals(wSanityCheck.length, p);
			float xwSanityCheck[] = MatUtil.multiply(XTrans, wSanityCheck);
			assertEquals(xwSanityCheck.length, n);
			for (int k = 0; k < n; k++) {
				assertEquals(xwSanityCheck[k], xw_minus_j[k], DELTA);
			}
		}

		return xw_minus_j;
	}

	float[] computeXWPlusJ(int j, int index, double wj) {
		float[] xw_plus_j = MatUtil.plus(xw,
				MatUtil.scale(XTrans[index], (float) wj));

		// sanity checks
		if (DEBUG) {
			float wSanityCheck[] = MatUtil.minus(wplus, wminus);
			assertEquals(wSanityCheck.length, p);
			float xwSanityCheck[] = MatUtil.multiply(XTrans, wSanityCheck);
			assertEquals(xwSanityCheck.length, n);
			return xwSanityCheck;
			// for(int k = 0; k < n; k++) {
			// assertEquals(xwSanityCheck[k], xw_plus_j[k], DELTA);
			// }
		}

		return xw_plus_j;
	}

	static float[] computeXWMinusJ(int p, int j, float[][] XTrans, float[] xw,
			float[] wminus, float[] wplus) {
		int index = (p > j) ? j : j - p;

		return (p > j) ? MatUtil.minus(xw,
				MatUtil.scale(XTrans[index], wplus[index])) : MatUtil.plus(xw,
				MatUtil.scale(XTrans[index], wminus[index]));
	}

	/**
	 * Run Sequential SCD until convergence or exceeding maxiter.
	 */
	public float[] scd(int maxiter) {
		int iter = 0;
		while (iter < maxiter) {
			shoot(p);
			/**
			 * Your code goes here.
			 */
			// Check whether the result has converged.
			w = MatUtil.minus(wplus, wminus);
			float[] wdelta = MatUtil.minus(w, oldw);
			float delta = MatUtil.l2(wdelta);
			if (delta < DELTA) {
				break;
			}
			oldw = w.clone();
			iter++;

			// sanity checks
			if (DEBUG) {
				logger.info(iter + ":: delta=" + delta);
				logger.info(iter + ":: w=" + implode(w));
			}
		}
		return w;
	}

	/**
	 * Run Shotgun parallel SCD until convergence or exceeding maxiter.
	 * 
	 * Jay's answer to my question regarding thread safe updates to members of
	 * this class:
	 * 
	 * "There is potential race condition. In ideal case, we want to have atomic
	 * update for Xw, however, this is too expensive and will completely kill
	 * the parallelism. Instead, we relax the consistency and guarantee that the
	 * update to any entry of Xw is atomic (because float is 4 bytes, and java
	 * did all primitive types with size <= 4 bytes in atomic. Empirically, the
	 * results are close enough."
	 * 
	 */
	public float[] shotgun(int maxiter) {
		final int batchsize = p;
		int iter = 0;

		while (iter < maxiter) {
			ExecutorService threadpool = Executors
					.newFixedThreadPool(NUM_CORES);
			// submit batchsize coordinate descent jobs in parallel.
			for (int i = 0; i < NUM_CORES; i++) {
				threadpool.submit(new Runnable() {
					public void run() {
						shoot(batchsize / NUM_CORES);
					}
				});
			}
			iter += batchsize;

			// Wait for jobs to terminate and check the result
			threadpool.shutdown();
			while (!threadpool.isTerminated()) {
				try {
					threadpool.awaitTermination(20, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// check whether the result has converged.
			w = MatUtil.minus(wplus, wminus);
			float[] wdelta = MatUtil.minus(w, oldw);
			float delta = MatUtil.l2(wdelta);
			if (delta < DELTA) {
				break;
			}
			oldw = w.clone();
		}
		return w;
	}

	public static String implode(float[] arr) {
		String delim = " ";
		StringBuilder builder = new StringBuilder();
		builder.append(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			builder.append(delim);
			builder.append(arr[i]);
		}
		return builder.toString();
	}
}
