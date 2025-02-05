/**
 * MIT License
 * 
 * Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ape.runtime.sys;

import ape.runtime.sys.metering.MeterReading;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ServiceHeatEstimatorTests {

  private static MeterReading record(String space, long mem, long cpu, long messages, long connections) {
    return new MeterReading(0, 0, space, "", new PredictiveInventory.MeteringSample(mem, cpu, -1, messages, connections, 0, 0, 0, 0, 0));
  }

  private static ArrayList<MeterReading> array(MeterReading... records) {
    ArrayList<MeterReading> list = new ArrayList<>();
    for (MeterReading record : records) {
      list.add(record);
    }
    return list;
  }

  private void assertZero(ServiceHeatEstimator.Heat heat) {
    Assert.assertEquals(true, heat.empty);
    Assert.assertEquals(true, heat.low);
    Assert.assertEquals(false, heat.hot);
  }

  @Test
  public void coverage() {
    ServiceHeatEstimator.HeatVector LOW = new ServiceHeatEstimator.HeatVector(10000, 100, 1000*1000, 100);
    ServiceHeatEstimator.HeatVector HOT = new ServiceHeatEstimator.HeatVector(1000L * 1000L * 1000L, 100000, 1000*1000*500L, 250L);
    ServiceHeatEstimator estimator = new ServiceHeatEstimator(LOW, HOT);
    assertZero(estimator.of("space"));

    estimator.apply(array(record("space", 0, 0,0,0)));
    Assert.assertTrue(estimator.of("space").empty);
    Assert.assertTrue(estimator.of("space").low);

    estimator.apply(array(record("x", 1, 1000L * 1000L * 1000L* 2L,1,10)));
    assertZero(estimator.of("space"));
    Assert.assertFalse(estimator.of("x").empty);
    Assert.assertFalse(estimator.of("x").low);
    Assert.assertTrue(estimator.of("x").hot);

    estimator.apply(array(record("y", 1, 0,50001,10), record("y", 1, 0,50001,10)));
    assertZero(estimator.of("x"));
    Assert.assertFalse(estimator.of("y").empty);
    Assert.assertFalse(estimator.of("y").low);
    Assert.assertTrue(estimator.of("y").hot);

    estimator.apply(array(record("z", 1000*1000*251L, 0,0L,10), record("z", 1000*1000*251L, 0,0L,10)));
    assertZero(estimator.of("y"));
    Assert.assertFalse(estimator.of("z").empty);
    Assert.assertFalse(estimator.of("z").low);
    Assert.assertTrue(estimator.of("z").hot);

    estimator.apply(array(record("w", 0, 0,0L,10), record("w", 0, 0,0L,249)));
    assertZero(estimator.of("z"));
    Assert.assertFalse(estimator.of("w").empty);
    Assert.assertFalse(estimator.of("w").low);
    Assert.assertTrue(estimator.of("w").hot);
  }
}
