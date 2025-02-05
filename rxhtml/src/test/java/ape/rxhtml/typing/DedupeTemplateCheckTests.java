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
package ape.rxhtml.typing;

import org.junit.Assert;
import org.junit.Test;

public class DedupeTemplateCheckTests {

  @Test
  public void sanity() {
    DedupeTemplateCheck a = new DedupeTemplateCheck("a", "b", "x");
    DedupeTemplateCheck b = new DedupeTemplateCheck("c", "d", "x");
    DedupeTemplateCheck c = new DedupeTemplateCheck("a", "e", "x");
    Assert.assertEquals(126166, a.hashCode());
    Assert.assertEquals(128150, b.hashCode());
    Assert.assertEquals(126259, c.hashCode());
    Assert.assertEquals(a, a);
    Assert.assertNotEquals(a, b);
    Assert.assertNotEquals(a, c);
    Assert.assertEquals(a, new DedupeTemplateCheck("a", "b", "x"));
    Assert.assertNotEquals(a, new DedupeTemplateCheck("a", "b", "y"));
    Assert.assertNotEquals(a, null);
    Assert.assertNotEquals(a, "xyz");
    Assert.assertEquals(-2, a.compareTo(b));
    Assert.assertEquals(2, b.compareTo(c));
    Assert.assertEquals(3, c.compareTo(a));
    Assert.assertEquals(-1, a.compareTo(new DedupeTemplateCheck("a", "b", "y")));
  }
}
