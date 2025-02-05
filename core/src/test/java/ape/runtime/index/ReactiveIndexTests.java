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
package ape.runtime.index;

import ape.runtime.contracts.IndexQuerySet;
import ape.runtime.mocks.MockRecord;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

public class ReactiveIndexTests {
  @Test
  public void del() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    unknowns.add(MockRecord.make(123));
    Assert.assertEquals(1, unknowns.size());
    index.delete(MockRecord.make(123));
    Assert.assertEquals(0, unknowns.size());
  }

  @Test
  public void flow() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    Assert.assertNull(index.of(42, IndexQuerySet.LookupMode.Equals));
    index.add(42, MockRecord.make(1));
    Assert.assertEquals(1, index.of(42, IndexQuerySet.LookupMode.Equals).size());
    index.add(42, MockRecord.make(12));
    Assert.assertEquals(2, index.of(42, IndexQuerySet.LookupMode.Equals).size());
    Assert.assertFalse(unknowns.contains(MockRecord.make(12)));
    Assert.assertFalse(unknowns.contains(MockRecord.make(1)));
    index.remove(42, MockRecord.make(12));
    Assert.assertTrue(unknowns.contains(MockRecord.make(12)));
    Assert.assertEquals(1, index.of(42, IndexQuerySet.LookupMode.Equals).size());
    index.remove(42, MockRecord.make(1));
    Assert.assertNull(index.of(42, IndexQuerySet.LookupMode.Equals));
    Assert.assertTrue(unknowns.contains(MockRecord.make(1)));
  }

  @Test
  public void flow_lessthan() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    Assert.assertNull(index.of(42, IndexQuerySet.LookupMode.LessThan));
    index.add(42, MockRecord.make(1));
    Assert.assertEquals(1, index.of(44, IndexQuerySet.LookupMode.LessThan).size());
    Assert.assertNull(index.of(40, IndexQuerySet.LookupMode.LessThan));
  }

  @Test
  public void flow_lessthan_eq() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);

    Assert.assertNull(index.of(42, IndexQuerySet.LookupMode.LessThanOrEqual));
    index.add(42, MockRecord.make(1));
    Assert.assertEquals(1, index.of(44, IndexQuerySet.LookupMode.LessThanOrEqual).size());
    Assert.assertNull(index.of(40, IndexQuerySet.LookupMode.LessThanOrEqual));
  }

  @Test
  public void flow_greaterthan() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    Assert.assertNull(index.of(42, IndexQuerySet.LookupMode.GreaterThan));
    index.add(42, MockRecord.make(1));
    Assert.assertEquals(1, index.of(40, IndexQuerySet.LookupMode.GreaterThan).size());
    Assert.assertNull(index.of(42, IndexQuerySet.LookupMode.GreaterThan));
  }

  @Test
  public void flow_greaterthan_eq() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    Assert.assertNull(index.of(42, IndexQuerySet.LookupMode.GreaterThanOrEqual));
    index.add(42, MockRecord.make(1));
    Assert.assertEquals(1, index.of(40, IndexQuerySet.LookupMode.GreaterThanOrEqual).size());
    Assert.assertNull(index.of(44, IndexQuerySet.LookupMode.GreaterThanOrEqual));
  }

  @Test
  public void memory() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    Assert.assertEquals(64, index.memory());
    index.add(42, MockRecord.make(1));
    Assert.assertEquals(104, index.memory());
  }
}
