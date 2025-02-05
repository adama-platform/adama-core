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
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class RxPrincipalTests {
  private static final NtPrincipal A = new NtPrincipal("a", "d");
  private static final NtPrincipal B = new NtPrincipal("b", "c");
  private static final NtPrincipal CC = new NtPrincipal("c", "c");

  @Test
  public void memory() {
    final var a = new RxPrincipal(null, A);
    Assert.assertEquals(64, a.__memory());
  }

  @Test
  public void compare() {
    final var a = new RxPrincipal(null, A);
    final var b = new RxPrincipal(null, B);
    final var c = new RxPrincipal(null, CC);
    Assert.assertEquals(1, a.compareTo(b));
    Assert.assertEquals(-1, b.compareTo(a));
    Assert.assertEquals(1, a.compareTo(c));
    Assert.assertEquals(-1, b.compareTo(c));
    Assert.assertEquals(-1, c.compareTo(a));
    Assert.assertEquals(1, c.compareTo(b));
  }

  @Test
  public void dirty_and_commit() {
    final var parent = new MockRxParent();
    final var c = new RxPrincipal(parent, NtPrincipal.NO_ONE);
    parent.assertDirtyCount(0);
    c.set(A);
    parent.assertDirtyCount(1);
    c.set(A);
    parent.assertDirtyCount(1);
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    c.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":{\"agent\":\"a\",\"authority\":\"d\"}", writer.toString());
    Assert.assertEquals("\"v\":{\"agent\":\"?\",\"authority\":\"?\"}", reverse.toString());
    final var writerAgain = new JsonStreamWriter();
    final var reverseAgain = new JsonStreamWriter();
    c.__commit("v2", writerAgain, reverseAgain);
    Assert.assertEquals("", writerAgain.toString());
    Assert.assertEquals("", reverseAgain.toString());
  }

  @Test
  public void dump() {
    final var d = new RxPrincipal(null, A);
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("{\"agent\":\"a\",\"authority\":\"d\"}", writer.toString());
  }

  @Test
  public void hash() {
    Assert.assertEquals(2016, new RxPrincipal(null, NtPrincipal.NO_ONE).getIndexValue());
    Assert.assertEquals(3107, new RxPrincipal(null, A).getIndexValue());
    Assert.assertEquals(3137, new RxPrincipal(null, B).getIndexValue());
    Assert.assertEquals(3168, new RxPrincipal(null, CC).getIndexValue());
  }

  @Test
  public void insert() {
    final var c = new RxPrincipal(null, NtPrincipal.NO_ONE);
    c.__insert(new JsonStreamReader("{\"agent\":\"foo\",\"authority\":\"xyz\"}"));
    final var g = c.get();
    Assert.assertEquals("foo", g.agent);
    Assert.assertEquals("xyz", g.authority);
  }

  @Test
  public void patch() {
    final var c = new RxPrincipal(null, NtPrincipal.NO_ONE);
    c.__patch(new JsonStreamReader("{\"agent\":\"foo\",\"authority\":\"xyz\"}"));
    final var g = c.get();
    Assert.assertEquals("foo", g.agent);
    Assert.assertEquals("xyz", g.authority);
  }

  @Test
  public void invalidate_and_revert() {
    final var c = new RxPrincipal(null, NtPrincipal.NO_ONE);
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
    Assert.assertEquals(NtPrincipal.NO_ONE, c.get());
    c.__raiseDirty();
    c.__revert();
    c.__revert();
    c.__revert();
    child.assertInvalidateCount(2);
  }
}
