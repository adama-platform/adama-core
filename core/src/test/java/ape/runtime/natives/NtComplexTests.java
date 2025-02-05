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

public class NtComplexTests {
  @Test
  public void equals() {
    NtComplex a = new NtComplex(1.2, 3.4);
    NtComplex b = new NtComplex(1.2, 3.4);
    NtComplex c = new NtComplex(3.4, -1.2);
    Assert.assertEquals(a, a);
    Assert.assertEquals(a, b);
    Assert.assertNotEquals(a, c);
    Assert.assertNotEquals(a, "z");
    Assert.assertNotEquals("z", a);
    Assert.assertEquals(-25689151, a.hashCode());
  }

  @Test
  public void compare() {
    NtComplex a = new NtComplex(1.2, 3.4);
    NtComplex b = new NtComplex(1.2, 3.4);
    NtComplex c = new NtComplex(3.4, -1.2);
    Assert.assertEquals(a.compareTo(b), -b.compareTo(a));
    Assert.assertEquals(a.compareTo(c), -c.compareTo(a));
    Assert.assertEquals(0, a.compareTo(a));
  }

  @Test
  public void str() {
    NtComplex a = new NtComplex(1.2, 3.4);
    Assert.assertEquals("1.2 3.4i", a.toString());
    Assert.assertEquals(16, a.memory());
    Assert.assertEquals("0.09230769230769231 -0.26153846153846155i", a.recip().toString());
  }

  @Test
  public void zero() {
    Assert.assertTrue(new NtComplex(0.0, 0.0).zero());
  }
}
