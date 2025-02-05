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
package ape.common;

import ape.common.gossip.MockTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeMachineTests {
  @Test
  public void flux_capacitor() throws Exception {
    MockTime mock = new MockTime();
    SimpleExecutor executor = SimpleExecutor.create("time");
    try {
      AtomicInteger at = new AtomicInteger(0);
      TimeMachine machine = new TimeMachine(mock, executor, () -> {
        at.incrementAndGet();
      }, (ln) -> {
        System.err.println(ln);
      });
      int attempts = 0;
      machine.add(2678400000L, 4);
      while (at.get() < 40 && attempts <= 500) {
        attempts++;
        Thread.sleep(100);
        System.err.println("@" + at.get() + "-->" + machine.nowMilliseconds());
      }
      Assert.assertEquals(40, at.get());
      Assert.assertEquals(2678400000L, machine.nowMilliseconds());
      Assert.assertTrue(attempts < 500);
      AtomicBoolean done = new AtomicBoolean(false);
      machine.reset(() -> {done.set(true);});
      while (!done.get()) {
        Thread.sleep(100);
        System.err.println("@" + at.get() + "-->" + machine.nowMilliseconds());
      }
      Assert.assertTrue(done.get());
    } finally {
      executor.shutdown();
    }
  }
}
