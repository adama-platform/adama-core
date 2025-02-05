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
package ape.caravan.data;

import ape.common.metrics.Inflight;
import ape.common.metrics.MetricsFactory;

public class DiskMetrics {
  public final Runnable flush;
  public final Inflight total_storage_allocated;
  public final Inflight free_storage_available;
  public final Inflight alarm_storage_over_80_percent;
  public final Inflight active_entries;
  public final Inflight items_total;
  public final Inflight items_over_tenk;
  public final Inflight items_over_twentyk;
  public final Inflight items_over_fiftyk;
  public final Inflight items_over_onehundredk;
  public final Inflight items_over_onemega;
  public final Runnable items_trimmed;
  public final Runnable appends;
  public final Runnable failed_append;
  public final Runnable reads;

  public DiskMetrics(MetricsFactory factory) {
    this.flush = factory.counter("disk_flush");
    this.total_storage_allocated = factory.inflight("disk_total_storage_allocated_mb");
    this.free_storage_available = factory.inflight("disk_free_storage_available_mb");
    this.alarm_storage_over_80_percent = factory.inflight("alarm_storage_over_80_percent");
    this.active_entries = factory.inflight("disk_active_entries");
    this.items_total = factory.inflight("storage_items_total");
    this.items_over_tenk = factory.inflight("storage_items_over_tenk");
    this.items_over_twentyk = factory.inflight("storage_items_over_twentyk");
    this.items_over_fiftyk = factory.inflight("storage_items_over_fiftyk");
    this.items_over_onehundredk = factory.inflight("storage_items_over_onehundredk");
    this.items_over_onemega = factory.inflight("storage_items_over_onemega");
    this.items_trimmed = factory.counter("storage_items_trimmed");
    this.failed_append = factory.counter("alarm_failed_append");
    this.appends = factory.counter("storage_appends");
    this.reads = factory.counter("storage_reads");
  }
}
