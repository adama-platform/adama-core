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
package ape.web.client;

import ape.common.metrics.CallbackMonitor;
import ape.common.metrics.Inflight;
import ape.common.metrics.MetricsFactory;

/**
 * Prometheus metrics collection for HTTP client operations.
 * Tracks response codes (200/204, 400, 403, 404, 410, 500+), request lifecycle
 * (start, send, fail), inflight requests, and connection pool performance.
 */
public class WebClientBaseMetrics {
  public final Inflight alarm_web_client_null_responder;
  public final Runnable web_client_instant_fail;
  public final Runnable web_client_200_or_204;
  public final Runnable web_client_403;
  public final Runnable web_client_404;
  public final Runnable web_client_410;
  public final Runnable web_client_400;
  public final Runnable web_client_500_plus;
  public final Runnable web_client_code_unknown;
  public final Runnable web_client_request_start;
  public final Runnable web_client_request_sent_small_full;
  public final Runnable web_client_request_send_large_started;
  public final Runnable web_client_request_send_large_finished;
  public final Runnable web_client_request_failed_send;
  public final Inflight inflight_web_requests;
  public final CallbackMonitor web_execute_find_pool_item;
  public final CallbackMonitor web_create_shared;

  public WebClientBaseMetrics(MetricsFactory factory) {
    this.alarm_web_client_null_responder = factory.inflight("alarm_web_client_null_responder");
    this.web_client_instant_fail = factory.counter("web_client_instant_fail");
    this.web_create_shared = factory.makeCallbackMonitor("web_create_shared");
    this.web_client_200_or_204 = factory.counter("web_client_200_or_204");
    this.web_client_400 = factory.counter("web_client_400");
    this.web_client_403 = factory.counter("web_client_403");
    this.web_client_404 = factory.counter("web_client_404");
    this.web_client_410 = factory.counter("web_client_410");
    this.web_client_500_plus = factory.counter("web_client_500_plus");
    this.web_client_code_unknown = factory.counter("web_client_code_unknown");
    this.web_client_request_start = factory.counter("web_client_request_start");
    this.web_client_request_sent_small_full = factory.counter("web_client_request_sent_small_full");
    this.web_client_request_send_large_started = factory.counter("web_client_request_send_large_started");
    this.web_client_request_send_large_finished = factory.counter("web_client_request_send_large_finished");
    this.web_client_request_failed_send = factory.counter("web_client_request_failed_send");
    this.inflight_web_requests = factory.inflight("web_inflight_web_requests");
    this.web_execute_find_pool_item = factory.makeCallbackMonitor("web_execute_find_pool_item");
  }
}
