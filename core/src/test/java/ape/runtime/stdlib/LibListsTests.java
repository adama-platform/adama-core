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

import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.lists.ArrayNtList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class LibListsTests {
  @Test
  public void flatten() {
    ArrayList<NtList<Integer>> x = new ArrayList<>();
    {
      ArrayList<Integer> z = new ArrayList<>();
      z.add(1);
      z.add(2);
      z.add(3);
      x.add(new ArrayNtList<>(z));
    }
    {
      ArrayList<Integer> z = new ArrayList<>();
      z.add(4);
      z.add(5);
      x.add(new ArrayNtList<>(z));
    }
    {
      ArrayList<Integer> z = new ArrayList<>();
      z.add(6);
      z.add(7);
      x.add(new ArrayNtList<>(z));
    }
    NtList<NtList<Integer>> result = new ArrayNtList<>(x);
    NtList<Integer> vals = LibLists.flatten(result);
    Assert.assertEquals(7, vals.size());
    for (int k = 0; k < 7; k++) {
      Assert.assertEquals(k + 1, (int) vals.lookup(k).get());
    }
  }

  @Test
  public void manifest() {
    ArrayList<NtMaybe<Integer>> x = new ArrayList<>();
    x.add(new NtMaybe<>(123));
    x.add(new NtMaybe<>());
    x.add(new NtMaybe<>(42));
    x.add(new NtMaybe<>(0));
    x.add(new NtMaybe<>());
    x.add(new NtMaybe<>(-13));
    NtList<Integer> result = LibLists.manifest(new ArrayNtList<>(x));
    Assert.assertEquals(4, result.size());
    Assert.assertEquals(123, (int) result.lookup(0).get());
    Assert.assertEquals(42, (int) result.lookup(1).get());
    Assert.assertEquals(0, (int) result.lookup(2).get());
    Assert.assertEquals(-13, (int) result.lookup(3).get());
  }

  @Test
  public void reverse() {
    ArrayList<Integer> z = new ArrayList<>();
    z.add(1);
    z.add(2);
    z.add(3);
    NtList<Integer> vals = LibLists.reverse(new ArrayNtList<>(z));
    Assert.assertEquals(3, vals.size());
    for (int k = 0; k < 3; k++) {
      Assert.assertEquals(3 - k, (int) vals.lookup(k).get());
    }
  }

  @Test
  public void skip() {
    ArrayList<Integer> z = new ArrayList<>();
    z.add(1);
    z.add(2);
    z.add(3);
    z.add(4);
    z.add(5);
    NtList<Integer> vals = LibLists.skip(new ArrayNtList<>(z), 2);
    Assert.assertEquals(3, vals.size());
    for (int k = 0; k < 3; k++) {
      Assert.assertEquals(3 + k, (int) vals.lookup(k).get());
    }
  }

  @Test
  public void drop() {
    ArrayList<Integer> z = new ArrayList<>();
    z.add(1);
    z.add(2);
    z.add(3);
    z.add(4);
    z.add(5);
    NtList<Integer> vals = LibLists.drop(new ArrayNtList<>(z), 2);
    Assert.assertEquals(3, vals.size());
    for (int k = 0; k < 3; k++) {
      Assert.assertEquals(1 + k, (int) vals.lookup(k).get());
    }
  }
}
