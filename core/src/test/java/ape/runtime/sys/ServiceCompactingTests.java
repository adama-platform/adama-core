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
package ape.runtime.sys;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.DocumentSnapshot;
import ape.runtime.data.Key;
import ape.runtime.data.InMemoryDataService;
import ape.runtime.data.LocalDocumentChange;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.mocks.*;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceCompactingTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());

  private static final Key KEY = new Key("space", "key");
  private static final String SUPER_COMPACT =
      "@static { create { return true; } maximum_history = 0; } public int x; public asset a; @connected { x = 42; return @who == @no_one; } message M {} channel foo(M y) { x += 100; }";
  private static final String MODERATE_COMPACT =
      "@static { create { return true; } maximum_history = 10; } public int x; @connected { x = 42; return @who == @no_one; } message M {} channel foo(M y) { x += 100; }";

  @Test
  public void super_compact() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SUPER_COMPACT, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    InMemoryDataService dataService = new InMemoryDataService(executor, TimeSource.REAL_TIME);
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      for (int k = 0; k < 100; k++) {
        LatchCallback cb1 = new LatchCallback();
        streamback.get().send("foo", null, "{}", cb1);
        cb1.await_success(5 + k);
      }
      AtomicInteger reads = new AtomicInteger(100);
      int attempts = 25;
      while (reads.get() > 1 && attempts > 0) {
        attempts--;
        CountDownLatch latch = new CountDownLatch(1);
        dataService.get(KEY, new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            reads.set(value.reads);
            executor.execute(() -> {
              latch.countDown();
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {
            ex.printStackTrace();
          }
        });
        Assert.assertTrue(latch.await(2500, TimeUnit.MILLISECONDS));
        Thread.sleep(500);
      }
    } finally {
      service.shutdown();
      executor.shutdown();
    }
  }

  @Test
  public void moderate_compact() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(MODERATE_COMPACT, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    InMemoryDataService dataService = new InMemoryDataService(executor, TimeSource.REAL_TIME);
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      for (int k = 0; k < 100; k++) {
        LatchCallback cb1 = new LatchCallback();
        streamback.get().send("foo", null, "{}", cb1);
        cb1.await_success(5 + k);
      }
      AtomicInteger reads = new AtomicInteger(100);
      int attempts = 25;
      while (reads.get() > 20 && attempts > 0) {
        attempts--;
        CountDownLatch latch = new CountDownLatch(1);
        dataService.get(KEY, new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            System.err.println("READS:" + value.reads);
            reads.set(value.reads);
            executor.execute(() -> {
              latch.countDown();
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {
            ex.printStackTrace();
          }
        });
        Assert.assertTrue(latch.await(2500, TimeUnit.MILLISECONDS));
        Thread.sleep(500);
      }
    } finally {
      service.shutdown();
      executor.shutdown();
    }
  }


  @Test
  public void compact_turned_off_and_on() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(MODERATE_COMPACT, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicBoolean compactOn = new AtomicBoolean(false);
    InMemoryDataService dataService = new InMemoryDataService(executor, TimeSource.REAL_TIME) {
      @Override
      public void snapshot(Key key, DocumentSnapshot snapshot, Callback<Integer> callback) {
        if (compactOn.get()) {
          super.snapshot(key, snapshot, callback);
          return;
        }
        callback.failure(new ErrorCodeException(-1));
      }
    };
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      for (int k = 0; k < 100; k++) {
        LatchCallback cb1 = new LatchCallback();
        streamback.get().send("foo", null, "{}", cb1);
        cb1.await_success(5 + k);
      }
      CountDownLatch nothingHappened = new CountDownLatch(1);
      dataService.get(KEY, new Callback<LocalDocumentChange>() {
        @Override
        public void success(LocalDocumentChange value) {
          Assert.assertEquals(103, value.reads);
          executor.execute(() -> {
            nothingHappened.countDown();
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          ex.printStackTrace();
        }
      });
      Assert.assertTrue(nothingHappened.await(5000, TimeUnit.MILLISECONDS));
      compactOn.set(true);
      {
        LatchCallback cb1 = new LatchCallback();
        streamback.get().send("foo", null, "{}", cb1);
        cb1.await_success(105);
      }
      AtomicInteger reads = new AtomicInteger(100);
      int attempts = 25;
      while (reads.get() > 20 && attempts > 0) {
        attempts--;
        CountDownLatch latch = new CountDownLatch(1);
        dataService.get(KEY, new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            System.err.println("READS:" + value.reads);
            reads.set(value.reads);
            executor.execute(() -> {
              latch.countDown();
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {
            ex.printStackTrace();
          }
        });
        Assert.assertTrue(latch.await(2500, TimeUnit.MILLISECONDS));
        Thread.sleep(500);
      }
    } finally {
      service.shutdown();
      executor.shutdown();
    }
  }
}
