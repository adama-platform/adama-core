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

public class PathingTests {

  @Test
  public void normal() {
    Assert.assertEquals("x/y", Pathing.normalize("x\\y"));
  }

  @Test
  public void max_suffix() {
    Assert.assertEquals("", Pathing.maxSharedSuffix("x/y/u", "z/y/v"));
    Assert.assertEquals("y", Pathing.maxSharedSuffix("x/y", "z/y"));
    Assert.assertEquals("y/x", Pathing.maxSharedSuffix("x/y/x", "z/y/x"));
    Assert.assertEquals("abc/def/ghi", Pathing.maxSharedSuffix("x/abc/def/ghi", "z/abc/def/ghi"));
  }

  @Test
  public void max_prefix() {
    Assert.assertEquals("", Pathing.maxSharedPrefix("x/y/u", "z/y/v"));
    Assert.assertEquals("y", Pathing.maxSharedPrefix("y/1", "y/2/3"));
    Assert.assertEquals("x/y", Pathing.maxSharedPrefix("x/y/z", "x/y/x"));
    Assert.assertEquals("abc/def/ghi", Pathing.maxSharedPrefix("abc/def/ghi/y", "abc/def/ghi/x"));
  }

  @Test
  public void remove_last() {
    Assert.assertEquals("x", Pathing.removeLast("x"));
    Assert.assertEquals("x", Pathing.removeLast("x/y"));
    Assert.assertEquals("x/y", Pathing.removeLast("x/y/z"));
  }

  @Test
  public void remove_common() {
    Assert.assertEquals("/z", Pathing.removeCommonRootFromB("x/y", "x/y/z"));
    Assert.assertEquals("x/y/z", Pathing.removeCommonRootFromB("z", "x/y/z")); // nothing in common
  }
}
