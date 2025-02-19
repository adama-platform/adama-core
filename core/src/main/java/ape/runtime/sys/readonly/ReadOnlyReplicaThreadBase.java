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

import ape.common.*;
import ape.ErrorCodes;
import ape.runtime.contracts.DeploymentMonitor;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.contracts.Perspective;
import ape.runtime.data.DataObserver;
import ape.runtime.data.Key;
import ape.runtime.sys.*;
import ape.runtime.sys.*;
import ape.translator.jvm.LivingDocumentFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

/** we create a readonly version of the entire system for simplicity sake */
public class ReadOnlyReplicaThreadBase {
  public final int threadId;
  public final ServiceShield shield;
  public final CoreMetrics metrics;
  public final SimpleExecutor executor;
  public final HashMap<Key, ReadOnlyLivingDocument> map;
  public final TimeSource time;
  private final HashMap<String, PredictiveInventory> inventoryBySpace;
  private final LivingDocumentFactoryFactory livingDocumentFactoryFactory;
  private final ReplicationInitiator initiator;
  private int millisecondsToPerformInventory;
  private int millisecondsToPerformInventoryJitter;
  private int millisecondsInactivityBeforeCleanup;
  private final Random rng;

  public ReadOnlyReplicaThreadBase(int threadId, ServiceShield shield, CoreMetrics metrics, LivingDocumentFactoryFactory livingDocumentFactoryFactory, ReplicationInitiator initiator, TimeSource time, SimpleExecutor executor) {
    this.threadId = threadId;
    this.shield = shield;
    this.metrics = metrics;
    this.executor = executor;
    this.livingDocumentFactoryFactory = livingDocumentFactoryFactory;
    this.initiator = initiator;
    this.map = new HashMap<>();
    this.inventoryBySpace = new HashMap<>();
    this.millisecondsToPerformInventory = 30000;
    this.millisecondsToPerformInventoryJitter = 15000;
    this.millisecondsInactivityBeforeCleanup = 120000;
    this.time = time;
    this.rng = new Random();
  }

  public void setInventoryMillisecondsSchedule(int period, int jitter) {
    this.millisecondsToPerformInventory = period;
    this.millisecondsToPerformInventoryJitter = jitter;
  }

  public int getMillisecondsInactivityBeforeCleanup() {
    return millisecondsInactivityBeforeCleanup;
  }

  public void setMillisecondsInactivityBeforeCleanup(int ms) {
    this.millisecondsInactivityBeforeCleanup = ms;
  }

