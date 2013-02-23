package org.deflaux.shotgun;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.deflaux.util.MatUtil;
import org.junit.Test;

public class LassoSimulationTest {

	@Test
	public void test() throws FileNotFoundException {
		/* Dimension of X */
		int n, p;

		float[][] X = MatUtil.readMatrix("data/lasso_synthetic/Xtrain.mtx");
		p = X.length;
		n = X[0].length;
		assertEquals(30, n);
		assertEquals(200, p);
	}

}
