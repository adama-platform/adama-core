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
import ape.runtime.mocks.MockRxChild;
import ape.runtime.mocks.MockRxParent;
import org.junit.Assert;
import org.junit.Test;

public class RxDoubleTests {
  @Test
  public void memory() {
    final var d1 = new RxDouble(null, 1);
    Assert.assertEquals(56, d1.__memory());
  }

  @Test
  public void compare() {
    final var d1 = new RxDouble(null, 1);
    final var d2 = new RxDouble(null, 2);
    Assert.assertEquals(-1, d1.compareTo(d2));
    Assert.assertEquals(1, d2.compareTo(d1));
  }

  @Test
  public void dirty_and_commit() {
    final var parent = new MockRxParent();
    final var d = new RxDouble(parent, 42);
    Assert.assertEquals(42, d.get(), 0.1);
    d.set(50);
    parent.assertDirtyCount(1);
    d.set(6.28);
    parent.assertDirtyCount(1);
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    d.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":6.28", writer.toString());
    Assert.assertEquals("\"v\":42.0", reverse.toString());
    final var writerAgain = new JsonStreamWriter();
    final var reverseAgain = new JsonStreamWriter();
    d.__commit("v2", writerAgain, reverseAgain);
    Assert.assertEquals("", writerAgain.toString());
    Assert.assertEquals("", reverseAgain.toString());
  }

  @Test
  public void dump() {
    final var d = new RxDouble(null, 1232.5);
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("1232.5", writer.toString());
  }

  @Test
  public void insert() {
    final var d = new RxDouble(null, 1);
    d.__insert(new JsonStreamReader("42"));
    Assert.assertEquals(42, d.get(), 0.1);
  }

  @Test
  public void patch() {
    final var d = new RxDouble(null, 1);
    d.__patch(new JsonStreamReader("42"));
    Assert.assertEquals(42, d.get(), 0.1);
  }

  @Test
  public void invalidate_and_revert() {
    final var d = new RxDouble(null, 42);
    final var child = new MockRxChild();
    d.__subscribe(child);
    d.set(50);
    child.assertInvalidateCount(1);
    final Double dFity = 50.0;
    d.set(dFity);
    child.assertInvalidateCount(1);
    d.set(55.0);
    child.assertInvalidateCount(1);
    d.__revert();
    child.assertInvalidateCount(1);
    Assert.assertEquals(42, d.get(), 0.1);
    d.__raiseDirty();
    d.__revert();
    d.__revert();
    d.__revert();
    child.assertInvalidateCount(2);
  }

  @Test
  public void ops() {
    final var d = new RxDouble(null, 1);
    d.bumpUpPre();
    Assert.assertEquals(2, d.get(), 0.1);
    d.bumpUpPost();
    Assert.assertEquals(3, d.get(), 0.1);
    d.bumpDownPre();
    Assert.assertEquals(2, d.get(), 0.1);
    d.bumpDownPost();
    Assert.assertEquals(1, d.get(), 0.1);
    d.opAddTo(10);
    Assert.assertEquals(11, d.get(), 0.1);
    d.opMultBy(2);
    Assert.assertEquals(22, d.get(), 0.1);
    d.opAddTo(-(7));
    Assert.assertEquals(15, d.get(), 0.1);
  }
}
