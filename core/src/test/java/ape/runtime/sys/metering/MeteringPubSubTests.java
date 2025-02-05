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

import ape.common.Callback;
import ape.runtime.deploy.AsyncByteCodeCache;
import ape.runtime.deploy.DeploymentFactoryBase;
import ape.runtime.deploy.DeploymentPlan;
import ape.runtime.mocks.MockTime;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.env.RuntimeEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MeteringPubSubTests {
  @Test
  public void flow() throws Exception {
    DeploymentPlan plan =
        new DeploymentPlan(
            "{\"versions\":{\"a\":\"public int x; @construct { x = 100; } @connected { return true; }\"},\"default\":\"a\"}",
            (t, errorCode) -> {
              t.printStackTrace();
            });
    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    CountDownLatch latchDeploy = new CountDownLatch(1);
    base.deploy("space", plan, new TreeMap<>(), Callback.FINISHED_LATCH_DONT_CARE_VOID(latchDeploy));
    Assert.assertTrue(latchDeploy.await(5000, TimeUnit.MILLISECONDS));
    MeteringPubSub pubsub = new MeteringPubSub(new MockTime(), base);
    {
      AtomicInteger pubs = new AtomicInteger(0);
      CountDownLatch latch = new CountDownLatch(10);
      Assert.assertEquals(0, pubsub.size());
      pubsub.subscribe((x) -> {
        latch.countDown();
        return pubs.getAndIncrement() < 5;
      });
      Assert.assertEquals(1, pubsub.size());
      Consumer<HashMap<String, PredictiveInventory.MeteringSample>> publisher = pubsub.publisher();
      HashMap<String, PredictiveInventory.MeteringSample> map = new HashMap<>();
      map.put("space", new PredictiveInventory.MeteringSample(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
      for (int k = 0; k < 11; k++) {
        publisher.accept(map);
      }
      Assert.assertEquals(0, pubsub.size());
      latch.await(1000, TimeUnit.MILLISECONDS);
      Assert.assertEquals(6, pubs.get());
    }
    {
      AtomicInteger pubs = new AtomicInteger(0);
      Assert.assertEquals(0, pubsub.size());
      pubsub.subscribe((x) -> {
        pubs.getAndIncrement();
        return false;
      });
      Assert.assertEquals(0, pubsub.size());
      Assert.assertEquals(1, pubs.get());
      pubsub.subscribe((x) -> {
        pubs.getAndIncrement();
        return true;
      });
      Assert.assertEquals(1, pubsub.size());
      Assert.assertEquals(2, pubs.get());
    }
  }
}
