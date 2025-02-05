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

import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.Key;
import ape.runtime.data.InMemoryDataService;
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

public class ServiceMultiboxTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final NtPrincipal ALICE = new NtPrincipal("alice", "test");
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CODE_MSG =
      "@static { create { return true; } } public int x; @connected { x += 1; return true; } @disconnected { x -= 1; } message M {} channel foo(M y) { x += 1000; }";

  @Test
  public void seq_conflict_at_data_service() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService realDataService = new MockInstantDataService();
    MockDelayDataService dataService = new MockDelayDataService(realDataService);
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    service.tune(
        (base) -> {
          base.setMillisecondsForCleanupCheck(25);
          base.setMillisecondsAfterLoadForReconciliation(10000);
        });
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(ALICE), KEY, "{}", "1", created);
      created.await_success();
      MockStreamback streamback1 = new MockStreamback();
      Runnable latch1 = streamback1.latchAt(6);
      service.connect(ContextSupport.WRAP(ALICE), KEY, "{}", ConnectionMode.Full, streamback1);
      streamback1.await_began();
      realDataService.skipAt(5);
      LatchCallback cb1 = new LatchCallback();
      streamback1.get().send("foo", null, "{}", cb1);
      cb1.await_success(10002);
      realDataService.skipAt(5005);
      LatchCallback cb2 = new LatchCallback();
      streamback1.get().send("foo", null, "{}", cb2);
      cb2.await_success(10003);
      realDataService.skipAt(10004);
      realDataService.killSkip();
      LatchCallback cb3 = new LatchCallback();
      streamback1.get().send("foo", null, "{}", cb3);
      cb3.await_failure(555);
      latch1.run();
      Assert.assertEquals("STATUS:Connected", streamback1.get(0));
      Assert.assertEquals("{\"data\":{\"x\":1},\"seq\":4}", streamback1.get(1));
      Assert.assertEquals("{\"data\":{\"x\":70000},\"seq\":10001}", streamback1.get(2));
      Assert.assertEquals("{\"data\":{\"x\":71000},\"seq\":10002}", streamback1.get(3));
      Assert.assertEquals("{\"data\":{\"x\":72000},\"seq\":10003}", streamback1.get(4));
      Assert.assertEquals("STATUS:Disconnected", streamback1.get(5));
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void too_many_conflicts() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService realDataService = new MockInstantDataService();
    MockDelayDataService dataService = new MockDelayDataService(realDataService);
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    service.tune(
        (base) -> {
          base.setMillisecondsForCleanupCheck(25);
          base.setMillisecondsAfterLoadForReconciliation(10000);
        });
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(ALICE), KEY, "{}", "1", created);
      created.await_success();
      MockStreamback streamback1 = new MockStreamback();
      Runnable latch1 = streamback1.latchAt(3);
      service.connect(ContextSupport.WRAP(ALICE), KEY, "{}", ConnectionMode.Full, streamback1);
      streamback1.await_began();
      realDataService.infiniteSkip();
      LatchCallback cb1 = new LatchCallback();
      streamback1.get().send("foo", null, "{}", cb1);
      cb1.await_failure(621580);
      latch1.run();
      Assert.assertEquals("STATUS:Connected", streamback1.get(0));
      Assert.assertEquals("{\"data\":{\"x\":1},\"seq\":4}", streamback1.get(1));
      Assert.assertEquals("STATUS:Disconnected", streamback1.get(2));
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void splitBrainMultiBoxSync() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    InMemoryDataService dataService = new InMemoryDataService((r) -> r.run(), time);
    CoreService service1 = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    CoreService service2 = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service1.create(ContextSupport.WRAP(ALICE), KEY, "{}", "1", created);
      created.await_success();

      MockStreamback streamback1 = new MockStreamback();
      Runnable latch1 = streamback1.latchAt(11);
      service1.connect(ContextSupport.WRAP(ALICE), KEY, "{}", ConnectionMode.Full, streamback1);
      streamback1.await_began();

      MockStreamback streamback2 = new MockStreamback();
      Runnable latch2 = streamback2.latchAt(10);
      service2.connect(ContextSupport.WRAP(ALICE), KEY, "{}", ConnectionMode.Full, streamback2);
      streamback2.await_began();

      { // 2000
        // service 1
        LatchCallback cb1 = new LatchCallback();
        streamback1.get().send("foo", null, "{}", cb1);
        cb1.await_success(7);
        // service 2
        LatchCallback cb2 = new LatchCallback();
        streamback2.get().send("foo", null, "{}", cb2);
        cb2.await_success(9);
      }

      { // 4000
        // service 1
        LatchCallback cb1 = new LatchCallback();
        streamback1.get().send("foo", null, "{}", cb1);
        cb1.await_success(11);
        LatchCallback cb2 = new LatchCallback();
        streamback1.get().send("foo", null, "{}", cb2);
        cb2.await_success(12);
      }

      { // 6000
        // service 2
        LatchCallback cb1 = new LatchCallback();
        streamback2.get().send("foo", null, "{}", cb1);
        cb1.await_success(14);
        LatchCallback cb2 = new LatchCallback();
        streamback2.get().send("foo", null, "{}", cb2);
        cb2.await_success(15);
      }
      { // 8000
        // service 1
        LatchCallback cb1 = new LatchCallback();
        streamback1.get().send("foo", null, "{}", cb1);
        cb1.await_success(17);
        // service 2
        LatchCallback cb2 = new LatchCallback();
        streamback2.get().send("foo", null, "{}", cb2);
        cb2.await_success(19);
      }
      { // 10000
        // service 2
        LatchCallback cb2 = new LatchCallback();
        streamback2.get().send("foo", null, "{}", cb2);
        cb2.await_success(20);
        // service 1
        LatchCallback cb1 = new LatchCallback();
        streamback1.get().send("foo", null, "{}", cb1);
        cb1.await_success(22);
      }
      latch1.run();
      latch2.run();

      Assert.assertEquals("STATUS:Connected", streamback1.get(0));
      Assert.assertEquals("{\"data\":{\"x\":1},\"seq\":4}", streamback1.get(1));
      Assert.assertEquals("{\"data\":{\"x\":1},\"seq\":6}", streamback1.get(2));
      Assert.assertEquals("{\"data\":{\"x\":1001},\"seq\":7}", streamback1.get(3));
      Assert.assertEquals("{\"seq\":10}", streamback1.get(4));
      Assert.assertEquals("{\"data\":{\"x\":3001},\"seq\":11}", streamback1.get(5));
      Assert.assertEquals("{\"data\":{\"x\":4001},\"seq\":12}", streamback1.get(6));
      Assert.assertEquals("{\"data\":{\"x\":6001},\"seq\":16}", streamback1.get(7));
      Assert.assertEquals("{\"data\":{\"x\":7001},\"seq\":17}", streamback1.get(8));
      Assert.assertEquals("{\"data\":{\"x\":9001},\"seq\":21}", streamback1.get(9));
      Assert.assertEquals("{\"data\":{\"x\":10001},\"seq\":22}", streamback1.get(10));
      // last write got to 10000, which is what we expect

      Assert.assertEquals("STATUS:Connected", streamback2.get(0));
      Assert.assertEquals("{\"data\":{\"x\":1},\"seq\":5}", streamback2.get(1));
      Assert.assertEquals("{\"seq\":8}", streamback2.get(2));
      Assert.assertEquals("{\"data\":{\"x\":2001},\"seq\":9}", streamback2.get(3));
      Assert.assertEquals("{\"data\":{\"x\":4001},\"seq\":13}", streamback2.get(4));
      Assert.assertEquals("{\"data\":{\"x\":5001},\"seq\":14}", streamback2.get(5));
      Assert.assertEquals("{\"data\":{\"x\":6001},\"seq\":15}", streamback2.get(6));
      Assert.assertEquals("{\"seq\":18}", streamback2.get(7));
      Assert.assertEquals("{\"data\":{\"x\":8001},\"seq\":19}", streamback2.get(8));
      Assert.assertEquals("{\"data\":{\"x\":9001},\"seq\":20}", streamback2.get(9));
      // NOTE: the second server hasn't caught up yet
    } finally {
      service1.shutdown();
      service2.shutdown();
    }
  }
}
