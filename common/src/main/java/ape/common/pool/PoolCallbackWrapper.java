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
package ape.common.pool;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Living;

/** adapt an existing callback to register success/failure signals onto a PoolItem */
public class PoolCallbackWrapper<X, S extends Living> implements Callback<X> {
  private final Callback<X> wrapped;
  private final PoolItem<S> item;

  public PoolCallbackWrapper(Callback<X> wrapped, PoolItem<S> item) {
    this.wrapped = wrapped;
    this.item = item;
  }

  @Override
  public void success(X value) {
    try {
      wrapped.success(value);
    } finally {
      item.returnToPool();
    }
  }

  @Override
  public void failure(ErrorCodeException ex) {
    try {
      wrapped.failure(ex);
    } finally {
      item.signalFailure();
    }
  }
}
