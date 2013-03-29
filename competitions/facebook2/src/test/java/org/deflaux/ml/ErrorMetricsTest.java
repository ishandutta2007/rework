package org.deflaux.ml;

import static org.junit.Assert.*;

import org.deflaux.ml.ErrorMetrics;
import org.junit.Test;

public class ErrorMetricsTest {
	static final double DELTA = 1e-6;

	@Test
	public void test() {
		ErrorMetrics metrics = new ErrorMetrics();

		// metrics.update(prediction, label);
		metrics.update(0, 0);
		metrics.update(1, 1);
		metrics.update(0, 1);
		metrics.update(1, 0);
		assertEquals(4, metrics.getCount(), DELTA);
		assertEquals(2, metrics.getLoss(), DELTA);
		assertEquals(.5, metrics.getAverageLoss(), DELTA);
		assertEquals(.5, metrics.getPrecision(), DELTA);
		assertEquals(.5, metrics.getRecall(), DELTA);
		assertEquals(.5, metrics.getFScore(), DELTA);

		metrics.update(1, 1);
		metrics.update(1, 1);
		metrics.update(1, 1);
		assertEquals(7, metrics.getCount(), DELTA);
		assertEquals(2, metrics.getLoss(), DELTA);
		assertEquals(2.0 / 7.0, metrics.getAverageLoss(), DELTA);
		assertEquals(4.0 / 5.0, metrics.getPrecision(), DELTA);
		assertEquals(4.0 / 5.0, metrics.getRecall(), DELTA);
		assertEquals(.8, metrics.getFScore(), DELTA);
	}

}
