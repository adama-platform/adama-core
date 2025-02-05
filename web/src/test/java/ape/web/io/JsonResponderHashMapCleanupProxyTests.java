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
import ape.common.SimpleExecutor;
import ape.common.metrics.StreamMonitor;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class JsonResponderHashMapCleanupProxyTests {

  public static class MockStreamMetrics implements StreamMonitor.StreamMonitorInstance {
    public int progress_count = 0;
    public int finish_count = 0;
    public int failure_count = 0;
    public int failure_last_code = -1;
    @Override
    public void progress() {
      progress_count++;
    }

    @Override
    public void finish() {
      finish_count++;
    }

    @Override
    public void failure(int code) {
      failure_count++;
      failure_last_code = code;
    }
  }

  @Test
  public void streamPass() {
    MockStreamMetrics metrics = new MockStreamMetrics();
    HashMap<Integer, Integer> map = new HashMap<>();
    map.put(42, 1);
    MockJsonResponder responder = new MockJsonResponder();
    JsonResponderHashMapCleanupProxy proxy =
        new JsonResponderHashMapCleanupProxy(metrics, SimpleExecutor.NOW, map, 42, responder, Json.newJsonObject(), JsonLogger.NoOp);
    Assert.assertTrue(map.containsKey(42));
    proxy.stream("X");
    Assert.assertEquals("STREAM:X", responder.events.get(0));
    Assert.assertTrue(map.containsKey(42));
    Assert.assertEquals(1, metrics.progress_count);
    Assert.assertEquals(0, metrics.finish_count);
    Assert.assertEquals(0, metrics.failure_count);
    Assert.assertEquals(-1, metrics.failure_last_code);
  }

  @Test
  public void finishRemoves() {
    MockStreamMetrics metrics = new MockStreamMetrics();
    HashMap<Long, Integer> map = new HashMap<>();
    map.put(42L, 1);
    MockJsonResponder responder = new MockJsonResponder();
    JsonResponderHashMapCleanupProxy proxy =
        new JsonResponderHashMapCleanupProxy(metrics, SimpleExecutor.NOW, map, 42, responder, Json.newJsonObject(), JsonLogger.NoOp);
    Assert.assertTrue(map.containsKey(42L));
    proxy.finish("X");
    Assert.assertEquals("FINISH:X", responder.events.get(0));
    Assert.assertFalse(map.containsKey(42L));
    Assert.assertEquals(0, metrics.progress_count);
    Assert.assertEquals(1, metrics.finish_count);
    Assert.assertEquals(0, metrics.failure_count);
    Assert.assertEquals(-1, metrics.failure_last_code);
  }

  @Test
  public void errorRemoves() {
    MockStreamMetrics metrics = new MockStreamMetrics();
    HashMap<Long, Integer> map = new HashMap<>();
    map.put(42L, 1);
    MockJsonResponder responder = new MockJsonResponder();
    JsonResponderHashMapCleanupProxy proxy =
        new JsonResponderHashMapCleanupProxy(metrics, SimpleExecutor.NOW, map, 42, responder, Json.newJsonObject(), JsonLogger.NoOp);
    Assert.assertTrue(map.containsKey(42L));
    proxy.error(new ErrorCodeException(123));
    Assert.assertEquals("ERROR:123", responder.events.get(0));
    Assert.assertFalse(map.containsKey(42L));
    Assert.assertEquals(0, metrics.progress_count);
    Assert.assertEquals(0, metrics.finish_count);
    Assert.assertEquals(1, metrics.failure_count);
    Assert.assertEquals(123, metrics.failure_last_code);
  }
}
