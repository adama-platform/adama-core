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

import ape.runtime.contracts.MultiIndexable;
import ape.runtime.contracts.RxChild;
import ape.runtime.contracts.RxKillable;
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDateTime;
import ape.runtime.reactives.tables.TablePubSub;

import java.util.function.Supplier;

/**
 * Abstract base class for generated table row record types.
 * Provides lifecycle management (alive, dying, killed states), indexing support
 * via MultiIndexable, parent-child reactive relationships, and primary key
 * identification. Generated subclasses add typed fields and implement
 * reindexing, linking, and field access methods.
 */
public abstract class RxRecordBase<Ty extends RxRecordBase<Ty>> extends RxBase implements Comparable<Ty>, MultiIndexable, RxParent, RxChild, RxKillable {
  protected boolean __isDying;
  private boolean __alive;
  private RxTable<Ty> __table;

  public RxRecordBase(final RxParent __owner) {
    super(__owner);
    this.__alive = true;
    this.__isDying = false;
    if (__owner instanceof RxTable) {
      this.__table = ((RxTable<Ty>) __owner);
    }
  }

  public abstract Ty __link();

  public abstract void __deindex();

  private void __raiseDying() {
    if (!__isDying) {
      __isDying = true;
      if (__table != null) {
        __table.invalidatePrimaryKey(__id(), (Ty) this);
      }
    }
  }

  public void __delete() {
    __raiseDying();
    __raiseDirty();
  }

  @Override
  public void __raiseDirty() {
    super.__raiseDirty();
  }

  @Override
  public long __memory() {
    return super.__memory() + 2;
  }

  public boolean __isDying() {
    return __isDying;
  }

  @Override
  public void __kill() {
    __raiseDying();
    __alive = false;
    __killFields();
  }

  public abstract void __killFields();

  public abstract String __name();

  @Override
  public boolean __raiseInvalid() {
    __invalidateSubscribers();
    return __alive;
  }

  @Override
  public void __invalidateUp() {
    __raiseInvalid();
    if (__parent != null) {
      __parent.__invalidateUp();
    }
  }

  @Override
  public boolean __isAlive() {
    if (__parent != null) {
      if (!__parent.__isAlive()) {
        return false;
      }
    }
    return __alive;
  }

  @Override
  public void __cost(int cost) {
    if (__parent != null) {
      __parent.__cost(cost);
    }
  }

  public abstract void __reindex();

  public abstract void __setId(int __id, boolean __useForce);

  public abstract void __invalidateIndex(TablePubSub pubsub);

  public abstract void __pumpIndexEvents(TablePubSub pubsub);

  @Override
  public int compareTo(final Ty o) {
    // induce a default ordering, perhaps?
    return __id() - o.__id();
  }

  public abstract int __id();

  @Override
  public int hashCode() {
    return __id();
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof RxRecordBase) {
      return __id() == ((RxRecordBase) o).__id();
    }
    return false;
  }

  public abstract void __writeRxReport(JsonStreamWriter __writer);

  public abstract Object __fieldOf(String name);

  public void __subscribeBump(RxInt32 i) {
    __subscribe(() -> {
      i.bumpUpPost();
      return true;
    });
  }

  public void __subscribeUpdated(RxDateTime dt, Supplier<NtDateTime> get) {
    __subscribe(() -> {
      dt.set(get.get());
      return true;
    });
  }

  public void __postIngest() {
  }
}
