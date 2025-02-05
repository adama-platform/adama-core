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
package ape.runtime.sys.capacity;

import ape.common.metrics.Inflight;
import ape.common.metrics.MetricsFactory;

/** metrics for the capacity agent */
public class CapacityMetrics {
  public final Inflight shield_active_new_documents;
  public final Inflight shield_active_existing_connections;
  public final Inflight shield_active_messages;

  public final Inflight shield_count_hosts;
  public final Inflight shield_count_metering;
  public final Runnable shield_heat;

  public CapacityMetrics(MetricsFactory factory) {
    this.shield_active_new_documents = factory.inflight("alarm_shield_active_new_documents");
    this.shield_active_existing_connections = factory.inflight("alarm_shield_active_existing_connections");
    this.shield_active_messages = factory.inflight("alarm_shield_active_messages");
    this.shield_count_hosts = factory.inflight("shield_count_hosts");
    this.shield_count_metering = factory.inflight("shield_count_metering");
    this.shield_heat = factory.counter("shield_heat");
  }
}
