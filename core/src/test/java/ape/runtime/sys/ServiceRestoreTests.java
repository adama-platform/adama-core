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

import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.DocumentRestore;
import ape.runtime.data.Key;
import ape.runtime.data.mocks.SimpleIntCallback;
import ape.runtime.data.mocks.SimpleVoidCallback;
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

public class ServiceRestoreTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CRON =
      "@static { create { return true; } } public int x = 0; @connected { x += 42; return @who == @no_one; } @cron fooz daily 8:00 { x += 7; doit.enqueue(@no_one, {}); } message M {} channel doit(M m) { x += 100; } ";

  @Test
  public void restore() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CRON, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    MockTime time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      {
        MockStreamback streamback = new MockStreamback();
        Runnable latch1 = streamback.latchAt(2);
        Runnable latch2 = streamback.latchAt(3);
        service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
        streamback.await_began();
        latch1.run();
        Assert.assertEquals("STATUS:Connected", streamback.get(0));
        Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":5}", streamback.get(1));
        SimpleVoidCallback cb_Restored = new SimpleVoidCallback();
        service.restore(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, new DocumentRestore(100, "{\"x\":1000,\"__seq\":10,\"__constructed\":true,\"__entropy\":\"8552295702242200522\"}", NtPrincipal.NO_ONE), cb_Restored);
        cb_Restored.assertSuccess();
        latch2.run();
        Assert.assertEquals("STATUS:Disconnected", streamback.get(2));
      }
      dataService.assertLogAt(1, "LOAD:space/key");
      dataService.assertLogAt(4, "RECOVER:space/key");
      {
        MockStreamback streamback = new MockStreamback();
        Runnable latch1 = streamback.latchAt(2);
        service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
        streamback.await_began();
        latch1.run();
        Assert.assertEquals("STATUS:Connected", streamback.get(0));
        Assert.assertEquals("{\"data\":{\"x\":1042},\"seq\":14}", streamback.get(1));
      }
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void restore_after_patch() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CRON, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    MockTime time = new MockTime();
    MockInstantDataService instant = new MockInstantDataService();
    MockDelayDataService dataService = new MockDelayDataService(instant);
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      {
        MockStreamback streamback = new MockStreamback();
        Runnable latch1 = streamback.latchAt(2);
        Runnable latch2 = streamback.latchAt(4);
        service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
        streamback.await_began();
        latch1.run();
        Assert.assertEquals("STATUS:Connected", streamback.get(0));
        Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":5}", streamback.get(1));
        dataService.pause();
        Runnable latchPatching = dataService.latchAt(1);
        SimpleIntCallback cb_Send = new SimpleIntCallback();
        streamback.get().send("doit", null, "{}", cb_Send);
        latchPatching.run();
        SimpleVoidCallback cb_Restored = new SimpleVoidCallback();
        service.restore(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, new DocumentRestore(100, "{\"x\":1000,\"__seq\":10,\"__constructed\":true,\"__entropy\":\"8552295702242200522\"}", NtPrincipal.NO_ONE), cb_Restored);
        dataService.unpause();
        cb_Restored.assertSuccess();
        latch2.run();
        cb_Send.assertSuccess(6);
        Assert.assertEquals("{\"data\":{\"x\":142},\"seq\":6}", streamback.get(2));
        Assert.assertEquals("STATUS:Disconnected", streamback.get(3));
      }
      instant.assertLogAt(1, "LOAD:space/key");
      instant.assertLogAt(5, "RECOVER:space/key");
      {
        MockStreamback streamback = new MockStreamback();
        Runnable latch1 = streamback.latchAt(2);
        service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
        streamback.await_began();
        latch1.run();
        Assert.assertEquals("STATUS:Connected", streamback.get(0));
        Assert.assertEquals("{\"data\":{\"x\":1042},\"seq\":14}", streamback.get(1));
      }
    } finally {
      service.shutdown();
    }
  }
}
