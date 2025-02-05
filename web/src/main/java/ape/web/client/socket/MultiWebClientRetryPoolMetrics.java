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
package ape.web.client.socket;

import ape.common.metrics.Inflight;
import ape.common.metrics.ItemActionMonitor;
import ape.common.metrics.MetricsFactory;

/** metrics for maintaining a connection to a websocket endpoint */
public class MultiWebClientRetryPoolMetrics {
  public final ItemActionMonitor queue;
  public final Runnable disconnected;
  public final Runnable failure;
  public final Runnable slow;
  public final Inflight inflight;

  public MultiWebClientRetryPoolMetrics(MetricsFactory factory) {
    queue = factory.makeItemActionMonitor("mwcr_pool_queue");
    disconnected = factory.counter("mwcr_pool_disconnected");
    failure = factory.counter("mwcr_pool_failure");
    slow = factory.counter("mwcr_pool_slow");
    inflight = factory.inflight("mwcr_pool_inflight");
  }
}
