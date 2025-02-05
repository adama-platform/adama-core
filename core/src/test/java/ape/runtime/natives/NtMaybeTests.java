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

import ape.runtime.mocks.MockCanGetAndSet;
import ape.runtime.stdlib.LibMath;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class NtMaybeTests {
  @Test
  public void flow() {
    final var maybe = new NtMaybe<Integer>();
    Assert.assertFalse(maybe.has());
    maybe.set(123);
    Assert.assertTrue(maybe.has());
    Assert.assertEquals(123, (int) maybe.get());
    final var copy = new NtMaybe<>(maybe);
    Assert.assertTrue(copy.has());
    Assert.assertEquals(123, (int) copy.get());
    Assert.assertTrue(maybe == maybe.resolve());
  }

  @Test
  public void set_chain() {
    final var cgs = new MockCanGetAndSet<Integer>();
    final var maybe = new NtMaybe<Integer>().withAssignChain(cgs);
    Assert.assertFalse(maybe.has());
    maybe.set(123);
    Assert.assertEquals(123, (int) cgs.get());
    maybe.set(new NtMaybe<>(52));
    Assert.assertEquals(52, (int) cgs.get());
  }

  @Test
  public void sort() {
    final var ints = new ArrayList<NtMaybe<Integer>>();
    ints.add(new NtMaybe<>());
    ints.add(new NtMaybe<>(10000));
    ints.add(new NtMaybe<>());
    ints.add(new NtMaybe<>(123));
    ints.add(new NtMaybe<>());
    ints.add(new NtMaybe<>(1000));
    ints.add(new NtMaybe<>());
    ints.add(new NtMaybe<>(12));
    ints.add(new NtMaybe<>());
    ints.add(new NtMaybe<>());
    ints.add(new NtMaybe<>());
    ints.sort((a, b) -> a.compareValues(b, Integer::compareTo));
    Assert.assertEquals(12, (int) ints.get(0).get());
    Assert.assertEquals(123, (int) ints.get(1).get());
    Assert.assertEquals(1000, (int) ints.get(2).get());
    Assert.assertEquals(10000, (int) ints.get(3).get());
    Assert.assertEquals(null, ints.get(4).get());
  }

  @Test
  public void via_set() {
    final var maybe = new NtMaybe<>(123);
    final var copy = new NtMaybe<Integer>();
    copy.set(maybe);
    Assert.assertEquals(123, (int) copy.get());
    copy.delete();
    Assert.assertFalse(copy.has());
    Assert.assertTrue(maybe.has());
    Assert.assertEquals(123, (int) maybe.get());
  }

  @Test
  public void with_value() {
    final var maybe = new NtMaybe<>(123);
    final var copy = new NtMaybe<>(maybe);
    Assert.assertTrue(copy.has());
    Assert.assertEquals(123, (int) copy.get());
    copy.delete();
    Assert.assertFalse(copy.has());
    Assert.assertTrue(maybe.has());
    Assert.assertEquals(123, (int) maybe.get());
  }

  @Test
  public void delete_chain() {
    AtomicBoolean called = new AtomicBoolean(false);
    final var maybe = new NtMaybe<>(123);
    maybe.withDeleteChain(() -> {
      called.set(true);
    });
    Assert.assertEquals(123, (int) maybe.getOrDefaultTo(42));
    maybe.delete();
    Assert.assertTrue(called.get());
    Assert.assertEquals(42, (int) maybe.getOrDefaultTo(42));
  }

  @Test
  public void unpack() {
    final var maybe = new NtMaybe<>(123);
    Assert.assertEquals(123 * 123, (int) ((maybe.unpack((x) -> x * x).get())));
    Assert.assertEquals("123", maybe.toString());
    maybe.delete();
    Assert.assertFalse(maybe.unpack((x) -> x * x).has());
    Assert.assertEquals("", maybe.toString());
  }

  @Test
  public void unpackTransfer() {
    final var maybe = new NtMaybe<>(123);
    Assert.assertEquals(123 * 123, (int) ((maybe.unpackTransfer((x) -> new NtMaybe<>(x * x)).get())));
    Assert.assertEquals("123", maybe.toString());
    maybe.delete();
    Assert.assertFalse(maybe.unpackTransfer((x) -> new NtMaybe<>(x * x)).has());
    Assert.assertEquals("", maybe.toString());
  }

  @Test
  public void regression() {
    NtMaybe<Integer> i871 = new NtMaybe<>(871);
    Assert.assertTrue(LibMath.equality(i871, 871, (__x, __y) -> __x.intValue() == __y));
  }
}