  public void shedFromWithinExecutor(Function<Key, Boolean> condition) {
    ArrayList<ReadOnlyLivingDocument> toShed = new ArrayList<>();
    for (Map.Entry<Key, ReadOnlyLivingDocument> entry : map.entrySet()) {
      if (condition.apply(entry.getKey())) {
        toShed.add(entry.getValue());
      }
    }
    for (ReadOnlyLivingDocument doc : toShed) {
      doc.kill();
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

  private void killFromWithinExecutor(Key key) {
    ReadOnlyLivingDocument document = map.remove(key);
    if (document != null) {
      document.kill();
    }
  }

  private void initiateReplication(Key key, ReadOnlyLivingDocument document) {
    initiator.startDocumentReplication(key, new DataObserver() {
      @Override
      public String machine() {
        // the higher level proxy will inject the machine name
        return null;
      }

      @Override
      public void start(String snapshot) {
        executor.execute(new NamedRunnable("replication-start") {
          @Override
          public void execute() throws Exception {
            document.start(snapshot);
          }
        });
      }

      @Override
      public void change(String delta) {
        executor.execute(new NamedRunnable("replication-change") {
          @Override
          public void execute() throws Exception {
            document.change(delta);
          }
        });
      }

      @Override
      public void failure(ErrorCodeException exception) {
        executor.execute(new NamedRunnable("replication-failure") {
          @Override
          public void execute() throws Exception {
            killFromWithinExecutor(key);
          }
        });
      }
    }, new Callback<Runnable>() {
      @Override
      public void success(Runnable cancel) {
        executor.execute(new NamedRunnable("replication-start") {
          @Override
          public void execute() throws Exception {
            document.setCancel(cancel);
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        executor.execute(new NamedRunnable("replication-start") {
          @Override
          public void execute() throws Exception {
            killFromWithinExecutor(key);
          }
        });
      }
    });
  }

    private void bindClone(Key key, LivingDocument clone, LivingDocumentFactory factory) {
      clone.__lateBind(key.space, key.key, (agent, key1, id, result, firstParty, callback) -> executor.execute(new NamedRunnable("bounce-to-readonly") {
        @Override
        public void execute() throws Exception {
          try {
            clone.__forceDeliverResult(id, result);
            clone.__forceBroadcastToKeepReadonlyObserverUpToDate();
            callback.success(0);
          } catch (ErrorCodeException ex) {
            callback.failure(ex);
          }
        }
      }), factory.registry);
  }

  private void constructDocument(Key key, Consumer<ReadOnlyLivingDocument> success, ReadOnlyStream stream) {
    ReadOnlyReplicaThreadBase self = this;
    if (!shield.canConnectNew.get()) {
      stream.failure(new ErrorCodeException(ErrorCodes.SHIELD_REJECT_OBSERVE_NEW_DOCUMENT));
      return;
    }
    livingDocumentFactoryFactory.fetch(key, new Callback<>() {
      @Override
      public void success(LivingDocumentFactory factory) {
        executor.execute(new NamedRunnable("post-create-document") {
          @Override
          public void execute() throws Exception {
            ReadOnlyLivingDocument document = map.get(key);
            if (document == null) {
              LivingDocument clone = factory.create(null);
              bindClone(key, clone, factory);

              // then we wrap it stash it
              document = new ReadOnlyLivingDocument(self, key, clone, factory);
              map.put(key, document);
              initiateReplication(key, document);
            }
            success.accept(document);
          };
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        stream.failure(ex);
      }
    });
  }

  public void deploy(DeploymentMonitor monitor) {
    executor.execute(new NamedRunnable("ro-deploy") {
      @Override
      public void execute() throws Exception {
        for (Map.Entry<Key, ReadOnlyLivingDocument> entry : map.entrySet()) {
          deploy(entry.getKey(), entry.getValue(), monitor);
        }
      }
    });
  }

  private  void deploy(Key key, ReadOnlyLivingDocument document, DeploymentMonitor monitor) {
    livingDocumentFactoryFactory.fetch(key, metrics.factoryFetchDeploy.wrap(new Callback<>() {
      @Override
      public void success(LivingDocumentFactory newFactory) {
        executor.execute(new NamedRunnable("deploy", key.space, key.key) {
          @Override
          public void execute() throws Exception {
            boolean toChange = document.getFactory() != newFactory;
            monitor.bumpDocument(toChange);
            if (toChange) {
              document.kill();
              map.remove(key);
            }
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        monitor.witnessException(ex);
      }
    }));
  }

  public void observe(CoreRequestContext context, Key key, String viewerState, ReadOnlyStream stream) {
    if (!shield.canConnectExisting.get()) {
      stream.failure(new ErrorCodeException(ErrorCodes.SHIELD_REJECT_OBSERVE_DOCUMENT));
      return;
    }
    Consumer<ReadOnlyLivingDocument> onDocument = (document) -> {
      StreamHandle handle = document.join(context.who, viewerState, new Perspective() {
        @Override
        public void data(String data) {
          stream.next(data);
        }

        @Override
        public void disconnect() {
          stream.close();
        }
      });
      stream.setupComplete(new ReadOnlyViewHandle(context.who, document, handle, executor));
      String viewStateFilter = document.getViewStateFilter();
      if (!"[]".equals(viewStateFilter)) {
        stream.next("{\"view-state-filter\":" + viewStateFilter + "}");
      }
    };
    executor.execute(new NamedRunnable("find-document") {
      @Override
      public void execute() throws Exception {
        ReadOnlyLivingDocument document = map.get(key);
        if (document != null) {
          onDocument.accept(document);
        } else {
          constructDocument(key, onDocument, stream);
        }
      }
    });
  }

  public PredictiveInventory getOrCreateInventory(String space) {
    PredictiveInventory inventory = inventoryBySpace.get(space);
    if (inventory == null) {
      inventory = new PredictiveInventory();
      inventoryBySpace.put(space, inventory);
    }
    return inventory;
  }

  public void kickOffInventory() {
    executor.schedule(new NamedRunnable("base-inventory") {
      @Override
      public void execute() throws Exception {
        performInventory();
      }
    }, 2500);
  }

  public void performInventory() {
    HashMap<String, PredictiveInventory.PreciseSnapshotAccumulator> accumulators = new HashMap<>(inventoryBySpace.size());
    Iterator<Map.Entry<Key, ReadOnlyLivingDocument>> it = map.entrySet().iterator();
    ArrayList<ReadOnlyLivingDocument> inactive = new ArrayList<>();
    while (it.hasNext()) {
      ReadOnlyLivingDocument document = it.next().getValue();
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
    for (ReadOnlyLivingDocument close : inactive) {
      close.kill();
      map.remove(close.key);
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

  public CountDownLatch close() {
    return executor.shutdown();
  }
}
