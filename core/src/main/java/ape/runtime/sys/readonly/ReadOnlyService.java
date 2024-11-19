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
package ape.runtime.sys.readonly;

import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.runtime.contracts.DeploymentMonitor;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.data.Key;
import ape.runtime.sys.CoreMetrics;
import ape.runtime.sys.CoreRequestContext;
import ape.runtime.sys.ServiceShield;
import ape.runtime.sys.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/** this is much like CoreService EXCEPT it negotiates with the source of truth */
public class ReadOnlyService {
  private final CoreMetrics metrics;
  private final ServiceShield shield;
  public final ReadOnlyReplicaThreadBase[] bases;

  public ReadOnlyService(CoreMetrics metrics, ServiceShield shield, LivingDocumentFactoryFactory livingDocumentFactoryFactory, ReplicationInitiator initiator, TimeSource time, int nThreads) {
    this.metrics = metrics;
    this.shield = shield;
    this.bases = new ReadOnlyReplicaThreadBase[nThreads];
    for (int k = 0; k < bases.length; k++) {
      this.bases[k] = new ReadOnlyReplicaThreadBase(k, shield, metrics, livingDocumentFactoryFactory, initiator, time, SimpleExecutor.create("ro-core-" + k));
      this.bases[k].kickOffInventory();
    }
  }

  public void obverse(CoreRequestContext context, Key key, String viewerState, ReadOnlyStream stream) {
    int threadId = key.hashCode() % bases.length;
    bases[threadId].observe(context, key, viewerState, stream);
  }

  public void shed(Function<Key, Boolean> condition) {
    for (int k = 0; k < bases.length; k++) {
      bases[k].shed(condition);
    }
  }

  public void deploy(DeploymentMonitor monitor) {
    for (int kThread = 0; kThread < bases.length; kThread++) {
      bases[kThread].deploy(monitor);
    }
  }

  public void shutdown() throws InterruptedException {
    CountDownLatch[] latches = new CountDownLatch[bases.length];
    for (int kThread = 0; kThread < bases.length; kThread++) {
      latches[kThread] = bases[kThread].close();
    }
    for (int kThread = 0; kThread < bases.length; kThread++) {
      latches[kThread].await(1000, TimeUnit.MILLISECONDS);
    }
  }
}
