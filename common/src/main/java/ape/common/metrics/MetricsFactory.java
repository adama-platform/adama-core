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

/**
 * Factory interface for creating monitoring and metrics infrastructure.
 * All metrics are registered at startup. Provides monitors for request/response
 * patterns, streaming operations, callbacks, counters, and in-flight tracking.
 * Supports dashboard organization via pages and sections.
 */
public interface MetricsFactory {
  /** produce a monitor for request response style operations */
  RequestResponseMonitor makeRequestResponseMonitor(String name);

  /** produce a monitor for a stream operation */
  StreamMonitor makeStreamMonitor(String name);

  /** produce a monitor for a callback */
  CallbackMonitor makeCallbackMonitor(String name);

  /** produce a counter */
  Runnable counter(String name);

  /** produce an inflight measurement */
  Inflight inflight(String name);

  /** produce a monitor for an item action queue */
  ItemActionMonitor makeItemActionMonitor(String name);

  /** kick of a dashboard page */
  void page(String name, String title);

  /** within a page group metrics under a section */
  void section(String title);
}
