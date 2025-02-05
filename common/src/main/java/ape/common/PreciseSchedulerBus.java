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
package ape.common;

import java.util.ArrayList;

/** This aims to be a high resolution scheduler such that events can be executed after minimal wait time (i.e. 1 ms) */
public class PreciseSchedulerBus implements Runnable {
  private final ArrayList<Runnable>[] schedule;
  private int at;
  private long snapshot;

  /** create with the given argument indicating how many milliseconds we can schedule in the future */
  public PreciseSchedulerBus(int max) {
    this.schedule = new ArrayList[max];
    this.at = 0;
    for (int k = 0; k < max; k++) {
      schedule[k] = new ArrayList<>();
    }
    this.snapshot = System.nanoTime();
  }

  @SuppressWarnings({"BusyWait","Unchecked"})
  @Override
  public void run() {
    while (true) {
      ArrayList<Runnable> local = null;
      synchronized (schedule) {
        if (schedule[at].size() > 0) {
          local = new ArrayList<>(schedule[at]);
          schedule[at].clear();
        }
        at++;
        at %= schedule.length;
      }
      if (local != null) {
        for (Runnable task : local) {
          task.run();
        }
      }
      try {
        long newSnapshot = System.nanoTime();
        int sinceLast = (int) (newSnapshot - snapshot);
        snapshot = newSnapshot;
        Thread.sleep(0, sinceLast >= 1000000 ? Math.max(250000, 1750000 - sinceLast) : 750000);
      } catch (InterruptedException ie) {
        return;
      }
    }
  }

  /** transfer the named runnable into the executor after the given milliseconds */
  public void schedule(SimpleExecutor executor, NamedRunnable runnable, int futureMilliseconds) {
    synchronized (schedule) {
      schedule[(at + futureMilliseconds / 2) % schedule.length].add(() -> {
        executor.execute(runnable);
      });
    }
  }
}
