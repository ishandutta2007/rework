package org.deflaux.shotgun;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import org.deflaux.util.MatUtil;

public class Shooting {

	static Logger logger = Logger.getLogger("Shooting");
	static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
	static final double DELTA = 1e-4;
	static final boolean DEBUG = false;

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

		// TODO scale and center data?
		// x_ij = (x_ij - mu_j) / sigma_J
		// OR
		// x_ij = (x_ij - mu_j) / [max(x_j)-min(x_j)]
		// TODO warm start?

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
			int indexJ = (p > j) ? j : j - p;

			float gj = 0;
			for (int indexI = 0; indexI < n; indexI++) {
				if (0 != XTrans[indexJ][indexI]) {
					float xij = (p > j) ? XTrans[indexJ][indexI] : -XTrans[indexJ][indexI];
					float zi = xw[indexI];
					float yi = Y[indexI];
					gj += (zi - yi) * xij;
				}
			}
			gj = (float) ((gj / n) + lambda);

			float prevWj = (p > j) ? wplus[indexJ] : wminus[indexJ];
			float eta = Math.max(-prevWj, -gj);
			float newWj = prevWj + eta;
			
			if (p > j) {
				wplus[indexJ] = newWj;
			} else {
				wminus[indexJ] = newWj;
			}

			for (int indexI = 0; indexI < n; indexI++) {
				if (0 != XTrans[indexJ][indexI]) {
					float xij = (p > j) ? XTrans[indexJ][indexI] : -1
							* XTrans[indexJ][indexI];
					xw[indexI] += eta * xij;
				}
			}
		}
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
			// sanity checks
			if (0 == iter % 100) {
				System.out.println("iter: " + iter + "NNZ: " + MatUtil.l0(w));
			}
			if (DEBUG) {
				logger.info(iter + ":: delta=" + delta);
				logger.info(iter + ":: w=" + implode(w));
			}

			iter++;
		}
		return w;
	}

	/**
	 * Run Shotgun parallel SCD until convergence or exceeding maxiter.
	 * 
	 * Jay's answer to my question regarding thread-safe updates to members of
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
