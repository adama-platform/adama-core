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
package ape.web.io;

import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.common.metrics.RequestResponseMonitor;
import org.junit.Assert;
import org.junit.Test;

public class SimpleMetricsProxyResponderTests {
  public static class MockMetricsRR implements RequestResponseMonitor.RequestResponseMonitorInstance {

    public int success_count = 0;
    public int extra_count = 0;
    public int failure_count = 0;
    public int failure_last_code = -1;
    @Override
    public void success() {
      success_count++;
    }

    @Override
    public void extra() {
      extra_count++;
    }

    @Override
    public void failure(int code) {
      failure_count++;
      failure_last_code = code;
    }
  }

  @Test
  public void flow() {
    MockMetricsRR metrics = new MockMetricsRR();
    MockJsonResponder responder = new MockJsonResponder();
    SimpleMetricsProxyResponder proxy = new SimpleMetricsProxyResponder(metrics, responder, Json.newJsonObject(), JsonLogger.NoOp, 0);
    proxy.stream("X");
    proxy.stream("X");
    proxy.stream("X");
    Assert.assertEquals(0, metrics.success_count);
    Assert.assertEquals(3, metrics.extra_count);
    Assert.assertEquals(0, metrics.failure_count);
    Assert.assertEquals(-1, metrics.failure_last_code);
    proxy.finish("Y");
    proxy.finish("Y");
    Assert.assertEquals(2, metrics.success_count);
    Assert.assertEquals(3, metrics.extra_count);
    Assert.assertEquals(0, metrics.failure_count);
    Assert.assertEquals(-1, metrics.failure_last_code);
    proxy.error(new ErrorCodeException(42));
    Assert.assertEquals(2, metrics.success_count);
    Assert.assertEquals(3, metrics.extra_count);
    Assert.assertEquals(1, metrics.failure_count);
    Assert.assertEquals(42, metrics.failure_last_code);
    Assert.assertEquals(6, responder.events.size());
    Assert.assertEquals("STREAM:X", responder.events.get(0));
    Assert.assertEquals("STREAM:X", responder.events.get(1));
    Assert.assertEquals("STREAM:X", responder.events.get(2));
    Assert.assertEquals("FINISH:Y", responder.events.get(3));
    Assert.assertEquals("FINISH:Y", responder.events.get(4));
    Assert.assertEquals("ERROR:42", responder.events.get(5));
  }
}
