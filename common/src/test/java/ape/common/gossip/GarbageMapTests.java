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
package ape.common.gossip;

import org.junit.Assert;
import org.junit.Test;

public class GarbageMapTests {
  @Test
  public void trim() {
    GarbageMap<String> map = new GarbageMap<>(10);
    for (int k = 0; k < 100; k++) {
      map.put("k+" + k, "v:" + k, 0);
    }
    Assert.assertEquals(10, map.size());
  }

  @Test
  public void flow() {
    GarbageMap<String> map = new GarbageMap<>(10);
    Assert.assertEquals(0, map.keys().size());
    map.put("x", "f(x)", 0);
    Assert.assertEquals(1, map.keys().size());
    Assert.assertEquals("f(x)", map.get("x"));
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("f(x)", map.remove("x"));
    Assert.assertEquals(0, map.size());
    map.put("x", "f(x)", 0);
    Assert.assertEquals(0, map.gc(0));
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("f(x)", map.get("x"));
    Assert.assertEquals(0, map.gc(Constants.MILLISECONDS_TO_SIT_IN_GARBAGE_MAP - 1));
    Assert.assertEquals(1, map.keys().size());
    Assert.assertEquals("f(x)", map.get("x"));
    Assert.assertEquals(0, map.gc(Constants.MILLISECONDS_TO_SIT_IN_GARBAGE_MAP));
    Assert.assertEquals(1, map.keys().size());
    Assert.assertEquals("f(x)", map.get("x"));
    Assert.assertEquals(1, map.gc(Constants.MILLISECONDS_TO_SIT_IN_GARBAGE_MAP + 1));
    Assert.assertEquals(0, map.keys().size());
    Assert.assertNull(map.get("x"));
    Assert.assertNull(map.remove("x"));
  }
}
