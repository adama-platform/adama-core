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
package ape.common.queue;

import ape.common.metrics.ItemActionMonitor;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ItemActionTests {

  private static final ItemActionMonitor.ItemActionMonitorInstance INSTANCE = new NoOpMetricsFactory().makeItemActionMonitor("x").start();

  @Test
  public void normal() {
    AtomicInteger x = new AtomicInteger(0);
    ItemAction<String> action = new ItemAction<>(100, 200, INSTANCE) {
      @Override
      protected void executeNow(String item) {
        x.incrementAndGet();
      }

      @Override
      protected void failure(int code) {
        x.addAndGet(code);
      }
    };
    Assert.assertTrue(action.isAlive());
    action.execute("z");
    Assert.assertFalse(action.isAlive());
    Assert.assertEquals(1, x.get());
  }

  @Test
  public void timeout() {
    AtomicInteger x = new AtomicInteger(0);
    ItemAction<String> action = new ItemAction<>(100, 200, INSTANCE) {
      @Override
      protected void executeNow(String item) {
        x.incrementAndGet();
      }

      @Override
      protected void failure(int code) {
        x.addAndGet(code);
      }
    };
    Assert.assertTrue(action.isAlive());
    action.killDueToTimeout();
    Assert.assertFalse(action.isAlive());
    action.execute("z");
    action.execute("z");
    action.execute("z");
    Assert.assertFalse(action.isAlive());
    Assert.assertEquals(100, x.get());
  }

  @Test
  public void rejected() {
    AtomicInteger x = new AtomicInteger(0);
    ItemAction<String> action = new ItemAction<String>(100, 1000, INSTANCE) {
      @Override
      protected void executeNow(String item) {
        x.incrementAndGet();
      }

      @Override
      protected void failure(int code) {
        x.addAndGet(code);
      }
    };
    Assert.assertTrue(action.isAlive());
    action.killDueToReject();
    Assert.assertFalse(action.isAlive());
    action.execute("z");
    action.execute("z");
    action.execute("z");
    Assert.assertEquals(1000, x.get());
  }
}
