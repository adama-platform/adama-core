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

public class PairTests {
  @Test
  public void construction() {
    Pair<String> pair = new Pair<>("a", "b");
    Assert.assertEquals("a", pair.x);
    Assert.assertEquals("b", pair.y);
  }

  @Test
  public void swap() {
    Pair<String> pair = new Pair<>("a", "b");
    Pair<String> swapped = pair.swap();
    Assert.assertEquals("b", swapped.x);
    Assert.assertEquals("a", swapped.y);
  }

  @Test
  public void equalsAndHashCode() {
    Pair<String> p1 = new Pair<>("a", "b");
    Pair<String> p2 = new Pair<>("a", "b");
    Pair<String> p3 = new Pair<>("b", "a");
    Assert.assertEquals(p1, p2);
    Assert.assertEquals(p1.hashCode(), p2.hashCode());
    Assert.assertNotEquals(p1, p3);
  }

  @Test
  public void equalsNull() {
    Pair<String> p1 = new Pair<>("a", "b");
    Assert.assertNotEquals(p1, null);
  }

  @Test
  public void equalsDifferentType() {
    Pair<String> p1 = new Pair<>("a", "b");
    Assert.assertNotEquals(p1, "not a pair");
  }

  @Test
  public void toStringWorks() {
    Pair<String> pair = new Pair<>("hello", "world");
    String result = pair.toString();
    Assert.assertNotNull(result);
    Assert.assertTrue(result.contains("hello"));
    Assert.assertTrue(result.contains("world"));
  }

  @Test
  public void integerPair() {
    Pair<Integer> pair = new Pair<>(1, 2);
    Assert.assertEquals(Integer.valueOf(1), pair.x);
    Assert.assertEquals(Integer.valueOf(2), pair.y);
    Pair<Integer> swapped = pair.swap();
    Assert.assertEquals(Integer.valueOf(2), swapped.x);
    Assert.assertEquals(Integer.valueOf(1), swapped.y);
  }

  @Test
  public void nullValues() {
    Pair<String> p1 = new Pair<>(null, null);
    Pair<String> p2 = new Pair<>(null, null);
    Assert.assertEquals(p1, p2);
    Assert.assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void swapPreservesEquality() {
    Pair<String> pair = new Pair<>("x", "x");
    Pair<String> swapped = pair.swap();
    Assert.assertEquals(pair, swapped);
  }
}
