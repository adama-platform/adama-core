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

public class MultiVoidCallbackLatchTests {
  @Test
  public void seven_success() {
    AtomicInteger successes = new AtomicInteger(0);
    AtomicInteger failures = new AtomicInteger(0);
    MultiVoidCallbackLatch latch = new MultiVoidCallbackLatch(new Callback<Void>() {
      @Override
      public void success(Void value) {
        successes.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        failures.incrementAndGet();
      }
    }, 7, 1000);

    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(0, failures.get());
    latch.success();
    latch.success();
    latch.success();
    latch.success();
    latch.success();
    latch.success();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(0, failures.get());
    latch.success();
    Assert.assertEquals(1, successes.get());
    Assert.assertEquals(0, failures.get());
    latch.success();
    latch.success();
    latch.success();
    Assert.assertEquals(1, successes.get());
    Assert.assertEquals(0, failures.get());
  }

  @Test
  public void six_success_one_failure() {
    AtomicInteger successes = new AtomicInteger(0);
    AtomicInteger failures = new AtomicInteger(0);
    MultiVoidCallbackLatch latch = new MultiVoidCallbackLatch(new Callback<Void>() {
      @Override
      public void success(Void value) {
        successes.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        failures.incrementAndGet();
      }
    }, 7, 1000);

    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(0, failures.get());
    latch.success();
    latch.success();
    latch.success();
    latch.success();
    latch.success();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(0, failures.get());
    latch.failure();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(1, failures.get());
    latch.success();
    latch.success();
    latch.success();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(1, failures.get());
  }


  @Test
  public void three_success_four_failure() {
    AtomicInteger successes = new AtomicInteger(0);
    AtomicInteger failures = new AtomicInteger(0);
    MultiVoidCallbackLatch latch = new MultiVoidCallbackLatch(new Callback<Void>() {
      @Override
      public void success(Void value) {
        successes.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        failures.incrementAndGet();
      }
    }, 7, 1000);

    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(0, failures.get());
    latch.success();
    latch.failure();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(1, failures.get());
    latch.failure();
    latch.failure();
    latch.success();
    latch.failure();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(1, failures.get());
    latch.success();
    latch.success();
    latch.success();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(1, failures.get());
  }

  @Test
  public void seven_failures() {
    AtomicInteger successes = new AtomicInteger(0);
    AtomicInteger failures = new AtomicInteger(0);
    MultiVoidCallbackLatch latch = new MultiVoidCallbackLatch(new Callback<Void>() {
      @Override
      public void success(Void value) {
        successes.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        failures.incrementAndGet();
      }
    }, 7, 1000);

    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(0, failures.get());
    latch.failure();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(1, failures.get());
    latch.failure();
    latch.failure();
    latch.failure();
    latch.failure();
    latch.failure();
    Assert.assertEquals(0, successes.get());
    Assert.assertEquals(1, failures.get());
  }
}
