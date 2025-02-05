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
package ape.runtime.stdlib;

import ape.runtime.natives.lists.ArrayNtList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class LibTokenTests {
  @Test
  public void prod_int() {
    ArrayList<Integer> input = new ArrayList<>();
    input.add(5);
    input.add(1);
    input.add(3);
    input.add(2);
    input.add(1);
    input.add(3);
    input.add(2);
    input.add(1);
    input.add(3);
    input.add(2);
    input.add(1);
    input.add(3);
    input.add(2);
    input.add(0);
    int[] result = LibToken.sortAndUniqueAsIntTokens(new ArrayNtList<>(input));
    Assert.assertEquals(5, result.length);
    Assert.assertEquals(0, result[0]);
    Assert.assertEquals(1, result[1]);
    Assert.assertEquals(2, result[2]);
    Assert.assertEquals(3, result[3]);
    Assert.assertEquals(5, result[4]);
  }

  @Test
  public void prod_str() {
    ArrayList<String> input = new ArrayList<>();
    input.add("X");
    input.add("Y");
    input.add("a");
    input.add("c");
    input.add("b");
    input.add("A");
    input.add("C");
    input.add("B");
    input.add("y");
    input.add("X");
    String[] result = LibToken.normalizeSortAndUniqueAsStringTokens(new ArrayNtList<>(input));
    Assert.assertEquals(5, result.length);
    Assert.assertEquals("a", result[0]);
    Assert.assertEquals("b", result[1]);
    Assert.assertEquals("c", result[2]);
    Assert.assertEquals("x", result[3]);
    Assert.assertEquals("y", result[4]);
  }

  @Test
  public void intersect_int_tokens() {
    int[] a = new int[] {1, 2, 4, 5, 7, 9};
    int[] b = new int[] {3, 5, 6, 8, 9};
    int[] c = LibToken.intersect(a, b);
    Assert.assertEquals(2, c.length);
    Assert.assertEquals(5, c[0]);
    Assert.assertEquals(9, c[1]);
  }

  @Test
  public void intersect_string_tokens() {
    String[] a = new String[] {"1", "2", "4", "5", "7", "9"};
    String[] b = new String[] {"3", "5", "6", "8", "9"};
    String[] c = LibToken.intersect(a, b);
    Assert.assertEquals(2, c.length);
    Assert.assertEquals("5", c[0]);
    Assert.assertEquals("9", c[1]);
  }
}
