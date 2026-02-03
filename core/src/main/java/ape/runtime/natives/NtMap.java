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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ordered key-value map using TreeMap for sorted iteration.
 * Provides lookup returning NtMaybe with assign/delete chains for fluent
 * updates. Supports iteration as NtPair stream, min/max access, and bulk
 * operations (insert, set, clear). Used as the storage backend for RxMap
 * reactive wrapper and for reduce() aggregation results.
 */
public class NtMap<TIn, TOut> implements Iterable<NtPair<TIn, TOut>> {
  public final TreeMap<TIn, TOut> storage;

  public NtMap() {
    this.storage = new TreeMap<>();
  }

  public NtMap(final NtMap<TIn, TOut> input) {
    this.storage = new TreeMap<>(input.storage);
  }

  @Override
  public Iterator<NtPair<TIn, TOut>> iterator() {
    Iterator<Map.Entry<TIn, TOut>> iterator = storage.entrySet().iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public NtPair<TIn, TOut> next() {
        return pairOf(iterator.next());
      }
    };
  }

  public static <TIn, TOut> NtPair<TIn, TOut> pairOf(Map.Entry<TIn, TOut> entry) {
    return new NtPair<>(entry.getKey(), entry.getValue());
  }

  public NtMaybe<TOut> lookup(final TIn key) {
    final var data = storage.get(key);
    return new NtMaybe<>(data).withAssignChain(update -> {
      if (update == null) {
        storage.remove(key);
      } else {
        storage.put(key, update);
      }
    });
  }

  public Iterable<Map.Entry<TIn, TOut>> entries() {
    return storage.entrySet();
  }

  public TOut put(final TIn key, final TOut value) {
    return storage.put(key, value);
  }

  public void set(final NtMap<TIn, TOut> input) {
    this.storage.clear();
    this.storage.putAll(new TreeMap<>(input.storage));
  }

  public TOut removeDirect(TIn key) {
    return this.storage.remove(key);
  }

  public NtMaybe<TOut> remove(TIn key) {
    return new NtMaybe<>(this.storage.remove(key));
  }

  public TOut get(TIn key) {
    return this.storage.get(key);
  }

  public NtMap<TIn, TOut> insert(final NtMap<TIn, TOut> input) {
    this.storage.putAll(new TreeMap<>(input.storage));
    return this;
  }

  public int size() {
    return storage.size();
  }

  public NtMaybe<NtPair<TIn, TOut>> min() {
    if (storage.size() > 0) {
      return new NtMaybe<>(pairOf(storage.firstEntry()));
    } else {
      return new NtMaybe<>();
    }
  }

  public NtMaybe<NtPair<TIn, TOut>> max() {
    if (storage.size() > 0) {
      return new NtMaybe<>(pairOf(storage.lastEntry()));
    } else {
      return new NtMaybe<>();
    }
  }

  public boolean has(TIn key) {
    return this.storage.containsKey(key);
  }

  public void clear() {
    storage.clear();
  }
}
