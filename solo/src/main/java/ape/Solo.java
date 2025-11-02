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
package ape;

import ape.common.*;
import ape.common.metrics.MetricsFactory;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.contracts.BackupService;
import ape.runtime.data.DataObserver;
import ape.runtime.data.InMemoryDataService;
import ape.runtime.data.Key;
import ape.runtime.deploy.AsyncByteCodeCache;
import ape.runtime.deploy.DeploymentFactoryBase;
import ape.runtime.remote.MetricsReporter;
import ape.runtime.sys.CoreMetrics;
import ape.runtime.sys.CoreService;
import ape.runtime.sys.PredictiveInventory;
import ape.runtime.sys.cron.WakeService;
import ape.runtime.sys.domains.Domain;
import ape.runtime.sys.domains.DomainFinder;
import ape.runtime.sys.readonly.ReplicationInitiator;
import ape.translator.env.RuntimeEnvironment;
import ape.web.service.ServiceRunnable;
import ape.web.service.WebConfig;
import ape.web.service.WebMetrics;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** a single instance of Adama using just WebSocket + local filesystem + simple static HTTP GET server */
public class Solo {
  public static void main(String[] args) throws Exception {
    String scanDir = ".";
    for (int k = 0; k + 1 < args.length; k++) {
      if (args[k].startsWith("--scan")) {
        k++;
        scanDir = args[k];
      }
    }
    System.out.println("scanning:" + scanDir);
    SimpleExecutor commonExecutor = SimpleExecutor.create("common");
    ExecutorService inmemoryExecutor = Executors.newSingleThreadExecutor();
    // TODO: create a web config
    WebConfig webConfig = new WebConfig(new ConfigObject(Json.newJsonObject()));
    RuntimeEnvironment runtimeEnvironment = RuntimeEnvironment.Production;
    try {
      MetricsFactory factory = new NoOpMetricsFactory();
      CoreMetrics coreMetrics = new CoreMetrics(factory);
      WebMetrics webMetrics = new WebMetrics(factory);
      DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, runtimeEnvironment);
      SoloBundler.scan(new File(scanDir), base, (err) -> {
      });
      Consumer<HashMap<String, PredictiveInventory.MeteringSample>> meteringEvent = (map) -> {
      };
      MetricsReporter reporter = new MetricsReporter() {
        @Override
        public void emitMetrics(Key key, String metricsPayload) {
        }
      };
      InMemoryDataService dataService = new InMemoryDataService(inmemoryExecutor, TimeSource.REAL_TIME);
      BackupService backupService = new BackupService() {
        @Override
        public void backup(Key key, int seq, Reason reason, String document, Callback<String> callback) {
          callback.success("fake-backup-id");
        }
      };
      WakeService wakeService = (key, when, callback) -> commonExecutor.schedule(new NamedRunnable("wake-up") {
        @Override
        public void execute() throws Exception {
          callback.success(null);
        }
      }, when);
      ReplicationInitiator replicationInitiator = new ReplicationInitiator() {
        @Override
        public void startDocumentReplication(Key key, DataObserver observer, Callback<Runnable> cancel) {
          // NO-OP
          cancel.success(() -> {
          });
        }
      };
      TimeSource timeSource = TimeSource.REAL_TIME;
      int nThreads = 1;
      CoreService service = new CoreService(coreMetrics, base, meteringEvent, reporter, dataService, backupService, wakeService, replicationInitiator, timeSource, nThreads);
      base.attachDeliverer(service);
      SoloServiceBase soloBase = new SoloServiceBase(service);
      ServiceRunnable webServer = new ServiceRunnable(webConfig, webMetrics, soloBase, (domain, callback) -> callback.success(null), new DomainFinder() {
        @Override
        public void find(String domain, Callback<Domain> callback) {
          callback.failure(new ErrorCodeException(-404));
        }
      }, () -> {
      });
      Thread serviceThread = new Thread(webServer);
      serviceThread.start();
      webServer.waitForReady(1000);
      serviceThread.join();
    } finally {
      commonExecutor.shutdown();
      inmemoryExecutor.shutdown();
    }
  }
}
