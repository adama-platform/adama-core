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

import ape.common.ErrorCodeException;
import ape.common.Stream;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class MockExportStream implements Stream<String> {

  private final ArrayList<String> values;
  private final LinkedList<CountDownLatch> latches;
  public MockExportStream() {
    this.values = new ArrayList<>();
    this.latches = new LinkedList<>();
  }

  public synchronized CountDownLatch latch(int at) {
    CountDownLatch latch = new CountDownLatch(at);
    latches.add(latch);
    return latch;
  }

  public void assertAt(int k, String value) {
    Assert.assertTrue(k < values.size());
    Assert.assertEquals(value, values.get(k));
  }

  @Override
  public synchronized void next(String value) {
    values.add(value);
    Iterator<CountDownLatch> it = latches.iterator();
    while (it.hasNext()) {
      CountDownLatch latch = it.next();
      latch.countDown();
      if (latch.getCount() == 0) {
        it.remove();
      }
    }
  }

  @Override
  public synchronized void complete() {
  }

  @Override
  public synchronized void failure(ErrorCodeException ex) {
  }
}
