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
package ape.runtime.sys.readonly;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.DataObserver;
import ape.runtime.data.Key;
import ape.runtime.data.mocks.SimpleVoidCallback;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.CoreMetrics;
import ape.runtime.sys.CoreService;
import ape.runtime.sys.mocks.MockInstantDataService;
import ape.runtime.sys.mocks.MockInstantLivingDocumentFactoryFactory;
import ape.runtime.sys.mocks.MockMetricsReporter;
import ape.runtime.sys.mocks.MockReplicationInitiator;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class LocalReplicationProxyTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CODE =
      "@static { create { return true; } } public int x = 100;";

  @Test
  public void skip() throws Exception {
    MockReplicationInitiator initiator = new MockReplicationInitiator();
    LocalReplicationProxy proxy = new LocalReplicationProxy("me", initiator);
    AtomicReference<Runnable> runGot = new AtomicReference<>(null);
    CountDownLatch latch = new CountDownLatch(3);
    proxy.startDocumentReplication(KEY, new DataObserver() {
      @Override
      public String machine() {
        return null;
      }

      @Override
      public void start(String snapshot) {
        latch.countDown();
      }

      @Override
      public void change(String delta) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException exception) {

      }
    }, new Callback<Runnable>() {
      @Override
      public void success(Runnable value) {
        runGot.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void go_to_service() throws Exception {
    MockReplicationInitiator initiator = new MockReplicationInitiator();
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    LocalReplicationProxy proxy = new LocalReplicationProxy("me", initiator);
    proxy.initialize(service);

    SimpleVoidCallback started = new SimpleVoidCallback();
    service.create(ContextSupport.WRAP(new NtPrincipal("agent", "doom")), KEY, "{}", "123", started);
    started.assertSuccess();
    CountDownLatch latch = new CountDownLatch(2);
    AtomicReference<Runnable> cancel = new AtomicReference<>(null);
    proxy.startDocumentReplication(KEY, new DataObserver() {
      @Override
      public String machine() {
        return "me";
      }

      @Override
      public void start(String snapshot) {
        Assert.assertEquals(100, Json.parseJsonObject(snapshot).get("x").asInt());
        latch.countDown();
      }

      @Override
      public void change(String delta) {
      }

      @Override
      public void failure(ErrorCodeException exception) {

      }
    }, new Callback<Runnable>() {
      @Override
      public void success(Runnable value) {
        cancel.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    Assert.assertNotNull(cancel.get());
    cancel.get().run();
  }
}
