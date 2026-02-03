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
package ape.runtime.natives;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Optional/Maybe type for representing nullable values in Adama.
 * Wraps a potentially absent value with chainable operations for assignment
 * and deletion notifications. Supports functional transformations via unpack()
 * and unpackTransfer(). Used extensively in the standard library to handle
 * absence propagation (e.g., math operations on maybe<double>).
 */
public class NtMaybe<T> {
  private Consumer<T> assignChain;
  private Runnable deleteChain;
  private T value;

  /** construct without a value */
  public NtMaybe() {
    this.value = null;
    this.deleteChain = null;
  }

  public NtMaybe(final NtMaybe<T> other) {
    this.value = null;
    if (other != null) {
      this.value = other.value;
    }
    this.deleteChain = null;
  }

  /** construct with a given value */
  public NtMaybe(final T value) {
    this.value = value;
    this.deleteChain = null;
  }

  public int compareValues(final NtMaybe<T> other, final Comparator<T> test) {
    if (value == null && other.value == null) {
      return 0;
    } else if (value == null) {
      return 1;
    } else if (other.value == null) {
      return -1;
    } else {
      return test.compare(value, other.value);
    }
  }

  public void delete() {
    this.value = null;
    if (deleteChain != null) {
      deleteChain.run();
    }
    if (assignChain != null) {
      assignChain.accept(this.value);
    }
  }

  /** get the value; note; this returns null and is not appropriate for the runtime */
  public T get() {
    return this.value;
  }

  /**
   * get the value if it is available, otherwise return the default value (appropriate for runtime)
   */
  public T getOrDefaultTo(T defaultValue) {
    if (this.value != null) {
      return this.value;
    }
    return defaultValue;
  }

  /** is it available */
  public boolean has() {
    return value != null;
  }

  public NtMaybe<T> resolve() {
    return this;
  }

  /** copy the value from another maybe */
  public void set(final NtMaybe<T> value) {
    this.value = value.value;
    if (assignChain != null) {
      assignChain.accept(this.value);
    }
  }

  /** set the value */
  public void set(final T value) {
    this.value = value;
    if (assignChain != null) {
      assignChain.accept(this.value);
    }
  }

  public NtMaybe<T> withAssignChain(final Consumer<T> assignChain) {
    this.assignChain = assignChain;
    return this;
  }

  public NtMaybe<T> withDeleteChain(final Runnable deleteChain) {
    this.deleteChain = deleteChain;
    return this;
  }

  public <O> NtMaybe<O> unpack(Function<T, O> func) {
    if (value == null) {
      return new NtMaybe<>();
    } else {
      return new NtMaybe<>(func.apply(value));
    }
  }

  public <O> NtMaybe<O> unpackTransfer(Function<T, NtMaybe<O>> func) {
    if (value == null) {
      return new NtMaybe<>();
    } else {
      return func.apply(value);
    }
  }

  @Override
  public String toString() {
    if (value != null) {
      return value.toString();
    } else {
      return "";
    }
  }
}
