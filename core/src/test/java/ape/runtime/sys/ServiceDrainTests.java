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

import ape.common.Callback;
import ape.common.ErrorCodeException;
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
import ape.runtime.sys.capacity.CurrentLoad;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.mocks.*;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceDrainTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final Key KEY2 = new Key("space", "key2");
  private static final String SIMPLE_CODE_MSG =
      "@static { create { return true; } } public int x; @connected { x += 1; return true; } @disconnected { x -= 1; } message M {} channel foo(M y) { x += 1000; }";

  @Test
  public void drain() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      Runnable latch = dataService.latchLogAt(9);
      {
        NullCallbackLatch created = new NullCallbackLatch();
        service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", "1", created);
        created.await_success();
      }
      {
        NullCallbackLatch created = new NullCallbackLatch();
        service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY2, "{}", "1", created);
        created.await_success();
      }

      MockStreamback streamback1 = new MockStreamback();
      Runnable latchStreamBack = streamback1.latchAt(3);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback1);
      streamback1.await_began();

      {
        CountDownLatch getLoadNow = new CountDownLatch(1);
        service.getLoad(new Callback<CurrentLoad>() {
          @Override
          public void success(CurrentLoad load) {
            Assert.assertEquals(2, load.documents);
            Assert.assertEquals(1, load.connections);
            getLoadNow.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(getLoadNow.await(5000, TimeUnit.MILLISECONDS));
      }

      SimpleVoidCallback svc = new SimpleVoidCallback();
      service.drainService(svc);
      svc.assertSuccess();
      latchStreamBack.run();
      Assert.assertEquals("STATUS:Connected", streamback1.get(0));
      Assert.assertEquals("{\"data\":{\"x\":1},\"seq\":4}", streamback1.get(1));
      Assert.assertEquals("STATUS:Disconnected", streamback1.get(2));
      latch.run();
      dataService.assertLogAt(0, "INIT:space/key:1->{\"__constructed\":true,\"__entropy\":\"-4964420948893066024\",\"__messages\":null,\"__seq\":1}");
      dataService.assertLogAt(1, "INIT:space/key2:1->{\"__constructed\":true,\"__entropy\":\"-4964420948893066024\",\"__messages\":null,\"__seq\":1}");
      dataService.assertLogAt(2, "LOAD:space/key");
      dataService.assertLogAt(6, "INVENTORY");
      dataService.assertLogAt(7, "SHED:space/key");
      dataService.assertLogAt(8, "SHED:space/key2");
      {
        NullCallbackLatch created = new NullCallbackLatch();
        service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), new Key("space", "key-what"), "{}", "1", created);
        created.await_failure(193265);
      }
      {
        MockStreamback streamback2 = new MockStreamback();
        service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback2);
        streamback2.await_failure(193265);
      }

      AtomicBoolean empty = new AtomicBoolean(false);
      int attempts = 10;
      while (attempts > 0 && !empty.get()) {
        attempts--;
        CountDownLatch getLoadNow = new CountDownLatch(1);
        service.getLoad(new Callback<CurrentLoad>() {
          @Override
          public void success(CurrentLoad load) {
            System.err.println("documents:" + load.documents + "; connections=" + load.connections);
            empty.set(load.documents == 0 && load.connections == 0);
            getLoadNow.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(getLoadNow.await(5000, TimeUnit.MILLISECONDS));
        if (!empty.get()) {
          Thread.sleep(250);
        }
      }
      Assert.assertTrue(empty.get());

    } finally {
      service.shutdown();
    }
  }

}
