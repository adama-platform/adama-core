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
import ape.runtime.data.mocks.SimpleStringCallback;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.mocks.*;
import ape.runtime.sys.mocks.*;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Test;

public class ServiceAuthTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String SIMPLE_CODE_MSG =
      "@static { create { return true; } }" +
          "public int x; @connected { x = 42; return @who == @no_one; } message M {} channel foo(M y) { x += 100; }" +
          "private string password_plain = \"password\";" +
          "@authorize(user, pw) { if (pw == password_plain) { return \"yes\"; } abort; }" +
          "@password(new_password) { password_plain = new_password; }";

  @Test
  public void flow() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile(SIMPLE_CODE_MSG, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {},  new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    try {
      NullCallbackLatch created = new NullCallbackLatch();
      service.create(ContextSupport.WRAP(NtPrincipal.NO_ONE), KEY, "{}", null, created);
      created.await_success();
      {
        SimpleStringCallback cb = new SimpleStringCallback();
        service.authorize("origin", "0.0.0.0", KEY, "user", "nope", null, cb);
        cb.assertFailure(191713);
      }
      {
        SimpleStringCallback cb = new SimpleStringCallback();
        service.authorize("origin", "0.0.0.0", KEY, "user", "password", null, cb);
        cb.assertSuccess("yes");
      }
      {
        SimpleStringCallback cb = new SimpleStringCallback();
        service.authorize("origin", "0.0.0.0", KEY, "user", "password", "next-password", cb);
        cb.assertSuccess("yes");
      }
      {
        SimpleStringCallback cb = new SimpleStringCallback();
        service.authorize("origin", "0.0.0.0", KEY, "user", "password", null, cb);
        cb.assertFailure(191713);
      }
      {
        SimpleStringCallback cb = new SimpleStringCallback();
        service.authorize("origin", "0.0.0.0", KEY, "user", "next-password", null, cb);
        cb.assertSuccess("yes");
      }
    } finally {
      service.shutdown();
    }
  }
}
