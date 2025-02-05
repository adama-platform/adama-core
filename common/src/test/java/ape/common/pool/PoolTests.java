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
package ape.common.pool;

import org.junit.Assert;
import org.junit.Test;

public class PoolTests {
  @Test
  public void trivial() {
    Pool<String> p = new Pool<>();
    Assert.assertEquals(0, p.size());
    p.bumpUp();
    Assert.assertEquals(1, p.size());
    p.bumpDown();
    Assert.assertEquals(0, p.size());
    p.bumpUp();
    p.bumpUp();
    p.bumpUp();
    Assert.assertEquals(3, p.size());
    p.bumpDown();
    Assert.assertEquals(2, p.size());
    p.add("xyz");
    Assert.assertEquals("xyz", p.next());
    Assert.assertNull(p.next());
    p.add("x");
    p.add("y");
    p.add("z");
    Assert.assertEquals("x", p.next());
    Assert.assertEquals("y", p.next());
    Assert.assertEquals("z", p.next());
    Assert.assertNull(p.next());
  }
}
