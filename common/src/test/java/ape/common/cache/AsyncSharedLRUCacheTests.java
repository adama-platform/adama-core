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
package ape.common.cache;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.common.gossip.MockTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncSharedLRUCacheTests {
  @Test
  public void sweep() throws Exception {
    MockTime time = new MockTime();
    SyncCacheLRU cache = new SyncCacheLRU<String, MeasuredString>(time, 1, 10, 1024, 50, (key, ms) -> {});
    SimpleExecutor executor = SimpleExecutor.create("test");
    try {
      AtomicInteger calls = new AtomicInteger(0);
      CountDownLatch latchFinish = new CountDownLatch(1);
      AtomicReference<Callback<MeasuredString>> cbRef = new AtomicReference<>();
      AtomicBoolean alive = new AtomicBoolean(true);
      AsyncSharedLRUCache<String, MeasuredString> async = new AsyncSharedLRUCache<String, MeasuredString>(executor, cache, (key, cb) -> {
        calls.incrementAndGet();
        try {
          cbRef.set(cb);
          latchFinish.countDown();
        } catch (Exception ex) {
        }
      });
      async.startSweeping(alive, 10, 20);
      CountDownLatch allIn = new CountDownLatch(10);
      for (int k = 0; k < 10; k++) {
        Callback<MeasuredString> cb = new Callback<MeasuredString>() {
          @Override
          public void success(MeasuredString value) {
            allIn.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {
          }
        };
        async.get("X", cb);
      }
      Assert.assertTrue(latchFinish.await(10000, TimeUnit.MILLISECONDS));
      cbRef.get().success(new MeasuredString("XYZ"));
      time.currentTime += 10000;
      CountDownLatch emptied = new CountDownLatch(1);
      boolean finished = false;
      for (int k = 0; k < 100 && !finished; k++) {
        executor.execute(new NamedRunnable("probe") {
          @Override
          public void execute() throws Exception {
            System.out.println(cache.size());
            if (cache.size() == 0) {
              emptied.countDown();
            }
            time.currentTime += 10;
          }
        });
        finished = emptied.await(100, TimeUnit.MILLISECONDS);
      }
      Assert.assertTrue(finished);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void herd_success() throws Exception {
    MockTime time = new MockTime();
    SyncCacheLRU cache = new SyncCacheLRU<String, MeasuredString>(time, 1, 10, 1024, 1000, (key, ms) -> {});
    SimpleExecutor executor = SimpleExecutor.create("test");
    try {
      AtomicInteger calls = new AtomicInteger(0);
      CountDownLatch latchFinish = new CountDownLatch(1);
      AtomicReference<Callback<MeasuredString>> cbRef = new AtomicReference<>();
      AtomicBoolean alive = new AtomicBoolean(true);
      AsyncSharedLRUCache<String, MeasuredString> async = new AsyncSharedLRUCache<String, MeasuredString>(executor, cache, (key, cb) -> {
        calls.incrementAndGet();
        try {
          cbRef.set(cb);
          latchFinish.countDown();
        } catch (Exception ex) {
        }
      });
      async.startSweeping(alive, 10, 20);
      CountDownLatch allIn = new CountDownLatch(10);
      for (int k = 0; k < 10; k++) {
        Callback<MeasuredString> cb = new Callback<MeasuredString>() {
          @Override
          public void success(MeasuredString value) {
            allIn.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {
          }
        };
        async.get("X", cb);
      }
      Assert.assertTrue(latchFinish.await(10000, TimeUnit.MILLISECONDS));
      cbRef.get().success(new MeasuredString("XYZ"));
      async.forceEvictionFromCacheNoDownstreamEviction("Y"); // dumb
      Assert.assertTrue(allIn.await(50000, TimeUnit.MILLISECONDS));
      Assert.assertEquals(1, calls.get());
      for (int k = 0; k < 10; k++) {
        CountDownLatch happy = new CountDownLatch(1);
        async.get("X", new Callback<MeasuredString>() {
          @Override
          public void success(MeasuredString value) {
            Assert.assertEquals("XYZ", value.str);
            happy.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {
          }
        });
        Assert.assertTrue(happy.await(10000, TimeUnit.MILLISECONDS));
        Assert.assertEquals(1, cache.size());
      }
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void herd_failure() throws Exception {
    MockTime time = new MockTime();
    ArrayList<String> evictions = new ArrayList<>();
    SyncCacheLRU<String, MeasuredString> cache = new SyncCacheLRU<>(time, 1, 10, 1024, 1000, (key, ms) -> evictions.add(key));
    SimpleExecutor executor = SimpleExecutor.create("test");
    try {
      AtomicInteger calls = new AtomicInteger(0);
      CountDownLatch latchStart = new CountDownLatch(1);
      CountDownLatch latchFinish = new CountDownLatch(1);
      AtomicReference<Callback<MeasuredString>> cbRef = new AtomicReference<>();
      AsyncSharedLRUCache<String, MeasuredString> async = new AsyncSharedLRUCache<String, MeasuredString>(executor, cache, (key, cb) -> {
        calls.incrementAndGet();
        try {
          latchStart.await(60000, TimeUnit.MILLISECONDS);
          cbRef.set(cb);
          latchFinish.countDown();
        } catch (Exception ex) {
        }
      });
      CountDownLatch allIn = new CountDownLatch(10);
      for (int k = 0; k < 10; k++) {
        Callback<MeasuredString> cb = new Callback<MeasuredString>() {
          @Override
          public void success(MeasuredString value) {
          }

          @Override
          public void failure(ErrorCodeException ex) {
            allIn.countDown();
          }
        };
        async.get("X", cb);
      }
      latchStart.countDown();
      Assert.assertTrue(latchFinish.await(10000, TimeUnit.MILLISECONDS));
      cbRef.get().failure(new ErrorCodeException(123));
      Assert.assertTrue(allIn.await(50000, TimeUnit.MILLISECONDS));
      Assert.assertEquals(1, calls.get());
      Assert.assertEquals(0, cache.size());
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void dontcachenull() throws Exception {
    MockTime time = new MockTime();
    ArrayList<String> evictions = new ArrayList<>();
    SyncCacheLRU<String, MeasuredString> cache = new SyncCacheLRU<>(time, 1, 10, 1024, 1000, (key, ms) -> evictions.add(key));
    SimpleExecutor executor = SimpleExecutor.create("test");
    try {
      AtomicReference<Callback<MeasuredString>> cbRef = new AtomicReference<>();
      AsyncSharedLRUCache<String, MeasuredString> async = new AsyncSharedLRUCache<String, MeasuredString>(executor, cache, (key, cb) -> {
        cb.success(null);
      });
      CountDownLatch allIn = new CountDownLatch(10);
      for (int k = 0; k < 10; k++) {
        Callback<MeasuredString> cb = new Callback<MeasuredString>() {
          @Override
          public void success(MeasuredString value) {
            Assert.assertNull(value);
            allIn.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        };
        async.get("X", cb);
      }
      Assert.assertTrue(allIn.await(50000, TimeUnit.MILLISECONDS));
      Assert.assertEquals(0, cache.size());
    } finally {
      executor.shutdown();
    }
  }
}
