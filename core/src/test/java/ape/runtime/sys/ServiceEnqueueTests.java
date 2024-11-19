/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.runtime.sys;

import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
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

public class ServiceEnqueueTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CRON =
      "@static { create { return true; } } public int x; @connected { x = 42; return @who == @no_one; } @cron fooz daily 8:00 { x += 7; doit.enqueue(@no_one, {}); } message M {} channel doit(M m) { x += 100; } ";

  @Test
  public void enqueue_transfer_happy() throws Exception {
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

      MockStreamback streamback = new MockStreamback();
      Runnable latch1 = streamback.latchAt(2);
      Runnable latch2 = streamback.latchAt(4);
       Runnable latch3 = streamback.latchAt(6);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      latch1.run();
      Assert.assertEquals("STATUS:Connected", streamback.get(0));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":5}", streamback.get(1));
      time.time += 1000L * 60L * 60L * 24 * 2;
      service.invalidateAll();
      latch2.run();
      Assert.assertEquals("{\"data\":{\"x\":49},\"seq\":6}", streamback.get(2));
      Assert.assertEquals("{\"data\":{\"x\":149},\"seq\":8}", streamback.get(3));
      time.time += 1000L * 60L * 60L * 24 * 2;
      service.invalidateAll();
      latch3.run();
      Assert.assertEquals("{\"data\":{\"x\":156},\"seq\":9}", streamback.get(4)); // ok, why?
      Assert.assertEquals("{\"data\":{\"x\":256},\"seq\":11}", streamback.get(5));
    } finally {
      service.shutdown();
    }
  }
}
