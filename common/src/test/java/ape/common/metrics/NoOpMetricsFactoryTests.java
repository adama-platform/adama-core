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
package ape.common.metrics;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class NoOpMetricsFactoryTests {
  @Test
  public void coverage() {
    NoOpMetricsFactory factory = new NoOpMetricsFactory();
    factory.page("page", "title");
    factory.section("section");
    RequestResponseMonitor.RequestResponseMonitorInstance rr = factory.makeRequestResponseMonitor("x").start();
    rr.success();
    rr.failure(1);
    rr.extra();
    StreamMonitor.StreamMonitorInstance s = factory.makeStreamMonitor("y").start();
    s.progress();
    s.failure(-1);
    s.finish();
    CallbackMonitor cb = factory.makeCallbackMonitor("z");
    AtomicInteger sum = new AtomicInteger(0);
    Callback<String> instance = cb.wrap(new Callback<String>() {
      @Override
      public void success(String value) {
        sum.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        sum.incrementAndGet();
        sum.incrementAndGet();
      }
    });
    instance.success("x");
    Assert.assertEquals(1, sum.get());
    instance.failure(new ErrorCodeException(-1));
    Assert.assertEquals(3, sum.get());
    factory.counter("z").run();
    factory.inflight("z").down();
    factory.inflight("z").up();
    factory.inflight("z").set(1);
    factory.makeItemActionMonitor("item").start().executed();
    factory.makeItemActionMonitor("item").start().rejected();
    factory.makeItemActionMonitor("item").start().timeout();
  }
}
