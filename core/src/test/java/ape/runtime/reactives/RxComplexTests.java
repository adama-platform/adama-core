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
import ape.runtime.natives.NtComplex;
import org.junit.Assert;
import org.junit.Test;

public class RxComplexTests {
  @Test
  public void memory() {
    final var d = new RxComplex(null, new NtComplex(1, 2));
    Assert.assertEquals(88, d.__memory());
  }

  @Test
  public void math() {
    final var parent = new MockRxParent();
    final var d = new RxComplex(parent, new NtComplex(1, 2));
    d.set(0.0);
    d.opAddTo(1);
    d.opAddTo(2L);
    d.opAddTo(3.14);
    d.opAddTo(new NtComplex(10.0, 10.0));
    Assert.assertTrue(Math.abs(d.get().real - 16.14) < 0.001);
    Assert.assertTrue(Math.abs(d.get().imaginary - 10.0) < 0.001);
    d.opMultBy(1.5);
    Assert.assertTrue(Math.abs(d.get().real - 24.21) < 0.001);
    Assert.assertTrue(Math.abs(d.get().imaginary - 15.0) < 0.001);
    d.opMultBy(new NtComplex(0.0, 1.0));
    Assert.assertTrue(Math.abs(d.get().real - -15) < 0.001);
    Assert.assertTrue(Math.abs(d.get().imaginary - 24.21) < 0.001);
    d.opMultBy(new NtComplex(1.0, 0.0));
    Assert.assertTrue(Math.abs(d.get().real - -15) < 0.001);
    Assert.assertTrue(Math.abs(d.get().imaginary - 24.21) < 0.001);
    d.opSubFrom(new NtComplex(1, 1));
    Assert.assertTrue(Math.abs(d.get().real - -16) < 0.001);
    Assert.assertTrue(Math.abs(d.get().imaginary - 23.21) < 0.001);
  }

  @Test
  public void dirty_and_commit() {
    final var parent = new MockRxParent();
    final var d = new RxComplex(parent, new NtComplex(1, 2));
    Assert.assertEquals(new NtComplex(1, 2), d.get());
    d.set(new NtComplex(3, 4));
    parent.assertDirtyCount(1);
    d.set(new NtComplex(4, 5));
    parent.assertDirtyCount(1);
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    d.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":{\"r\":4.0,\"i\":5.0}", writer.toString());
    Assert.assertEquals("\"v\":{\"r\":1.0,\"i\":2.0}", reverse.toString());
    final var writerAgain = new JsonStreamWriter();
    final var reverseAgain = new JsonStreamWriter();
    d.__commit("v2", writerAgain, reverseAgain);
    Assert.assertEquals("", writerAgain.toString());
    Assert.assertEquals("", reverseAgain.toString());
  }

  @Test
  public void dump() {
    final var d = new RxComplex(null, new NtComplex(1, 2));
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("{\"r\":1.0,\"i\":2.0}", writer.toString());
  }

  @Test
  public void insert() {
    final var d = new RxComplex(null, new NtComplex(1, 2));
    d.__insert(new JsonStreamReader("{\"r\":3,\"i\":4}"));
    Assert.assertEquals(new NtComplex(3, 4), d.get());
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    d.__commit("v", writer, reverse);
    Assert.assertEquals("", writer.toString());
    Assert.assertEquals("", reverse.toString());
  }

  @Test
  public void patch() {
    final var d = new RxComplex(null, new NtComplex(1, 2));
    d.__patch(new JsonStreamReader("{\"r\":3,\"i\":4}"));
    Assert.assertEquals(new NtComplex(3, 4), d.get());
    final var writer = new JsonStreamWriter();
    final var reverse = new JsonStreamWriter();
    d.__commit("v", writer, reverse);
    Assert.assertEquals("\"v\":{\"r\":3.0,\"i\":4.0}", writer.toString());
    Assert.assertEquals("\"v\":{\"r\":1.0,\"i\":2.0}", reverse.toString());
  }

  @Test
  public void invalidate_and_revert() {
    final var d = new RxComplex(null, new NtComplex(1, 2));
    final var child = new MockRxChild();
    d.__subscribe(child);
    d.set(new NtComplex(3, 4));
    Assert.assertEquals(new NtComplex(3, 4), d.get());
    child.assertInvalidateCount(1);
    d.set(new NtComplex(4, 5));
    child.assertInvalidateCount(1);
    d.set(new NtComplex(5, 6));
    child.assertInvalidateCount(1);
    d.__revert();
    child.assertInvalidateCount(1);
    Assert.assertEquals(new NtComplex(1, 2), d.get());
    d.__raiseDirty();
    d.__revert();
    d.__revert();
    d.__revert();
    child.assertInvalidateCount(2);
  }
}
