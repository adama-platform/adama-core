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
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.Key;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.remote.Deliverer;
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

public class ServiceReflectTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CODE_MSG =
      "@static { create { return true; } } public int x; @connected { x = 42; return @who == @no_one; } message M {} channel foo(M y) { x += 100; }";

  @Test
  public void reflect_happy() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference valueRef = new AtomicReference(null);
      service.reflect(KEY, new Callback<String>() {
        @Override
        public void success(String value) {
          valueRef.set(value);
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      Assert.assertTrue(latch.await(2500, TimeUnit.MILLISECONDS));
      Assert.assertEquals(
          "{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{\"x\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"public\"}}},\"__ViewerType\":{\"nature\":\"native_message\",\"name\":\"__ViewerType\",\"anonymous\":true,\"fields\":{}},\"M\":{\"nature\":\"native_message\",\"name\":\"M\",\"anonymous\":false,\"fields\":{}}},\"channels\":{\"foo\":\"M\"},\"channels-privacy\":{\"foo\":{\"open\":false,\"privacy\":[]}},\"constructors\":[],\"labels\":[]}",
          valueRef.get());
    } finally {
      service.shutdown();
    }
  }

  @Test
  public void reflect_sad() throws Exception {
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(null);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference valueRef = new AtomicReference(null);
      service.reflect(KEY, new Callback<String>() {
        @Override
        public void success(String value) {
        }

        @Override
        public void failure(ErrorCodeException ex) {
          latch.countDown();
        }
      });
      Assert.assertTrue(latch.await(2500, TimeUnit.MILLISECONDS));
    } finally {
      service.shutdown();
    }
  }
}
