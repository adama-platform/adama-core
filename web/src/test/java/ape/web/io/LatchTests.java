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

import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import org.junit.Test;

public class LatchTests {

  @Test
  public void basic() {
    MockIntegerCallback callback = new MockIntegerCallback();
    BulkLatch<Integer> bl = new BulkLatch<>(SimpleExecutor.NOW, 1, callback);
    bl.with(() -> 42);
    bl.countdown(null);
    callback.assertValue(42);
  }

  @Test
  public void withRef() {
    MockIntegerCallback callback = new MockIntegerCallback();
    BulkLatch<Integer> bl = new BulkLatch<>(SimpleExecutor.NOW, 2, callback);
    LatchRefCallback<Integer> ref1 = new LatchRefCallback<>(bl);
    LatchRefCallback<Integer> ref2 = new LatchRefCallback<>(bl);
    bl.with(() -> ref1.get() + ref2.get());
    ref1.success(50);
    ref2.success(32);
    callback.assertValue(82);
  }

  @Test
  public void failure1() {
    MockIntegerCallback callback = new MockIntegerCallback();
    BulkLatch<Integer> bl = new BulkLatch<>(SimpleExecutor.NOW, 2, callback);
    LatchRefCallback<Integer> ref1 = new LatchRefCallback<>(bl);
    LatchRefCallback<Integer> ref2 = new LatchRefCallback<>(bl);
    bl.with(() -> ref1.get() + ref2.get());
    ref1.failure(new ErrorCodeException(2));
    ref2.success(32);
    callback.assertErrorCode(2);
  }

  @Test
  public void failure2() {
    MockIntegerCallback callback = new MockIntegerCallback();
    BulkLatch<Integer> bl = new BulkLatch<>(SimpleExecutor.NOW, 2, callback);
    LatchRefCallback<Integer> ref1 = new LatchRefCallback<>(bl);
    LatchRefCallback<Integer> ref2 = new LatchRefCallback<>(bl);
    bl.with(() -> ref1.get() + ref2.get());
    ref1.success(32);
    ref2.failure(new ErrorCodeException(7));
    callback.assertErrorCode(7);
  }

  @Test
  public void failureBoth1() {
    MockIntegerCallback callback = new MockIntegerCallback();
    BulkLatch<Integer> bl = new BulkLatch<>(SimpleExecutor.NOW, 2, callback);
    LatchRefCallback<Integer> ref1 = new LatchRefCallback<>(bl);
    LatchRefCallback<Integer> ref2 = new LatchRefCallback<>(bl);
    bl.with(() -> ref1.get() + ref2.get());
    ref1.failure(new ErrorCodeException(2));
    ref2.failure(new ErrorCodeException(7));
    callback.assertErrorCode(2);
  }

  @Test
  public void failureBoth2() {
    MockIntegerCallback callback = new MockIntegerCallback();
    BulkLatch<Integer> bl = new BulkLatch<>(SimpleExecutor.NOW, 2, callback);
    LatchRefCallback<Integer> ref1 = new LatchRefCallback<>(bl);
    LatchRefCallback<Integer> ref2 = new LatchRefCallback<>(bl);
    bl.with(() -> ref1.get() + ref2.get());
    ref2.failure(new ErrorCodeException(7));
    ref1.failure(new ErrorCodeException(2));
    callback.assertErrorCode(2);
  }
}
