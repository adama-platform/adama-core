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

public class TimeSourceTests {
  @Test
  public void realTimeReturnsPositive() {
    long now = TimeSource.REAL_TIME.nowMilliseconds();
    Assert.assertTrue(now > 0);
  }

  @Test
  public void realTimeMonotonicallyIncreasing() {
    long t1 = TimeSource.REAL_TIME.nowMilliseconds();
    long t2 = TimeSource.REAL_TIME.nowMilliseconds();
    Assert.assertTrue(t2 >= t1);
  }

  @Test
  public void customTimeSource() {
    TimeSource custom = () -> 42L;
    Assert.assertEquals(42L, custom.nowMilliseconds());
  }
}
