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
package ape.runtime.data.managed;

import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.runtime.data.ArchivingDataService;
import ape.runtime.data.InMemoryDataService;
import ape.runtime.data.Key;
import ape.runtime.data.*;
import ape.runtime.data.mocks.MockArchiveDataSource;
import ape.runtime.data.mocks.MockFinderService;
import ape.runtime.data.mocks.MockPostDocumentDelete;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BaseTests {
  @FunctionalInterface
  public static interface ThrowConsumer<T> {
    public void run(T item) throws Exception;
  }

  @Test
  public void coverage() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      MockArchiveDataSource data = new MockArchiveDataSource(new InMemoryDataService(executor, TimeSource.REAL_TIME));
      flow((base) -> {
        CountDownLatch latch = new CountDownLatch(2);
        base.on(new Key("space", "key"), (machine) -> {
          latch.countDown();
        });
        base.on(new Key("space", "key"), (machine) -> {
          latch.countDown();
        });
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
      }, data);
    } finally {
      executor.shutdown();
    }
  }

  public static void flow(ThrowConsumer<Base> body, ArchivingDataService data) throws Exception {
    MockFinderService mockFinder = new MockFinderService("test-machine");
    mockFinder.bindLocal(new Key("space", "key"));
    MockPostDocumentDelete delete = new MockPostDocumentDelete();
    SimpleExecutor bexecutor = SimpleExecutor.create("executor");
    Base base = new Base(mockFinder, data, delete, "test-region", "test-machine", bexecutor, 1000);
    try {
      body.run(base);
    } finally {
      bexecutor.shutdown().await(1000, TimeUnit.MILLISECONDS);
    }
    while (base.reportFailureGetRetryBackoff() < 2000) {
    }
    for (int k = 0; k < 1000; k++) {
      base.reportSuccess();
    }
  }
}
