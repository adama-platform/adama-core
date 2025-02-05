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
import ape.runtime.contracts.DeleteTask;
import ape.runtime.data.*;
import ape.runtime.data.*;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockDelayDataService implements DataService {
  private final ArrayList<CountDownLatch> latches;
  private DataService parent;
  private boolean paused;
  private ArrayList<Runnable> actions;

  public MockDelayDataService(DataService parent) {
    this.paused = false;
    this.parent = parent;
    this.actions = new ArrayList<>();
    this.latches = new ArrayList<>();
  }

  public synchronized void pause() {
    System.out.println("PAUSE");
    this.paused = true;
  }

  public synchronized void set(DataService service) {
    this.parent = service;
  }

  public void unpause() {
    System.out.println("UNPAUSED");
    for (Runnable r : unpauseWithLock()) {
      r.run();
    }
  }

  private synchronized ArrayList<Runnable> unpauseWithLock() {
    ArrayList<Runnable> copy = new ArrayList<>(actions);
    actions.clear();
    paused = false;
    return copy;
  }

  public void once() {
    System.out.println("ONCE");
    onceWithLock().run();
  }

  private synchronized Runnable onceWithLock() {
    return actions.remove(0);
  }

  @Override
  public void get(Key key, Callback<LocalDocumentChange> callback) {
    enqueue(() -> parent.get(key, callback));
  }

  private void enqueue(Runnable run) {
    enqueueWithLock(run).run();
  }

  private synchronized Runnable enqueueWithLock(Runnable run) {
    if (paused) {
      actions.add(run);
      Iterator<CountDownLatch> it = latches.iterator();
      while (it.hasNext()) {
        CountDownLatch latch = it.next();
        latch.countDown();
        if (latch.getCount() == 0) {
          it.remove();
        }
      }
      return () -> {};
    } else {
      return run;
    }
  }

  @Override
  public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
    enqueue(() -> parent.initialize(key, patch, callback));
  }

  @Override
  public void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback) {
    enqueue(() -> parent.patch(key, patches, callback));
  }

  @Override
  public void compute(
      Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
    enqueue(() -> parent.compute(key, method, seq, callback));
  }

  @Override
  public void delete(Key key, DeleteTask task, Callback<Void> callback) {
    enqueue(() -> parent.delete(key, task, callback));
  }

  @Override
  public void snapshot(Key key, DocumentSnapshot snapshot, Callback<Integer> callback) {
    enqueue(() -> parent.snapshot(key, snapshot, callback));
  }

  @Override
  public void shed(Key key) {
    enqueue(() -> parent.shed(key));
  }

  @Override
  public void inventory(Callback<Set<Key>> callback) {
    enqueue(() -> parent.inventory(callback));
  }

  @Override
  public void recover(Key key, DocumentRestore restore, Callback<Void> callback) {
    enqueue(() -> parent.recover(key, restore, callback));
  }

  @Override
  public void close(Key key, Callback<Void> callback) {
    enqueue(() -> parent.close(key, callback));
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
}
