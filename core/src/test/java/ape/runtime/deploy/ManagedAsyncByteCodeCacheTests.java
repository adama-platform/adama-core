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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class ManagedAsyncByteCodeCacheTests {
  @Test
  public void flow() throws Exception {
    HashMap<String, CachedByteCode> cache = new HashMap<>();
    ExternalByteCodeSystem sys = new ExternalByteCodeSystem() {
      @Override
      public void fetchByteCode(String className, Callback<CachedByteCode> callback) {
        CachedByteCode code = cache.get(className);
        if (code != null) {
          System.err.println("FOUND:" + code.className + "::" + code.classBytes.size());
          callback.success(code);
          return;
        }
        System.err.println("FAILED:" + className);
        callback.failure(new ErrorCodeException(404));
      }

      @Override
      public void storeByteCode(String className, CachedByteCode code, Callback<Void> callback) {
        cache.put(className, code);
        System.err.println("STASH:" + className + "/" + code.className.equals(className) + "::" + code.classBytes.size());
        callback.success(null);
      }
    };
    ManagedAsyncByteCodeCache managed = new ManagedAsyncByteCodeCache(sys, SimpleExecutor.NOW, new DeploymentMetrics(new NoOpMetricsFactory()));
    DeploymentPlan plan = new DeploymentPlan("{\"versions\":{\"x\":\"public int x = 123;\"},\"default\":\"x\"}", (t, errorCode) -> {});
    Assert.assertEquals(0, cache.size());
    Assert.assertTrue(AsyncCompilerTests.pump(null, plan, managed) instanceof DeploymentFactory);
    Assert.assertEquals(1, cache.size());
    Assert.assertTrue(AsyncCompilerTests.pump(null, plan, managed) instanceof DeploymentFactory);
    Assert.assertEquals(1, cache.size());
  }
}
