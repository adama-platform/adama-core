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

public class NtPrincipalTests {
  @Test
  public void comparisons() {
    final var cv1 = new NtPrincipal("a", "b");
    final var cv2 = new NtPrincipal("b", "b");
    final var cv3 = new NtPrincipal("b", "a");
    final var cv4 = new NtPrincipal("b", "c");
    Assert.assertEquals(-1, cv1.compareTo(cv2));
    Assert.assertEquals(1, cv1.compareTo(cv3));
    Assert.assertEquals(-1, cv1.compareTo(cv4));
    Assert.assertEquals(1, cv2.compareTo(cv1));
    Assert.assertEquals(1, cv2.compareTo(cv3));
    Assert.assertEquals(-1, cv2.compareTo(cv4));
    Assert.assertEquals(-1, cv3.compareTo(cv1));
    Assert.assertEquals(-1, cv3.compareTo(cv2));
    Assert.assertEquals(-2, cv3.compareTo(cv4));
    Assert.assertEquals(1, cv4.compareTo(cv1));
    Assert.assertEquals(1, cv4.compareTo(cv2));
    Assert.assertEquals(2, cv4.compareTo(cv3));
    Assert.assertEquals(0, cv1.compareTo(cv1));
    Assert.assertEquals(0, cv2.compareTo(cv2));
    Assert.assertEquals(0, cv3.compareTo(cv3));
    Assert.assertEquals(0, cv4.compareTo(cv4));
    Assert.assertFalse(cv1.equals(cv2));
    Assert.assertFalse(cv1.equals(cv3));
    Assert.assertFalse(cv1.equals(cv4));
    Assert.assertFalse(cv2.equals(cv1));
    Assert.assertFalse(cv2.equals(cv3));
    Assert.assertFalse(cv2.equals(cv4));
    Assert.assertFalse(cv3.equals(cv1));
    Assert.assertFalse(cv3.equals(cv2));
    Assert.assertFalse(cv3.equals(cv4));
    Assert.assertFalse(cv4.equals(cv1));
    Assert.assertFalse(cv4.equals(cv2));
    Assert.assertFalse(cv4.equals(cv3));
    Assert.assertTrue(cv1.equals(cv1));
    Assert.assertTrue(cv2.equals(cv2));
    Assert.assertTrue(cv3.equals(cv3));
    Assert.assertTrue(cv4.equals(cv4));
    Assert.assertFalse(cv4.equals("sys"));
  }

  @Test
  public void coverage() {
    NtPrincipal.NO_ONE.toString();
    Assert.assertEquals(4, NtPrincipal.NO_ONE.memory());
  }
}
