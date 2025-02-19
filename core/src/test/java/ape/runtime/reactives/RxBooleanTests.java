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

public class RxBooleanTests {
  @Test
  public void memory() {
    final var rt = new RxBoolean(null, true);
    Assert.assertEquals(42, rt.__memory());
  }

  @Test
  public void compare() {
    final var rt = new RxBoolean(null, true);
    final var rf = new RxBoolean(null, false);
    Assert.assertEquals(1, rt.compareTo(rf));
    Assert.assertEquals(-1, rf.compareTo(rt));
  }


  @Test
  public void idx_value() {
    final var rt = new RxBoolean(null, true);
    final var rf = new RxBoolean(null, false);
    Assert.assertEquals(1, rt.getIndexValue());
    Assert.assertEquals(0, rf.getIndexValue());
  }


  @Test
  public void dirty_and_commit() {
    final var parent = new MockRxParent();
    final var rx = new RxBoolean(parent, false);
    Assert.assertEquals(false, rx.get());
    parent.assertDirtyCount(0);
    rx.set(true);
    parent.assertDirtyCount(1);
    Assert.assertEquals(true, rx.get());
    Assert.assertEquals(0, rx.compareTo(rx));
    rx.set(true);
    parent.assertDirtyCount(1);
    rx.set(true);
    parent.assertDirtyCount(1);
    rx.set(false);
    parent.assertDirtyCount(1);
    rx.set(true);
    parent.assertDirtyCount(1);
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    rx.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":true", writer.toString());
    Assert.assertEquals("\"v\":false", reverse.toString());
    final var writerAgain = new JsonStreamWriter();
    final var reverseAgain = new JsonStreamWriter();
    rx.__commit("v2", writerAgain, reverseAgain);
    Assert.assertEquals("", writerAgain.toString());
    Assert.assertEquals("", reverseAgain.toString());
  }

  @Test
  public void dump() {
    final var d = new RxBoolean(null, false);
    JsonStreamWriter writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("false", writer.toString());
  }

  @Test
  public void insert() {
    final var d = new RxBoolean(null, false);
    d.__insert(new JsonStreamReader("true"));
    Assert.assertTrue(d.get());
    d.__insert(new JsonStreamReader("false"));
    Assert.assertFalse(d.get());
    d.__insert(new JsonStreamReader("true"));
    Assert.assertTrue(d.get());
  }

  @Test
  public void patch() {
    final var d = new RxBoolean(null, false);
    d.__patch(new JsonStreamReader("true"));
    Assert.assertTrue(d.get());
    d.__patch(new JsonStreamReader("false"));
    Assert.assertFalse(d.get());
    d.__patch(new JsonStreamReader("true"));
    Assert.assertTrue(d.get());
  }

  @Test
  public void invalidate_and_revert() {
    final var parent = new MockRxParent();
    final var rx = new RxBoolean(parent, false);
    final var invalidate = new MockRxChild();
    rx.__subscribe(invalidate);
    invalidate.assertInvalidateCount(0);
    rx.set(true);
    invalidate.assertInvalidateCount(1);
    rx.set(true);
    invalidate.assertInvalidateCount(1);
    rx.__revert();
    invalidate.assertInvalidateCount(1);
    rx.__raiseDirty();
    rx.__revert();
    rx.__revert();
    rx.__revert();
    invalidate.assertInvalidateCount(2);
  }
}
