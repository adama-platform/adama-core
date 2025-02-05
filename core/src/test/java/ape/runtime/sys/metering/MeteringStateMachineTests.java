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
package ape.runtime.sys.metering;

import ape.common.*;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.ContextSupport;
import ape.runtime.LivingDocumentTests;
import ape.runtime.data.Key;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.data.InMemoryDataService;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import ape.runtime.sys.*;
import ape.runtime.sys.*;
import ape.runtime.sys.mocks.MockMetricsReporter;
import ape.runtime.sys.readonly.ReadOnlyReplicaThreadBase;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class MeteringStateMachineTests {
  @Test
  public void flow() throws Exception {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    DocumentThreadBase[] bases = new DocumentThreadBase[5];
    for (int k = 0; k < bases.length; k++) {
      bases[k] =
          new DocumentThreadBase(0, new ServiceShield(), new MockMetricsReporter(),
              new InMemoryDataService((x) -> x.run(), TimeSource.REAL_TIME), new MockBackupService(), new MockWakeService(), new CoreMetrics(new NoOpMetricsFactory()),
              new SimpleExecutor() {
                @Override
                public void execute(NamedRunnable command) {
                  service.execute(command);
                }

                @Override
                public Runnable schedule(NamedRunnable command, long milliseconds) {
                  service.schedule(command, milliseconds, TimeUnit.MILLISECONDS);
                  return () -> {};
                }

                @Override
                public Runnable scheduleNano(NamedRunnable command, long nanoseconds) {
                  service.schedule(command, nanoseconds, TimeUnit.NANOSECONDS);
                  return () -> {};
                }

                @Override
                public CountDownLatch shutdown() {
                  return new CountDownLatch(0);
                }
              },
              TimeSource.REAL_TIME);
      }
    AtomicReference<HashMap<String, PredictiveInventory.MeteringSample>> billing = new AtomicReference<>(null);
    CountDownLatch latch = new CountDownLatch(1);
    MeteringStateMachine.estimate(bases, new ReadOnlyReplicaThreadBase[0], new LivingDocumentFactoryFactory() {
      @Override
      public void fetch(Key key, Callback<LivingDocumentFactory> callback) {
      }

      @Override
      public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {
      }

      @Override
      public Collection<String> spacesAvailable() {
        HashSet<String> set = new HashSet<>();
        set.add("x");
        set.add("y");
        return set;
      }
    }, (bill) -> {
      billing.set(bill);
      latch.countDown();
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    Assert.assertEquals(2, billing.get().size());
  }

  @Test
  public void more_real() throws Exception {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    DocumentThreadBase[] bases = new DocumentThreadBase[5];
    for (int k = 0; k < bases.length; k++) {
      bases[k] =
          new DocumentThreadBase(0, new ServiceShield(), new MockMetricsReporter(),
              new InMemoryDataService((x) -> x.run(), TimeSource.REAL_TIME), new MockBackupService(), new MockWakeService(), new CoreMetrics(new NoOpMetricsFactory()),
              new SimpleExecutor() {
                @Override
                public void execute(NamedRunnable command) {
                  service.execute(command);
                }

                @Override
                public Runnable schedule(NamedRunnable command, long milliseconds) {
                  service.schedule(command, milliseconds, TimeUnit.MILLISECONDS);
                  return () -> {};
                }

                @Override
                public Runnable scheduleNano(NamedRunnable command, long nanoseconds) {
                  service.schedule(command, nanoseconds, TimeUnit.NANOSECONDS);
                  return () -> {};
                }

                @Override
                public CountDownLatch shutdown() {
                  return new CountDownLatch(0);
                }
              },
              TimeSource.REAL_TIME);
      bases[k].setInventoryMillisecondsSchedule(10, 5);
      bases[k].kickOffInventory();
    }
    LivingDocumentFactory factory = LivingDocumentTests.compile("public int x = 123; @construct { transition #foo in 2; } #foo { transition #foo in 2; }", Deliverer.FAILURE);
    {
      CountDownLatch latch = new CountDownLatch(1);
      DurableLivingDocument.fresh(new Key("space", "key"), factory, new CoreRequestContext(NtPrincipal.NO_ONE, "origin", "ip", "key"), "{}", null, null, bases[0], new Callback<DurableLivingDocument>() {
        @Override
        public void success(DurableLivingDocument value) {
          bases[0].executor.execute(new NamedRunnable("test") {
            @Override
            public void execute() throws Exception {
              bases[0].map.put(new Key("space", "key"), value);
              value.connect(ContextSupport.WRAP(NtPrincipal.NO_ONE), Callback.DONT_CARE_INTEGER);
              latch.countDown();
            }
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          ex.printStackTrace();
        }
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    }
    int attempts = 0;
    while (attempts < 50) {
      attempts++;
      AtomicReference<HashMap<String, PredictiveInventory.MeteringSample>> billing = new AtomicReference<>(null);
      CountDownLatch latch = new CountDownLatch(1);
      MeteringStateMachine.estimate(
          bases,
          new ReadOnlyReplicaThreadBase[0],
          new LivingDocumentFactoryFactory() {
            @Override
            public void fetch(Key key, Callback<LivingDocumentFactory> callback) {
              try {
                callback.success(factory);
              } catch (Exception ex) {
                callback.failure(ErrorCodeException.detectOrWrap(100, ex, (t, c) -> {}));
              }
            }

            @Override
            public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {

            }

            @Override
            public Collection<String> spacesAvailable() {
              return Collections.emptyList();
            }
          },
          (bill) -> {
            billing.set(bill);
            latch.countDown();
          });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
      if (billing.get().size() == 1) {
        Assert.assertEquals(1, billing.get().get("space").count);
        return;
      }
      Thread.sleep(500);
    }
    Assert.fail("never billed for the object");
  }

  @Test
  public void preload() throws Exception {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    DocumentThreadBase[] bases = new DocumentThreadBase[5];
    for (int k = 0; k < bases.length; k++) {
      bases[k] =
          new DocumentThreadBase(0, new ServiceShield(), new MockMetricsReporter(),
              new InMemoryDataService((x) -> x.run(), TimeSource.REAL_TIME), new MockBackupService(), new MockWakeService(), new CoreMetrics(new NoOpMetricsFactory()),
              new SimpleExecutor() {
                @Override
                public void execute(NamedRunnable command) {
                  service.execute(command);
                }

                @Override
                public Runnable schedule(NamedRunnable command, long milliseconds) {
                  service.schedule(command, milliseconds, TimeUnit.MILLISECONDS);
                  return () -> {};
                }

                @Override
                public Runnable scheduleNano(NamedRunnable command, long nanoseconds) {
                  service.schedule(command, nanoseconds, TimeUnit.NANOSECONDS);
                  return () -> {};
                }

                @Override
                public CountDownLatch shutdown() {
                  return new CountDownLatch(0);
                }
              },
              TimeSource.REAL_TIME);
      bases[k].kickOffInventory();
    }
    LivingDocumentFactory factory = LivingDocumentTests.compile("public int x = 123;", Deliverer.FAILURE);
    AtomicReference<HashMap<String, PredictiveInventory.MeteringSample>> billing = new AtomicReference<>(null);
    {
      CountDownLatch latch = new CountDownLatch(1);
      DurableLivingDocument.fresh(new Key("space", "key"), factory, new CoreRequestContext(NtPrincipal.NO_ONE, "origin", "ip", "key"), "{}", null, null, bases[0], new Callback<DurableLivingDocument>() {
        @Override
        public void success(DurableLivingDocument value) {
          bases[0].executor.execute(new NamedRunnable("test") {
            @Override
            public void execute() throws Exception {
              bases[0].map.put(new Key("space", "key"), value);
              bases[0].performInventory();
              latch.countDown();
            }
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          ex.printStackTrace();
        }
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    }
    CountDownLatch latch = new CountDownLatch(1);
    MeteringStateMachine.estimate(bases, new ReadOnlyReplicaThreadBase[0], new LivingDocumentFactoryFactory() {
      @Override
      public void fetch(Key key, Callback<LivingDocumentFactory> callback) {
        try {
          callback.success(factory);
        } catch (Exception ex) {
          callback.failure(ErrorCodeException.detectOrWrap(100, ex, (t, c) -> {}));
        }
      }

      @Override
      public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {

      }

      @Override
      public Collection<String> spacesAvailable() {
        return Collections.singleton("space");
      }
    }, (bill) -> {
      billing.set(bill);
      latch.countDown();
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    Assert.assertEquals(1, billing.get().size());
    Assert.assertEquals(1, billing.get().get("space").count);
  }
}
