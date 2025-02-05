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
package ape.web.service.cache;

import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.runtime.natives.NtAsset;
import ape.web.contracts.HttpHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class HttpResultCacheTests {
  @Test
  public void expiry_get() {
    AtomicLong time = new AtomicLong(0);
    TimeSource mock = new TimeSource() {
      @Override
      public long nowMilliseconds() {
        return time.get();
      }
    };
    HttpResultCache cache = new HttpResultCache(mock);
    cache.inject("key").accept(1000, new HttpHandler.HttpResult(200, "space", "key", NtAsset.NOTHING, null, false, 0));
    Assert.assertEquals("space", cache.get("key").space);
    time.set(999);
    Assert.assertEquals("space", cache.get("key").space);
    time.set(1001);
    Assert.assertNull(cache.get("key"));
    Assert.assertNull(cache.get("key"));
  }

  @Test
  public void expiry_sweep() {
    AtomicLong time = new AtomicLong(0);
    TimeSource mock = new TimeSource() {
      @Override
      public long nowMilliseconds() {
        return time.get();
      }
    };
    HttpResultCache cache = new HttpResultCache(mock);
    cache.inject("key").accept(1000, new HttpHandler.HttpResult(200, "space", "key", NtAsset.NOTHING, null, false, 0));
    Assert.assertEquals("space", cache.get("key").space);
    Assert.assertEquals(0, cache.sweep());
    time.set(999);
    Assert.assertEquals(0, cache.sweep());
    time.set(1001);
    Assert.assertEquals(1, cache.sweep());
    Assert.assertNull(cache.get("key"));
    Assert.assertNull(cache.get("key"));
  }

  @Test
  public void expiry_sweeper() throws Exception {
    HttpResultCache cache = new HttpResultCache(TimeSource.REAL_TIME);
    SimpleExecutor executor = SimpleExecutor.create("time");
    try {
      AtomicBoolean alive = new AtomicBoolean(true);
      HttpResultCache.sweeper(executor, alive, cache, 10, 20);
      cache.inject("key").accept(100, new HttpHandler.HttpResult(200, "space", "key", NtAsset.NOTHING, null, false, 0));
      while (cache.get("key") != null) {
        System.out.println("poll");
        Thread.sleep(5);
      }
      alive.set(false);

    } finally {
      executor.shutdown();
    }
  }
}
