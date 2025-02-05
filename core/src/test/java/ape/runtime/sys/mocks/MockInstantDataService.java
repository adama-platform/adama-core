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

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.data.*;
import ape.runtime.data.*;
import ape.runtime.json.JsonAlgebra;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import org.junit.Assert;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockInstantDataService implements DataService {
  private final HashMap<Key, ArrayList<RemoteDocumentUpdate>> logByKey;
  private final ArrayList<String> log;
  private final ArrayList<CountDownLatch> latches;
  private final ArrayList<Key> bootstrap;
  private boolean enableSeqSkip = false;
  private int seqToSkip = 0;
  private boolean infiniteSkip = false;
  private boolean crashSeqSkip = false;
  private int skipTrack = 0;
  private int patchFailAt;

  public MockInstantDataService() {
    this.logByKey = new HashMap<>();
    log = new ArrayList<>();
    this.latches = new ArrayList<>();
    this.bootstrap = new ArrayList<>();
    this.patchFailAt = Integer.MAX_VALUE;
  }

  public void setPatchFailureAt(int patchFailAt) {
    this.patchFailAt = patchFailAt;
  }

  public void ready(Key key) {
    bootstrap.add(key);
  }

  @Override
  public void get(Key key, Callback<LocalDocumentChange> callback) {
    ArrayList<RemoteDocumentUpdate> log = logByKey.get(key);
    if (log == null) {
      if ("delete-while-archive".equals(key.key)) {
        callback.success(new LocalDocumentChange("{}", 1, 1000));
        return;
      }
      callback.failure(new ErrorCodeException(ErrorCodes.UNIVERSAL_LOOKUP_FAILED));
      return;
    }
    println("LOAD:" + key.space + "/" + key.key);
    Object obj = null;
    for (RemoteDocumentUpdate update : log) {
      if (obj == null) {
        obj = new JsonStreamReader(update.redo).readJavaTree();
      } else {
        obj = JsonAlgebra.merge(obj, new JsonStreamReader(update.redo).readJavaTree(), false);
      }
    }
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.writeTree(obj);
    callback.success(new LocalDocumentChange(writer.toString(), 1, log.get(log.size() - 1).seqEnd));
  }

  private boolean correctFailureCodeForFailure = true;
  public void failInitializationAgainWithWrongErrorCode() {
    correctFailureCodeForFailure = false;
  }

  @Override
  public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
    if (logByKey.containsKey(key)) {
      if (correctFailureCodeForFailure) {
        callback.failure(new ErrorCodeException(ErrorCodes.UNIVERSAL_INITIALIZE_FAILURE));
      } else {
        callback.failure(new ErrorCodeException(1));
      }
    } else {
      println("INIT:" + key.space + "/" + key.key + ":" + patch.seqEnd + "->" + patch.redo);
      ArrayList<RemoteDocumentUpdate> log = new ArrayList<>();
      log.add(patch);
      logByKey.put(key, log);
      callback.success(null);
    }
  }

  @Override
  public synchronized void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback) {
    patchFailAt--;
    if (patchFailAt < 0) {
      callback.failure(new ErrorCodeException(9999));
      return;
    }
    ArrayList<RemoteDocumentUpdate> log = logByKey.get(key);
    for (RemoteDocumentUpdate patch : patches) {
      println("PATCH:" + key.space + "/" + key.key + ":" + patch.seqBegin + "-" + patch.seqEnd + "->" + patch.redo);
      if (infiniteSkip) {
        callback.failure(new ErrorCodeException(ErrorCodes.UNIVERSAL_PATCH_FAILURE_HEAD_SEQ_OFF));
        println("SKIP++");
        return;
      }
      if (enableSeqSkip) {
        if (patch.seqBegin <= seqToSkip && seqToSkip <= patch.seqEnd) {
          callback.failure(new ErrorCodeException(ErrorCodes.UNIVERSAL_PATCH_FAILURE_HEAD_SEQ_OFF));
          println("SKIP");
          return;
        }
      }
    }
    if (key != null) {
      for (RemoteDocumentUpdate patch : patches) {
        log.add(patch);
      }
      callback.success(null);
    } else {
      callback.failure(new ErrorCodeException(3));
    }
  }

  @Override
  public void compute(
      Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
    if (method == ComputeMethod.HeadPatch) {
      if (crashSeqSkip) {
        callback.failure(new ErrorCodeException(555));
        println("CRASH");
      } else {
        skipTrack += 10000;
        callback.success(
            new LocalDocumentChange("{\"x\":" + skipTrack * 7 + ",\"__seq\":" + skipTrack + "}", 1, 100));
      }
    } else if (method == ComputeMethod.Rewind) {
      callback.success(new LocalDocumentChange("{\"x\":1000}", 1, 200));
    } else {
      callback.failure(new ErrorCodeException(5));
    }
  }

  @Override
  public void delete(Key key, DeleteTask task, Callback<Void> callback) {
    if ("cant-delete-delete".equals(key.key)) {
      callback.failure(new ErrorCodeException(-42));
      return;
    }
    println("DELETE:" + key.space + "/" + key.key);
    logByKey.remove(key);
    task.executeAfterMark(callback);
  }

  @Override
  public void snapshot(Key key, DocumentSnapshot snapshot, Callback<Integer> callback) {
    callback.success(-1);
  }

  @Override
  public void shed(Key key) {
    logByKey.remove(key);
    println("SHED:" + key.space + "/" + key.key);
  }

  @Override
  public void inventory(Callback<Set<Key>> callback) {
    println("INVENTORY");
    callback.success(new TreeSet<>(logByKey.keySet()));
  }

  @Override
  public void recover(Key key, DocumentRestore restore, Callback<Void> callback) {
    println("RECOVER:" + key.space + "/" + key.key);
    if (logByKey.containsKey(key)) {
      ArrayList<RemoteDocumentUpdate> log = logByKey.get(key);
      log.clear();
      log.add(new RemoteDocumentUpdate(restore.seq, restore.seq, restore.who, "{}", restore.document, "{}", false, 10, 0, UpdateType.Restore));
      callback.success(null);
    } else {
      callback.failure(new ErrorCodeException(-404));
    }
  }

  @Override
  public void close(Key key, Callback<Void> callback) {
    callback.success(null);
    println("CLOSE:" + key.space + "/" + key.key);
  }

  private synchronized void println(String x) {
    System.out.println(x);
    log.add(x);
    Iterator<CountDownLatch> it = latches.iterator();
    while (it.hasNext()) {
      CountDownLatch latch = it.next();
      latch.countDown();
      if (latch.getCount() == 0) {
        it.remove();
      }
    }
  }

  public synchronized void assertLogAt(int k, String expected) {
    Assert.assertEquals(expected, log.get(k));
  }

  public synchronized void assertLogAtContains(int k, String expected) {
    Assert.assertTrue(log.get(k).contains(expected));
  }

  public synchronized void assertLogAtStartsWith(int k, String prefix) {
    Assert.assertTrue(log.get(k).startsWith(prefix));
  }

  public synchronized String getLogAt(int k) {
    return log.get(k);
  }

  public synchronized Runnable latchLogAt(int count) {
    CountDownLatch latch = new CountDownLatch(count);
    latches.add(latch);
    return () -> {
      try {
        Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
      } catch (InterruptedException ie) {
        Assert.fail();
      }
    };
  }

  public void skipAt(int seq) {
    this.enableSeqSkip = true;
    this.seqToSkip = seq;
  }

  public void infiniteSkip() {
    this.infiniteSkip = true;
  }

  public void killSkip() {
    crashSeqSkip = true;
  }
}
