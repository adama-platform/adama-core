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

public class VolumeRateLimiterTests {

  private static TimeSource mockTime(AtomicLong now) {
    return () -> now.get();
  }

  @Test
  public void basicAllowAndReject() {
    AtomicLong now = new AtomicLong(0);
    // 16KB per second
    VolumeRateLimiter limiter = new VolumeRateLimiter(16384, 1000, mockTime(now));
    // first 16KB should be allowed
    Assert.assertTrue(limiter.consume("ip1", 8192));
    Assert.assertTrue(limiter.consume("ip1", 8192));
    // next byte should be rejected
    Assert.assertFalse(limiter.consume("ip1", 1));
  }

  @Test
  public void differentKeysAreIndependent() {
    AtomicLong now = new AtomicLong(0);
    VolumeRateLimiter limiter = new VolumeRateLimiter(1000, 1000, mockTime(now));
    Assert.assertTrue(limiter.consume("ip1", 1000));
    Assert.assertFalse(limiter.consume("ip1", 1));
    // different key should still have budget
    Assert.assertTrue(limiter.consume("ip2", 1000));
  }

  @Test
  public void windowResets() {
    AtomicLong now = new AtomicLong(0);
    VolumeRateLimiter limiter = new VolumeRateLimiter(1000, 1000, mockTime(now));
    Assert.assertTrue(limiter.consume("ip1", 1000));
    Assert.assertFalse(limiter.consume("ip1", 1));
    // advance past the window
    now.set(1001);
    Assert.assertTrue(limiter.consume("ip1", 1000));
  }

  @Test
  public void partialDrain() {
    AtomicLong now = new AtomicLong(0);
    VolumeRateLimiter limiter = new VolumeRateLimiter(1000, 1000, mockTime(now));
    Assert.assertTrue(limiter.consume("ip1", 1000));
    Assert.assertFalse(limiter.consume("ip1", 1));
    // advance half the window - should drain 500 bytes worth
    now.set(500);
    Assert.assertTrue(limiter.consume("ip1", 500));
    Assert.assertFalse(limiter.consume("ip1", 1));
  }

  @Test
  public void gcRemovesStaleEntries() {
    AtomicLong now = new AtomicLong(0);
    VolumeRateLimiter limiter = new VolumeRateLimiter(1000, 1000, mockTime(now));
    limiter.consume("ip1", 100);
    limiter.consume("ip2", 100);
    // advance past 2x window
    now.set(2001);
    limiter.gc();
    // entries should be removed; new consume on ip1 should work fresh
    Assert.assertTrue(limiter.consume("ip1", 1000));
  }

  @Test
  public void gcKeepsRecentEntries() {
    AtomicLong now = new AtomicLong(0);
    VolumeRateLimiter limiter = new VolumeRateLimiter(1000, 1000, mockTime(now));
    limiter.consume("ip1", 100);
    // advance less than 2x window
    now.set(500);
    limiter.gc();
    // entry should still be there (not removed)
    Assert.assertTrue(limiter.consume("ip1", 900));
  }
}
