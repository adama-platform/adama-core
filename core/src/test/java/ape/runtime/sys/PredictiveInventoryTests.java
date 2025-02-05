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

import org.junit.Assert;
import org.junit.Test;

public class PredictiveInventoryTests {
  @Test
  public void flow() {
    PredictiveInventory inventory = new PredictiveInventory();
    inventory.grow();
    PredictiveInventory.PreciseSnapshotAccumulator snapshot = new PredictiveInventory.PreciseSnapshotAccumulator();
    snapshot.count = 1;
    snapshot.memory = 1000;
    snapshot.ticks = 10000;
    inventory.accurate(snapshot);
    inventory.grow();
    inventory.message();
    inventory.message();
    inventory.message();
    PredictiveInventory.MeteringSample meteringSample = inventory.sample();
    Assert.assertEquals(2, meteringSample.count);
    Assert.assertEquals(2000, meteringSample.memory);
    Assert.assertEquals(20000, meteringSample.cpu);
    Assert.assertEquals(3, meteringSample.messages);
    snapshot.count = 2;
    snapshot.memory = 2000;
    snapshot.ticks = 20000;
    inventory.accurate(snapshot);
    inventory.grow();
    inventory.grow();
    inventory.message();
    inventory.message();
    meteringSample = inventory.sample();
    Assert.assertEquals(4, meteringSample.count);
    Assert.assertEquals(4000, meteringSample.memory);
    Assert.assertEquals(40000, meteringSample.cpu);
    Assert.assertEquals(2, meteringSample.messages);
    snapshot.count = 10;
    snapshot.memory = 5000;
    snapshot.ticks = 60000;
    inventory.accurate(snapshot);
    inventory.grow();
    inventory.grow();
    inventory.message();
    inventory.message();
    inventory.message();
    inventory.message();
    inventory.message();
    meteringSample = inventory.sample();
    Assert.assertEquals(12, meteringSample.count);
    Assert.assertEquals(6230, meteringSample.memory);
    Assert.assertEquals(73846, meteringSample.cpu);
    Assert.assertEquals(5, meteringSample.messages);
  }

  @Test
  public void add() {
    PredictiveInventory.MeteringSample a = new PredictiveInventory.MeteringSample(100, 1000, 5, 100, 41, 123, 456, 789, 13, 1000);
    PredictiveInventory.MeteringSample b = new PredictiveInventory.MeteringSample(1100, 21000, 51, 1100, 91, 123, 456, 789, 42, 100000);
    PredictiveInventory.MeteringSample s = PredictiveInventory.MeteringSample.add(a, b);
    Assert.assertEquals(1200, s.memory);
    Assert.assertEquals(22000, s.cpu);
    Assert.assertEquals(56, s.count);
    Assert.assertEquals(1200, s.messages);
    Assert.assertEquals(132, s.connections);
    Assert.assertEquals(55, s.cpu_milliseconds);
    Assert.assertEquals(101000, s.backup_byte_hours);
  }
}
