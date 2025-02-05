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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.runtime.mocks.MockLivingDocument;
import ape.runtime.remote.Deliverer;
import ape.runtime.remote.ServiceRegistry;

import org.junit.Assert;
import org.junit.Test;

public class PerfTrackerTests {
  @Test
  public void flow() throws Exception {
    MockLivingDocument doc = new MockLivingDocument();
    doc.__lateBind("space", "key", Deliverer.FAILURE, new ServiceRegistry("space"));
    PerfTracker tracker = new PerfTracker(doc);
    tracker.measure("xyz").run();
    Runnable x = tracker.measure("xyz");
    Runnable y = tracker.measure("cost");
    doc.__code_cost += 100;
    Thread.sleep(5);
    y.run();
    x.run();
    tracker.measure("xyz").run();
    String result = tracker.dump(0.0);
    System.out.println(result);
    Assert.assertTrue(result.contains("\"type\":\"document\""));
    Assert.assertTrue(result.contains("\"avg_cost\":100.0"));
    Assert.assertNull(tracker.dump(0.0));
  }

  @Test
  public void lightning() {
    MockLivingDocument doc = new MockLivingDocument();
    doc.__lateBind("space", "key", Deliverer.FAILURE, new ServiceRegistry("space"));
    PerfTracker tracker = new PerfTracker(doc);
    tracker.measureLightning();
    Runnable x = tracker.measure("foo");
    Runnable y = tracker.measure("dip");
    y.run();
    x.run();
    ObjectNode report = Json.parseJsonObject(tracker.getLightningJsonAndReset());
    String normalized = report.toString().replaceAll("[0-9]+", "x");
    Assert.assertEquals("{\"foo\":{\"dip\":{\"__ms\":x},\"__ms\":x}}", normalized);
  }
}
