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
package ape.common.queue;

import ape.common.metrics.ItemActionMonitor;

/** an unit of work that sits within a queue for processing */
public abstract class ItemAction<T> {
  private static final Runnable DEFAULT_CANCEL_TIMEOUT = () -> {
  };
  private final int errorTimeout;
  private final int errorRejected;
  private final ItemActionMonitor.ItemActionMonitorInstance monitor;
  private boolean alive;
  private Runnable cancelTimeout;

  public ItemAction(int errorTimeout, int errorRejected, ItemActionMonitor.ItemActionMonitorInstance monitor) {
    this.alive = true;
    this.errorTimeout = errorTimeout;
    this.errorRejected = errorRejected;
    this.monitor = monitor;
    this.cancelTimeout = DEFAULT_CANCEL_TIMEOUT;
  }

  /** is the queue action alive */
  public boolean isAlive() {
    return alive;
  }

  /** execute the item if it is still valid */
  public void execute(T item) {
    if (alive) {
      executeNow(item);
      alive = false;
      cancelTimeout.run();
      monitor.executed();
    }
  }

  protected abstract void executeNow(T item);

  /** how the timeout is executed */
  public void killDueToTimeout() {
    if (alive) {
      alive = false;
      failure(errorTimeout);
      monitor.timeout();
    }
  }

  protected abstract void failure(int code);

  public void killDueToReject() {
    if (alive) {
      alive = false;
      failure(errorRejected);
      monitor.rejected();
      cancelTimeout.run();
    }
  }

  public void setCancelTimeout(Runnable cancelTimeout) {
    this.cancelTimeout = cancelTimeout;
  }
}
