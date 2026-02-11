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

/** For when you need to be able to cancel something but a race may require handing off the signal */
public class AtomicSimpleCancel implements SimpleCancel {
  private SimpleCancel value;
  private boolean cancelled;

  public AtomicSimpleCancel() {
    this.value = null;
    this.cancelled = false;
  }

  private synchronized SimpleCancel atomicCancel() {
    this.cancelled = true;
    if (this.value != null) {
      return this.value;
    }
    return null;
  }


  public synchronized boolean atomicSet(SimpleCancel value) {
    this.value = value;
    return cancelled;
  }

  @Override
  public void cancel() {
    SimpleCancel toCancel = atomicCancel();
    if (toCancel != null) {
      toCancel.cancel();
    }
  }

  public void set(SimpleCancel value) {
    if (atomicSet(value)) {
      value.cancel();
    }
  }
}
