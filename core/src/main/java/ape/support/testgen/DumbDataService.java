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
package ape.support.testgen;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.data.*;
import ape.runtime.data.*;
import ape.runtime.json.JsonAlgebra;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.PrivateView;
import ape.runtime.sys.DurableLivingDocument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class DumbDataService implements DataService {
  public static Callback<PrivateView> makePrinterPrivateView(String prefix, StringBuilder sb) {
    return new Callback<PrivateView>() {
      @Override
      public void success(PrivateView value) {
        sb.append(prefix + ": CREATED PRIVATE VIEW\n");
      }

      @Override
      public void failure(ErrorCodeException ex) {
        sb.append(prefix + ": FAILED PRIVATE VIEW DUE TO:" + ex.code + "\n");
      }
    };
  }
  public static Callback<Integer> makePrinterInt(String prefix, StringBuilder sb) {
    return new Callback<>() {
      @Override
      public void success(Integer value) {
        sb.append(prefix + "|SUCCESS:" + value + "\n");
      }

      @Override
      public void failure(ErrorCodeException ex) {
        sb.append(prefix + "|FAILURE:" + ex.code + "\n");
      }
    };
  }
  public final HashSet<Key> deleted;
  public boolean deletesWork = true;
  public boolean computesWork = true;
  public boolean dropPatches = false;
  private Object tree;
  private String data;
  private final Consumer<RemoteDocumentUpdate> updates;
  public long assetsSeen = 0L;

  public DumbDataService(Consumer<RemoteDocumentUpdate> updates) {
    this.tree = new HashMap<String, Object>();
    this.deleted = new HashSet<>();
    this.data = null;
    this.updates = updates;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public void get(Key key, Callback<LocalDocumentChange> callback) {
    if (data != null) {
      callback.success(new LocalDocumentChange(data, 1, 1));
    } else {
      callback.failure(new ErrorCodeException(0, new UnsupportedOperationException()));
    }
  }

  @Override
  public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
    patch(key, new RemoteDocumentUpdate[]{patch}, callback);
  }

  @Override
  public void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback) {
    for (RemoteDocumentUpdate update : patches) {
      assetsSeen += update.assetBytes;
    }
    if (dropPatches) {
      return;
    }
    for (RemoteDocumentUpdate patch : patches) {
      Json.parseJsonObject(patch.redo);
      Json.parseJsonObject(patch.undo);
      updates.accept(patch);
      JsonStreamReader reader = new JsonStreamReader(patch.redo);
      tree = JsonAlgebra.merge(tree, reader.readJavaTree(), false);
    }
    callback.success(null);
  }

  @Override
  public void compute(Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
    if (computesWork) {
      if (method == ComputeMethod.Rewind) {
        callback.success(new LocalDocumentChange("{\"x\":1000}", 1, 1));
      }
    } else {
      callback.failure(new ErrorCodeException(23456));
    }
  }

  @Override
  public void delete(Key key, DeleteTask task, Callback<Void> callback) {
    if (deletesWork) {
      deleted.add(key);
      task.executeAfterMark(callback);
    } else {
      callback.failure(new ErrorCodeException(1234567));
    }
  }

  @Override
  public void snapshot(Key key, DocumentSnapshot snapshot, Callback<Integer> callback) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void shed(Key key) {
  }

  @Override
  public void inventory(Callback<Set<Key>> callback) {
    callback.failure(new ErrorCodeException(-123));
  }

  @Override
  public void recover(Key key, DocumentRestore restore, Callback<Void> callback) {
    callback.failure(new ErrorCodeException(-42));
  }

  @Override
  public void close(Key key, Callback<Void> callback) {
    callback.success(null);
  }

  public static class DumbDurableLivingDocumentAcquire implements Callback<DurableLivingDocument> {
    private DurableLivingDocument value;

    public DumbDurableLivingDocumentAcquire() {
      this.value = null;
    }

    public DurableLivingDocument get() {
      if (value == null) {
        throw new NullPointerException();
      }
      return value;
    }

    @Override
    public void success(DurableLivingDocument value) {
      this.value = value;
    }

    @Override
    public void failure(ErrorCodeException ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex);
    }
  }
}
