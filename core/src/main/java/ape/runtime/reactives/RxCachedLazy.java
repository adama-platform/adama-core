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

import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;

import java.util.Set;
import java.util.function.Supplier;

/** a formula that is cached by time (or evaluated exactly once) */
public class RxCachedLazy<Ty> extends RxDependent {
  private final Supplier<Ty> formula;
  private final Supplier<Runnable> perf;
  private final long millisecondsToKeep;
  private final RxInt64 time;
  protected Ty cached;
  private int generation;
  private long computedAt;

  public RxCachedLazy(final RxParent parent, final Supplier<Ty> formula, final Supplier<Runnable> perf, int secondsToKeep, RxInt64 time) {
    super(parent);
    this.formula = formula;
    this.perf = perf;
    this.millisecondsToKeep = secondsToKeep * 1000L;
    this.time = time;
    this.cached = null;
    this.generation = 0;
    this.computedAt = 0;
  }

  public boolean alive() {
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    reader.skipValue();
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    reader.skipValue();
  }

  @Override
  public void __revert() {
  }

  @Override
  public boolean __raiseInvalid() {
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  public Ty get() {
    if (cached == null) {
      cached = computeWithGuard();
      inc();
    }
    return cached;
  }

  private void inc() {
    if (__parent instanceof RxRecordBase && generation == 0) {
      generation = ((RxRecordBase) __parent).__id();
    } else if (generation == 0 && cached != null) {
      generation = cached.hashCode();
    }
    generation *= 65521;
    generation++;
  }

  private Ty computeWithGuard() {
    Runnable track = null;
    if (perf != null) {
      track = perf.get();
    }
    Ty result = formula.get();
    computedAt = time.get();
    if (track != null) {
      track.run();
    }
    return result;
  }

  public int getGeneration() {
    if (generation == 0) {
      inc();
    }
    return generation;
  }

  public void __settle(Set<Integer> views) {
    if (millisecondsToKeep > 0) {
      long since = time.get() - computedAt;
      if (since > millisecondsToKeep && cached != null) {
        inc();
        cached = null;
        __invalidateSubscribers();
        __lowerInvalid();
      }
    }
  }
}
