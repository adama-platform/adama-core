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
package ape.common.pool;

import java.util.Iterator;
import java.util.LinkedList;

/** a pool is a simple list wrapper where we count the inflight items; consumers of the pool are expected to return the item back to the pool */
public class Pool<S> {
  private final LinkedList<S> queue;
  private int size;

  public Pool() {
    this.queue = new LinkedList<>();
    this.size = 0;
  }

  /** increase the size of the pool */
  public void bumpUp() {
    size++;
  }

  /** decrease the size of the pool */
  public void bumpDown() {
    size--;
  }

  /** @return the size of the pool */
  public int size() {
    return size;
  }

  /** add an item back into the pool as available */
  public void add(S item) {
    queue.add(item);
  }

  public Iterator<S> iterator() {
    return queue.iterator();
  }

  /** remove the next item from the queue if available */
  public S next() {
    if (queue.size() > 0) {
      return queue.removeFirst();
    }
    return null;
  }
}
