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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class SimpleTimeoutTests {
  @Test
  public void make() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("test");
    try {
      CountDownLatch doesnt = new CountDownLatch(1);
      CountDownLatch fires = new CountDownLatch(1);
      SimpleTimeout.make(executor, 10, () -> {
        fires.countDown();
      });
      SimpleTimeout.make(executor, 50, () -> {
        doesnt.countDown();
      }).run();
      Assert.assertTrue(fires.await(1000, TimeUnit.MILLISECONDS));
      Assert.assertFalse(doesnt.await(100, TimeUnit.MILLISECONDS));
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void wrap() throws Exception {
    SimpleExecutor executor = SimpleExecutor.create("test");
    try {
      BiFunction<Runnable, Long, Callback<String>> cons = (action, ms) -> {
        Callback<String> toWrap = new Callback<String>() {
          @Override
          public void success(String value) {}

          @Override
          public void failure(ErrorCodeException ex) {}
        };
        return SimpleTimeout.WRAP(SimpleTimeout.make(executor, ms, action), toWrap);
      };
      {
        CountDownLatch latch = new CountDownLatch(1);
        cons.apply(() -> latch.countDown(), 5L).success("");
        Assert.assertFalse(latch.await(50, TimeUnit.MILLISECONDS));
      }
      {
        CountDownLatch latch = new CountDownLatch(1);
        cons.apply(() -> latch.countDown(), 5L).failure(new ErrorCodeException(0));
        Assert.assertFalse(latch.await(50, TimeUnit.MILLISECONDS));
      }
      {
        CountDownLatch latch = new CountDownLatch(1);
        cons.apply(() -> latch.countDown(), 50L);// dangle
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
      }
    } finally {
      executor.shutdown();
    }
  }
}
