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
package ape.common.gossip;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Time-based expiring map with LRU eviction for size limits.
 * Items are timestamped on insertion and removed during gc() if they
 * exceed the age threshold. Also enforces a maximum entry count via
 * LinkedHashMap's removeEldestEntry mechanism.
 */
public class GarbageMap<T> implements Iterable<T> {

  private final LinkedHashMap<String, AgingItem> items;

  public GarbageMap(int maxSize) {
    this.items = new LinkedHashMap<>(maxSize, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, AgingItem> eldest) {
        return this.size() > maxSize;
      }
    };
  }

  public Collection<String> keys() {
    return items.keySet();
  }

  @Override
  public Iterator<T> iterator() {
    Iterator<AgingItem> it = items.values().iterator();
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public T next() {
        return it.next().item;
      }
    };
  }

  public void put(String key, T value, long now) {
    items.put(key, new AgingItem(value, now));
  }

  public T get(String key) {
    AgingItem item = items.get(key);
    if (item != null) {
      return item.item;
    }
    return null;
  }

  public T remove(String key) {
    AgingItem item = items.remove(key);
    if (item != null) {
      return item.item;
    }
    return null;
  }

  public int size() {
    return items.size();
  }

  public int gc(long now) {
    int removals = 0;
    Iterator<Map.Entry<String, AgingItem>> it = items.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, AgingItem> entry = it.next();
      long age = now - entry.getValue().time;
      if (age > Constants.MILLISECONDS_TO_SIT_IN_GARBAGE_MAP) {
        it.remove();
        removals++;
      }
    }
    return removals;
  }

  private class AgingItem {
    public final T item;
    public final long time;

    public AgingItem(T item, long now) {
      this.item = item;
      this.time = now;
    }
  }
}
