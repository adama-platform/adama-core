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
package ape.common.rate;

import ape.common.TimeSource;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class MessageRateLimiterTests {

  private static TimeSource mockTime(AtomicLong now) {
    return () -> now.get();
  }

  @Test
  public void allowsUpToLimit() {
    AtomicLong now = new AtomicLong(0);
    MessageRateLimiter limiter = new MessageRateLimiter(5, 3, mockTime(now));
    for (int i = 0; i < 5; i++) {
      Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    }
  }

  @Test
  public void rejectsWithErrorThenSilent() {
    AtomicLong now = new AtomicLong(0);
    MessageRateLimiter limiter = new MessageRateLimiter(2, 3, mockTime(now));
    // first 2 allowed
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    // next 3 should reject with error
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    // after that, silent reject
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_SILENT, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_SILENT, limiter.check());
  }

  @Test
  public void windowResetsAfterOneSecond() {
    AtomicLong now = new AtomicLong(0);
    MessageRateLimiter limiter = new MessageRateLimiter(2, 3, mockTime(now));
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    // advance 1 second
    now.set(1000);
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
  }

  @Test
  public void successResetsConsecutiveRejectCounter() {
    AtomicLong now = new AtomicLong(0);
    MessageRateLimiter limiter = new MessageRateLimiter(2, 2, mockTime(now));
    // exhaust limit
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    // 2 error rejects
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    // now silent
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_SILENT, limiter.check());
    // new window - success resets counter
    now.set(1000);
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    // reject again - should get error responses since counter was reset
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_SILENT, limiter.check());
  }

  @Test
  public void highRateLimitAllowsManyMessages() {
    AtomicLong now = new AtomicLong(0);
    MessageRateLimiter limiter = new MessageRateLimiter(1000, 10, mockTime(now));
    for (int i = 0; i < 1000; i++) {
      Assert.assertEquals(MessageRateLimiter.Result.ALLOWED, limiter.check());
    }
    Assert.assertEquals(MessageRateLimiter.Result.REJECT_WITH_ERROR, limiter.check());
  }
}
