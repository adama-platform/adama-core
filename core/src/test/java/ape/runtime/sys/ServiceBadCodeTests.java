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
import ape.runtime.natives.NtAsset;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.mocks.*;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServiceBadCodeTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String BAD_CODE = "@can_attach { int x = 1; while(true) { x++; } return true; } @attached(what) { while(true) {} } @static { create { return true; } } @connected { return true; } message M {} channel foo(M y) { while(true) {} }  ";

  @Test
  public void bad_code() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(BAD_CODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockFailureDataService failureDataService = new MockFailureDataService();
    MockInstantDataService realDataService = new MockInstantDataService();
    MockDelayDataService dataService = new MockDelayDataService(realDataService);
    CoreService service = new CoreService(METRICS, factoryFactory,(bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", "1", created);
      created.await_success();

      MockStreamback streamback = new MockStreamback();
      service.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", ConnectionMode.Full, streamback);
      streamback.await_began();
      CountDownLatch latch = new CountDownLatch(2);
      streamback.get().canAttach(new Callback<Boolean>() {
        @Override
        public void success(Boolean value) {

        }

        @Override
        public void failure(ErrorCodeException ex) {
          Assert.assertEquals(146569, ex.code);
          latch.countDown();
        }
      });
      NtAsset a = NtAsset.NOTHING;
      streamback.get().attach(a.id, a.name, a.contentType, a.size, a.md5, a.sha384, new Callback<Integer>() {
        @Override
        public void success(Integer value) {

        }

        @Override
        public void failure(ErrorCodeException ex) {
          Assert.assertEquals(950384, ex.code);
          latch.countDown();
        }
      });
      streamback.get().send("foo", "marker", "{}", new Callback<Integer>() {
        @Override
        public void success(Integer value) {
        }

        @Override
        public void failure(ErrorCodeException ex) {
          Assert.assertEquals(950384, ex.code);
          latch.countDown();
        }
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    } finally {
      service.shutdown();
    }
  }
}
