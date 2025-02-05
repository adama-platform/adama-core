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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedRunnableTests {
  @Test
  public void coverageLongName() {
    AtomicInteger x = new AtomicInteger(0);
    NamedRunnable runnable = new NamedRunnable("me", "too", "tired") {
      @Override
      public void execute() throws Exception {
        if (x.incrementAndGet() == 3) {
          throw new Exception("huh");
        }
      }
    };
    Assert.assertEquals("me/too/tired", runnable.toString());
    runnable.run();
    runnable.run();
    runnable.run();
    runnable.run();
  }

  @Test
  public void noisy() {
    Assert.assertFalse(NamedRunnable.noisy(new RuntimeException()));
    Assert.assertFalse(NamedRunnable.noisy(new Exception()));
    Assert.assertTrue(NamedRunnable.noisy(new RejectedExecutionException()));
  }

  @Test
  public void coverageSingle() {
    AtomicInteger x = new AtomicInteger(0);
    NamedRunnable runnable = new NamedRunnable("me") {
      @Override
      public void execute() throws Exception {
        if (x.incrementAndGet() == 3) {
          throw new Exception("huh");
        }
      }
    };
    Assert.assertEquals("me", runnable.toString());
    runnable.run();
    runnable.run();
    runnable.run();
    runnable.run();
  }
}
