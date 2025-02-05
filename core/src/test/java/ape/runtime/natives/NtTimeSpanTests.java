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
package ape.runtime.natives;

import org.junit.Assert;
import org.junit.Test;

public class NtTimeSpanTests {
  @Test
  public void coverage() {
    NtTimeSpan ts = new NtTimeSpan(123);
    Assert.assertEquals(ts, ts);
    Assert.assertEquals(ts, new NtTimeSpan(123));
    Assert.assertNotEquals(ts, new NtTimeSpan(423));
    Assert.assertNotEquals(ts, "");
    Assert.assertNotEquals(ts, null);
    ts.hashCode();
    Assert.assertEquals("123.0 sec", ts.toString());
    Assert.assertEquals(24, ts.memory());
  }

  @Test
  public void compare() {
    NtTimeSpan a = new NtTimeSpan(1);
    NtTimeSpan b = new NtTimeSpan(54);
    Assert.assertTrue(a.compareTo(b) < 0);
    Assert.assertTrue(b.compareTo(a) > 0);
    Assert.assertEquals(0, a.compareTo(a));
  }
}
