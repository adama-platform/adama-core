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
package ape.runtime.reactives;

import ape.runtime.contracts.RxChild;
import ape.runtime.contracts.RxParent;
import ape.runtime.natives.NtMaybe;
import ape.runtime.reactives.maps.MapGuardTarget;
import ape.runtime.reactives.maps.MapPubSub;
import ape.runtime.reactives.tables.TableSubscription;

import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

/** A projection of a field out of a table into a map */
public class RxProjectionMap<RowTy extends RxRecordBase<RowTy>, RangeTy extends RxBase> extends RxNerfedBase implements RxParent, RxChild, MapGuardTarget {
  private final RxTable<RowTy> table;
  private final HashMap<Integer, RowTy> cache;
  private final Function<RowTy, RangeTy> extract;
  private final MapPubSub<Integer> pubsub;
  private final Stack<RxMapGuard> guardsInflight;
  private RxMapGuard activeGuard;

  protected RxProjectionMap(RxParent __parent, RxTable<RowTy> table, Function<RowTy, RangeTy> extract) {
    super(__parent);
    this.table = table;
    this.cache = new HashMap<>();
    this.extract = extract;
    this.pubsub = new MapPubSub<>(this);
    this.guardsInflight = new Stack<>();
    this.activeGuard = null;
    // subscribe to the table
    table.pubsub.subscribe(new TableSubscription() {
      @Override
      public boolean alive() {
        return __isAlive();
      }

      @Override
      public boolean primary(int primaryKey) {
        invalidate(primaryKey);
        return false;
      }

      @Override
      public void index(int index, int value) {
      }
    });
  }

  private void forward(int key) {
    pubsub.changed(key);
  }

  private void invalidate(int primaryKey) {
    if (cache.containsKey(primaryKey)) {
      if (!table.has(primaryKey)) {
        // deletions must propagate
        cache.remove(primaryKey);
        forward(primaryKey);
      }
      return;
    } else {
      RowTy row = table.getById(primaryKey);
      if (row != null) {
        RangeTy item = extract.apply(row);
        item.__subscribe(() -> {
          forward(primaryKey);
          return RxProjectionMap.this.__isAlive();
        });
        cache.put(primaryKey, row);
        forward(primaryKey);
      }
    }
  }

  public NtMaybe<RowTy> lookup(Integer key) {
    if (activeGuard != null) {
      activeGuard.readKey(key);
    }
    if (__parent != null) {
      __parent.__cost(4);
    }
    return new NtMaybe<>(cache.get(key));
  }

  public void __subscribe(RxMapGuard<Integer> guard) {
    pubsub.subscribe(guard);
  }

  @Override
  public void __settle(Set<Integer> viewers) {
    pubsub.settle();
    pubsub.gc();
  }

  @Override
  public boolean __raiseInvalid() {
    return __isAlive();
  }

  @Override
  public boolean __isAlive() {
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  @Override
  public void __cost(int cost) {
    if (__parent != null) {
      __parent.__cost(cost);
    }
  }

  @Override
  public void __invalidateUp() {
    if (__parent != null) {
      __parent.__invalidateUp();
    }
  }

  @Override
  public void pushGuard(RxMapGuard guard) {
    guardsInflight.push(guard);
    activeGuard = guard;
  }

  @Override
  public void popGuard() {
    guardsInflight.pop();
    if (guardsInflight.empty()) {
      activeGuard = null;
    } else {
      activeGuard = guardsInflight.peek();
    }
  }

  public long memory() {
    return 1024 + 128 * cache.size();
  }
}
