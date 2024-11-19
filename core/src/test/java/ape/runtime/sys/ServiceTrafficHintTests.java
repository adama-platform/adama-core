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
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.web.WebContext;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.web.*;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServiceTrafficHintTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final Key BADKEY = new Key("space", "key-bad");
  private static WebContext CONTEXT = new WebContext(NtPrincipal.NO_ONE, "Origin", "1.2.3.4");
  private static final String SIMPLE_CODE_MSG = "@static { create { return true; } }" +
      "@traffic \"howdy\";";
  private static final String SIMPLE_CODE_DEFAULT = "@static { create { return true; } }";

  @Test
  public void get_default_hint() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_DEFAULT, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      CountDownLatch latch = new CountDownLatch(2);
      service.trafficHint(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, new Callback<String>() {
        @Override
        public void success(String value) {
          Assert.assertEquals("", value);
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      service.trafficHint(ContextSupport.WRAP(NtPrincipal.NO_ONE), new Key("nope", "nope"), new Callback<String>() {
        @Override
        public void success(String value) {
        }

        @Override
        public void failure(ErrorCodeException ex) {
          Assert.assertEquals(625676, ex.code);
          latch.countDown();
        }
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void get_hint() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      CountDownLatch latch = new CountDownLatch(2);
      service.trafficHint(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, new Callback<String>() {
        @Override
        public void success(String value) {
          Assert.assertEquals("howdy", value);
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      service.trafficHint(ContextSupport.WRAP(NtPrincipal.NO_ONE), new Key("nope", "nope"), new Callback<String>() {
        @Override
        public void success(String value) {
        }

        @Override
        public void failure(ErrorCodeException ex) {
          Assert.assertEquals(625676, ex.code);
          latch.countDown();
        }
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    } finally {
      service.shutdown();
    }
  }
}
