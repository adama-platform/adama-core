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
package ape.common.rate;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTokenLimiterTests {
  @Test
  public void flow() throws Exception{
    SimpleExecutor executor = SimpleExecutor.create("async-token-limiter");
    try {
      AsyncTokenLimiter limit = new AsyncTokenLimiter(executor, new TokenRateLimiter(5, 2000, 2, 1000, TimeSource.REAL_TIME));
      CountDownLatch latch = new CountDownLatch(100);
      AtomicInteger count = new AtomicInteger(0);
      AtomicInteger errors = new AtomicInteger(0);
      for (int i = 0; i < 100; i++) {
        limit.execute(3, 25, 10, new Callback<Void>() {
          @Override
          public void success(Void value) {
            count.incrementAndGet();
            latch.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {
            errors.incrementAndGet();
            latch.countDown();
          }
        });
      }
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertTrue(count.get() < 15);
      Assert.assertTrue(errors.get() > 80);
    } finally {
      executor.shutdown();
    }
  }
}
