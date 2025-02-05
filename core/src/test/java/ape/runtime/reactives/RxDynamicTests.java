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
import ape.runtime.natives.NtDynamic;
import org.junit.Assert;
import org.junit.Test;

public class RxDynamicTests {
  private static final NtDynamic A = new NtDynamic("true");
  private static final NtDynamic B = new NtDynamic("false");

  @Test
  public void memory() {
    final var a = new RxDynamic(null, A);
    final var b = new RxDynamic(null, B);
    Assert.assertEquals(72, a.__memory());
    Assert.assertEquals(76, b.__memory());
  }

  @Test
  public void compare() {
    final var a = new RxDynamic(null, A);
    final var b = new RxDynamic(null, B);
    Assert.assertEquals(14, a.compareTo(b));
    Assert.assertEquals(-14, b.compareTo(a));
  }

  @Test
  public void dirty_and_commit() {
    final var parent = new MockRxParent();
    final var c = new RxDynamic(parent, NtDynamic.NULL);
    parent.assertDirtyCount(0);
    c.set(A);
    parent.assertDirtyCount(1);
    c.set(A);
    parent.assertDirtyCount(1);
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    c.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":true", writer.toString());
    Assert.assertEquals("\"v\":null", reverse.toString());
    final var writerAgain = new JsonStreamWriter();
    final var reverseAgain = new JsonStreamWriter();
    c.__commit("v2", writerAgain, reverseAgain);
    Assert.assertEquals("", writerAgain.toString());
    Assert.assertEquals("", reverseAgain.toString());
  }

  @Test
  public void dumpA() {
    final var d = new RxDynamic(null, A);
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("true", writer.toString());
  }

  @Test
  public void dumpB() {
    final var d = new RxDynamic(null, B);
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("false", writer.toString());
  }

  @Test
  public void insert() {
    final var c = new RxDynamic(null, NtDynamic.NULL);
    c.__insert(new JsonStreamReader("{\"x\":\"foo\"}"));
    final var g = c.get();
    Assert.assertEquals("{\"x\":\"foo\"}", g.json);
  }

  @Test
  public void patch() {
    final var c = new RxDynamic(null, NtDynamic.NULL);
    c.__patch(new JsonStreamReader("{\"x\":\"foo\"}"));
    final var g = c.get();
    Assert.assertEquals("{\"x\":\"foo\"}", g.json);
  }

  @Test
  public void invalidate_and_revert() {
    final var c = new RxDynamic(null, NtDynamic.NULL);
    final var child = new MockRxChild();
    c.__subscribe(child);
    child.assertInvalidateCount(0);
    c.set(A);
    child.assertInvalidateCount(1);
    c.set(A);
    Assert.assertEquals(A, c.get());
    child.assertInvalidateCount(1);
    c.set(B);
    Assert.assertEquals(B, c.get());
    child.assertInvalidateCount(1);
    c.__revert();
    child.assertInvalidateCount(1);
    Assert.assertEquals(NtDynamic.NULL, c.get());
    c.__raiseDirty();
    c.__revert();
    c.__revert();
    child.assertInvalidateCount(2);
  }

  @Test
  public void seq() {
    final var c = new RxDynamic(null, NtDynamic.NULL);
    c.set(new NtDynamic("{\"x\":123,\"y\":42}"));
    {
      JsonStreamWriter redo = new JsonStreamWriter();
      JsonStreamWriter undo = new JsonStreamWriter();
      c.__commit("x", redo, undo);
      Assert.assertEquals("\"x\":{\"x\":123,\"y\":42}", redo.toString());
      Assert.assertEquals("\"x\":null", undo.toString());
    }
    c.set(new NtDynamic("{\"x\":42,\"z\":42}"));
    {
      JsonStreamWriter redo = new JsonStreamWriter();
      JsonStreamWriter undo = new JsonStreamWriter();
      c.__commit("x", redo, undo);
      Assert.assertEquals("\"x\":{\"x\":42,\"z\":42,\"y\":null}", redo.toString());
      Assert.assertEquals("\"x\":{\"x\":123,\"y\":42,\"z\":null}", undo.toString());
    }


  }
}
