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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NullCallbackLatch implements Callback<Void> {
  private final CountDownLatch latch;
  private final AtomicBoolean failed;
  private final AtomicInteger failed_code;

  public NullCallbackLatch() {
    this.latch = new CountDownLatch(1);
    this.failed = new AtomicBoolean(false);
    this.failed_code = new AtomicInteger(0);
  }

  @Override
  public void success(Void value) {
    latch.countDown();
  }

  @Override
  public void failure(ErrorCodeException ex) {
    failed_code.set(ex.code);
    failed.set(true);
    latch.countDown();
  }

  public void await_success() throws Exception {
    Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
    Assert.assertFalse(failed.get());
  }

  public void await_failure() throws Exception {
    Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
    Assert.assertTrue(failed.get());
  }

  public void await_failure(int code) throws Exception {
    Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
    Assert.assertTrue(failed.get());
    Assert.assertEquals(code, failed_code.get());
  }
}
