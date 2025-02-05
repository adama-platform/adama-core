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
package ape.runtime.sys.readonly;

import ape.common.ErrorCodeException;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockReadOnlyStream implements ReadOnlyStream {

  private final CountDownLatch began;
  private final CountDownLatch failed;
  private final ArrayList<CountDownLatch> latches;
  private ErrorCodeException failure;
  private ReadOnlyViewHandle stream;
  private ArrayList<String> dataList;

  public MockReadOnlyStream() {
    this.began = new CountDownLatch(1);
    this.failure = null;
    this.failed = new CountDownLatch(1);
    this.dataList = new ArrayList<>();
    this.latches = new ArrayList<>();
  }

  public ReadOnlyViewHandle get() {
    return stream;
  }

  @Override
  public void setupComplete(ReadOnlyViewHandle handle) {
    this.stream = handle;
    if (began.getCount() == 0) {
      Assert.fail();
    }
    began.countDown();
  }

  @Override
  public synchronized void next(String data) {
    this.dataList.add(data);
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
  public void failure(ErrorCodeException exception) {
    failure = exception;
    failed.countDown();
  }

  @Override
  public void close() {
    next("CLOSED");
  }

  public void await_began() {
    try {
      Assert.assertTrue(began.await(2000, TimeUnit.MILLISECONDS));
    } catch (InterruptedException ie) {
      Assert.fail();
    }
  }

  public synchronized String get(int k) {
    return dataList.get(k);
  }

  public synchronized int size() {
    return dataList.size();
  }

  public synchronized Runnable latchAt(int count) {
    CountDownLatch latch = new CountDownLatch(count);
    latches.add(latch);
    return () -> {
      try {
        Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
      } catch (InterruptedException ie) {
        Assert.fail();
      }
    };
  }

  public void await_failure(int code) {
    try {
      Assert.assertTrue(failed.await(2000, TimeUnit.MILLISECONDS));
      Assert.assertEquals(code, failure.code);
    } catch (InterruptedException ie) {
      Assert.fail();
    }
  }
}
