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

import ape.common.TimeSource;
import ape.runtime.deploy.DeploymentFactoryBase;
import ape.runtime.sys.PredictiveInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/** ultimately, billing will replicate to every caller and the billing system has to dedupe by id */
public class MeteringPubSub {
  private final TimeSource time;
  private final DeploymentFactoryBase base;
  private final ArrayList<Function<ArrayList<MeterReading>, Boolean>> subscribers;
  private ArrayList<MeterReading> lastValue;
  private long lastPublish;

  public MeteringPubSub(TimeSource time, DeploymentFactoryBase base) {
    this.time = time;
    this.base = base;
    this.subscribers = new ArrayList<>();
    this.lastValue = null;
    this.lastPublish = time.nowMilliseconds();
  }

  public synchronized void subscribe(Function<ArrayList<MeterReading>, Boolean> subscriber) {
    if (lastValue != null) {
      if (subscriber.apply(lastValue)) {
        subscribers.add(subscriber);
      }
    } else {
      subscribers.add(subscriber);
    }
  }

  public synchronized int size() {
    return subscribers.size();
  }

  public Consumer<HashMap<String, PredictiveInventory.MeteringSample>> publisher() {
    return samples -> {
      long now = time.nowMilliseconds();
      long delta = now - lastPublish;
      lastPublish = now;
      ArrayList<MeterReading> meterReading = new ArrayList<>();
      for (Map.Entry<String, PredictiveInventory.MeteringSample> sample : samples.entrySet()) {
        String space = sample.getKey();
        String hash = base.hashOf(space);
        if (hash != null) {
          meterReading.add(new MeterReading(time.nowMilliseconds(), delta, space, hash, sample.getValue()));
        }
      }
      publish(meterReading);
    };
  }

  private synchronized void publish(ArrayList<MeterReading> meterReading) {
    Iterator<Function<ArrayList<MeterReading>, Boolean>> it = subscribers.iterator();
    lastValue = meterReading;
    while (it.hasNext()) {
      if (!it.next().apply(meterReading)) {
        it.remove();
      }
    }
  }
}
