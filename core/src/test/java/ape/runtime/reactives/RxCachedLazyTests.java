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
import ape.runtime.mocks.MockRecord;
import ape.runtime.mocks.MockRxParent;
import org.junit.Assert;
import org.junit.Test;

public class RxCachedLazyTests {
  @Test
  public void flow() {
    final RxInt64 time = new RxInt64(null, 0L);
    final var val = new RxInt32(null, 42);
    final var clz = new RxCachedLazy<>(null, () -> val.get() * val.get(), null, 1, time);
    Assert.assertTrue(clz.alive());
    clz.__commit(null, null, null);
    clz.__revert();
    clz.__insert(new JsonStreamReader("{}"));
    clz.__patch(new JsonStreamReader("{}"));
    clz.__dump(new JsonStreamWriter());
    Assert.assertTrue(clz.__raiseInvalid());
    Assert.assertEquals(1764, (int) clz.get());
    val.set(100);
    Assert.assertEquals(1764, (int) clz.get());
    Assert.assertEquals(115579045, clz.getGeneration());
    clz.__settle(null);
    Assert.assertEquals(1764, (int) clz.get());
    Assert.assertEquals(115579045, clz.getGeneration());
    time.set(100000);
    clz.__settle(null);
    Assert.assertEquals(10000, (int) clz.get());
    Assert.assertEquals(716450039, clz.getGeneration());
  }

  @Test
  public void mirror_parent_life() {
    final RxInt64 time = new RxInt64(null, 0L);
    final var val = new RxInt32(null, 42);
    MockRxParent p = new MockRxParent();
    final var clz = new RxCachedLazy<>(p, () -> val.get() * val.get(), null, 1, time);
    Assert.assertTrue(clz.alive());
    p.alive = false;
    Assert.assertFalse(clz.alive());
  }

  @Test
  public void flow_parent_and_perf() {
    MockRxParent parent = new MockRxParent();
    final RxInt64 time = new RxInt64(null, 0L);
    final var val = new RxInt32(null, 42);
    final var clz = new RxCachedLazy<>(parent, () -> val.get() * val.get(), () -> (() -> {}), 1, time);
    Assert.assertTrue(clz.alive());
    Assert.assertTrue(clz.__raiseInvalid());
    clz.__commit(null, null, null);
    clz.__revert();
    clz.__insert(new JsonStreamReader("{}"));
    clz.__patch(new JsonStreamReader("{}"));
    clz.__dump(new JsonStreamWriter());
    parent.alive = false;
    Assert.assertFalse(clz.alive());
    Assert.assertFalse(clz.__raiseInvalid());
    Assert.assertEquals(1764, (int) clz.get());
    val.set(100);
    Assert.assertEquals(1764, (int) clz.get());
    Assert.assertEquals(115579045, clz.getGeneration());
    time.set(100000);
    clz.__settle(null);
    Assert.assertEquals(10000, (int) clz.get());
    Assert.assertEquals(716450039, clz.getGeneration());
  }

  @Test
  public void init_record() {
    MockRecord record = new MockRecord(new MockRxParent());
    record.id = 123;
    final RxInt64 time = new RxInt64(null, 0L);
    final var val = new RxInt32(null, 42);
    final var clz = new RxCachedLazy<>(record, () -> val.get() * val.get(), () -> (() -> {}), 1, time);
    Assert.assertEquals(8059084, clz.getGeneration());
    Assert.assertEquals(1764, (int) clz.get());
    Assert.assertEquals(-241734643, clz.getGeneration());
  }
}
