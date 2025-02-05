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
package ape.runtime.deploy;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.data.Key;
import ape.common.SimpleExecutor;
import ape.runtime.mocks.MockBackupService;
import ape.runtime.mocks.MockTime;
import ape.runtime.mocks.MockWakeService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.*;
import ape.runtime.sys.*;
import ape.runtime.sys.mocks.MockInstantDataService;
import ape.runtime.sys.mocks.MockMetricsReporter;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DeploymentTests {
  @Test
  public void transition() throws Exception {
    DeploymentPlan plan1 =
        new DeploymentPlan(
            "{\"versions\":{\"a\":\"public int x; @construct { x = 100; } @connected { return true; }\"},\"default\":\"a\"}",
            (t, errorCode) -> {
              t.printStackTrace();
            });
    DeploymentPlan plan2 =
        new DeploymentPlan(
            "{\"versions\":{\"a\":\"public int x; @construct { x = 100; } @connected { return true; }\",\"b\":\"public int x; @construct { x = 200; } @connected { return true; }\"},\"plan\":[{\"version\":\"b\",\"seed\":\"x\",\"percent\":50}],\"default\":\"a\"}",
            (t, errorCode) -> {
              t.printStackTrace();
            });
    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    {
      CountDownLatch latch = new CountDownLatch(1);
      base.deploy("MySpace", plan1, new TreeMap<>(), new Callback<Void>() {
        @Override
        public void success(Void value) {
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {
          Assert.fail();
        }
      });
      Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }
    AtomicInteger count1 = new AtomicInteger(0);
    AtomicInteger count2 = new AtomicInteger(0);

    Callback<LivingDocumentFactory> shred =
        new Callback<LivingDocumentFactory>() {
          @Override
          public void success(LivingDocumentFactory factory) {
            try {
              LivingDocument doc = factory.create(null);

              MockInstantDataService dataService = new MockInstantDataService();
              DocumentThreadBase docBase =
                  new DocumentThreadBase(0, new ServiceShield(), new MockMetricsReporter(), dataService, new MockBackupService(), new MockWakeService(), new CoreMetrics(new NoOpMetricsFactory()), SimpleExecutor.NOW, new MockTime());
              DurableLivingDocument.fresh(
                  new Key("space", "key"),
                  factory,
                  new CoreRequestContext(NtPrincipal.NO_ONE, "origin", "ip", "key"),
                  "{}",
                  null,
                  null,
                  docBase,
                  new Callback<DurableLivingDocument>() {
                    @Override
                    public void success(DurableLivingDocument value) {
                      if (dataService.getLogAt(0).contains("\"x\":200")) {
                        count2.getAndIncrement();
                      } else if (dataService.getLogAt(0).contains("\"x\":100")) {
                        count1.getAndIncrement();
                      } else {
                        Assert.fail();
                      }
                    }

                    @Override
                    public void failure(ErrorCodeException ex) {
                      Assert.fail();
                    }
                  });
            } catch (Exception ex) {
              Assert.fail();
            }
          }

          @Override
          public void failure(ErrorCodeException ex) {
            Assert.fail();
          }
        };
    for (int k = 0; k < 10; k++) {
      base.fetch(new Key("MySpace", "key" + k), shred);
    }
    Assert.assertEquals(10, count1.get());
    Assert.assertEquals(0, count2.get());
    {
      CountDownLatch latch = new CountDownLatch(1);
      base.deploy("MySpace", plan2, new TreeMap<>(), new Callback<Void>() {
        @Override
        public void success(Void value) {
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {
          Assert.fail();
        }
      });
      Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }
    for (int k = 0; k < 100; k++) {
      base.fetch(new Key("MySpace", "keyX" + k), shred);
    }
    Assert.assertEquals(61, count1.get());
    Assert.assertEquals(49, count2.get());
  }
}
