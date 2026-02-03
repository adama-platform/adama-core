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
package ape.common.cache;

import ape.common.TimeSource;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Synchronous LRU cache with size measurement and time-based expiration.
 * Evicts entries based on count limits, total measured size, and age.
 * Items must implement Measurable to report their size (memory/bytes).
 * Maintains a minimum entry count to prevent cache blowout from large items.
 * Not thread-safe; callers must synchronize externally.
 */
public class SyncCacheLRU<D, R extends Measurable> {
  private final TimeSource time;
  private final LinkedHashMap<D, CacheEntry<R>> cache;
  private final long ageMillisecondLimit;
  private final BiConsumer<D, R> evict;
  private long measure;

  /**
   * @param time the time source for knowing what time it is
   * @param minSize the minimum number of items to hold within a cache (useful when a big item comes in and evicts... everything
   * @param maxSize the maximum number of items to hold in the cache
   * @param measureLimit the maximum measure for the cache to take on (i.e. what physical limit do we care about)
   * @param ageMillisecondLimit the maximum age of an item within the cache
   * @param evict an event when a key is evicted
   */
  public SyncCacheLRU(TimeSource time, int minSize, int maxSize, long measureLimit, long ageMillisecondLimit, BiConsumer<D, R> evict) {
    this.time = time;
    this.ageMillisecondLimit = ageMillisecondLimit;
    this.evict = evict;
    this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<D, CacheEntry<R>> eldest) {
        // this ensures a minimum number of items in the cache; this useful to prevent an entire cache blowout due to a large item
        if (size() <= minSize) {
          return false;
        }
        // measure the age of the item
        long age = time.nowMilliseconds() - eldest.getValue().timestamp;
        // evict the eldest on size, too big, and too old
        if (size() > maxSize || measure > measureLimit || age > ageMillisecondLimit) {
          removed(eldest);
          return true;
        }
        return false;
      }
    };
  }

  /** internal: an item was removed for some reason */
  private void removed(Map.Entry<D, CacheEntry<R>> entry) {
    this.measure -= entry.getValue().item.measure();
    evict.accept(entry.getKey(), entry.getValue().item);
  }

  /** add an item to the cache; will trigger evictions */
  public void add(D key, R item) {
    CacheEntry<R> entry = new CacheEntry<>(item, time.nowMilliseconds());
    this.measure += item.measure();
    cache.put(key, entry);
  }

  /** get an item from the cache */
  public R get(D key) {
    CacheEntry<R> entry = cache.get(key);
    if (entry == null) {
      return null;
    }
    return entry.item;
  }

  /** sweep the items in the cache to evict items that are too old */
  public void sweep() {
    Iterator<Map.Entry<D, CacheEntry<R>>> it = cache.entrySet().iterator();
    long now = time.nowMilliseconds();
    while (it.hasNext()) {
      Map.Entry<D, CacheEntry<R>> entry = it.next();
      long age = now - entry.getValue().timestamp;
      if (age >= ageMillisecondLimit) {
        removed(entry);
        it.remove();
      }
    }
  }

  /** how big is the cache; the sum of all items measured */
  public long measure() {
    return measure;
  }

  /** how many items are in the cache */
  public int size() {
    return cache.size();
  }

  public void forceEvictionFromCacheNoDownstreamEviction(D key) {
    CacheEntry<R> value = cache.remove(key);
    if (value != null) {
      this.measure -= value.item.measure();
    }
  }

  public static <D, R extends Measurable> BiConsumer<D, R> MAKE_NO_OP() {
    return (x, y) -> {};
  }
}
