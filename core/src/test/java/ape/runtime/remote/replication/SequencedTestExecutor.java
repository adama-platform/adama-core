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
package ape.runtime.remote.replication;

import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class SequencedTestExecutor implements SimpleExecutor {

  private final ArrayList<NamedRunnable> runnables;

  public void next() {
    Assert.assertTrue(runnables.size() > 0);
    runnables.remove(0).run();
  }

  public void swap() {
    Assert.assertTrue(runnables.size() > 1);
    NamedRunnable first = runnables.remove(0);
    runnables.add(first);
  }

  public void wave() {
    int n = runnables.size();
    while (n > 0) {
      next();
      n--;
    }
  }

  public NamedRunnable extract() {
    Assert.assertTrue(runnables.size() > 0);
    return runnables.remove(0);
  }

  public void drain() {
    while (runnables.size() > 0) {
      next();
    }
  }

  public void assertEmpty() {
    Assert.assertEquals(0, runnables.size());
  }

  public SequencedTestExecutor() {
    this.runnables = new ArrayList<>();
  }

  @Override
  public void execute(NamedRunnable command) {
    this.runnables.add(command);
  }

  @Override
  public Runnable schedule(NamedRunnable command, long milliseconds) {
    this.runnables.add(command);
    return command;
  }

  @Override
  public Runnable scheduleNano(NamedRunnable command, long nanoseconds) {
    this.runnables.add(command);
    return command;
  }

  @Override
  public CountDownLatch shutdown() {
    return new CountDownLatch(0);
  }
}
