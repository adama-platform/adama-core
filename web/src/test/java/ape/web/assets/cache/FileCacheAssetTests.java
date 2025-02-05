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

import ape.common.SimpleExecutor;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class FileCacheAssetTests {
  @Test
  public void simpleFlow() throws Exception {
    File root = File.createTempFile("adamafcat", "001");
    root.delete();
    Assert.assertTrue(root.mkdir());
    try {
      FileCacheAsset fca = new FileCacheAsset(123L, root, CacheBattery.ASSET, SimpleExecutor.NOW);
      Assert.assertEquals(1, root.listFiles().length);
      CacheBattery.driveSimple(fca);
      Assert.assertEquals(0, root.listFiles().length);
    } finally {
      for (File x : root.listFiles()) {
        x.delete();
      }
      root.delete();
    }
  }

  @Test
  public void simpleFailure() throws Exception {
    File root = File.createTempFile("adamafcat", "002");
    root.delete();
    Assert.assertTrue(root.mkdir());
    try {
      FileCacheAsset fca = new FileCacheAsset(123L, root, CacheBattery.ASSET, SimpleExecutor.NOW);
      Assert.assertEquals(1, root.listFiles().length);
      CacheBattery.driveFailure(fca);
      Assert.assertEquals(0, root.listFiles().length);
    } finally {
      for (File x : root.listFiles()) {
        x.delete();
      }
      root.delete();
    }
  }

  @Test
  public void concurrentEviction() throws Exception {
    File root = File.createTempFile("adamafcat", "003");
    root.delete();
    Assert.assertTrue(root.mkdir());
    try {
      FileCacheAsset fca = new FileCacheAsset(123L, root, CacheBattery.ASSET, SimpleExecutor.NOW);
      Assert.assertEquals(1, root.listFiles().length);
      CacheBattery.driveEvictionConcurrent(fca).run();
      Assert.assertEquals(0, root.listFiles().length);
    } finally {
      for (File x : root.listFiles()) {
        x.delete();
      }
      root.delete();
    }
  }
}
