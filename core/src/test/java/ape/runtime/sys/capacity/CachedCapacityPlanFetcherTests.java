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
package ape.runtime.sys.capacity;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.runtime.json.JsonStreamReader;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedCapacityPlanFetcherTests {
  @Test
  public void flow() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("x");
    AtomicBoolean alive = new AtomicBoolean(true);
    try {
      MockCapacityPlanFetcher mock = new MockCapacityPlanFetcher();
      mock.plans.put("a", new CapacityPlan(new JsonStreamReader("{\"min\":42}")));
      CachedCapacityPlanFetcher finder = new CachedCapacityPlanFetcher(TimeSource.REAL_TIME, 100, 100000, executor, mock);
      CountDownLatch latch = new CountDownLatch(2);
      finder.fetch("host", new Callback<>() {
        @Override
        public void success(CapacityPlan value) {
        }

        @Override
        public void failure(ErrorCodeException ex) {
          latch.countDown();
        }
      });
      finder.fetch("a", new Callback<>() {
        @Override
        public void success(CapacityPlan value) {
          Assert.assertEquals(42, value.minimum);
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {
        }
      });
      finder.startSweeping(alive, 5, 10);
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    } finally {
      alive.set(false);
      executor.shutdown();
    }
  }
}
