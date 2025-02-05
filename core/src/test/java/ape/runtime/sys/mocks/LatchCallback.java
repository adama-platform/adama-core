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
package ape.runtime.sys.mocks;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LatchCallback implements Callback<Integer> {

  private CountDownLatch latch;
  private int value;
  private ErrorCodeException ex;

  public LatchCallback() {
    this.latch = new CountDownLatch(1);
    this.value = 0;
    this.ex = null;
  }

  public Callback<Boolean> toBool(int trueValue, int falseValue) {
    return new Callback<>() {
      @Override
      public void success(Boolean value) {
        LatchCallback.this.success(value ? trueValue : falseValue);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        LatchCallback.this.failure(ex);
      }
    };
  }

  @Override
  public void success(Integer value) {
    this.value = value;
    latch.countDown();
  }

  @Override
  public void failure(ErrorCodeException ex) {
    this.ex = ex;
    latch.countDown();
  }

  public void await_success(int value) {
    try {
      Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
      Assert.assertEquals(value, this.value);
    } catch (InterruptedException ie) {
      Assert.fail();
    }
  }

  public void awaitJustSuccess() {
    try {
      Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
      Assert.assertNull(ex);
    } catch (InterruptedException ie) {
      Assert.fail();
    }
  }

  public void await_failure(int code) {
    try {
      Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
      Assert.assertEquals(code, ex.code);
    } catch (InterruptedException ie) {
      Assert.fail();
    }
  }
}
