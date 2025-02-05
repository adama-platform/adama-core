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

import ape.common.SimpleExecutor;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemQueueTests {

  @Test
  public void full() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("items");
    try {
      ItemQueue<String> queue = new ItemQueue<>(executor, 10, 5000);
      MyItemAction[] items = new MyItemAction[20];
      for (int k = 0; k < items.length; k++) {
        items[k] = new MyItemAction();
        queue.add(items[k]);
      }
      queue.ready("xyz");
      for (int k = 0; k < items.length; k++) {
        items[k].awaitDone();
      }
      for (int k = 0; k < 10; k++) {
        items[k].assertSum(4);
        items[k + 10].assertSum(1100);
      }
    } finally {
      executor.shutdown().await(1000, TimeUnit.MILLISECONDS);
    }
  }

  @Test
  public void nuke() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("items");
    try {
      ItemQueue<String> queue = new ItemQueue<>(executor, 100, 5000);
      MyItemAction[] items = new MyItemAction[20];
      for (int k = 0; k < items.length; k++) {
        items[k] = new MyItemAction();
        queue.add(items[k]);
      }
      queue.unready();
      queue.nuke();
      for (int k = 0; k < items.length; k++) {
        items[k].awaitDone();
      }
      for (int k = 0; k < items.length; k++) {
        items[k].assertSum(1100);
      }
    } finally {
      executor.shutdown().await(1000, TimeUnit.MILLISECONDS);
    }
  }

  @Test
  public void timeout() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("items");
    try {
      ItemQueue<String> queue = new ItemQueue<>(executor, 100, 25);
      MyItemAction[] items = new MyItemAction[20];
      for (int k = 0; k < items.length; k++) {
        items[k] = new MyItemAction();
        queue.add(items[k]);
      }
      for (int k = 0; k < items.length; k++) {
        items[k].awaitDone();
      }
      for (int k = 0; k < items.length; k++) {
        items[k].assertSum(600);
      }
    } finally {
      executor.shutdown().await(1000, TimeUnit.MILLISECONDS);
    }
  }

  @Test
  public void ready() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("items");
    try {
      ItemQueue<String> queue = new ItemQueue<>(executor, 100, 25);
      MyItemAction[] items = new MyItemAction[20];
      queue.ready("x");
      for (int k = 0; k < items.length; k++) {
        items[k] = new MyItemAction();
        queue.add(items[k]);
      }
      for (int k = 0; k < items.length; k++) {
        items[k].awaitDone();
      }
    } finally {
      executor.shutdown().await(1000, TimeUnit.MILLISECONDS);
    }
  }

  public class MyItemAction extends ItemAction<String> {
    private final AtomicInteger sum = new AtomicInteger(0);
    private final CountDownLatch done = new CountDownLatch(1);

    public MyItemAction() {
      super(500, 1000, new NoOpMetricsFactory().makeItemActionMonitor("x").start());
    }

    @Override
    protected void executeNow(String item) {
      sum.addAndGet(1 + item.length());
      done.countDown();
    }

    @Override
    protected void failure(int code) {
      sum.addAndGet(100 + code);
      done.countDown();
    }

    public void awaitDone() throws Exception {
      Assert.assertTrue(done.await(5000, TimeUnit.MILLISECONDS));
    }

    public void assertSum(int x) {
      Assert.assertEquals(x, sum.get());
    }
  }
}
