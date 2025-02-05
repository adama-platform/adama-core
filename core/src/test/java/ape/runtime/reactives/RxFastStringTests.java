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

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockRxChild;
import ape.runtime.mocks.MockRxParent;
import org.junit.Assert;
import org.junit.Test;

public class RxFastStringTests {
  @Test
  public void memory() {
    final var a = new RxFastString(null, "a");
    final var b = new RxFastString(null, "bison");
    Assert.assertEquals(60, a.__memory());
    Assert.assertEquals(76, b.__memory());
  }

  @Test
  public void compare1() {
    final var a = new RxFastString(null, "a");
    final var b = new RxFastString(null, "b");
    Assert.assertEquals(-1, a.compareTo(b));
    Assert.assertEquals(1, b.compareTo(a));
  }

  @Test
  public void compare2() {
    final var a = new RxString(null, "a");
    final var b = new RxFastString(null, "b");
    Assert.assertEquals(-1, a.compareTo(b));
    Assert.assertEquals(1, b.compareTo(a));
  }

  @Test
  public void compare3() {
    final var a = new RxFastString(null, "a");
    final var b = new RxString(null, "b");
    Assert.assertEquals(-1, a.compareTo(b));
    Assert.assertEquals(1, b.compareTo(a));
  }

  @Test
  public void dirty_and_commit() {
    final var parent = new MockRxParent();
    final var s = new RxFastString(parent, "xxx");
    Assert.assertEquals("xxx", s.get());
    parent.assertDirtyCount(0);
    s.set("cake");
    parent.assertDirtyCount(1);
    s.set("cake");
    parent.assertDirtyCount(1);
    Assert.assertEquals("cake", s.get());
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    s.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":\"cake\"", writer.toString());
    Assert.assertEquals("\"v\":\"xxx\"", reverse.toString());
    final var writerAgain = new JsonStreamWriter();
    final var reverseAgain = new JsonStreamWriter();
    s.__commit("v2", writerAgain, reverseAgain);
    Assert.assertEquals("", writerAgain.toString());
    Assert.assertEquals("", reverseAgain.toString());
  }

  @Test
  public void dump() {
    final var d = new RxFastString(null, "xyz");
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("\"xyz\"", writer.toString());
  }

  @Test
  public void invalidate_and_revert() {
    final var child = new MockRxChild();
    final var s = new RxFastString(null, "xyz");
    s.__subscribe(child);
    Assert.assertEquals("xyz", s.get());
    child.assertInvalidateCount(0);
    s.set("cake");
    child.assertInvalidateCount(1);
    s.set("cake");
    child.assertInvalidateCount(1);
    Assert.assertEquals("cake", s.get());
    s.__revert();
    Assert.assertEquals("xyz", s.get());
    child.assertInvalidateCount(1);
    s.__raiseDirty();
    s.__revert();
    s.__revert();
    child.assertInvalidateCount(2);
  }

  @Test
  public void ops() {
    final var parent = new MockRxParent();
    final var s = new RxFastString(parent, "a");
    s.opAddTo(0);
    parent.assertDirtyCount(1);
    s.opAddTo(true);
    parent.assertDirtyCount(1);
    s.opAddTo(0.0);
    parent.assertDirtyCount(1);
    s.opAddTo("b");
    parent.assertDirtyCount(1);
    Assert.assertEquals("a0true0.0b", s.get());
  }
}
