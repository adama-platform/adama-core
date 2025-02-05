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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CallbackTests {

  @Test
  public void sanity() {
    MockCallback<Integer> callback = new MockCallback<Integer>();
    callback.success(50);
    Assert.assertEquals(50, (int) callback.result);
  }

  private void waitFor(ScheduledExecutorService executor) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    executor.execute(() -> latch.countDown());
    latch.await(1000, TimeUnit.MILLISECONDS);
  }

  @Test
  public void transform() {
    MockCallback<Integer> callback = new MockCallback<Integer>();
    callback.success(50);
    Assert.assertEquals(50, (int) callback.result);
    Callback<Integer> t = Callback.transform(callback, 5, (x) -> x * x);
    t.success(10);
    Assert.assertEquals(100, (int) callback.result);
    t.failure(new ErrorCodeException(15, new Exception()));
  }

  @Test
  public void transform_failure() {
    MockCallback<Integer> callback = new MockCallback<Integer>();
    callback.success(50);
    Assert.assertEquals(50, (int) callback.result);
    Callback<Integer> t = Callback.transform(callback, 5, (x) -> {
      throw new NullPointerException();
    });
    t.success(10);
    Assert.assertEquals(5, callback.exception.code);
  }

  @Test
  public void handoff() {
    MockCallback<Integer> callback = new MockCallback<Integer>();
    callback.success(50);
    Assert.assertEquals(50, (int) callback.result);
    AtomicInteger i = new AtomicInteger(0);
    Callback<Void> t = Callback.handoff(callback, 5, () -> {
      i.set(42);
    });
    t.failure(new ErrorCodeException(15, new RuntimeException()));
    t.success(null);
    Assert.assertEquals(50, (int) callback.result);
    Assert.assertEquals(42, i.get());
  }

  @Test
  public void handoff_crash() {
    MockCallback<Integer> callback = new MockCallback<Integer>();
    callback.success(50);
    Assert.assertEquals(50, (int) callback.result);
    AtomicInteger i = new AtomicInteger(0);
    Callback<Void> t = Callback.handoff(callback, 5, () -> {
      throw new NullPointerException();
    });
    t.success(null);
    Assert.assertEquals(5, callback.exception.code);
  }

  @Test
  public void dontcare() {
    Callback.DONT_CARE_INTEGER.success(123);
    Callback.DONT_CARE_INTEGER.failure(new ErrorCodeException(123));
    Callback.DONT_CARE_VOID.success(null);
    Callback.DONT_CARE_VOID.failure(new ErrorCodeException(123));
    Callback.DONT_CARE_STRING.success("xyz");
    Callback.DONT_CARE_STRING.failure(new ErrorCodeException(123));
  }

  @Test
  public void throwaway() {
    AtomicInteger s = new AtomicInteger(0);
    AtomicInteger f = new AtomicInteger(0);
    Callback<Integer> xyz = Callback.SUCCESS_OR_FAILURE_THROW_AWAY_VALUE(new Callback<Void>() {
      @Override
      public void success(Void value) {
        s.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        f.incrementAndGet();
      }
    });
    Assert.assertEquals(0, s.get());
    xyz.success(123);
    Assert.assertEquals(1, s.get());
    Assert.assertEquals(0, f.get());
    xyz.failure(new ErrorCodeException(12));
    Assert.assertEquals(1, f.get());
  }

  public class MockCallback<T> implements Callback<T> {
    public T result = null;
    public ErrorCodeException exception;

    @Override
    public void success(T value) {
      result = value;
    }

    @Override
    public void failure(ErrorCodeException ex) {
      exception = ex;
    }
  }
}
