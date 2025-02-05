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
package ape.runtime.reactives.tables;

import ape.runtime.contracts.RxParent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/** simple pubsub fanout for TableSubscription's under a parent  */
public class TablePubSub implements TableSubscription {
  private final RxParent owner;
  private final ArrayList<TableSubscription> subscriptions;
  private final TreeSet<Integer> filter;
  private final TreeSet<IndexInvalidCacheHit> filterIndex;

  class IndexInvalidCacheHit implements Comparable<IndexInvalidCacheHit> {
    public final int column;
    public final int value;

    public IndexInvalidCacheHit(int column, int value) {
      this.column = column;
      this.value = value;
    }

    @Override
    public int compareTo(IndexInvalidCacheHit o) {
      int delta = Integer.compare(value, o.value);
      if (delta == 0) {
        return Integer.compare(column, o.column);
      }
      return delta;
    }
  }

  public TablePubSub(RxParent owner) {
    this.owner = owner;
    this.subscriptions = new ArrayList<>();
    this.filter = new TreeSet<>();
    this.filterIndex = new TreeSet<>();
  }

  public int count() {
    return subscriptions.size();
  }

  public void subscribe(TableSubscription ts) {
    subscriptions.add(ts);
  }

  @Override
  public boolean alive() {
    if (owner != null) {
      return owner.__isAlive();
    }
    return true;
  }

  @Override
  public boolean primary(int primaryKey) {
    if (filter.contains(primaryKey)) {
      return false;
    }
    filter.add(primaryKey);
    for (TableSubscription ts : subscriptions) {
      ts.primary(primaryKey);
    }
    return true;
  }

  @Override
  public void index(int field, int value) {
    IndexInvalidCacheHit hit = new IndexInvalidCacheHit(field, value);
    if (filterIndex.contains(hit)) {
      return;
    }
    filterIndex.add(hit);
    for (TableSubscription ts : subscriptions) {
      ts.index(field, value);
    }
  }

  public void settle() {
    filter.clear();
    filterIndex.clear();
  }

  public void gc() {
    Iterator<TableSubscription> it = subscriptions.iterator();
    while (it.hasNext()) {
      if (!it.next().alive()) {
        it.remove();
      }
    }
  }

  public long __memory() {
    return 128 * subscriptions.size() + 2048;
  }
}
