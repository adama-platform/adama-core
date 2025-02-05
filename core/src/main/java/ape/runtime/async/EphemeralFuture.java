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

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;

/** Ephemeral futures connect two ends together with a tiny state machine */
public class EphemeralFuture<T> {
  private boolean done;
  private boolean cancel;
  private T result;
  private Callback<T> callback;
  private Integer error;

  public EphemeralFuture() {
    this.done = false;
    this.cancel = false;
    this.result = null;
    this.callback = null;
  }

  /** send the future an object */
  public void send(T result) {
    boolean ship;
    synchronized (this) {
      this.result = result;
      ship = callback != null && result != null && !done;
      if (ship) {
        done = true;
      }
    }
    if (ship) {
      callback.success(result);
    }
  }

  public void abort(int errorCode) {
    boolean ship;
    synchronized (this) {
      error = errorCode;
      ship = callback != null && !done;
      if (ship) {
        done = true;
      }
    }
    if (ship) {
      callback.failure(new ErrorCodeException(errorCode));
    }
  }

  /** attach a callback */
  public void attach(Callback<T> callback) {
    boolean ship;
    boolean kill = false;
    boolean abort = false;
    synchronized (this) {
      this.callback = callback;
      ship = callback != null && result != null && !done;
      if (cancel) {
        kill = true;
        ship = false;
      }
      if (error != null) {
        abort = true;
        ship = false;
      }
      if (ship) {
        done = true;
      }
    }
    if (ship) {
      callback.success(result);
    }
    if (abort) {
      callback.failure(new ErrorCodeException(error));
    }
    if (kill) {
      kill();
    }
  }

  /** internal: send the failure out */
  private void kill() {
    callback.failure(new ErrorCodeException(ErrorCodes.TASK_CANCELLED));
  }

  /** cancel the future */
  public void cancel() {
    boolean kill;
    synchronized (this) {
      kill = callback != null && !done;
      done = true;
      cancel = true;
    }
    if (kill) {
      kill();
    }
  }
}
