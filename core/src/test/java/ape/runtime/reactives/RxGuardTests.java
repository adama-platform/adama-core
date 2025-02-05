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

import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockRecord;
import ape.runtime.mocks.MockRxChild;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class RxGuardTests {
  @Test
  public void dump() {
    final var d = new RxGuard(null);
    final var writer = new JsonStreamWriter();
    d.__dump(writer);
    Assert.assertEquals("", writer.toString());
  }

  @Test
  public void flow() {
    final var guard = new RxGuard(null);
    Assert.assertEquals(1, guard.getGeneration(0));
    Assert.assertEquals(true, guard.__invalid);
    guard.__settle(null);
    Assert.assertEquals(1, guard.getGeneration(0));
    Assert.assertEquals(false, guard.__invalid);
    final var child = new MockRxChild();
    guard.__subscribe(child);
    guard.__raiseInvalid();
    child.assertInvalidateCount(1);
    Assert.assertEquals(65522, guard.getGeneration(0));
    Assert.assertEquals(true, guard.__invalid);
    guard.__settle(null);
    Assert.assertEquals(false, guard.__invalid);
    Assert.assertEquals(65522, guard.getGeneration(0));
    guard.__insert(new JsonStreamReader("{}"));
    guard.__patch(new JsonStreamReader("{}"));
    guard.__commit(null, null, null);
    guard.__revert();
    guard.__dump(null);
    Assert.assertEquals(64, guard.__memory());
  }

  @Test
  public void preventDeadlock() {
    AtomicReference<RxGuard> self = new AtomicReference<>();
    RxGuard g = new RxGuard(new RxParent() {
      @Override
      public void __raiseDirty() {
        self.get().__raiseInvalid();
      }

      @Override
      public boolean __isAlive() {
        return false;
      }

      @Override
      public void __cost(int cost) {
      }

      @Override
      public void __invalidateUp() {
      }

      @Override
      public void __settle(Set<Integer> viewers) {
      }
    });
    self.set(g);
    g.__raiseInvalid();
    Assert.assertEquals(64, g.__memory());
  }

  @Test
  public void inheritRecordId() {
    MockRecord record = new MockRecord(null);
    RxGuard g = new RxGuard(record);
    g.__raiseInvalid();
    Assert.assertEquals(1, g.getGeneration(0));
    Assert.assertEquals(64, g.__memory());
  }
}
