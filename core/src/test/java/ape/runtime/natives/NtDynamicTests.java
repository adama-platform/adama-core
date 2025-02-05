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

public class NtDynamicTests {
  @Test
  public void coverage() {
    NtDynamic T = new NtDynamic("true");
    NtDynamic F = new NtDynamic("false");
    Assert.assertEquals("true", T.toString());
    Assert.assertEquals("false", F.toString());
    Assert.assertEquals(14, T.compareTo(F));
    Assert.assertEquals(-14, F.compareTo(T));
    Assert.assertTrue(T.equals(T));
    Assert.assertTrue(F.equals(F));
    Assert.assertTrue(T.equals(new NtDynamic("true")));
    Assert.assertTrue(F.equals(new NtDynamic("false")));
    Assert.assertFalse(T.equals(F));
    Assert.assertFalse(F.equals(T));
    Assert.assertFalse(F.equals(false));
    T.hashCode();
    Assert.assertTrue(NtDynamic.NULL.equals(new NtDynamic("null")));
    Assert.assertEquals(10, F.memory());
    Assert.assertEquals(T, T.to_dynamic());
    Assert.assertTrue(T == T.to_dynamic());
  }

  @Test
  public void cache() {
    NtDynamic T = new NtDynamic("{}");
    Assert.assertTrue(T.cached() == T.cached());
  }
}
