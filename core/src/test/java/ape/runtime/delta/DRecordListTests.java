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
package ape.runtime.delta;

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.json.PrivateLazyDeltaWriter;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

public class DRecordListTests {
  @Test
  public void flow() {
    final var list = new DRecordList<DBoolean>();
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      final var walk = list.begin();
      walk.next(42);
      final var a = list.getPrior(42, DBoolean::new);
      a.show(true, delta.planField(42));
      walk.end(delta);
      delta.end();
      Assert.assertEquals("{\"42\":true,\"@o\":[42]}", stream.toString());
      Assert.assertEquals(112, list.__memory());
    }
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      final var walk = list.begin();
      walk.next(10);
      final var a = list.getPrior(10, DBoolean::new);
      a.show(false, delta.planField(10));
      walk.next(42);
      final var b = list.getPrior(42, DBoolean::new);
      b.show(true, delta.planField(42));
      walk.end(delta);
      delta.end();
      Assert.assertEquals("{\"10\":false,\"@o\":[10,42]}", stream.toString());
      Assert.assertEquals(224, list.__memory());
    }
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      final var walk = list.begin();
      walk.next(42);
      final var b = list.getPrior(42, DBoolean::new);
      b.show(true, delta.planField(42));
      walk.end(delta);
      delta.end();
      Assert.assertEquals("{\"@o\":[42],\"10\":null}", stream.toString());
      Assert.assertEquals(112, list.__memory());
    }
    {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      list.hide(writer);
      list.hide(writer);
      Assert.assertEquals("null", stream.toString());
      Assert.assertEquals(0, list.__memory());
    }
  }

  @Test
  public void range() {
    final var list = new DRecordList<DBoolean>();
    final Function<Integer[], String> process = inserts -> {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      final var walk = list.begin();
      for (final Integer insert : inserts) {
        walk.next(insert);
        final var a = list.getPrior(insert, DBoolean::new);
        a.show(true, delta.planField(insert));
      }
      walk.end(delta);
      delta.end();
      return stream.toString();
    };
    Assert.assertEquals("{\"42\":true,\"100\":true,\"50\":true,\"@o\":[42,100,50]}", process.apply(new Integer[]{42, 100, 50}));
    Assert.assertEquals("{\"23\":true,\"@o\":[23,[0,2]]}", process.apply(new Integer[]{23, 42, 100, 50}));
    Assert.assertEquals("{\"77\":true,\"980\":true,\"@o\":[[0,3],77,980]}", process.apply(new Integer[]{23, 42, 100, 50, 77, 980}));
    Assert.assertEquals("{\"1\":true,\"2\":true,\"3\":true,\"4\":true,\"@o\":[1,2,3,4],\"50\":null,\"100\":null,\"980\":null,\"23\":null,\"42\":null,\"77\":null}", process.apply(new Integer[]{1, 2, 3, 4}));
    Assert.assertEquals("{\"0\":true,\"5\":true,\"6\":true,\"@o\":[0,[0,3],5,6]}", process.apply(new Integer[]{0, 1, 2, 3, 4, 5, 6}));
    list.clear();
  }

  @Test
  public void range_removal_regression() {
    final var list = new DRecordList<DBoolean>();
    final Function<Integer[], String> process = inserts -> {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      final var walk = list.begin();
      for (final Integer insert : inserts) {
        walk.next(insert);
        final var a = list.getPrior(insert, DBoolean::new);
        a.show(true, delta.planField(insert));
      }
      walk.end(delta);
      delta.end();
      return stream.toString();
    };
    Assert.assertEquals("{\"0\":true,\"1\":true,\"2\":true,\"3\":true,\"4\":true,\"5\":true,\"6\":true,\"7\":true,\"8\":true,\"9\":true,\"@o\":[0,1,2,3,4,5,6,7,8,9]}", process.apply(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    // { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }
    // -5
    // { [0, 4], [6
    Assert.assertEquals("{\"@o\":[[0,4],[6,9]],\"5\":null}", process.apply(new Integer[]{0, 1, 2, 3, 4, 6, 7, 8, 9}));
  }

  @Test
  public void to_nothing() {
    final var list = new DRecordList<DBoolean>();
    final Function<Integer[], String> process = inserts -> {
      final var stream = new JsonStreamWriter();
      final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
      final var delta = writer.planObject();
      final var walk = list.begin();
      for (final Integer insert : inserts) {
        walk.next(insert);
        final var a = list.getPrior(insert, DBoolean::new);
        a.show(true, delta.planField(insert));
      }
      walk.end(delta);
      delta.end();
      return stream.toString();
    };
    Assert.assertEquals("{\"0\":true,\"1\":true,\"2\":true,\"3\":true,\"4\":true,\"6\":true,\"7\":true,\"8\":true,\"9\":true,\"@o\":[0,1,2,3,4,6,7,8,9]}", process.apply(new Integer[]{0, 1, 2, 3, 4, 6, 7, 8, 9}));
    Assert.assertEquals("{\"@o\":[],\"0\":null,\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"6\":null,\"7\":null,\"8\":null,\"9\":null}", process.apply(new Integer[]{}));
  }
}
