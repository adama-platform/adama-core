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
package ape.common.net;

import ape.common.ErrorCodeException;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientTests {
  @Test
  public void nope() throws Exception {
    NetBase base = new NetBase(new NetMetrics(new NoOpMetricsFactory()), NetSuiteTests.identity(), 1, 4);
    try {
      CountDownLatch latch = new CountDownLatch(1);
      base.connect("192.1.200.1:4242", new Lifecycle() {
        @Override
        public void connected(ChannelClient channel) {
        }

        @Override
        public void failed(ErrorCodeException ex) {
          latch.countDown();
          System.err.println("GotException:" + latch.getCount());
          ex.printStackTrace();
        }

        @Override
        public void disconnected() {
        }
      });
      System.err.println("Waiting");
      long started = System.currentTimeMillis();
      Assert.assertTrue(latch.await(60000, TimeUnit.MILLISECONDS));
      System.err.println("Took:" + (System.currentTimeMillis() - started));
    } finally {
      base.shutdown();
    }
  }

  @Test
  public void badtarget() throws Exception {
    NetBase base = new NetBase(new NetMetrics(new NoOpMetricsFactory()), NetSuiteTests.identity(), 2, 4);
    try {
      CountDownLatch latch = new CountDownLatch(1);
      base.connect("192.1.200.1", new Lifecycle() {
        @Override
        public void connected(ChannelClient channel) {
        }

        @Override
        public void failed(ErrorCodeException ex) {
          latch.countDown();
          System.err.println("GotException:" + latch.getCount());
          ex.printStackTrace();
        }

        @Override
        public void disconnected() {
        }
      });
      System.err.println("Waiting");
      long started = System.currentTimeMillis();
      Assert.assertTrue(latch.await(60000, TimeUnit.MILLISECONDS));
      System.err.println("Took:" + (System.currentTimeMillis() - started));
    } finally {
      base.shutdown();
    }
  }
}
