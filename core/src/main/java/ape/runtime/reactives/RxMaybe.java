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

import ape.runtime.contracts.CanGetAndSet;
import ape.runtime.contracts.RxChild;
import ape.runtime.contracts.RxKillable;
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtMaybe;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

/** a reactive maybe */
public class RxMaybe<Ty extends RxBase, Ry> extends RxBase implements RxParent, RxChild, RxKillable {
  private final Function<RxParent, Ty> maker;
  private Ty priorValue;
  private Ty value;

  public RxMaybe(final RxParent owner, final Function<RxParent, Ty> maker) {
    super(owner);
    this.value = null;
    this.maker = maker;
    this.priorValue = null;
  }

  @Override
  public boolean __isAlive() {
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  public void __link() {
    if (value != null && value instanceof RxRecordBase) {
      ((RxRecordBase<?>) value).__link();
    }
  }

  @Override
  public void __cost(int cost) {
    if (__parent != null) {
      __parent.__cost(cost);
    }
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      if (value != null) {
        if (priorValue == null) {
          value.__commit(name, forwardDelta, new JsonStreamWriter());
          reverseDelta.writeObjectFieldIntro(name);
          reverseDelta.writeNull();
        } else {
          value.__commit(name, forwardDelta, reverseDelta);
        }
      } else { // value is null
        forwardDelta.writeObjectFieldIntro(name);
        forwardDelta.writeNull();
        if (priorValue != null) {
          reverseDelta.writeObjectFieldIntro(name);
          priorValue.__dump(reverseDelta);
        }
      }

      priorValue = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    if (value != null) {
      value.__dump(writer);
    } else {
      writer.writeNull();
    }
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    if (reader.testLackOfNull()) {
      if (value == null) {
        value = maker.apply(this);
        value.__subscribe(this);
      }
      priorValue = value;
      value.__insert(reader);
    } else {
      value = null;
      priorValue = null;
    }
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    if (reader.testLackOfNull()) {
      make().__patch(reader);
    } else {
      delete();
    }
  }

  @Override
  public void __revert() {
    if (__isDirty()) {
      value = priorValue;
      if (value != null) {
        value.__revert();
      }
      __lowerDirtyRevert();
    }
  }

  @Override
  public long __memory() {
    return super.__memory() + 24 + (value != null ? value.__memory() : 0) + (priorValue != null ? priorValue.__memory() : 0);
  }

  public Ty make() {
    if (value == null) {
      value = maker.apply(this);
      value.__subscribe(this);
      value.__raiseDirty();
    }
    return value;
  }

  public void delete() {
    if (value != null) {
      if (value instanceof RxKillable) {
        ((RxKillable) value).__kill();
      }
      value = null;
      __raiseDirty();
    }
  }

  @Override
  public void __kill() {
    if (value instanceof RxKillable) {
      ((RxKillable) value).__kill();
    }
  }

  @Override
  public boolean __raiseInvalid() {
    __invalidateSubscribers();
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  @Override
  public void __invalidateUp() {
    __raiseInvalid();
  }

  public int compareValues(final RxMaybe<Ty, Ry> other, final Comparator<Ty> test) {
    if (value == null) {
      if (other.value == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (other.value == null) {
        return -1;
      } else {
        return test.compare(value, other.value);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public NtMaybe<Ry> get() {
    if (value == null) {
      return new NtMaybe();
    } else {
      if (value instanceof CanGetAndSet) {
        return new NtMaybe<>((Ry)((CanGetAndSet) value).get()).withDeleteChain(() -> delete());
      } else {
        return new NtMaybe(value).withDeleteChain(() -> delete());
      }
    }
  }

  public boolean has() {
    return value != null;
  }

  @SuppressWarnings("unchecked")
  public void set(final NtMaybe other) {
    if (other.has()) {
      ((CanGetAndSet) this.make()).set(other.get());
    } else {
      delete();
    }
    __raiseDirty();
  }

  @Override
  public void __settle(Set<Integer> viewers) {
    __lowerInvalid();
    if (value != null) {
      if (value instanceof RxParent) {
        ((RxParent) value).__settle(viewers);
      }
    }
  }
}
