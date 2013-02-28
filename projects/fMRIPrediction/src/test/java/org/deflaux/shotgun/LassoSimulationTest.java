package org.deflaux.shotgun;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.deflaux.util.MatUtil;
import org.junit.Test;

public class LassoSimulationTest {
	static final double DELTA = 1e-6;

	@Test
	public void test() throws FileNotFoundException {
		/* Dimension of X */
		int n, p;

		float[][] X = MatUtil.readMatrix("data/lasso_synthetic/Xtrain.mtx");
		p = X.length;
		n = X[0].length;
		assertEquals(30, n);
		assertEquals(200, p);

		float[] wplus = new float[p];
		float[] wminus = new float[p];
		float[] w = new float[p];
		float[] xw = new float[n];
		float[] xwPlusMinus = new float[n];

		w[5] = 2;
		w[6] = -3;
		wplus[5] = 2;
		wminus[6] = 3;

		// Batch update test
		xw = MatUtil.multiply(X, w);
		xwPlusMinus = MatUtil.minus(MatUtil.multiply(X, wplus),
				MatUtil.multiply(X, wminus));
		for (int k = 0; k < n; k++) {
			assertEquals(xwPlusMinus[k], xw[k], DELTA);
		}

		float wSanityCheck[] = MatUtil.minus(wplus, wminus);
		assertEquals(wSanityCheck.length, p);
		float xwSanityCheck[] = MatUtil.multiply(X, wSanityCheck);
		for (int k = 0; k < n; k++) {
			assertEquals(xwSanityCheck[k], xw[k], DELTA);
		}
	}
}
