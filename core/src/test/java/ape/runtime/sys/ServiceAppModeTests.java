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

import ape.common.Json;
import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.InMemoryDataService;
import ape.runtime.data.Key;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceAppModeTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());

  private static final Key KEY = new Key("space", "key");
  private static final String APP_MODE =
      "@static { create { return true; } invent { return true; } frequency = 100; } public int x; public int y; @connected { x = 42; y = 0; return true; } message M {} channel foo(M z) { x += 100; y++; }";

  @Test
  public void app_mode() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(APP_MODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    InMemoryDataService dataService = new InMemoryDataService(executor, TimeSource.REAL_TIME);
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 1);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();

      MockStreamback streambackViewer = new MockStreamback();
      service.connect(ContextSupport.WRAP(new NtPrincipal("ag", "au")), KEY, "{}", ConnectionMode.Full, streambackViewer);
      streambackViewer.await_began();

      ArrayList<LatchCallback> callbacks = new ArrayList<>();
      for (int k = 0; k < 1000; k++) {
        LatchCallback cb = new LatchCallback();
        callbacks.add(cb);
        streamback.get().send("foo", null, "{}", cb);
        if (k % 50 == 0) {
          // do this so there are multiple
          cb.awaitJustSuccess();
        }
      }
      for(LatchCallback cb : callbacks) {
        cb.awaitJustSuccess();
      }
      int attempts = 1000;
      while (Json.parseJsonObject(streamback.get(streamback.size() - 1)).get("seq").intValue() < 1000 && attempts > 0) {
        System.err.println(streamback.get(streamback.size() - 1));
        Thread.sleep(25);
        attempts--;
      }
      Assert.assertTrue(streamback.size() < 1050);
      Assert.assertTrue(streambackViewer.size() < 100);
    } finally {
      service.shutdown();
      executor.shutdown();
    }
  }

}
