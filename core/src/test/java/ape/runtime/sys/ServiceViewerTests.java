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

import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.Key;
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

public class ServiceViewerTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CODE_V = "@static { create { return true; } }" +
      "@connected { return true; }" +
      "view int x;" + //
      "bubble my_x = @viewer.x;";

  @Test
  public void viewer_updates() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_V, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      Runnable latch1 = streamback.latchAt(3);
      Runnable latch2 = streamback.latchAt(4);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{\"x\":42}", ConnectionMode.Full, streamback);
      latch1.run();
      Assert.assertEquals("STATUS:Connected", streamback.get(0));
      Assert.assertEquals("{\"view-state-filter\":[\"x\"]}", streamback.get(1));
      Assert.assertEquals("{\"data\":{\"my_x\":42},\"seq\":4}", streamback.get(2));
      SimpleVoidCallback updateRan = new SimpleVoidCallback();
      streamback.get().update("{\"x\":5050}", updateRan);
      updateRan.assertSuccess();
      latch2.run();
      Assert.assertEquals("{\"data\":{\"my_x\":5050}}", streamback.get(3));
    } finally {
      service.shutdown();
    }
  }
}
