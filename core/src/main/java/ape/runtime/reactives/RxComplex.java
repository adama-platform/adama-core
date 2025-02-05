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
import ape.runtime.natives.NtComplex;

/** a reactive complex number */
public class RxComplex extends RxBase implements CanGetAndSet<NtComplex> {
  private NtComplex backup;
  private NtComplex value;

  public RxComplex(final RxParent parent, final NtComplex value) {
    super(parent);
    backup = value;
    this.value = value;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.writeNtComplex(value);
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.writeNtComplex(backup);
      backup = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.writeNtComplex(value);
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    backup = reader.readNtComplex();
    value = backup;
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    set(reader.readNtComplex());
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
    return super.__memory() + backup.memory() + value.memory() + 16;
  }

  @Override
  public NtComplex get() {
    return value;
  }

  @Override
  public void set(final NtComplex value) {
    this.value = value;
    __raiseDirty();
  }

  public void set(final double value) {
    this.value = new NtComplex(value, 0.0);
    __raiseDirty();
  }

  public void opAddTo(int x) {
    this.value = new NtComplex(value.real + x, value.imaginary);
    __raiseDirty();
  }

  public void opAddTo(long x) {
    this.value = new NtComplex(value.real + x, value.imaginary);
    __raiseDirty();
  }

  public void opAddTo(double x) {
    this.value = new NtComplex(value.real + x, value.imaginary);
    __raiseDirty();
  }

  public void opAddTo(NtComplex x) {
    this.value = new NtComplex(value.real + x.real, value.imaginary + x.imaginary);
    __raiseDirty();
  }

  public void opMultBy(double x) {
    this.value = new NtComplex(value.real * x, value.imaginary * x);
    __raiseDirty();
  }

  public void opMultBy(NtComplex x) {
    this.value = new NtComplex(value.real * x.real - value.imaginary * x.imaginary, value.imaginary * x.real + value.real * x.imaginary);
    __raiseDirty();
  }

  public void opSubFrom(NtComplex x) {
    this.value = new NtComplex(value.real - x.real, value.imaginary - x.imaginary);
    __raiseDirty();
  }
}
