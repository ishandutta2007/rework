package org.deflaux.shotgun;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deflaux.util.MatUtil;

public class Shooting {

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

	static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

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

	/*********** TODO thread safety? ***********/

	/**
	 * This method assumes xw was already computed and that j is in {0, p-1}
	 * 
	 * @param j
	 * @return
	 */
	private double minimizeWj(int jIn2P) {

		int j = (p > jIn2P) ? jIn2P : jIn2P - p;

		double aj = 2 * MatUtil.dot(XTrans[j], XTrans[j]);

		// TODO are we using w_t+1 or w_t ?
		float[] xw_minus_j_in_t_plusOne = (p > jIn2P) ? MatUtil.minus(xw,
				MatUtil.scale(XTrans[j], wplus[j])) : MatUtil.plus(xw,
				MatUtil.scale(XTrans[j], wminus[j]));

		float[] xw_minus_j_in_t = MatUtil.minus(xw,
				MatUtil.scale(XTrans[j], w[j]));

		double cj = 2 * MatUtil.dot(XTrans[j],
				MatUtil.minus(Y, xw_minus_j_in_t));

		return soft(aj, cj, lambda);
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

		// Initialize w~
		wplus = new float[p]; // default initialization is zero
		wminus = new float[p]; // default initialization is zero
		for (int i = 0; i < p; i++) {
			if (w[i] > 0) {
				wplus[i] = w[i];
			} else if (w[i] < 0) {
				wminus[i] = -1 * w[i];
			}
		}

		// Compute xw so that it is cached for subsequent use
		xw = MatUtil.multiply(XTrans, w);

		Random r = new Random();
		for (int i = 0; i < K; i++) {
			int j = r.nextInt(2 * p);

			/**
			 * Your code goes here.
			 */
			double minWj = minimizeWj(j);

			int index = (p > j) ? j : j - p;
			float prevWj = (p > j) ? wplus[index] : wminus[index];
			double newWj = prevWj + Math.max(-1 * prevWj, -1 * minWj);

			// TODO should I be updating w(t+1) or w(t) (w, oldw, or wplus and
			// wminus)?

			// Enforce the non-negativity constraint
			if (0 > newWj) {
				wplus[index] = 0;
				wminus[index] = (float) (-1.0 * newWj);
			} else if (0 <= newWj) {
				wplus[index] = (float) newWj;
				wminus[index] = 0;
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
			if (delta < 1e-5) {
				break;
			}
			oldw = w.clone();
		}
		return w;
	}

	/**
	 * Run Shotgun parallel SCD until convergence or exceeding maxiter.
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
			if (delta < 1e-5) {
				break;
			}
			oldw = w.clone();
		}
		return w;
	}

	// public double likelihood() {
	// // TODO cache xw and only update the row that needs an update
	// xw = MatUtil.minus(MatUtil.multiply(XTrans, wplus),
	// MatUtil.multiply(XTrans, wminus));
	// double likelihood = ((1 / (2 * n)) * MatUtil.l2(MatUtil.minus(Y, xw)) +
	// lambda
	// * (sum(wplus) + sum(wminus)));
	// return likelihood;
	// }

	// public static float sum(float[] v) {
	// float ret = 0;
	// for (int i = 0; i < v.length; i++) {
	// ret += v[i];
	// }
	// return ret;
	// }

	// double cj = 0;
	// for(int i = 0; i < n; i++) {
	// double wxi_minus_j = xw[i] - XTrans[j][i]*w[j];
	// cj += XTrans[j][i] * (Y[i] - wxi_minus_j);
	// }
	// cj = 2 * cj;

}
