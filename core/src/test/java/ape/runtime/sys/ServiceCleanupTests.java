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
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.Key;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtDynamic;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.web.WebContext;
import ape.runtime.sys.web.WebGet;
import ape.runtime.sys.web.WebResponse;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceCleanupTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CODE_MSG =
      "@static { create { return true; } } public int x; @connected { x = 42; return @who == @no_one; } message M {} channel foo(M y) { x += 100; } @web get / { return {html:\"Hi\"}; } ";

  @Test
  public void cleanup_happens() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    MockTime time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    AtomicReference<CountDownLatch> latchMade = new AtomicReference<>(null);
    CountDownLatch latchSet = new CountDownLatch(1);
    CoreService service = new CoreService(METRICS, factoryFactory, (samples) -> {
      PredictiveInventory.MeteringSample meteringSample = samples.get(KEY.space);
      if (meteringSample != null) {
        if (meteringSample.count == 1) {
          if (latchMade.get() == null) {
            latchMade.set(new CountDownLatch(1));
            latchSet.countDown();
          }
        } else if (meteringSample.count == 0) {
          if (latchMade.get() != null) {
            latchMade.get().countDown();
          }
        }
        System.err.println(meteringSample.count);
      }
    },  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    service.tune(
        (base) -> {
          base.setInventoryMillisecondsSchedule(1000, 50);
        });
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      Runnable latch1 = streamback.latchAt(2);
      Runnable latch2 = streamback.latchAt(3);
      Runnable latch3 = streamback.latchAt(4);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      latch1.run();
      Assert.assertEquals("STATUS:Connected", streamback.get(0));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":4}", streamback.get(1));
      TreeMap<String, String> query = new TreeMap<>();
      CountDownLatch latchQuery = new CountDownLatch(1);
      query.put("space", KEY.space);
      query.put("key", KEY.key);
      AtomicReference<String> queryResult = new AtomicReference<>();
      service.query(query, new Callback<String>() {
        @Override
        public void success(String value) {
          queryResult.set(value);
          latchQuery.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      Assert.assertTrue(latchQuery.await(5000, TimeUnit.MILLISECONDS));
      {
        TreeMap<String, String> queryList = new TreeMap<>();
        queryList.put("list", "");
        AtomicReference<String> queryResult2 = new AtomicReference<>();
        CountDownLatch latchQuery2 = new CountDownLatch(1);
        service.query(queryList, new Callback<String>() {
          @Override
          public void success(String value) {
            queryResult2.set(value);
            latchQuery2.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(latchQuery2.await(10000, TimeUnit.MILLISECONDS));
        Assert.assertEquals("{\"space/key\":{\"ex\":2}}", queryResult2.get());
      }
      System.err.println(queryResult.get());
      Assert.assertTrue(queryResult.get().startsWith("{\"thread\":2,\"space\":\"space\",\"key\":\"key\""));
      LatchCallback cb1 = new LatchCallback();
      streamback.get().send("foo", null, "{}", cb1);
      cb1.await_success(5);
      latch2.run();
      Assert.assertEquals("{\"data\":{\"x\":142},\"seq\":5}", streamback.get(2));
      streamback.get().close();
      latch3.run();
      Assert.assertEquals("STATUS:Disconnected", streamback.get(3));
      Assert.assertTrue(latchSet.await(5000, TimeUnit.MILLISECONDS));
      while (!latchMade.get().await(1000, TimeUnit.MILLISECONDS)) {
        time.time += 10000;
      }
      Thread.sleep(1000);
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void cleanup_happens_just_load() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    MockTime time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    AtomicReference<CountDownLatch> latchMade = new AtomicReference<>(null);
    CountDownLatch latchSet = new CountDownLatch(1);
    Runnable dataLatch = dataService.latchLogAt(3);
    CoreService service = new CoreService(METRICS, factoryFactory, (samples) -> {
      time.time += 1000;
      PredictiveInventory.MeteringSample meteringSample = samples.get(KEY.space);
      if (meteringSample != null) {
        if (meteringSample.count == 1) {
          if (latchMade.get() == null) {
            latchMade.set(new CountDownLatch(1));
            latchSet.countDown();
          }
        } else if (meteringSample.count == 0) {
          if (latchMade.get() != null) {
            latchMade.get().countDown();
          }
        }
        System.err.println(meteringSample.count);
      }
    },  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    service.tune(
        (base) -> {
          base.setInventoryMillisecondsSchedule(250, 50);
          base.setMillisecondsInactivityBeforeCleanup(25);
        });
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      CountDownLatch got = new CountDownLatch(1);
      service.webGet(KEY, new WebGet(new WebContext(NtPrincipal.NO_ONE, "Origin", "1.2.3.4"), "/", new TreeMap<>(), new NtDynamic("{}")), new Callback<WebResponse>() {
        @Override
        public void success(WebResponse value) {
          got.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      Assert.assertTrue(got.await(5000, TimeUnit.MILLISECONDS));
      dataLatch.run();
      dataService.assertLogAtStartsWith(0, "INIT:space/key:");
      dataService.assertLogAt(1, "LOAD:space/key");
      dataService.assertLogAt(2, "CLOSE:space/key");
    } finally {
      service.shutdown();
    }
  }

}
