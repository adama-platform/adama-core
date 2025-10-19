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

import ape.caravan.CaravanDataService;
import ape.caravan.CaravanMetrics;
import ape.caravan.contracts.Cloud;
import ape.caravan.data.DiskMetrics;
import ape.caravan.data.DurableListStore;
import ape.common.Callback;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.common.metrics.MetricsFactory;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.contracts.BackupService;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.data.DataObserver;
import ape.runtime.data.DataService;
import ape.runtime.data.Key;
import ape.runtime.deploy.AsyncByteCodeCache;
import ape.runtime.deploy.DeploymentFactory;
import ape.runtime.deploy.DeploymentFactoryBase;
import ape.runtime.remote.MetricsReporter;
import ape.runtime.sys.CoreMetrics;
import ape.runtime.sys.CoreService;
import ape.runtime.sys.PredictiveInventory;
import ape.runtime.sys.cron.WakeService;
import ape.runtime.sys.readonly.ReplicationInitiator;
import ape.translator.env.RuntimeEnvironment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

public class Solo {
    public static void main(String[] args) throws Exception {
        SimpleExecutor wakeExecutor = SimpleExecutor.create("wake-up");
        SimpleExecutor dataServiceExecutor = SimpleExecutor.create("data-service");
        SimpleExecutor diskExecutor = SimpleExecutor.create("disk");
        File corePath = new File(".");

        File walRoot = new File(corePath, "wal");
        File dataRoot = new File(corePath, "data");
        File backups = new File(corePath, "backups");
        File cloudPath = new File(corePath, "cloud");
        File storePath = new File(dataRoot, "store");
        walRoot.mkdir();
        dataRoot.mkdir();
        backups.mkdir();

        RuntimeEnvironment runtimeEnvironment = RuntimeEnvironment.Production;
        try {
            MetricsFactory factory = new NoOpMetricsFactory();
            CoreMetrics coreMetrics = new CoreMetrics(factory);
            CaravanMetrics caravanMetrics = new CaravanMetrics(factory);
            DiskMetrics diskMetrics = new DiskMetrics(factory);
            DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, runtimeEnvironment);
            // TODO: scan the directory of spaces to find spaces
            // TODO: check cache

            Consumer<HashMap<String, PredictiveInventory.MeteringSample>> meteringEvent = (map) -> {};
            MetricsReporter reporter = new MetricsReporter() {
                @Override
                public void emitMetrics(Key key, String metricsPayload) {
                }
            };

            Cloud cloud = new Cloud() {
                @Override
                public File path() {
                    return null;
                }

                @Override
                public void exists(Key key, String archiveKey, Callback<Void> callback) {

                }

                @Override
                public void restore(Key key, String archiveKey, Callback<File> callback) {

                }

                @Override
                public void backup(Key key, File archiveFile, Callback<Void> callback) {

                }

                @Override
                public void delete(Key key, String archiveKey, Callback<Void> callback) {

                }
            };

            DurableListStore store = new DurableListStore(diskMetrics, storePath, walRoot, 4L * 1024 * 1024 * 1024, 16 * 1024 * 1024, 64 * 1024 * 1024);
            DataService dataService = new CaravanDataService(caravanMetrics, cloud, store, diskExecutor);

            BackupService backupService = new BackupService() {
                @Override
                public void backup(Key key, int seq, Reason reason, String document, Callback<String> callback) {
                    // TODO: write the document to disk
                }
            };

            WakeService wakeService = new WakeService() {
                @Override
                public void wakeIn(Key key, long when, Callback<Void> callback) {
                    wakeExecutor.schedule(new NamedRunnable("wake-up") {
                        @Override
                        public void execute() throws Exception {
                            callback.success(null);
                        }
                    }, when);
                }
            };

            ReplicationInitiator replicationInitiator = new ReplicationInitiator() {
                @Override
                public void startDocumentReplication(Key key, DataObserver observer, Callback<Runnable> cancel) {
                    // WTF
                }
            };

            TimeSource timeSource = TimeSource.REAL_TIME;
            int nThreads = 2;
            CoreService service = new CoreService(coreMetrics, base, meteringEvent, reporter, dataService, backupService, wakeService, replicationInitiator, timeSource, nThreads);

        } finally {
            wakeExecutor.shutdown();
            dataServiceExecutor.shutdown();
        }
    }
}
