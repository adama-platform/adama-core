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
package ape.runtime.sys.capacity;

import ape.common.SimpleExecutor;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.Key;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.CoreMetrics;
import ape.runtime.sys.CoreService;
import ape.runtime.sys.ServiceHeatEstimator;
import ape.runtime.sys.ServiceShield;
import ape.runtime.sys.mocks.MockInstantDataService;
import ape.runtime.sys.mocks.MockInstantLivingDocumentFactoryFactory;
import ape.runtime.sys.mocks.MockMetricsReporter;
import ape.runtime.sys.mocks.MockReplicationInitiator;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class CapacityAgentTests {
  private static final CoreMetrics METRICS = new CoreMetrics(new NoOpMetricsFactory());
  private static final Key KEY = new Key("space", "key");
  private static final String PUBSUB_CODE = "@static {\n" + "  // anyone can create\n" + "  create { return true; }\n" + "}\n" + "\n" + "@connected {\n" + "   // let everyone connect; sure, what can go wrong\n" + "  return true;\n" + "}\n" + "\n" + "// we build a table of publishes with who published it and when they did so\n" + "record Publish {\n" + "  public principal who;\n" + "  public long when;\n" + "  public string payload;\n" + "}\n" + "\n" + "table<Publish> _publishes;\n" + "\n" + "// since tables are private, we expose all publishes to all connected people\n" + "public formula publishes = iterate _publishes order by when asc;\n" + "\n" + "// we wrap a payload inside a message\n" + "message PublishMessage {\n" + "  string payload;\n" + "}\n" + "\n" + "// and then open a channel to accept the publish from any connected client\n" + "channel publish(PublishMessage message) {\n" + "  _publishes <- {who: @who, when: Time.now(), payload: message.payload };\n" + "  \n" + "  // At this point, we encounter a key problem with maintaining a\n" + "  // log of publishes. Namely, the log is potentially infinite, so\n" + "  // we have to leverage some product intelligence to reduce it to\n" + "  // a reasonably finite set which is important for the product.\n" + "\n" + "  // First, we age out publishes too old (sad face)\n" + "  (iterate _publishes\n" + "     where when < Time.now() - 60000L).delete();\n" + "  \n" + "  // Second, we hard cap the publishes biasing younger ones\n" + "  (iterate _publishes\n" + "     order by when desc\n" + "     limit _publishes.size() offset 100).delete();\n" + "     \n" + "  // Hindsight: I should decouple the offset from\n" + "  // the limit because this is currently silly (TODO)\n" + "}";
  private static final String MAXSEQ_CODE = "@static {\n" + "  create { return true; }\n" + "}\n" + "\n" + "@connected {\n" + "  return true;\n" + "}\n" + "\n" + "public int max_db_seq = 0;\n" + "\n" + "message NotifyWrite {\n" + "  int db_seq;\n" + "}\n" + "\n" + "channel notify(NotifyWrite message) {\n" + "  if (message.db_seq > max_db_seq) {\n" + "    max_db_seq = message.db_seq;\n" + "  }\n" + "}";

  @Test
  public void flow() throws Exception {
    MockCapacityOverseer overseer = new MockCapacityOverseer();
    LivingDocumentFactory factory = LivingDocumentTests.compile(PUBSUB_CODE, Deliverer.FAILURE);
    MockInstantLivingDocumentFactoryFactory factoryFactory =
        new MockInstantLivingDocumentFactoryFactory(factory);
    MockTime time = new MockTime();
    MockInstantDataService dataService = new MockInstantDataService();
    CoreService service = new CoreService(METRICS, factoryFactory, (bill) -> {}, new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new MockReplicationInitiator(), time, 3);
    CapacityMetrics capacityMetrics = new CapacityMetrics(new NoOpMetricsFactory());
    ServiceHeatEstimator.HeatVector low = new ServiceHeatEstimator.HeatVector(1, 1, 1, 100);
    ServiceHeatEstimator.HeatVector high = new ServiceHeatEstimator.HeatVector(1000, 10000, 250, 10000);
    ServiceHeatEstimator estimator = new ServiceHeatEstimator(low, high);
    ServiceShield shield = new ServiceShield();
    String region = "my-region";
    String machine = "my-machine";
    MockUndeploy undeploy = new MockUndeploy();
    try {
      CapacityAgent agent = new CapacityAgent(capacityMetrics, overseer, service, undeploy, estimator, SimpleExecutor.NOW, new AtomicBoolean(false), shield, region, machine);
      agent.addCapacity();
      agent.rebalance();
    } finally {
      service.shutdown();
    }
  }
}
