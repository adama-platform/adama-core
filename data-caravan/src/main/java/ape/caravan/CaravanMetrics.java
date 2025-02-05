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
package ape.caravan;

import ape.common.metrics.CallbackMonitor;
import ape.common.metrics.Inflight;
import ape.common.metrics.MetricsFactory;

public class CaravanMetrics {
  public Runnable caravan_waste;
  public Runnable caravan_seq_off;
  public Inflight caravan_datalog_loss;
  public CallbackMonitor caravan_backup;
  public CallbackMonitor caravan_restore;

  public CaravanMetrics(MetricsFactory factory) {
    this.caravan_waste = factory.counter("caravan_waste");
    this.caravan_seq_off = factory.counter("caravan_seq_off");
    this.caravan_datalog_loss = factory.inflight("alarm_caravan_datalog_loss");
    this.caravan_backup = factory.makeCallbackMonitor("caravan_backup");
    this.caravan_restore = factory.makeCallbackMonitor("caravan_restore");
  }
}
