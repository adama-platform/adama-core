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
package ape.runtime.deploy;

import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class CachedAsyncByteCodeCacheTests {
  @Test
  public void flow() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("now");
    AtomicBoolean alive = new AtomicBoolean(true);
    try {
      CachedAsyncByteCodeCache cache = new CachedAsyncByteCodeCache(TimeSource.REAL_TIME, 100, 10000, executor, AsyncByteCodeCache.DIRECT);
      cache.startSweeping(alive, 5, 10);
      DeploymentPlan plan = new DeploymentPlan("{\"versions\":{\"x\":\"public int x = 123;\"},\"default\":\"x\"}", (t, errorCode) -> {});
      Assert.assertTrue(AsyncCompilerTests.pump(null, plan, cache) instanceof DeploymentFactory);
    } finally {
      alive.set(false);
      executor.shutdown();
    }
  }
}
