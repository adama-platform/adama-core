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

import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;

import java.util.ArrayList;

/** a queue of work for an item which may not be available */
public class ItemQueue<T> {
  private final SimpleExecutor executor;
  private final int bound;
  private final int timeout;
  private ArrayList<ItemAction<T>> buffer;
  private T item;

  public ItemQueue(SimpleExecutor executor, int bound, int timeout) {
    this.executor = executor;
    this.item = null;
    this.buffer = null;
    this.bound = bound;
    this.timeout = timeout;
  }

  public void ready(T item) {
    this.item = item;
    if (buffer != null) {
      for (ItemAction<T> action : buffer) {
        action.execute(item);
      }
      buffer = null;
    }
  }

  public void unready() {
    this.item = null;
  }

  public T nuke() {
    if (buffer != null) {
      for (ItemAction<T> action : buffer) {
        action.killDueToReject();
      }
      buffer = null;
    }
    T result = item;
    item = null;
    return result;
  }

  public void add(ItemAction<T> action) {
    add(action, timeout);
  }

  public void add(ItemAction<T> action, int customTimeout) {
    if (item != null) {
      action.execute(item);
      return;
    }
    if (buffer == null) {
      buffer = new ArrayList<>();
    }
    if (buffer.size() >= bound) {
      action.killDueToReject();
    } else {
      buffer.add(action);
      action.setCancelTimeout(executor.schedule(new NamedRunnable("expire-action") {
        @Override
        public void execute() throws Exception {
          action.killDueToTimeout();
          buffer.remove(action);
        }
      }, customTimeout));
    }
  }
}
