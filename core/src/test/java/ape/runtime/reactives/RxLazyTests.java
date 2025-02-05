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
package ape.runtime.reactives;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockLivingDocument;
import ape.runtime.mocks.MockRecord;
import ape.runtime.mocks.MockRxParent;
import ape.runtime.sys.PerfTracker;
import org.junit.Assert;
import org.junit.Test;

public class RxLazyTests {
  @Test
  public void flow() {
    final var val = new RxInt32(null, 42);
    final var lz = new RxLazy<>(null, () -> val.get() * val.get(), null);
    final var lz2 = new RxLazy<>(null, () -> lz.get() / 2, null);
    Assert.assertTrue( lz.alive() );
    Assert.assertEquals(1, lz.getGeneration());
    Assert.assertEquals(1, lz2.getGeneration());
    val.__subscribe(lz);
    lz.__subscribe(lz2);
    val.set(4);
    Assert.assertEquals(16, (int) lz.get());
    Assert.assertEquals(8, (int) lz2.get());
    val.set(6);
    Assert.assertEquals(18, (int) lz2.get());
    Assert.assertEquals(36, (int) lz.get());
    Assert.assertEquals(18, (int) lz2.get());
    Assert.assertEquals(36, (int) lz.get());
    val.set(10);
    Assert.assertEquals(50, (int) lz2.get());
    lz.__insert(new JsonStreamReader("{}"));
    lz.__dump(null);
    lz.__patch(new JsonStreamReader("{}"));
    Assert.assertEquals(1, lz.getGeneration());
    Assert.assertEquals(1, lz2.getGeneration());
    lz.__forceSettle();
  }

  @Test
  public void mirror_parent_life() {
    final var val = new RxInt32(null, 42);
    MockRxParent p = new MockRxParent();
    final var clz = new RxLazy<>(p, () -> val.get() * val.get(), null);
    Assert.assertTrue(clz.alive());
    p.alive = false;
    Assert.assertFalse(clz.alive());
  }

  @Test
  public void perf() {
    final var val = new RxInt32(null, 42);
    MockRxParent p = new MockRxParent();
    PerfTracker tracker = new PerfTracker(new MockLivingDocument());
    final var clz = new RxLazy<>(p, () -> val.get() * val.get(), () -> tracker.measure("x"));
    clz.get();
    System.out.println(tracker.dump(0));
  }

  @Test
  public void flow_with_commit() {
    final var val = new RxInt32(null, 42);
    final var lz = new RxLazy<>(null, () -> val.get() * val.get(), null);
    final var lz2 = new RxLazy<>(null, () -> lz.get() / 2, null);
    Assert.assertEquals(1, lz.getGeneration());
    Assert.assertEquals(1, lz2.getGeneration());
    val.__subscribe(lz);
    lz.__subscribe(lz2);
    val.set(4);
    Assert.assertEquals(16, (int) lz.get());
    Assert.assertEquals(8, (int) lz2.get());
    JsonStreamWriter f = new JsonStreamWriter();
    JsonStreamWriter r = new JsonStreamWriter();
    val.__commit("val", f, r);
    lz.__settle(null);
    lz2.__settle(null);
    Assert.assertEquals("\"val\":4", f.toString());
    Assert.assertEquals("\"val\":42", r.toString());
    Assert.assertEquals(65522, lz.getGeneration());
    Assert.assertEquals(65522, lz2.getGeneration());
    val.set(6);
    Assert.assertEquals(18, (int) lz2.get());
    Assert.assertEquals(36, (int) lz.get());
    Assert.assertEquals(18, (int) lz2.get());
    Assert.assertEquals(36, (int) lz.get());
    val.set(10);
    lz.__settle(null);
    lz2.__settle(null);
    Assert.assertEquals(50, (int) lz2.get());
    lz2.__raiseInvalid();
    Assert.assertEquals(50, (int) lz2.get());
    lz.__insert(new JsonStreamReader("{}"));
    lz.__dump(null);
    lz.__patch(new JsonStreamReader("{}"));
    Assert.assertEquals(-1900333, lz.getGeneration());
    Assert.assertEquals(-1900333, lz2.getGeneration());
    lz.__forceSettle();
    Assert.assertEquals(-1900333, lz.getGeneration());
    lz.__raiseInvalid();
    lz.__forceSettle();
    Assert.assertEquals(42333092, lz.getGeneration());
  }

  @Test
  public void alive_with_parent() {
    MockRxParent parent = new MockRxParent();
    final var val = new RxLazy(parent, () -> 123, null);
    Assert.assertTrue(val.__raiseInvalid());
    parent.alive = false;
    Assert.assertFalse(val.__raiseInvalid());
  }

  @Test
  public void chase_id() {
    MockRecord root = new MockRecord(null) {
      @Override
      public int __id() {
        return 1000;
      }
    };
    MockRecord proxy = new MockRecord(root);
    final var lz = new RxLazy(proxy, () -> 123, null);
    Assert.assertEquals(65521001, lz.getGeneration());
  }

  @Test
  public void alive_without_parent() {
    final var val = new RxLazy(null, () -> 123, null);
    Assert.assertTrue(val.__raiseInvalid());
  }

  @Test
  public void trivial() {
    final var val = new RxInt32(null, 42);
    final var lz = new RxLazy<>(null, () -> val.get(), null);
    lz.__commit(null, null, null);
    lz.__revert();
  }
}
