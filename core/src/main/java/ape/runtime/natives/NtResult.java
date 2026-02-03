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

import ape.ErrorCodes;
import ape.runtime.exceptions.ComputeBlockedException;

/**
 * Async operation result container with success/failure/pending states.
 * Wraps a value with failure code and message for service call results.
 * The await() method blocks document execution (throws ComputeBlockedException)
 * until the result is available or failed. The finished() check returns
 * true when either a value exists or a failure occurred.
 */
public class NtResult<T> {
  private final T value;
  private final boolean failed;
  private final int failureCode;
  private final String message;

  public NtResult(final T value, boolean failed, int failureCode, String message) {
    this.value = value;
    this.failed = failed;
    this.failureCode = failureCode;
    this.message = failed ? message : (value != null ? "OK" : "waiting...");
  }

  public NtResult(NtResult<T> other) {
    this.value = other.value;
    this.failed = other.failed;
    this.failureCode = other.failureCode;
    this.message = other.message;
  }

  /** get the value; note; this returns null and is not appropriate for the runtime */
  public T get() {
    return this.value;
  }

  /** is it available */
  public boolean has() {
    return value != null;
  }

  /** are we in a failure state */
  public boolean failed() {
    return failed;
  }

  /** get the message about the progress */
  public String message() {
    return message;
  }

  /** the failure code of the result */
  public int code() {
    return failureCode;
  }

  public NtMaybe<T> await() {
    boolean retry = failed && failureCode == ErrorCodes.DOCUMENT_NOT_READY;
    if (!finished() || retry) {
      throw new ComputeBlockedException();
    }
    return as_maybe();
  }

  /** is the result a failure */
  public boolean finished() {
    return value != null || failed;
  }

  public NtMaybe<T> as_maybe() {
    if (this.value != null) {
      return new NtMaybe<>(value);
    } else {
      return new NtMaybe<>();
    }
  }
}
