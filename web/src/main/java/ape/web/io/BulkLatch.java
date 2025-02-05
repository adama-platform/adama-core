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
package ape.web.io;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;

import java.util.function.Supplier;

/**
 * This is a latch which has N outstanding tasks. Once all the tasks are complete, the supplier is
 * called, and the latch fires the callback. Any failures result in a failure of the lowest code.
 */
public class BulkLatch<T> {
  public final SimpleExecutor executor;
  public final Callback<T> callback;

  public Supplier<T> supply;
  public int outstanding;
  private Integer errorCode;

  public BulkLatch(SimpleExecutor executor, int outstanding, Callback<T> callback) {
    this.executor = executor;
    this.outstanding = outstanding;
    this.callback = callback;
    this.supply = null;
    this.errorCode = null;
  }

  /**
   * we have a split constructor since the latch is going to be defined prior to a complete
   * supplier. The LatchRefCallback is used before the with() call
   */
  public void with(Supplier<T> supply) {
    this.supply = supply;
  }

  /**
   * a service completed either successfully (newErrorCode == null) or not (newErrorCode != null)
   */
  public void countdown(Integer newErrorCode) {
    executor.execute(new NamedRunnable("bulk-latch") {
      @Override
      public void execute() throws Exception {
        // something bad happened
        if (newErrorCode != null) {
          // absorb the error code
          if (errorCode == null) {
            errorCode = newErrorCode;
          } else {
            // if conflicts, pick the smallest error code
            if (newErrorCode < errorCode) {
              errorCode = newErrorCode;
            }
          }
        }
        outstanding--;
        if (outstanding == 0) {
          if (errorCode == null) {
            T value = supply.get();
            callback.success(value);
          } else {
            callback.failure(new ErrorCodeException(errorCode));
          }
        }
      }
    });
  }
}
