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
package ape.runtime.natives;

import ape.common.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NtGrid<TIn, TOut> implements Iterable<NtPair<Pair<TIn>, TOut>>  {
  public final HashMap<Pair<TIn>, TOut> storage;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedMinX;
  private int cachedMinY;

  public NtGrid() {
    this.storage = new HashMap<>();
    resetSizeCache();
  }

  public NtGrid(final NtGrid<TIn, TOut> input) {
    this.storage = new HashMap<>(input.storage);
    resetSizeCache();
  }

  private void resetSizeCache() {
    this.cachedWidth = -1;
    this.cachedHeight = -1;
  }

  public NtMaybe<TOut> lookup(final TIn a, final TIn b) {
    Pair<TIn> key = new Pair<>(a, b);
    final var data = storage.get(key);
    return new NtMaybe<>(data).withAssignChain(update -> {
      if (update == null) {
        storage.remove(key);
      } else {
        storage.put(key, update);
      }
      resetSizeCache();
    });
  }

  public NtMaybe<TOut> remove(final TIn a, final TIn b) {
    Pair<TIn> key = new Pair<>(a, b);
    final var data = storage.remove(key);
    resetSizeCache();
    return new NtMaybe<>(data);
  }

  public boolean has(final TIn a, final TIn b) {
    return storage.containsKey(new Pair<>(a, b));
  }

  public void clear() {
    storage.clear();
    resetSizeCache();
  }

  public void transpose() {
    NtGrid<TIn, TOut> next = new NtGrid<>();
    for (Map.Entry<Pair<TIn>, TOut> entry : storage.entrySet()) {
      next.storage.put(entry.getKey().swap(), entry.getValue());
    }
    storage.clear();
    storage.putAll(next.storage);
  }

  public int size() {
    return storage.size();
  }

  @Override
  public Iterator<NtPair<Pair<TIn>, TOut>> iterator() {
    final Iterator<Map.Entry<Pair<TIn>, TOut>> iterator = storage.entrySet().iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public NtPair<Pair<TIn>, TOut> next() {
        Map.Entry<Pair<TIn>, TOut> entry = iterator.next();
        return new NtPair<>(entry.getKey(), entry.getValue());
      }
    };
  }

  private void updateCaches() {
    int max_x = Integer.MIN_VALUE;
    int min_x = Integer.MAX_VALUE;
    int max_y = Integer.MIN_VALUE;
    int min_y = Integer.MAX_VALUE;
    for (Map.Entry<Pair<TIn>, TOut> entry : storage.entrySet()) {
      Pair<TIn> p = entry.getKey();
      if (p.x instanceof Integer && p.y instanceof Integer) {
        max_x = Math.max(max_x, (int) p.x);
        min_x = Math.min(min_x, (int) p.x);
        max_y = Math.max(max_y, (int) p.y);
        min_y = Math.min(min_y, (int) p.y);
      }
    }
    cachedWidth = (max_x - min_x) + 1;
    cachedHeight = (max_y - min_y) + 1;
    cachedMinX = min_x;
    cachedMinY = min_y;
  }

  public int minX() {
    if (cachedWidth == -1) {
      updateCaches();
    }
    return cachedMinX;
  }
  public int minY() {
    if (cachedWidth == -1) {
      updateCaches();
    }
    return cachedMinY;
  }

  public int width() {
    if (cachedWidth < 0) {
      updateCaches();
    }
    return cachedWidth;
  }

  public int height() {
    if (cachedHeight < 0) {
      updateCaches();
    }
    return cachedHeight;
  }
}
