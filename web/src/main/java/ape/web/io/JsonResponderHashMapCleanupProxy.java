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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.common.metrics.StreamMonitor;

import java.util.HashMap;

/**
 * a JsonResponder wrapper which will remove the given key from a map on a terminal signal. Note:
 * all mutations to the map are executed in the provided executor
 */
public class JsonResponderHashMapCleanupProxy<T> implements JsonResponder {
  private final StreamMonitor.StreamMonitorInstance metrics;
  private final SimpleExecutor executor;
  private final HashMap<Long, T> map;
  private final long key;
  private final JsonResponder responder;
  private final ObjectNode itemToLog;
  private final JsonLogger logger;

  public JsonResponderHashMapCleanupProxy(StreamMonitor.StreamMonitorInstance metrics, SimpleExecutor executor, HashMap<Long, T> map, long key, JsonResponder responder, ObjectNode itemToLog, JsonLogger logger) {
    this.metrics = metrics;
    this.executor = executor;
    this.map = map;
    this.key = key;
    this.responder = responder;
    this.itemToLog = itemToLog;
    this.logger = logger;
  }

  @Override
  public void stream(String json) {
    metrics.progress();
    responder.stream(json);
  }

  @Override
  public void finish(String json) {
    metrics.finish();
    executor.execute(new NamedRunnable("json-hashmap-cleanup") {
      @Override
      public void execute() throws Exception {
        map.remove(key);
      }
    });
    responder.finish(json);
    itemToLog.put("success", true);
    logger.log(itemToLog);
  }

  @Override
  public void error(ErrorCodeException ex) {
    metrics.failure(ex.code);
    executor.execute(new NamedRunnable("json-hashmap-cleanup") {
      @Override
      public void execute() throws Exception {
        map.remove(key);
      }
    });
    responder.error(ex);
    itemToLog.put("success", false);
    itemToLog.put("failure-code", ex.code);
    logger.log(itemToLog);
  }
}
