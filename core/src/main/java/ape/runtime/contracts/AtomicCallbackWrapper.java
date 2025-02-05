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
package ape.runtime.contracts;

import ape.common.Callback;
import ape.common.ErrorCodeException;

/** an attomic callback for swapping out the end path */
public class AtomicCallbackWrapper<T> implements Callback<T> {
  private Callback<T> ref;

  public AtomicCallbackWrapper(Callback<T> initial) {
    this.ref = initial;
  }

  @Override
  public synchronized void success(T value) {
    ref.success(value);
  }

  @Override
  public synchronized void failure(ErrorCodeException ex) {
    ref.failure(ex);
  }

  public synchronized Callback<T> set(Callback<T> v) {
    Callback<T> prior = this.ref;
    this.ref = v;
    return prior;
  }
}
