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
package ape.web.assets.cache;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.TimeSource;
import ape.runtime.natives.NtAsset;
import ape.web.assets.MockAssetStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class WebHandlerAssetCacheTests {
  @Test
  public void memory_policy() {
    Assert.assertTrue(WebHandlerAssetCache.policyCacheMemory(new NtAsset(null, null, "text/html", 40, null, null)));
    Assert.assertFalse(WebHandlerAssetCache.policyCacheMemory(new NtAsset(null, null, "text/nope", 40, null, null)));
    Assert.assertFalse(WebHandlerAssetCache.policyCacheMemory(new NtAsset(null, null, "text/html", 1024 * 1024 * 1024, null, null)));
  }

  @Test
  public void disk_policy() {
    Assert.assertTrue(WebHandlerAssetCache.policyCacheDisk(new NtAsset(null, null, "text/none", 40, null, null)));
    Assert.assertFalse(WebHandlerAssetCache.policyCacheDisk(new NtAsset(null, null, "text/html", 1024 * 1024 * 1024, null, null)));
  }

  @Test
  public void can() {
    Assert.assertTrue(WebHandlerAssetCache.canCache(new NtAsset(null, null, "text/html", 40, null, null)));
    Assert.assertTrue(WebHandlerAssetCache.canCache(new NtAsset(null, null, "text/nope", 40, null, null)));
    Assert.assertFalse(WebHandlerAssetCache.canCache(new NtAsset(null, null, "text/html", 1024 * 1024 * 1024, null, null)));
  }

  @Test
  public void flow() throws Exception {
    File root = File.createTempFile("adamafcat", "001");
    root.delete();
    Assert.assertTrue(root.mkdir());
    try {
      WebHandlerAssetCache cache = new WebHandlerAssetCache(TimeSource.REAL_TIME, root);
      CountDownLatch gotBoth = new CountDownLatch(2);
      cache.failure(new NtAsset(null, null, "text/html", 40, null, null));
      cache.get(new NtAsset("id-1", "name-1", "text/html", 40, "", ""), new Callback<CachedAsset>() {
        @Override
        public void success(CachedAsset value) {
          gotBoth.countDown();
          value.attachWhileInExecutor(new MockAssetStream()).failure(42);
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      cache.get(new NtAsset("id-2", "name-2", "text/none", 512 * 1024, "", ""), new Callback<CachedAsset>() {
        @Override
        public void success(CachedAsset value) {
          gotBoth.countDown();
          value.attachWhileInExecutor(new MockAssetStream()).failure(42);
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
    } finally {
      for (File x : root.listFiles()) {
        x.delete();
      }
      root.delete();
    }
  }
}
