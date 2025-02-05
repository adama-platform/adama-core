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

public class NtAssetTests {
  @Test
  public void flow() {
    NtAsset a = new NtAsset("123", "name", "png", 42, "hash", "sheesh");
    NtAsset b = new NtAsset("42", "name", "png", 42, "hash", "sheesh");

    Assert.assertEquals(-3, a.compareTo(b));
    Assert.assertEquals(3, b.compareTo(a));
    Assert.assertEquals(-973748297, a.hashCode());
    Assert.assertTrue(a.equals(a));
    Assert.assertFalse(a.equals(""));
    Assert.assertFalse(a.equals(b));
    Assert.assertEquals("123", a.id());
    Assert.assertEquals("name", a.name());
    Assert.assertEquals("png", a.type());
    Assert.assertEquals(42, a.size());
    Assert.assertTrue(a.valid());
    Assert.assertEquals(88, a.memory());
    Assert.assertEquals("hash", a.md5());
    Assert.assertEquals("sheesh", a.sha384());
  }
}
