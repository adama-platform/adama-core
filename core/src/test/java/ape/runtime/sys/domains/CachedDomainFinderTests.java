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
package ape.runtime.sys.domains;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedDomainFinderTests {
  @Test
  public void passthrough() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("x");
    AtomicBoolean alive = new AtomicBoolean(true);
    try {
      MockDomainFinder mock = new MockDomainFinder() //
          .with("host", new Domain("domain", 1, "space", "key", null, false, "", null, 123L, true));
      CachedDomainFinder finder = new CachedDomainFinder(TimeSource.REAL_TIME, 100, 100000, executor, mock);
      CountDownLatch latch = new CountDownLatch(1);
      finder.find("host", new Callback<Domain>() {
        @Override
        public void success(Domain value) {
          Assert.assertEquals("domain", value.domain);
          Assert.assertEquals("space", value.space);
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

  @Test
  public void max() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("x");
    try {
      int N = 50000;
      MockDomainFinder mock = new MockDomainFinder();
      for (int k = 0; k < N; k++) {
        mock.with("host-" + k, new Domain("domain", 1, "space", "key", null, false, "", null, 123L, false));
      }
      CachedDomainFinder finder = new CachedDomainFinder(TimeSource.REAL_TIME, 100, 100000, executor, mock);
      CountDownLatch latch = new CountDownLatch(20000);
      for (int k = 0; k < N; k++) {
        finder.find("host-" +k, new Callback<Domain>() {
          @Override
          public void success(Domain value) {
            Assert.assertEquals("domain", value.domain);
            Assert.assertEquals("space", value.space);
            latch.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
      }
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    } finally {
      executor.shutdown();
    }
  }


  @Test
  public void expiry() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("x");
    try {
      MockDomainFinder mock = new MockDomainFinder();
      mock.with("host", new Domain("domain", 1, "space", "key", null, false, "", null, 123L, false));
      CountDownLatch latch = new CountDownLatch(5);
      CachedDomainFinder finder = new CachedDomainFinder(TimeSource.REAL_TIME, 100, 5, executor, new DomainFinder() {
        @Override
        public void find(String domain, Callback<Domain> callback) {
          System.out.println("hit");
          latch.countDown();
          mock.find(domain, callback);
        }
      });
      AtomicBoolean alive = new AtomicBoolean(true);
      finder.startSweeping(alive, 1, 2);
      long start = System.currentTimeMillis();
      while (latch.getCount() > 0 && (System.currentTimeMillis() - start) < 2000) {
        finder.find("host", new Callback<Domain>() {
          @Override
          public void success(Domain value) {
            Assert.assertEquals("domain", value.domain);
            Assert.assertEquals("space", value.space);
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Thread.sleep(10);
      }
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
      alive.set(false);
    } finally {
      executor.shutdown();
    }
  }
}
