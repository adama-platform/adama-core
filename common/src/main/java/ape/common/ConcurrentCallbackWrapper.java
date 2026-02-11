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
package ape.common;

import java.util.concurrent.atomic.AtomicInteger;

/** Wraps a callback to track in-flight operations via an AtomicInteger counter.
 * On creation the counter is incremented; on any terminal response (success or failure) the counter is decremented exactly once. */
public class ConcurrentCallbackWrapper<T> implements Callback<T> {
  private final Callback<T> delegate;
  private final AtomicInteger inflight;

  private ConcurrentCallbackWrapper(Callback<T> delegate, AtomicInteger inflight) {
    this.delegate = delegate;
    this.inflight = inflight;
    this.inflight.incrementAndGet();
  }

  @Override
  public void success(T value) {
    inflight.decrementAndGet();
    delegate.success(value);
  }

  @Override
  public void failure(ErrorCodeException ex) {
    inflight.decrementAndGet();
    delegate.failure(ex);
  }

  /** Attempt to wrap the given callback with in-flight tracking. If the current count is already at or above maxInflight,
   * the callback is immediately failed with the provided error code and null is returned. Otherwise a wrapped callback is returned
   * with the counter already incremented. */
  public static <T> Callback<T> wrap(AtomicInteger inflight, int maxInflight, int rejectErrorCode, Callback<T> callback) {
    if (inflight.get() >= maxInflight) {
      callback.failure(new ErrorCodeException(rejectErrorCode));
      return null;
    }
    return new ConcurrentCallbackWrapper<>(callback, inflight);
  }
}
