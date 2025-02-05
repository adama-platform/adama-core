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

import java.util.Iterator;

public class NtMapTests {
  @Test
  public void flow() {
    final var map = new NtMap<Integer, Integer>();
    final var ptr = map.lookup(42);
    Assert.assertEquals(0, map.size());
    ptr.set(100);
    Assert.assertEquals(1, map.size());
    final var it = map.iterator();
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals(100, (int) it.next().value);
    Assert.assertFalse(it.hasNext());
    final var copy = new NtMap<>(map);
    Assert.assertEquals(1, copy.size());
    final var copy2 = new NtMap<Integer, Integer>();
    copy2.set(copy);
    Assert.assertEquals(1, copy2.size());
    final var copy3 = new NtMap<Integer, Integer>();
    copy3.insert(copy);
    Assert.assertEquals(1, copy3.size());
    ptr.delete();
    Assert.assertEquals(0, map.size());
    map.put(1000, 40);
    Assert.assertEquals(1, map.size());
    Assert.assertTrue(map.has(1000));
    Assert.assertEquals(40, (int) map.lookup(1000).get());
    map.entries();
    Assert.assertEquals(40, (int) map.remove(1000).getOrDefaultTo(-100));
    map.clear();
    Assert.assertEquals(0, map.size());
  }

  @Test
  public void ordering() {
    final var map = new NtMap<Integer, Integer>();
    Assert.assertFalse(map.min().has());
    Assert.assertFalse(map.max().has());
    map.put(1, 2);
    map.put(5, 6);
    map.put(3, 4);
    Assert.assertEquals(2, (int) map.min().get().value);
    Assert.assertEquals(6, (int) map.max().get().value);
    Iterator<NtPair<Integer, Integer>> it = map.iterator();
    var f1 = it.next();
    var f2 = it.next();
    var f3 = it.next();
    Assert.assertFalse(it.hasNext());
    Assert.assertEquals(1, (int) f1.key);
    Assert.assertEquals(3, (int) f2.key);
    Assert.assertEquals(5, (int) f3.key);
  }
}
