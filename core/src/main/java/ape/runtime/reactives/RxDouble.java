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
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;

/**
 * Reactive double-precision floating-point with change tracking.
 * Maintains backup value for commit/revert semantics. Supports arithmetic
 * operations (add, multiply, increment, decrement) with proper dirty flag
 * propagation. Unlike integer types, does not extend RxIndexableBase since
 * floating-point values cannot be efficiently indexed.
 */
public class RxDouble extends RxBase implements Comparable<RxDouble>, CanGetAndSet<Double> {
  private double backup;
  private double value;

  public RxDouble(final RxParent parent, final double value) {
    super(parent);
    backup = value;
    this.value = value;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.writeDouble(value);
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.writeDouble(backup);
      backup = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.writeDouble(value);
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    backup = reader.readDouble();
    value = backup;
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    set(reader.readDouble());
  }

  @Override
  public void __revert() {
    if (__isDirty()) {
      value = backup;
      __lowerDirtyRevert();
    }
  }

  @Override
  public long __memory() {
    return super.__memory() + 16;
  }

  public void set(final double value) {
    this.value = value;
    __raiseDirty();
  }

  public double bumpDownPost() {
    final var result = value--;
    __raiseDirty();
    return result;
  }

  public double bumpDownPre() {
    final var result = --value;
    __raiseDirty();
    return result;
  }

  public double bumpUpPost() {
    final var result = value++;
    __raiseDirty();
    return result;
  }

  public double bumpUpPre() {
    final var result = ++value;
    __raiseDirty();
    return result;
  }

  @Override
  public int compareTo(final RxDouble other) {
    return Double.compare(value, other.value);
  }

  @Override
  public Double get() {
    return value;
  }

  @Override
  public void set(final Double value) {
    set(value.doubleValue());
  }

  public double opAddTo(final double incoming) {
    value += incoming;
    __raiseDirty();
    return value;
  }

  public double opMultBy(final double x) {
    value *= x;
    __raiseDirty();
    return value;
  }

  public void set(final int value) {
    this.value = value;
    __raiseDirty();
  }
}
