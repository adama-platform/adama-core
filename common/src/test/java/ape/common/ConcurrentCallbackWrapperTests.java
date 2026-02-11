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

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentCallbackWrapperTests {

  @Test
  public void wrap_success_decrements() {
    AtomicInteger inflight = new AtomicInteger(0);
    final String[] captured = new String[1];
    Callback<String> inner = new Callback<String>() {
      @Override
      public void success(String value) {
        captured[0] = value;
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    };
    Callback<String> wrapped = ConcurrentCallbackWrapper.wrap(inflight, 2, 12345, inner);
    Assert.assertNotNull(wrapped);
    Assert.assertEquals(1, inflight.get());
    wrapped.success("hello");
    Assert.assertEquals(0, inflight.get());
    Assert.assertEquals("hello", captured[0]);
  }

  @Test
  public void wrap_failure_decrements() {
    AtomicInteger inflight = new AtomicInteger(0);
    final int[] capturedCode = new int[1];
    Callback<String> inner = new Callback<String>() {
      @Override
      public void success(String value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        capturedCode[0] = ex.code;
      }
    };
    Callback<String> wrapped = ConcurrentCallbackWrapper.wrap(inflight, 2, 12345, inner);
    Assert.assertNotNull(wrapped);
    Assert.assertEquals(1, inflight.get());
    wrapped.failure(new ErrorCodeException(999));
    Assert.assertEquals(0, inflight.get());
    Assert.assertEquals(999, capturedCode[0]);
  }

  @Test
  public void wrap_rejected_at_max() {
    AtomicInteger inflight = new AtomicInteger(5);
    final int[] capturedCode = new int[1];
    Callback<String> inner = new Callback<String>() {
      @Override
      public void success(String value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        capturedCode[0] = ex.code;
      }
    };
    Callback<String> wrapped = ConcurrentCallbackWrapper.wrap(inflight, 5, 12345, inner);
    Assert.assertNull(wrapped);
    Assert.assertEquals(5, inflight.get());
    Assert.assertEquals(12345, capturedCode[0]);
  }

  @Test
  public void wrap_rejected_above_max() {
    AtomicInteger inflight = new AtomicInteger(10);
    final int[] capturedCode = new int[1];
    Callback<String> inner = new Callback<String>() {
      @Override
      public void success(String value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        capturedCode[0] = ex.code;
      }
    };
    Callback<String> wrapped = ConcurrentCallbackWrapper.wrap(inflight, 5, 12345, inner);
    Assert.assertNull(wrapped);
    Assert.assertEquals(10, inflight.get());
    Assert.assertEquals(12345, capturedCode[0]);
  }

  @Test
  public void wrap_multiple_inflight() {
    AtomicInteger inflight = new AtomicInteger(0);
    Callback<String> noop = new Callback<String>() {
      @Override
      public void success(String value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    };
    Callback<String> w1 = ConcurrentCallbackWrapper.wrap(inflight, 3, 12345, noop);
    Assert.assertNotNull(w1);
    Assert.assertEquals(1, inflight.get());
    Callback<String> w2 = ConcurrentCallbackWrapper.wrap(inflight, 3, 12345, noop);
    Assert.assertNotNull(w2);
    Assert.assertEquals(2, inflight.get());
    Callback<String> w3 = ConcurrentCallbackWrapper.wrap(inflight, 3, 12345, noop);
    Assert.assertNotNull(w3);
    Assert.assertEquals(3, inflight.get());
    // fourth should be rejected
    final int[] capturedCode = new int[1];
    Callback<String> w4 = ConcurrentCallbackWrapper.wrap(inflight, 3, 12345, new Callback<String>() {
      @Override
      public void success(String value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        capturedCode[0] = ex.code;
      }
    });
    Assert.assertNull(w4);
    Assert.assertEquals(3, inflight.get());
    Assert.assertEquals(12345, capturedCode[0]);
    // complete one, then a new one should be accepted
    w1.success("done");
    Assert.assertEquals(2, inflight.get());
    Callback<String> w5 = ConcurrentCallbackWrapper.wrap(inflight, 3, 12345, noop);
    Assert.assertNotNull(w5);
    Assert.assertEquals(3, inflight.get());
  }
}
