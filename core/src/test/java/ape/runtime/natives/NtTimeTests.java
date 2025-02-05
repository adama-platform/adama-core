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

public class NtTimeTests {
  @Test
  public void coverage() {
    NtTime t = new NtTime(12, 17);
    Assert.assertEquals(t, t);
    Assert.assertEquals(t, new NtTime(12, 17));
    Assert.assertNotEquals(t, new NtTime(1, 4));
    Assert.assertNotEquals(t, null);
    Assert.assertNotEquals(t, "");
    t.hashCode();
    Assert.assertEquals("12:17", t.toString());
  }

  @Test
  public void toint() {
    NtTime t = new NtTime(12, 17);
    Assert.assertEquals(737, t.toInt());
  }

  @Test
  public void pad() {
    NtTime t = new NtTime(12, 1);
    Assert.assertEquals("12:01", t.toString());
  }

  @Test
  public void compare() {
    NtTime t1 = new NtTime(12, 1);
    NtTime t2 = new NtTime(12, 5);
    NtTime t3 = new NtTime(11, 1);
    NtTime t4 = new NtTime(13, 1);
    Assert.assertTrue(t1.compareTo(t2) < 0);
    Assert.assertTrue(t2.compareTo(t1) > 0);
    Assert.assertTrue(t1.compareTo(t4) < 0);
    Assert.assertTrue(t3.compareTo(t1) < 0);
  }
}
