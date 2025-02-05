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
package ape.runtime.mocks;

import ape.runtime.contracts.RxParent;
import org.junit.Assert;

import java.util.Set;

public class MockRxParent implements RxParent {
  public int dirtyCount;
  public boolean alive;
  public int cost;
  public int settleCount;
  public int invalidateUpCalls;

  public MockRxParent() {
    dirtyCount = 0;
    alive = true;
    settleCount = 0;
    invalidateUpCalls = 0;
  }

  @Override
  public void __raiseDirty() {
    dirtyCount++;
  }

  public void assertDirtyCount(final int expected) {
    Assert.assertEquals(expected, dirtyCount);
  }

  @Override
  public boolean __isAlive() {
    return alive;
  }

  @Override
  public void __cost(int cost) {
    this.cost += cost;
  }


  @Override
  public void __invalidateUp() {
    invalidateUpCalls++;
  }

  @Override
  public void __settle(Set<Integer> viewers) {
    settleCount++;
  }
}
