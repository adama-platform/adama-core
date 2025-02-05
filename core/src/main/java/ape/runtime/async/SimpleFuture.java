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
package ape.runtime.async;

import ape.runtime.exceptions.ComputeBlockedException;
import ape.runtime.natives.NtPrincipal;

/** represents a future which holds a value which */
public class SimpleFuture<T> {
  public final String channel;
  public final NtPrincipal who;
  private final T value;

  public SimpleFuture(final String channel, final NtPrincipal who, final T value) {
    this.channel = channel;
    this.who = who;
    this.value = value;
  }

  /** the code is asking for it now */
  public T await() throws ComputeBlockedException {
    if (value != null) {
      return value;
    }
    throw new ComputeBlockedException(who, channel);
  }

  /** does the value for the future exist in the moment at this time */
  public boolean exists() {
    return value != null;
  }
}
