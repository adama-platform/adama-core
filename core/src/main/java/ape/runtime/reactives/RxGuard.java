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
import java.util.TreeMap;

/**
 * a condition to learn if changes have occured. This is like a Lazy, but gives people the ability
 * to learn if changes have happened since the last time a commited happened
 */
public class RxGuard extends RxDependent {
  private int generation;
  private TreeMap<Integer, Integer> bumps;

  public RxGuard(RxParent parent) {
    super(parent);
    generation = 0;
    __invalid = true;
    bumps = null;
  }

  @Override
  public boolean alive() {
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  private void inc() {
    if (__parent instanceof RxRecordBase && generation == 0) {
      generation = ((RxRecordBase) __parent).__id();
    }
    generation *= 65521;
    generation++;
  }

  @Override
  public boolean __raiseInvalid() {
    if (!__invalid) {
      inc();
      __invalidateSubscribers();
    }
    if (__parent != null) {
      return __parent.__isAlive();
    }
    return true;
  }

  public void __settle(Set<Integer> viewers) {
    __lowerInvalid();
  }

  public int getGeneration(int viewerId) {
    if (generation == 0) {
      inc();
    }
    int bump = 0;
    if (bumps != null) {
      Integer bumpTest = bumps.get(viewerId);
      if (bumpTest != null) {
        bump = bumpTest;
      }
    }
    if (isFired(viewerId)) {
      if (bumps == null) {
        bumps = new TreeMap<>();
      }
      Integer val = bumps.get(viewerId);
      if (val == null) {
        val = 0;
      }
      val += generation * 17;
      bumps.put(viewerId, val);
      bump = val;
    }
    return generation + bump;
  }

  @Override
  public long __memory() {
    return 64 + (bumps != null ? 32 * bumps.size() : 0);
  }
}
