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
package ape.runtime.sys.web.rxhtml;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.rxhtml.routing.Table;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedRxHtmlFetcherTests {
  @Test
  public void trivial() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("exe");
    try {
      MockRxHtmlFetcher mock = new MockRxHtmlFetcher();
      CachedRxHtmlFetcher fetcher = new CachedRxHtmlFetcher(TimeSource.REAL_TIME, 1000, 5 * 1000, executor, mock);
      CountDownLatch latch = new CountDownLatch(1);
      fetcher.fetch("trivial", new Callback<Table>() {
        @Override
        public void success(Table value) {
          System.err.println("Got one");
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void expiry() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("exe");
    try {
      MockRxHtmlFetcher mock = new MockRxHtmlFetcher();
      CountDownLatch latch = new CountDownLatch(5);
      CachedRxHtmlFetcher fetcher = new CachedRxHtmlFetcher(TimeSource.REAL_TIME, 1000, 5, executor, new RxHtmlFetcher() {
        @Override
        public void fetch(String space, Callback<Table> callback) {
          latch.countDown();
          mock.fetch(space, callback);
        }
      });
      AtomicBoolean alive = new AtomicBoolean(true);
      fetcher.startSweeping(alive, 1, 2);
      long start = System.currentTimeMillis();
      while (latch.getCount() > 0 && (System.currentTimeMillis() - start) < 2000) {
        fetcher.fetch("trivial", new Callback<Table>() {
          @Override
          public void success(Table value) {
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Thread.sleep(10);
      }
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    } finally {
      executor.shutdown();
    }
  }
}
