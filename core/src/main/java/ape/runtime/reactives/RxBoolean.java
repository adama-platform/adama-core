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
 * Reactive boolean with change tracking and index support.
 * Maintains backup value for commit/revert semantics. Index value maps
 * true to 1 and false to 0, enabling efficient boolean-based table
 * filtering via ReactiveIndex. Changes trigger dirty flag propagation
 * and index update notifications.
 */
public class RxBoolean extends RxIndexableBase implements Comparable<RxBoolean>, CanGetAndSet<Boolean> {
  private boolean backup;
  private boolean value;

  public RxBoolean(final RxParent owner, final boolean value) {
    super(owner);
    backup = value;
    this.value = value;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.writeBoolean(value);
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.writeBoolean(backup);
      backup = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.writeBoolean(value);
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    backup = reader.readBoolean();
    value = backup;
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    set(reader.readBoolean());
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
    return super.__memory() + 2;
  }

  @Override
  public int compareTo(final RxBoolean other) {
    return Boolean.compare(value, other.value);
  }

  @Override
  public Boolean get() {
    return value;
  }

  @Override
  public void set(final Boolean value) {
    if (this.value != value) {
      trigger();
      this.value = value;
      trigger();
      __raiseDirty();
    }
  }

  @Override
  public int getIndexValue() {
    return value ? 1 : 0;
  }
}
