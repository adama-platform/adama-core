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
package ape.runtime.sys.mocks;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.DataObserver;
import ape.runtime.data.Key;
import ape.runtime.sys.readonly.ReplicationInitiator;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MockReplicationInitiator implements ReplicationInitiator {
  private final AtomicInteger starts;
  private final AtomicInteger cancels;
  private boolean simpleMode;
  private final String initialize;
  private final String change;
  private DataObserver lastObserver;
  private boolean delayed;
  private Runnable resume;
  private CountDownLatch readyResume;

  public MockReplicationInitiator() {
    this.starts = new AtomicInteger(0);
    this.cancels = new AtomicInteger(0);
    this.simpleMode = true;
    this.initialize = "{}";
    this.change = "{}";
    this.delayed = false;
    this.resume = null;
    this.readyResume = new CountDownLatch(1);
  }

  public MockReplicationInitiator(String initialize, String change) {
    this.starts = new AtomicInteger(0);
    this.cancels = new AtomicInteger(0);
    this.simpleMode = true;
    this.initialize = initialize;
    this.change = change;
    this.delayed = false;
    this.resume = null;
    this.readyResume = new CountDownLatch(1);
  }

  public DataObserver getLastObserver() {
    return lastObserver;
  }

  private void runDefault() {

  }
  @Override
  public void startDocumentReplication(Key key, DataObserver observer, Callback<Runnable> cancel) {
    Runnable defaultMode = () -> {
      observer.machine(); // TODO?
      lastObserver = observer;
      starts.incrementAndGet();
      if (simpleMode) {
        if (initialize != null) {
          cancel.success(() -> {
            cancels.incrementAndGet();
          });
          observer.start(initialize);
          if (change != null) {
            observer.change(change);
          }
        } else {
          cancel.failure(new ErrorCodeException(-135));
        }
      }
    };
    if (delayed) {
      resume = defaultMode;
      readyResume.countDown();
    } else {
      defaultMode.run();
    }
  }

  public void setDelayed() {
    this.delayed = true;
  }

  public void executeDelay() throws Exception {
    Assert.assertTrue(readyResume.await(5000, TimeUnit.MILLISECONDS));
    readyResume = new CountDownLatch(1);
    this.delayed = false;
    resume.run();
    this.resume = null;
  }
}
