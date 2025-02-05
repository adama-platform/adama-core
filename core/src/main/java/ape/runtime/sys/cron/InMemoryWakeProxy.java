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
package ape.runtime.sys.cron;

import ape.common.Callback;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.runtime.data.Key;

import java.util.HashSet;

/** this is the first line of waking up a document that has a cron job and is being put to sleep */
public class InMemoryWakeProxy implements WakeService {
  private final SimpleExecutor executor;
  private final KeyAlarm alarm;
  private final WakeService durable;
  private final HashSet<Key> inflight;

  public InMemoryWakeProxy(SimpleExecutor executor, KeyAlarm alarm, WakeService durable) {
    this.executor = executor;
    this.alarm = alarm;
    this.durable = durable;
    this.inflight = new HashSet<>();
  }

  @Override
  public void wakeIn(Key key, long when, Callback<Void> callback) {
    executor.execute(new NamedRunnable("locked") {
      @Override
      public void execute() throws Exception {
        if (!inflight.contains(key)) {
          executor.schedule(new NamedRunnable("wake") {
            @Override
            public void execute() throws Exception {
              inflight.remove(key);
              alarm.wake(key);
            }
          }, when);
          durable.wakeIn(key, when, callback);
        }
      }
    });
  }
}
