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
package ape.runtime.deploy;

import ape.common.Callback;
import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.LivingDocumentTests;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
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

import java.util.ArrayList;

public class DelayedDeployTests {
  @Test
  public void flow() throws Exception {
    LivingDocumentFactory factory = LivingDocumentTests.compile("", Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    TimeSource time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(new CoreMetrics(new NoOpMetricsFactory()), factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);

    DelayedDeploy dd = new DelayedDeploy();
    dd.deploy("space", Callback.DONT_CARE_VOID);
    ArrayList<String> deployed = new ArrayList<>();
    dd.set(new Deploy() {
      @Override
      public void deploy(String space, Callback<Void> callback) {
        deployed.add(space);
        callback.success(null);
      }
    }, service);
    Assert.assertEquals(1, deployed.size());
    dd.deploy("now", Callback.DONT_CARE_VOID);
    Assert.assertEquals(2, deployed.size());
    Assert.assertEquals("space", deployed.get(0));
    Assert.assertEquals("now", deployed.get(1));
  }
}
