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

import ape.runtime.sys.metering.MeterReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** estimate the heat of a particular space from billing records */
public class ServiceHeatEstimator implements Function<ArrayList<MeterReading>, Boolean> {
  private final ConcurrentHashMap<String, Heat> heat;
  private final HeatVector low;
  private final HeatVector hot;

  public ServiceHeatEstimator(HeatVector low, HeatVector hot) {
    this.heat = new ConcurrentHashMap<>();
    this.low = low;
    this.hot = hot;
  }

  public Heat of(String space) {
    Heat val = heat.get(space);
    if (val == null) {
      return new Heat(true, true, false);
    }
    return val;
  }

  @Override
  public Boolean apply(ArrayList<MeterReading> meterReadings) {
    HashMap<String, HeatVector> sums = new HashMap<>();
    for (MeterReading reading : meterReadings) {
      HeatVector current = new HeatVector(reading.cpu, reading.messages, reading.memory, reading.connections);
      HeatVector prior = sums.get(reading.space);
      if (prior == null) {
        sums.put(reading.space, current);
      } else {
        sums.put(reading.space, HeatVector.add(prior, current));
      }
    }
    for (Map.Entry<String, HeatVector> entry : sums.entrySet()) {
      boolean empty = entry.getValue().connections == 0;
      heat.put(entry.getKey(), new Heat(empty, !low.test(entry.getValue()), hot.test(entry.getValue())));
    }
    Iterator<Map.Entry<String, Heat>> it = heat.entrySet().iterator();
    while (it.hasNext()) {
      if (!sums.containsKey(it.next().getKey())) {
        it.remove();
      }
    }
    return true;
  }

  public static class Heat {
    public final boolean empty;
    public final boolean low;
    public final boolean hot;

    public Heat(boolean empty, boolean low, boolean hot) {
      this.empty = empty;
      this.low = low;
      this.hot = hot;
    }
  }

  public static class HeatVector {
    public final long cpu;
    public final long messages; // proxy for network
    public final long mem;
    public final long connections;

    public HeatVector(long cpu, long messages, long mem, long connections) {
      this.cpu = cpu;
      this.messages = messages;
      this.mem = mem;
      this.connections = connections;
    }

    public static HeatVector add(HeatVector a, HeatVector b) {
      return new HeatVector(a.cpu + b.cpu, a.messages + b.messages, a.mem + b.mem, a.connections + b.connections);
    }

    public boolean test(HeatVector heat) {
      if (heat.cpu >= cpu) {
        return true;
      }
      if (heat.messages >= messages) {
        return true;
      }
      if (heat.mem >= mem) {
        return true;
      }
      return heat.connections >= connections;
    }
  }
}
