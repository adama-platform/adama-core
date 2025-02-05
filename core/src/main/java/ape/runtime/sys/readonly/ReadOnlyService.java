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
