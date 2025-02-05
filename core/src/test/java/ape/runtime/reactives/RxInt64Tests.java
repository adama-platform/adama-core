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

public class RxInt64Tests {
  @Test
  public void memory() {
    final var l1 = new RxInt64(null, 1);
    final var l2 = new RxInt64(null, 2);
    Assert.assertEquals(56, l1.__memory());
    Assert.assertEquals(56, l2.__memory());
  }

  @Test
  public void compare() {
    final var l1 = new RxInt64(null, 1);
    final var l2 = new RxInt64(null, 2);
    Assert.assertEquals(-1, l1.compareTo(l2));
    Assert.assertEquals(1, l2.compareTo(l1));
  }

  @Test
  public void dirty_and_commit() {
    final var parent = new MockRxParent();
    final var l = new RxInt64(parent, 42);
    Assert.assertEquals(42, (long) l.get());
    l.set(50L);
    parent.assertDirtyCount(1);
    l.set(60);
    parent.assertDirtyCount(1);
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    l.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":\"60\"", writer.toString());
    Assert.assertEquals("\"v\":\"42\"", reverse.toString());
    final var writerAgain = new JsonStreamWriter();
    final var reverseAgain = new JsonStreamWriter();
    l.__commit("v2", writerAgain, reverseAgain);
    Assert.assertEquals("", writerAgain.toString());
    Assert.assertEquals("", reverseAgain.toString());
    l.set(12354124314124L);
    final var writerThird = new JsonStreamWriter();
    final var reverseThird = new JsonStreamWriter();
    l.__commit("v", writerThird, reverseThird);
    Assert.assertEquals("\"v\":\"12354124314124\"", writerThird.toString());
    Assert.assertEquals("\"v\":\"60\"", reverseThird.toString());
  }

  @Test
  public void dump() {
    final var d = new RxInt64(null, 424242L);
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("\"424242\"", writer.toString());
  }

  @Test
  public void insert1() {
    final var d = new RxInt64(null, 1);
    d.__insert(new JsonStreamReader("42"));
    Assert.assertEquals(42, (long) d.get());
  }

  @Test
  public void insert2() {
    final var d = new RxInt64(null, 1);
    d.__insert(new JsonStreamReader("\"42\""));
    Assert.assertEquals(42, (long) d.get());
  }

  @Test
  public void patch1() {
    final var d = new RxInt64(null, 1);
    d.__patch(new JsonStreamReader("42"));
    Assert.assertEquals(42, (long) d.get());
  }

  @Test
  public void patch2() {
    final var d = new RxInt64(null, 1);
    d.__patch(new JsonStreamReader("\"42\""));
    Assert.assertEquals(42, (long) d.get());
  }

  @Test
  public void invalidate_and_revert() {
    final var l = new RxInt64(null, 42);
    final var child = new MockRxChild();
    l.__subscribe(child);
    l.set(50);
    child.assertInvalidateCount(1);
    l.set(50);
    child.assertInvalidateCount(1);
    l.set(55);
    child.assertInvalidateCount(1);
    Assert.assertEquals(55, l.getIndexValue());
    l.__revert();
    child.assertInvalidateCount(1);
    Assert.assertEquals(42, (long) l.get());
    l.__raiseDirty();
    l.__revert();
    l.__revert();
    child.assertInvalidateCount(2);
    l.__revert();
    child.assertInvalidateCount(2);
  }

  @Test
  public void ops() {
    final var l = new RxInt64(null, 1);
    l.bumpUpPre();
    Assert.assertEquals(2, (long) l.get());
    l.bumpUpPost();
    Assert.assertEquals(3, (long) l.get());
    l.bumpDownPre();
    Assert.assertEquals(2, (long) l.get());
    l.bumpDownPost();
    Assert.assertEquals(1, (long) l.get());
    l.opAddTo(10);
    Assert.assertEquals(11, (long) l.get());
    l.opMultBy(2);
    Assert.assertEquals(22, (long) l.get());
    l.opAddTo(-(7));
    Assert.assertEquals(15, (long) l.get());
  }
}
