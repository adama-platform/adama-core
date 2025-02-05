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

/** a latch for pumping a callback */
public class MultiVoidCallbackLatch {
  private final Callback<Void> callback;
  private int remaining;
  private final int errorCode;
  private boolean failed;

  public MultiVoidCallbackLatch(Callback<Void> callback, int remaining, int errorCode) {
    this.callback = callback;
    this.remaining = remaining;
    this.errorCode = errorCode;
    this.failed = false;
  }

  private synchronized boolean atomicCuccess() {
    if (failed) {
      return false;
    }
    remaining--;
    return remaining == 0;
  }

  public void success() {
    if (atomicCuccess()) {
      callback.success(null);
    }
  }

  private synchronized boolean atomicFailure() {
    if (failed) {
      return false;
    }
    failed = true;
    return true;
  }

  public void failure() {
    if (atomicFailure()) {
      callback.failure(new ErrorCodeException(errorCode));
    }
  }
}
