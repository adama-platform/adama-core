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

import java.util.Set;
import java.util.function.Supplier;

/**
 * Reactive computed value that lazily evaluates a formula on demand.
 * Caches the result until invalidated by dependency changes. Supports
 * performance tracking via optional perf supplier. Generation numbers
 * provide efficient change detection for delta synchronization without
 * comparing actual values.
 */
public class RxLazy<Ty> extends RxDependent {
  protected final Supplier<Ty> formula;
  private final Supplier<Runnable> perf;
  protected Ty cached;
  private int generation;

  public RxLazy(final RxParent parent, final Supplier<Ty> formula, final Supplier<Runnable> perf) {
    super(parent);
    this.formula = formula;
    this.cached = null;
    this.generation = 0;
    this.perf = perf;
  }

  public boolean alive() {
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  @Override
  public boolean __raiseInvalid() {
    cached = null;
    __invalidateSubscribers();
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  public Ty get() {
    if (__invalid) {
      Runnable track = null;
      if (perf != null) {
        track = perf.get();
      }
      Ty result = formula.get();
      if (track != null) {
        track.run();
      }
      return result;
    }
    if (cached == null) {
      cached = computeWithGuard();
    }
    return cached;
  }

  private void inc() {
    RxParent parentToQuery = __parent;
    while (parentToQuery != null && parentToQuery instanceof RxRecordBase && generation == 0) {
      generation = ((RxRecordBase) parentToQuery).__id();
      parentToQuery = ((RxRecordBase) parentToQuery).__parent;
    }
    if (generation == 0 && cached != null) {
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
    start();
    Ty result = formula.get();
    finish();
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
    if (__invalid) {
      cached = null;
      inc();
      __lowerInvalid();
    }
  }

  public void __forceSettle() {
    if (__invalid) {
      inc();
      __lowerInvalid();
      get();
    }
  }
}
