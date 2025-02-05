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
import ape.runtime.data.Key;
import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockRacerLivingDocumentFactoryFactory implements LivingDocumentFactoryFactory {
  private final HashMap<Key, ArrayList<Callback<LivingDocumentFactory>>> calls;
  private final ArrayList<CountDownLatch> latches;

  public MockRacerLivingDocumentFactoryFactory() {
    this.calls = new HashMap<>();
    this.latches = new ArrayList<>();
  }

  @Override
  public void account(HashMap<String, PredictiveInventory.MeteringSample> sample) {
  }

  @Override
  public Collection<String> spacesAvailable() {
    return Collections.singleton("space");
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

  public void satisfyAll(Key key, LivingDocumentFactory factory) {
    ArrayList<Callback<LivingDocumentFactory>> callbacks = removeAt(key);
    if (callbacks != null) {
      for (Callback<LivingDocumentFactory> callback : callbacks) {
        callback.success(factory);
      }
    }
  }

  private synchronized ArrayList<Callback<LivingDocumentFactory>> removeAt(Key key) {
    return calls.remove(key);
  }

  public void satisfyNone(Key key) {
    ArrayList<Callback<LivingDocumentFactory>> callbacks = removeAt(key);
    for (Callback<LivingDocumentFactory> callback : callbacks) {
      callback.failure(new ErrorCodeException(50000));
    }
  }

  @Override
  public synchronized void fetch(Key key, Callback<LivingDocumentFactory> callback) {
    ArrayList<Callback<LivingDocumentFactory>> callsForKey = calls.get(key);
    if (callsForKey == null) {
      callsForKey = new ArrayList<>();
      calls.put(key, callsForKey);
    }
    callsForKey.add(callback);
    Iterator<CountDownLatch> it = latches.iterator();
    while (it.hasNext()) {
      CountDownLatch latch = it.next();
      latch.countDown();
      if (latch.getCount() == 0) {
        it.remove();
      }
    }
  }
}
