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
 * Reactive string with change tracking and index support.
 * Maintains backup value for commit/revert semantics. Supports string
 * concatenation via opAddTo() with proper dirty flag propagation and
 * index trigger notification. Index value is the string's hashCode(),
 * enabling efficient equality-based lookups in ReactiveIndex.
 */
public class RxString extends RxIndexableBase implements Comparable<RxString>, CanGetAndSet<String> {
  protected String backup;
  protected String value;

  public RxString(final RxParent owner, final String value) {
    super(owner);
    backup = value;
    this.value = value;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.writeString(value);
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.writeString(backup);
      backup = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.writeString(value);
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    backup = reader.readString();
    value = backup;
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    set(reader.readString());
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
    return super.__memory() + (backup.length() + value.length()) * 2L + 16;
  }

  @Override
  public int compareTo(final RxString other) {
    return value.compareTo(other.value);
  }

  public int compareToIgnoreCase(final RxString other) {
    return value.compareToIgnoreCase(other.value);
  }

  @Override
  public String get() {
    return value;
  }

  @Override
  public void set(final String value) {
    if (this.value != null && this.value.equals(value)) {
      return;
    }
    trigger();
    this.value = value;
    trigger();
    __raiseDirty();
  }


  @Override
  public int getIndexValue() {
    return value.hashCode();
  }

  public boolean has() {
    return !value.isEmpty();
  }

  public String opAddTo(final Object incoming) {
    trigger();
    value += incoming.toString();
    trigger();
    __raiseDirty();
    return value;
  }
}
