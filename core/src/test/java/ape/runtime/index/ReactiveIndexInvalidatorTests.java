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

import ape.runtime.mocks.MockRecord;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

public class ReactiveIndexInvalidatorTests {
  @Test
  public void flow1() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    unknowns.add(MockRecord.make(123));
    final ReactiveIndexInvalidator<MockRecord> inv =
        new ReactiveIndexInvalidator<>(index, MockRecord.make(123)) {
          @Override
          public int pullValue() {
            return 42;
          }
        };
    inv.reindex();
    unknowns.clear();
    Assert.assertEquals(0, unknowns.size());
    inv.__raiseInvalid();
    Assert.assertEquals(1, unknowns.size());
    inv.deindex();
    Assert.assertEquals(0, unknowns.size());
  }

  @Test
  public void flow2() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    unknowns.add(MockRecord.make(123));
    final ReactiveIndexInvalidator<MockRecord> inv =
        new ReactiveIndexInvalidator<>(index, MockRecord.make(123)) {
          @Override
          public int pullValue() {
            return 42;
          }
        };
    inv.reindex();
    unknowns.clear();
    inv.deindex();
    Assert.assertEquals(0, unknowns.size());
  }

  @Test
  public void flow3() {
    final var unknowns = new TreeSet<MockRecord>();
    final var index = new ReactiveIndex<>(unknowns);
    unknowns.add(MockRecord.make(123));
    final ReactiveIndexInvalidator<MockRecord> inv =
        new ReactiveIndexInvalidator<>(index, MockRecord.make(123)) {
          @Override
          public int pullValue() {
            return 42;
          }
        };
    Assert.assertEquals(1, unknowns.size());
    inv.deindex();
    Assert.assertEquals(0, unknowns.size());
  }
}
