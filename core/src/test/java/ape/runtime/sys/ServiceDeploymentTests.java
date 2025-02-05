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

import ape.common.ErrorCodeException;
import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.contracts.DeploymentMonitor;
import ape.runtime.data.Key;
import ape.runtime.deploy.SyncCompiler;
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

import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServiceDeploymentTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CODE_MSG_FROM =
      "@static { create { return true; } } public int x; @connected { x = 42; return @who == @no_one; } message M {} channel foo(M y) { x += 100; }";
  private static final String SIMPLE_CODE_MSG_TO =
      "@static { create { return true; } } public int x; @connected { x = 42; return @who == @no_one; } message M {} channel foo(M y) { x += 5000; }";

  @Test
  public void deploy_happy() throws Exception {
    LivingDocumentFactory factoryFrom = LivingDocumentTests.compile(SIMPLE_CODE_MSG_FROM, Deliverer.FAILURE);
    LivingDocumentFactory factoryTo = LivingDocumentTests.compile(SIMPLE_CODE_MSG_TO, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factoryFrom);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      Runnable latch = streamback.latchAt(5);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_success(5);
      }
      factoryFactory.set(factoryTo);
      CountDownLatch deployed = new CountDownLatch(1);
      service.deploy(
          new DeploymentMonitor() {
            @Override
            public void bumpDocument(boolean changed) {
              if (changed) {
                deployed.countDown();
              }
            }

            @Override
            public void witnessException(ErrorCodeException ex) {
              ex.printStackTrace();
            }

            @Override
            public void finished(int ms) {

            }
          });
      Assert.assertTrue(deployed.await(1000, TimeUnit.MILLISECONDS));
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_success(7);
      }
      latch.run();
      Assert.assertEquals("STATUS:Connected", streamback.get(0));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":4}", streamback.get(1));
      Assert.assertEquals("{\"data\":{\"x\":142},\"seq\":5}", streamback.get(2));
      Assert.assertEquals("{\"data\":{\"x\":142},\"seq\":6}", streamback.get(3));
      Assert.assertEquals("{\"data\":{\"x\":5142},\"seq\":7}", streamback.get(4));
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void deploy_crash_data() throws Exception {
    LivingDocumentFactory factoryFrom = LivingDocumentTests.compile(SIMPLE_CODE_MSG_FROM, Deliverer.FAILURE);
    LivingDocumentFactory factoryTo = LivingDocumentTests.compile(SIMPLE_CODE_MSG_TO, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factoryFrom);
    TimeSource time = new MockTime();
    MockInstantDataService realDataService = new MockInstantDataService();
    MockDelayDataService dataService = new MockDelayDataService(realDataService);
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      Runnable latch = streamback.latchAt(4);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_success(5);
      }
      factoryFactory.set(factoryTo);
      dataService.pause();
      dataService.set(new MockFailureDataService());
      dataService.unpause();
      CountDownLatch deployed = new CountDownLatch(1);
      service.deploy(
          new DeploymentMonitor() {
            @Override
            public void bumpDocument(boolean changed) {}

            @Override
            public void witnessException(ErrorCodeException ex) {
              deployed.countDown();
            }

            @Override
            public void finished(int ms) {

            }
          });
      Assert.assertTrue(deployed.await(1000, TimeUnit.MILLISECONDS));
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_failure(144416);
      }
      latch.run();
      Assert.assertEquals("STATUS:Connected", streamback.get(0));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":4}", streamback.get(1));
      Assert.assertEquals("{\"data\":{\"x\":142},\"seq\":5}", streamback.get(2));
      Assert.assertEquals("STATUS:Disconnected", streamback.get(3));
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void deploy_bad_code() throws Exception {
    LivingDocumentFactory factoryFrom = LivingDocumentTests.compile(SIMPLE_CODE_MSG_FROM, Deliverer.FAILURE);
    LivingDocumentFactory factoryTo = LivingDocumentTests.compile(SIMPLE_CODE_MSG_TO, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factoryFrom);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      Runnable latch = streamback.latchAt(4);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_success(5);
      }
      factoryFactory.set(null);
      CountDownLatch deployed = new CountDownLatch(1);
      service.deploy(
          new DeploymentMonitor() {
            @Override
            public void bumpDocument(boolean changed) {}

            @Override
            public void witnessException(ErrorCodeException ex) {
              deployed.countDown();
            }

            @Override
            public void finished(int ms) {

            }
          });
      Assert.assertTrue(deployed.await(1000, TimeUnit.MILLISECONDS));
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_success(6);
      }
      latch.run();
      Assert.assertEquals("STATUS:Connected", streamback.get(0));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":4}", streamback.get(1));
      Assert.assertEquals("{\"data\":{\"x\":142},\"seq\":5}", streamback.get(2));
      Assert.assertEquals("{\"data\":{\"x\":242},\"seq\":6}", streamback.get(3));
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void deploy_really_bad_code() throws Exception {
    LivingDocumentFactory factoryFrom = LivingDocumentTests.compile(SIMPLE_CODE_MSG_FROM, Deliverer.FAILURE);
    LivingDocumentFactory factoryTo = LivingDocumentTests.compile(SIMPLE_CODE_MSG_TO, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factoryFrom);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      MockStreamback streamback = new MockStreamback();
      Runnable latch = streamback.latchAt(4);
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_success(5);
      }
      factoryFactory.set(
          new LivingDocumentFactory(
              SyncCompiler.compile("space",
              "Foo",
              "import java.util.HashMap;\nimport ape.runtime.contracts.DocumentMonitor;" +
                  "import ape.runtime.natives.*;import ape.runtime.sys.*;import ape.runtime.remote.client.*; import ape.runtime.remote.*;\n" +
                  "public class Foo { public Foo(DocumentMonitor dm) {} " +
                  "public static boolean __onCanCreate(CoreRequestContext who) { return false; } " +
                  "public static boolean __onCanInvent(CoreRequestContext who) { return false; } " +
                  "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { return false; } " +
                  "public static HashMap<String, Object> __config() { return new HashMap<>(); }" +
                  "public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); }" +
                  "public static void __create_generic_clients(ServiceRegistry s, HeaderDecryptor h) {}" +
                  "}",
              "{}"), Deliverer.FAILURE, new TreeMap<>()));
      CountDownLatch deployed = new CountDownLatch(1);
      service.deploy(
          new DeploymentMonitor() {
            @Override
            public void bumpDocument(boolean changed) {}

            @Override
            public void witnessException(ErrorCodeException ex) {
              deployed.countDown();
            }

            @Override
            public void finished(int ms) {

            }
          });
      Assert.assertTrue(deployed.await(1000, TimeUnit.MILLISECONDS));
      {
        LatchCallback callback = new LatchCallback();
        streamback.get().send("foo", null, "{}", callback);
        callback.await_success(6);
      }
      latch.run();
      Assert.assertEquals("STATUS:Connected", streamback.get(0));
      Assert.assertEquals("{\"data\":{\"x\":42},\"seq\":4}", streamback.get(1));
      Assert.assertEquals("{\"data\":{\"x\":142},\"seq\":5}", streamback.get(2));
      Assert.assertEquals("{\"data\":{\"x\":242},\"seq\":6}", streamback.get(3));
    } finally {
      service.shutdown();
    }
  }
}
