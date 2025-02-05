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
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;

import java.util.ArrayList;

/** the base class of any reactive object */
public abstract class RxBase {
  protected final RxParent __parent;
  private boolean __dirty;
  private ArrayList<RxChild> __subscribers;
  private boolean __notifying;
  protected boolean __invalid;

  protected RxBase(final RxParent __parent) {
    this.__parent = __parent;
    __subscribers = null;
    __notifying = false;
    __invalid = false;
  }

  /** commit the changes to the object, and emit a delta */
  public abstract void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta);

  /** take a dump of the data */
  public abstract void __dump(JsonStreamWriter writer);

  /** how many children are subscribed to this item */
  public int __getSubscriberCount() {
    if (__subscribers != null) {
      return __subscribers.size();
    }
    return 0;
  }

  public void __reportRx(String name, JsonStreamWriter __writer) {
    __writer.writeObjectFieldIntro(name);
    __writer.writeInteger(__getSubscriberCount());
  }

  /** initialize data & merge data in */
  public abstract void __insert(JsonStreamReader reader);

  /** patch the data */
  public abstract void __patch(JsonStreamReader reader);

  /** is the data dirty within this item */
  public boolean __isDirty() {
    return __dirty;
  }

  /** lower the dirtiness based on a commit */
  public void __lowerDirtyCommit() {
    __dirty = false;
    __invalid = false;
  }

  /** lower the dirtiness based on a revert; will invalidate subscribers */
  public void __lowerDirtyRevert() {
    __dirty = false;
    __invalidateSubscribers();
    __invalid = false;
  }

  public void __lowerInvalid() {
    __invalid = false;
  }

  /** tell all subscribers that they need to recompute */
  protected void __invalidateSubscribers() {
    if (__invalid) {
      return;
    }
    __invalid = true;
    if (__parent != null) {
      __parent.__invalidateUp();
    }
    if (__subscribers != null && !__notifying) {
      __notifying = true;
      try {
        final var it = __subscribers.iterator();
        while (it.hasNext()) {
          if (!it.next().__raiseInvalid()) {
            it.remove();
          }
        }
      } finally {
        __notifying = false;
      }
    }
  }

  /** inform the object that it is dirty, which in turn will notify the parents */
  public void __raiseDirty() {
    if (__dirty) {
      return;
    }
    __dirty = true;
    if (__parent != null) {
      __parent.__raiseDirty();
    }
    __invalidateSubscribers();
  }

  /** rollback state */
  public abstract void __revert();

  /** subscribe a child to the state of this object */
  public void __subscribe(final RxChild link) {
    if (__subscribers == null) {
      __subscribers = new ArrayList<>();
    }
    __subscribers.add(link);
  }

  /** return the rough # of bytes */
  public long __memory() {
    return 40L;
  }
}
