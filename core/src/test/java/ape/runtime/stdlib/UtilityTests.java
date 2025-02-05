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

import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtMaybe;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Function;

public class UtilityTests {
  @Test
  public void convert_array() {
    final Function<Integer, Integer[]> makeArray = (final Integer n) -> new Integer[n];
    final Function<String, Integer> conv = (final String s) -> Integer.parseInt(s);
    final var data = new String[] {"123", "42"};
    final var arr = Utility.convertMultiple(data, makeArray, conv);
    Assert.assertEquals(123, (int) arr[0]);
    Assert.assertEquals(42, (int) arr[1]);
  }

  @Test
  public void read_array() {
    JsonStreamReader reader = new JsonStreamReader("[1,2,3]");
    Integer[] x = Utility.readArray(reader, (r) -> r.readInteger(), (n) -> new Integer[n]);
    Assert.assertEquals(1, (int) x[0]);
    Assert.assertEquals(2, (int) x[1]);
    Assert.assertEquals(3, (int) x[2]);
  }

  @Test
  public void convert_list() {
    final Function<Integer, Integer[]> makeArray = (final Integer n) -> new Integer[n];
    final Function<String, Integer> conv = (final String s) -> Integer.parseInt(s);
    final var list = new ArrayList<String>();
    list.add("123");
    list.add("42");
    final var arr = Utility.convertMultiple(list, makeArray, conv);
    Assert.assertEquals(123, (int) arr[0]);
    Assert.assertEquals(42, (int) arr[1]);
  }

  @Test
  public void convert_maybe() {
    final var ms = new NtMaybe<String>();
    Assert.assertFalse(Utility.convertMaybe(ms, (final String s) -> Integer.parseInt(s)).has());
    ms.set("123");
    Assert.assertTrue(Utility.convertMaybe(ms, (final String s) -> Integer.parseInt(s)).has());
    Assert.assertEquals(
        123, (int) Utility.convertMaybe(ms, (final String s) -> Integer.parseInt(s)).get());
  }

  @Test
  public void convert_single() {
    Assert.assertEquals(
        123, (int) Utility.convertSingle("123", (final String s) -> Integer.parseInt(s)));
  }

  @Test
  public void coverage() {
    Utility.identity(123);
    Utility.identity(123L);
  }

  @Test
  public void lookup() {
    final var X = new Integer[] {1, 3, 5};
    Assert.assertEquals(1, (int) Utility.lookup(X, 0).get());
    Assert.assertEquals(3, (int) Utility.lookup(X, 1).get());
    Assert.assertEquals(5, (int) Utility.lookup(X, 2).get());
    Assert.assertFalse(Utility.lookup(X, -1).has());
    Assert.assertFalse(Utility.lookup(X, 4).has());
  }

  @Test
  public void lookup_maybe() {
    final var X = new Integer[] {1, 3, 5};
    Assert.assertEquals(1, (int) Utility.lookup(X, new NtMaybe<>(0)).get());
    Assert.assertEquals(3, (int) Utility.lookup(X, new NtMaybe<>(1)).get());
    Assert.assertEquals(5, (int) Utility.lookup(X, new NtMaybe<>(2)).get());
    Assert.assertFalse(Utility.lookup(X, new NtMaybe<>(-1)).has());
    Assert.assertFalse(Utility.lookup(X, new NtMaybe<>(4)).has());
    Assert.assertFalse(Utility.lookup(X, new NtMaybe<>()).has());
  }
}
