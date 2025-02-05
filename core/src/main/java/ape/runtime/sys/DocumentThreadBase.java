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
package ape.runtime.sys;

import ape.common.Callback;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.runtime.contracts.BackupService;
import ape.runtime.data.DataService;
import ape.runtime.data.Key;
import ape.runtime.remote.MetricsReporter;
import ape.runtime.sys.cron.WakeService;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This defines the state required within a thread to run a document. As Documents run in isolated
 * thread without synchronization, access to a durable living document must be access via this base.
 */
public class DocumentThreadBase {
  public final int threadId;
  public final ServiceShield shield;
  public final DataService service;
  public final BackupService backup;
  public final WakeService wake;
  public final CoreMetrics metrics;
  public final SimpleExecutor executor;
  public final HashMap<Key, DurableLivingDocument> map;
  public final HashMap<Key, ArrayList<Runnable>> pending;
  public final TimeSource time;
  public final MetricsReporter metricsReporter;
  private final HashMap<String, PredictiveInventory> inventoryBySpace;
  private final Random rng;
  private int millisecondsForCleanupCheck;
  private int millisecondsAfterLoadForReconciliation;
  private int millisecondsToPerformInventory;
  private int millisecondsToPerformInventoryJitter;
  private int millisecondsInactivityBeforeCleanup;
  private boolean drained;

  public DocumentThreadBase(int threadId, ServiceShield shield, MetricsReporter metricsReporter, DataService service, BackupService backup, WakeService wake, CoreMetrics metrics, SimpleExecutor executor, TimeSource time) {
    this.threadId = threadId;
    this.shield = shield;
    this.metricsReporter = metricsReporter;
    this.service = service;
    this.backup = backup;
    this.wake = wake;
    this.metrics = metrics;
    this.executor = executor;
    this.time = time;
    this.map = new HashMap<>();
    this.pending = new HashMap<>();
    this.inventoryBySpace = new HashMap<>();
    this.millisecondsForCleanupCheck = 2500;
    this.millisecondsAfterLoadForReconciliation = 2500;
    this.rng = new Random();
    this.millisecondsToPerformInventory = 30000;
    this.millisecondsToPerformInventoryJitter = 15000;
    this.millisecondsInactivityBeforeCleanup = 120000;
    this.drained = false;
  }

  public void drain() {
    this.drained = true;
  }

  public boolean isDrained() {
    return drained;
  }

  public int getMillisecondsInactivityBeforeCleanup() {
    return millisecondsInactivityBeforeCleanup;
  }

  public void setMillisecondsInactivityBeforeCleanup(int ms) {
    this.millisecondsInactivityBeforeCleanup = ms;
  }

  public void kickOffInventory() {
    executor.schedule(new NamedRunnable("base-inventory") {
      @Override
      public void execute() throws Exception {
        performInventory();
      }
    }, 2500);
  }

  public PredictiveInventory getOrCreateInventory(String space) {
    PredictiveInventory inventory = inventoryBySpace.get(space);
    if (inventory == null) {
      inventory = new PredictiveInventory();
      inventoryBySpace.put(space, inventory);
    }
    return inventory;
  }

  public void sampleMetering(Consumer<HashMap<String, PredictiveInventory.MeteringSample>> callback) {
    executor.execute(new NamedRunnable("base-meter-sampling") {
      @Override
      public void execute() throws Exception {
        HashMap<String, PredictiveInventory.MeteringSample> result = new HashMap<>();
        for (Map.Entry<String, PredictiveInventory> entry : inventoryBySpace.entrySet()) {
          result.put(entry.getKey(), entry.getValue().sample());
        }
        callback.accept(result);
      }
    });
  }

  public void shedFromWithinExecutor(Function<Key, Boolean> condition) {
    ArrayList<DurableLivingDocument> toShed = new ArrayList<>();
    for (Map.Entry<Key, DurableLivingDocument> entry : map.entrySet()) {
      if (condition.apply(entry.getKey())) {
        toShed.add(entry.getValue());
      }
    }
    for (DurableLivingDocument doc : toShed) {
      doc.shedWhileInExecutor();
    }
  }

  public void shed(Function<Key, Boolean> condition) {
    executor.execute(new NamedRunnable("shed") {
      @Override
      public void execute() throws Exception {
        shedFromWithinExecutor(condition);
      }
    });
  }

  public void performInventory() {
    HashMap<String, PredictiveInventory.PreciseSnapshotAccumulator> accumulators = new HashMap<>(inventoryBySpace.size());
    Iterator<Map.Entry<Key, DurableLivingDocument>> it = map.entrySet().iterator();
    ArrayList<DurableLivingDocument> inactive = new ArrayList<>();
    while (it.hasNext()) {
      DurableLivingDocument document = it.next().getValue();
      document.triggerExpire();
      PredictiveInventory.PreciseSnapshotAccumulator accum = accumulators.get(document.key.space);
      if (accum == null) {
        accum = new PredictiveInventory.PreciseSnapshotAccumulator();
        accumulators.put(document.key.space, accum);
      }
      accum.memory += document.getMemoryBytes();
      accum.ticks += document.getCodeCost();
      accum.cpu_ms += document.getCpuMilliseconds();
      document.zeroOutCodeCost();
      accum.connections += document.getConnectionsCount();
      accum.count++;
      if (document.testInactive()) {
        inactive.add(document);
      }
    }
    for (DurableLivingDocument close : inactive) {
      close.cleanupWhileInExecutor(false);
    }
    HashMap<String, PredictiveInventory> nextInventoryBySpace = new HashMap<>();
    for (Map.Entry<String, PredictiveInventory.PreciseSnapshotAccumulator> entry : accumulators.entrySet()) {
      PredictiveInventory inventory = getOrCreateInventory(entry.getKey());
      inventory.accurate(entry.getValue());
      nextInventoryBySpace.put(entry.getKey(), inventory);
    }
    inventoryBySpace.clear();
    inventoryBySpace.putAll(nextInventoryBySpace);
    executor.schedule(new NamedRunnable("base-inventory-scheduled") {
      @Override
      public void execute() throws Exception {
        performInventory();
      }
    }, millisecondsToPerformInventory + rng.nextInt(millisecondsToPerformInventoryJitter) + rng.nextInt(millisecondsToPerformInventoryJitter));
  }

  public int getMillisecondsForCleanupCheck() {
    return millisecondsForCleanupCheck;
  }

  public void setMillisecondsForCleanupCheck(int ms) {
    this.millisecondsForCleanupCheck = ms;
  }

  public int getMillisecondsAfterLoadForReconciliation() {
    return millisecondsAfterLoadForReconciliation;
  }

  public void setMillisecondsAfterLoadForReconciliation(int ms) {
    this.millisecondsAfterLoadForReconciliation = ms;
  }

  public void setInventoryMillisecondsSchedule(int period, int jitter) {
    this.millisecondsToPerformInventory = period;
    this.millisecondsToPerformInventoryJitter = jitter;
  }

  public void invalidateAll() {
    executor.execute(new NamedRunnable("invalidate-all") {
      @Override
      public void execute() throws Exception {
        for (DurableLivingDocument doc : new ArrayList<>(map.values())) {
          doc.invalidate(Callback.DONT_CARE_INTEGER);
        }
      }
    });
  }
}
